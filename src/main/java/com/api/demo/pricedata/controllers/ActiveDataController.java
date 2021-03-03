package com.api.demo.pricedata.controllers;

import com.api.demo.mongorepositories.applicationpackage.promotions.Promotion;
import com.api.demo.pricedata.functions.ArizonaDateTimeComponent;
import com.api.demo.mongorepositories.applicationpackage.promotionmodellist.PromotionModelList;
import com.api.demo.mongorepositories.applicationpackage.promotionmodellist.PromotionModelListRepository;
import com.api.demo.mongorepositories.applicationpackage.promotions.PromotionRepository;
import com.api.demo.mongorepositories.applicationpackage.salesoffices.SalesOffice;
import com.api.demo.mongorepositories.applicationpackage.salesoffices.SalesOfficeRepository;
import com.api.demo.pricedata.repositories.pricedata.PriceData;
import com.api.demo.pricedata.repositories.pricedata.PriceDataRepository;
import com.api.demo.pricedata.repositories.pricedata.PriceDataResponsePackage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.HttpStatusCodeException;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.List;

import static com.api.demo.pricedata.repositories.pricedata.PriceDataStatus.ACTIVE;
import static com.api.demo.pricedata.repositories.pricedata.PriceDataStatus.PENDING;

@RestController
@RequestMapping("/pricedata/active")
public class ActiveDataController {
    @Autowired
    private PromotionRepository promotionRepository;
    @Autowired
    private SalesOfficeRepository salesOfficeRepository;
    @Autowired
    private PromotionModelListRepository promotionModelListRepository;
    @Autowired
    private PriceDataRepository priceDataRepository;

    private static Logger logger = LoggerFactory.getLogger(ActiveDataController.class);
    // date format
    SimpleDateFormat format = new SimpleDateFormat("MM/dd/yyyy");

    /**
     * Get the current price data for a location.  This route WILL mutate data, as it checks for any pending data and pushes it
     * live, while subsequently expiring the old data.
     *
     * This function can only be used to get the current data for a location.  It cannot look forward or backwards in time
     *
     * @param locationId
     * @return
     */
    @GetMapping("/current")
    @ResponseBody
    public ResponseEntity<Object> getCurrentData(@RequestParam("locationId") String locationId) {
        // create a new PriceDataResponsePackage
        // this is the response object to be returned to the client
        PriceDataResponsePackage responsePackage = new PriceDataResponsePackage();

        // initialize the sales office object
        SalesOffice office;

        /* get the sales office */
        if(!locationId.isEmpty()) {
            try {
                office = salesOfficeRepository.findById(locationId).orElseThrow(() -> new HttpStatusCodeException(HttpStatus.NOT_FOUND) {});
                responsePackage.setSalesOffice(office);
            } catch (HttpStatusCodeException e) {
                return new ResponseEntity<>("Could not find sales office", HttpStatus.NOT_FOUND);
            }
        } else {
            return new ResponseEntity<>("Sales office must not be empty", HttpStatus.NOT_FOUND);
        }

        /* get the day (today!) of the request so you can get the current promotion */
        ArizonaDateTimeComponent arizonaDateTimeComponent = new ArizonaDateTimeComponent();
        int day = arizonaDateTimeComponent.getDayInteger();
        int month = arizonaDateTimeComponent.getMonthInteger(); // Warning: January = 0
        int year = arizonaDateTimeComponent.getYearInteger();

        // Formatted date string
        String dateToday = arizonaDateTimeComponent.formatMonth(month) + "/" + arizonaDateTimeComponent.formatDay(day) + "/" + year;
        // System.out.println("Date formatted: " + dateToday);

        try {
            // get the current promotion (also queries by 0)
            Promotion currentPromotion = returnCurrentPromotionForDate(dateToday, month, year);
            responsePackage.setPromotion(currentPromotion);

            // set the promotion half
            int promotionHalf = getCurrentPromotionHalf(dateToday, responsePackage.getPromotion());
            responsePackage.setPromotionHalf(promotionHalf);

            PromotionModelList promotionModels = promotionModelListRepository.findByLocationIdAndPromotionId(office.getId(), currentPromotion.getId());
            // logger.info(String.valueOf(promotionModelsForOffice.getModelIds().size()));
            responsePackage.setPromotionModelList(promotionModels);


            // get all current active data
            List<PriceData> currentData = priceDataRepository.findByLocationIdAndStatus(office.getId(), ACTIVE);
            // get all the pending data
            List<PriceData> pendingDataList = priceDataRepository.findByLocationIdAndStatus(office.getId(), PENDING);


            // go through each pending data
            for (PriceData pendingData : pendingDataList) {
                // if the pending data active data is on or before today
                if (!arizonaDateTimeComponent.isFirstDateAfterSecondDate(pendingData.getActiveDate(), arizonaDateTimeComponent.getTodayDateString())) {
                    boolean modelExists = false;
                    // remove any data that expires after the selected date
                    for (int i = 0; i < currentData.size(); i++) {
                        PriceData currentPriceData = currentData.get(i);
                        // if the modelId matches, expire the old data and save it.  The set the pending data to active and save it.
                        if (currentPriceData.getModelId().equals(pendingData.getModelId())) {
                            currentPriceData.expireData(arizonaDateTimeComponent.getYesterdaysDateString());
                            priceDataRepository.save(currentPriceData);
                            modelExists = true;
                            pendingData.setToActive();
                            priceDataRepository.save(pendingData);
                            currentData.set(i, pendingData);
                        }
                    }
                    // if the model was not found, then it didn't exist in the current data and can be activated as normal
                    if (!modelExists) {
                        pendingData.setToActive();
                        priceDataRepository.save(pendingData);
                        currentData.add(pendingData);
                    }
                }
            }

            responsePackage.setPriceDataList(currentData);
            return new ResponseEntity<>(responsePackage, HttpStatus.ACCEPTED);
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("Error getting current data");
            logger.error(e.getMessage());
            return new ResponseEntity<>(e.getMessage(), HttpStatus.CONFLICT);
        }
    }

