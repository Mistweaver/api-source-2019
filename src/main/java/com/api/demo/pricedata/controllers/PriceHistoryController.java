package com.api.demo.pricedata.controllers;

import com.api.demo.mongorepositories.applicationpackage.promotions.Promotion;
import com.api.demo.pricedata.functions.ArizonaDateTimeComponent;
import com.api.demo.pricedata.repositories.pricedata.PriceData;
import com.api.demo.pricedata.repositories.pricedata.PriceDataResponsePackage;
import com.api.demo.mongorepositories.applicationpackage.promotionmodellist.PromotionModelList;
import com.api.demo.mongorepositories.applicationpackage.promotionmodellist.PromotionModelListRepository;
import com.api.demo.mongorepositories.applicationpackage.promotions.PromotionRepository;
import com.api.demo.mongorepositories.applicationpackage.salesoffices.SalesOffice;
import com.api.demo.mongorepositories.applicationpackage.salesoffices.SalesOfficeRepository;
import com.api.demo.pricedata.repositories.pricedata.PriceDataRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.HttpStatusCodeException;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import static com.api.demo.pricedata.repositories.pricedata.PriceDataStatus.*;


@RestController
@RequestMapping("/pricedata/active")
public class PriceHistoryController {
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
	 * Retrieve the price data for a selected day.  No data is updated.
	 *
	 * @param locationId    the sales office you wish to query data for
	 * @param day           the day you wish to view
	 * @param month			the month you wish to view (0 indexed, so January = 0, February = 1, etc)
	 * @param year			the year you wish to view
	 * @return              returns the promotion, the promotion model list, the sales office, and the list of all active models for the queried date
	 */
	@GetMapping("/search/date")
	@ResponseBody
	public ResponseEntity<Object> getDataForDate(@RequestParam("locationId") String locationId, @RequestParam("day") int day, @RequestParam("month") int month, @RequestParam("year") int year) {
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

		// Formatted date string
		String queriedDateString = arizonaDateTimeComponent.formatMonth(month) + "/" + arizonaDateTimeComponent.formatDay(day) + "/" + year;
		// System.out.println("Date formatted: " + dateToday);

		try {
			// get the current promotion (also queries by 0)
			Promotion promotionForDate = returnCurrentPromotionForDate(queriedDateString, month, year);
			responsePackage.setPromotion(promotionForDate);
			// initialize the promotion model list for the sales office
			PromotionModelList promotionModelsForOffice = promotionModelListRepository.findByLocationIdAndPromotionId(office.getId(), promotionForDate.getId());
			responsePackage.setPromotionModelList(promotionModelsForOffice);
			// set the promotion half
			int promotionHalf = getCurrentPromotionHalf(queriedDateString, responsePackage.getPromotion());
			responsePackage.setPromotionHalf(promotionHalf);

			// initialize the data lists
			List<PriceData> expiredData = priceDataRepository.findByLocationIdAndStatus(office.getId(), EXPIRED);
			List<PriceData> activeData = priceDataRepository.findByLocationIdAndStatus(office.getId(), ACTIVE);
			List<PriceData> pendingData = priceDataRepository.findByLocationIdAndStatus(office.getId(), PENDING);

			// combine all the data into a single list to iterate through
			List<PriceData> pastAndCurrentData = new ArrayList<>();
			pastAndCurrentData.addAll(expiredData);
			pastAndCurrentData.addAll(activeData);

			// List of Price Data to return
			List<PriceData> responsePriceData = new ArrayList<>();
			// if the dateToday string is between the active date and expiration date on the data, then it is the data that was active at that time
			for(PriceData data : pastAndCurrentData) {
				boolean isDataActiveOnDate = arizonaDateTimeComponent.isDateBetweenTwoDates(queriedDateString, data.getActiveDate(), data.getExpirationDate());
				if(isDataActiveOnDate) {
					responsePriceData.add(data);
				}
			}


			// go through each pending data and update it
			// do this separate because it's possible for the date query to be after both active data and pending data if the
			// pending data has not gone live yet.  This removes duplicate data
			for (PriceData data : pendingData) {
				try {
					// if the pending data active data is on or before the queried string
					if (!arizonaDateTimeComponent.isFirstDateAfterSecondDate(data.getActiveDate(), queriedDateString)) {
						boolean modelExists = false;
						// remove any data that expires after the selected date
						for (int i = 0; i < activeData.size(); i++) {
							PriceData currentPriceData = responsePriceData.get(i);
							// if the modelId matches, swap them in the list
							if (currentPriceData.getModelId().equals(data.getModelId())) {
								System.out.println("Replacing " + currentPriceData.getModel() + " with " + data.getModelId());
								modelExists = true;
								responsePriceData.set(i, data);
							}
						}

						// if the model was not found, return it
						if (!modelExists) {
							responsePriceData.add(data);
						}
					}
				} catch (Exception e) {
					logger.error("Could not parse active date : " + data.getActiveDate());
				}
			}



			responsePackage.setPriceDataList(responsePriceData);
			return new ResponseEntity<>(responsePackage, HttpStatus.ACCEPTED);
		} catch (Exception e) {
			logger.error("Error getting current data");
			logger.error(e.getMessage());
			return new ResponseEntity<>(e.getMessage(), HttpStatus.CONFLICT);
		}
	}