    /**
     * Retrieves a promotion by its database ID.  Throws an exception if the ID is empty or the promotion cannot be found
     * @param id    ID of the promotion you are looking for
     * @return
     * @throws HttpStatusCodeException
     */
    public Promotion getPromotionById(String id) throws HttpStatusCodeException {
        /* get promotion by id */
        if(!id.isEmpty()) {
            // return promotion or throw not found exception
            return promotionRepository.findById(id).orElseThrow(() -> new HttpStatusCodeException(HttpStatus.NOT_FOUND, "Promotion with ID " + id + " could not be found") {});
        } else {
            throw new HttpStatusCodeException(HttpStatus.CONFLICT, "Promotion ID must not be empty") {};  // promotion ID must not be empty
        }
    }

    /**
     *
     * @param dateToCompare     The date you are analyzing
     * @param promotion         The promotion you are analyzing
     * @return                  Returns true if the date is inside the promo, false if it is outside
     * @throws ParseException   If one of the dates cannot be parsed by the ArizonaDateTimeComponent
     */
    public boolean isDateInPromotionMonth(String dateToCompare, Promotion promotion) throws ParseException {
        ArizonaDateTimeComponent arizonaDateTimeComponent = new ArizonaDateTimeComponent();
        boolean isDateOnOrAfterStart = arizonaDateTimeComponent.isFirstDateBeforeSecondDate(dateToCompare, promotion.getStartDate());
        boolean isDateOnOrBeforeEndDate = arizonaDateTimeComponent.isFirstDateAfterSecondDate(dateToCompare, promotion.getEndDate());
        if(isDateOnOrBeforeEndDate || isDateOnOrAfterStart) {
            return false;
        } else {
            return true;
        }
    }

    /**
     * Assumes that the function isDateInPromotionMonth() evaluates as true, as well as the first half end date and the second half
     * start date being adjacent on the calendar
     * @param dateToCompare     The date you are analyzing
     * @param promotion         The promotion you are analyzing
     * @return int              Returns an integer.  1 for first half, 2 for second half, -1 for error
     * @throws ParseException   If one of the dates cannot be parsed by the ArizonaDateTimeComponent
     */
    public int getCurrentPromotionHalf(String dateToCompare, Promotion promotion) throws ParseException {
        ArizonaDateTimeComponent arizonaDateTimeComponent = new ArizonaDateTimeComponent();
        boolean isDateBeforeMidMonth = arizonaDateTimeComponent.isFirstDateBeforeSecondDate(dateToCompare, promotion.getMidMonthDate());
        if(isDateBeforeMidMonth) {
            return 1;
        } else {
            return 2;
        }
    }

    /**
     * Returns the current promotion based upon the date provided
     * This return data is encapsulated in a PriceDataResponsePackage.  This allows the function to return the formatted
     * start and end strings as well as the promotion data itself.
     * @param todayDateString       Formatted string of todays date
     * @param month     Month of date
     * @param year      Year of date
     * @return                  Returns a price data package
     * @throws ParseException   If one of the dates cannot be parsed by the ArizonaDateTimeComponent
     */
    public Promotion returnCurrentPromotionForDate(String todayDateString, int month, int year) throws ParseException {
        ArizonaDateTimeComponent arizonaDateTimeComponent = new ArizonaDateTimeComponent();

        Promotion promotionFromDate = promotionRepository.findByDate(month + "/" + year);
        // promotionFromDate.debug();
        if(promotionFromDate == null) {
            logger.warn("No promotion found for date " + month + "/" + year);
            return null;
        }

        Promotion previousMonthsPromotion = promotionRepository.findByDate(promotionFromDate.getPreviousMonthsQueryString());
        Promotion nextMonthsPromotion = promotionRepository.findByDate(promotionFromDate.getNextMonthsQueryString());

        // figure out which promotion to use by comparing today's date vs the start and end dates
        if(arizonaDateTimeComponent.isFirstDateBeforeSecondDate(todayDateString, promotionFromDate.getStartDate())) {
            // logger.info("Previous month's promotion");
            return previousMonthsPromotion;
        } else if (arizonaDateTimeComponent.isFirstDateAfterSecondDate(todayDateString, promotionFromDate.getEndDate())) {
            // logger.info("Next months promotion");
            return nextMonthsPromotion;
        } else {
            // logger.info("Already have current promotion");
            return promotionFromDate;
        }
    }
}