	/**
	 * Get a preview of the price data for a given date.  This route is intended for the promotion client.
	 * No data is updated.  The month and year query must be older than today's data aka the function only looks forward in time,
	 * not backwards.
	 *
	 * USE THIS ALGORITHM
	 * https://stackoverflow.com/questions/13513932/algorithm-to-detect-overlapping-periods
	 *
	 * @param locationId    the sales office you wish to query data for
	 * @param promotionId   the promotion you wish to query data for
	 * @return              returns the promotion, the promotion model list, the sales office, and the list of all active models for the queried date
	 */
	@GetMapping("/search/promotion")
	@ResponseBody
	public ResponseEntity<Object> getDataForPromotionMonth(@RequestParam("locationId") String locationId, @RequestParam("promotionId") String promotionId) {
		PriceDataResponsePackage responsePackage = new PriceDataResponsePackage();
		// initialize the sales office object
		SalesOffice office;

		/* get the sales office */
		if(!locationId.isEmpty()) {
			try {
				office = salesOfficeRepository.findById(locationId).orElseThrow(() -> new HttpStatusCodeException(HttpStatus.NOT_FOUND) {});
				responsePackage.setSalesOffice(office);
			} catch (HttpStatusCodeException e) {
				return new ResponseEntity<>("Could not find sales office ID: " + locationId, HttpStatus.NOT_FOUND);
			}
		} else {
			return new ResponseEntity<>("Location ID must not be empty", HttpStatus.NOT_FOUND);
		}

		Promotion promotionById;
		if(!promotionId.isEmpty()) {
			try {
				promotionById = promotionRepository.findById(promotionId).orElseThrow(() -> new HttpStatusCodeException(HttpStatus.NOT_FOUND) {});
				// get the current promotion (also queries by 0)
				responsePackage.setPromotion(promotionById);
			} catch (HttpStatusCodeException e) {
				return new ResponseEntity<>("Could not find promotion ID: " + promotionId, HttpStatus.NOT_FOUND);
			}
		} else {
			return new ResponseEntity<>("Promotion ID must not be empty", HttpStatus.NOT_FOUND);
		}

		/* get the day (today!) of the request so you can get the current promotion */
		ArizonaDateTimeComponent arizonaDateTimeComponent = new ArizonaDateTimeComponent();


		try {

			// initialize the promotion model list for the sales office
			PromotionModelList promotionModelsForOffice = promotionModelListRepository.findByLocationIdAndPromotionId(office.getId(), promotionById.getId());
			responsePackage.setPromotionModelList(promotionModelsForOffice);

			// initialize the data lists
			List<PriceData> expiredData = priceDataRepository.findByLocationIdAndStatus(office.getId(), EXPIRED);
			List<PriceData> activeData = priceDataRepository.findByLocationIdAndStatus(office.getId(), ACTIVE);
			List<PriceData> pendingData = priceDataRepository.findByLocationIdAndStatus(office.getId(), PENDING);

			// List of Price Data to return
			List<PriceData> responsePriceData = new ArrayList<>();

			// process the expired data first
			// add expired data if it's active date or expiration data falls on or between the promo month start and end dates
			for(PriceData data : expiredData) {
				boolean isActiveDateInMonth = arizonaDateTimeComponent.isDateBetweenTwoDates(data.getActiveDate(), promotionById.getStartDate(), promotionById.getEndDate());
				boolean isExpirationDateInMonth = arizonaDateTimeComponent.isDateBetweenTwoDates(data.getExpirationDate(), promotionById.getStartDate(), promotionById.getEndDate());
				if(isActiveDateInMonth || isExpirationDateInMonth) {
					// System.out.println("Adding expired data " + data.getName() + " " + data.getModel().getModelNumber());
					responsePriceData.add(data);
				}
			}

			// process the active data
			for(PriceData data : activeData) {
				// any active data whose active date is before the promotion month should be included
				boolean isActiveDateBeforePromotionEnd = arizonaDateTimeComponent.isFirstDateAfterSecondDate(data.getActiveDate(), promotionById.getEndDate());
				if(isActiveDateBeforePromotionEnd) {
					// System.out.println("Adding active data " + data.getName() + " " + data.getModel().getModelNumber());
					responsePriceData.add(data);
				}
			}

			// process the pending data
			for(PriceData data : pendingData) {
				// for pending data
				// 1) It's active date has to be before the promotion month end date
				boolean isActiveDateBeforePromotionEnd = arizonaDateTimeComponent.isFirstDateAfterSecondDate(data.getActiveDate(), promotionById.getEndDate());
				boolean isExpirationDateInMonth = arizonaDateTimeComponent.isDateBetweenTwoDates(data.getExpirationDate(), promotionById.getStartDate(), promotionById.getEndDate());
				if(isActiveDateBeforePromotionEnd) {
					// System.out.println("Adding expired data " + data.getName() + " " + data.getModel().getModelNumber());
					// 2) You need to see if it is replacing any existing active data
					for (int i = 0; i < responsePriceData.size(); i++) {
						PriceData currentPriceData = responsePriceData.get(i);
						if(currentPriceData.getModelId() == data.getModelId()) {
							//		a) swap the active data with the empty expiration date with the new active data D-1
							//      b) if D-1 is before the promo month start, remove the active data
						}
					}
					responsePriceData.add(data);
				}
			}

			responsePackage.setPriceDataList(responsePriceData);

			// go through each pending data and update it
			// do this separate because it's possible for the date query to be after both active data and pending data if the
			// pending data has not gone live yet.  This removes duplicate data
			for (PriceData data : pendingData) {
				try {
					// if the pending data active data is on or before the queried string
					/*if (!arizonaDateTimeComponent.isFirstDateAfterSecondDate(data.getActiveDate(), promotionById.getStartDate())) {
						boolean modelExists = false;
						// remove any data that expires after the selected date
						for (int i = 0; i < responsePriceData.size(); i++) {
							PriceData currentPriceData = responsePriceData.get(i);
							// if the modelId matches, swap them in the list
							// if the model Ids match and the current data has no expiration date, this data is meant to replace that data
							if (currentPriceData.getModelId().equals(data.getModelId())) {
								// if the current price data has no expiration, we need to find when it would expire
								if(currentPriceData.getExpirationDate() == "") {
									boolean isNewExpirationDateAfterPromoStart = arizonaDateTimeComponent.isFirstDateAfterSecondDate(data.getActiveDate(), promotionById.getStartDate());
									if(isNewExpirationDateAfterPromoStart) {
										// it means this new data starts after the month promotion start, and that the previous data is in this month
										// do nothing
										responsePriceData.add(data);
									} else {
										// if false, it means the new data is at the start of the promo month, and the old data will be replaced by it and should not be returned
										System.out.println("Replacing " + currentPriceData.getModel() + " with " + data.getModelId());
										responsePriceData.set(i, data);
									}
								} else {
									// System.out.println("Replacing " + currentPriceData.getModel() + " with " + data.getModelId());
									// modelExists = true;
									// responsePriceData.set(i, data);
									responsePriceData.add(data);

								}
							}
						}*/

						// if the model was not found, return it
						/*if (!modelExists) {
							responsePriceData.add(data);
						}
					}*/
				} catch (Exception e) {
					e.printStackTrace();
					logger.error("Could not parse active date : " + data.getActiveDate());
				}
			}

			responsePackage.setPriceDataList(responsePriceData);
			return new ResponseEntity<>(responsePackage, HttpStatus.ACCEPTED);
		} catch (Exception e) {
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
