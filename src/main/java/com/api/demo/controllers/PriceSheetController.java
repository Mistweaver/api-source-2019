package com.api.demo.controllers;

import com.api.demo.mongorepositories.applicationpackage.pricesheetchanges.Change;
import com.api.demo.mongorepositories.applicationpackage.pricesheetchanges.ChangeRepository;
import com.api.demo.mongorepositories.applicationpackage.pricesheets.PriceSheet;
import com.api.demo.mongorepositories.applicationpackage.pricesheets.PriceSheetRepository;
import com.api.demo.mongorepositories.applicationpackage.pricesheets.Series;
import com.api.demo.utilityfunctions.JsonResponse;
import com.api.demo.security.utils.TokenUtility;
import com.google.gson.Gson;
import org.json.simple.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.HttpStatusCodeException;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import static java.lang.Integer.parseInt;

@RestController
@RequestMapping("/pricesheets")
public class PriceSheetController {
    private static Logger logger = LoggerFactory.getLogger(PriceSheetController.class);
    @Autowired
    private PriceSheetRepository priceSheetRepository;
    @Autowired
    private ChangeRepository changeRepository;

    private TokenUtility tokenUtility = new TokenUtility();
    Gson gson = new Gson();

    @GetMapping("/getpricesheet")
    @ResponseBody
    public ResponseEntity<JSONObject> getlocationpricesheet(@RequestHeader("Authorization") String token, @RequestParam int locationId) {
        List<PriceSheet> activePriceSheets = new ArrayList<>();
        // List<PriceSheet> priceSheets = priceSheetRepository.findByLocationId(locationId);

        return new ResponseEntity<>(JsonResponse.JsonResponse("Lead add success", "", HttpStatus.ACCEPTED), HttpStatus.CREATED);
    }

    @GetMapping("/admingetcurrentsheet")    // automates price sheet creation for next month
    @ResponseBody
    public ResponseEntity<String> adminGetCurrentSheet(@RequestParam String locationId) {
        // check current date
        Calendar cal = Calendar.getInstance();
        cal.get(Calendar.MONTH);    // current month
        PriceSheet thisMonthsPriceSheet = new PriceSheet();
        PriceSheet nextMonthsPriceSheet = new PriceSheet();

        // check if this months sheet exists
            // true, fetch sheet then check for next months sheet
                // check if next months sheet exists
                if(cal.get(Calendar.MONTH) == 11) {
                    nextMonthsPriceSheet = priceSheetRepository.findByMonthAndYearAndLocationId(0, (cal.get(Calendar.YEAR) + 1), locationId);
                } else {
                    nextMonthsPriceSheet = priceSheetRepository.findByMonthAndYearAndLocationId(cal.get(Calendar.MONTH) + 1, cal.get(Calendar.YEAR), locationId);
                }
                // if no, create next months sheet

            // false, create this months sheet
        // return this months sheet
        return new ResponseEntity<>("Maintenance run", HttpStatus.ACCEPTED);
    }

    // add price sheet
    @PostMapping("/add-price-sheet")
    @ResponseBody
    public ResponseEntity<JSONObject> addPriceSheet(@RequestHeader("Authorization") String token, @RequestBody JSONObject body) {
        try {
            String locationId = body.get("locationId").toString();
            PriceSheet newSheet = new PriceSheet();
            newSheet.setLocationId(locationId);
            PriceSheet sheet = priceSheetRepository.save(newSheet);

            addPriceSheetChange( tokenUtility.getUsernameFromToken(token) + " added price sheet for " + (sheet.getMonth() + 1) + "/" + sheet.getYear(), locationId);


            return new ResponseEntity<>(JsonResponse.JsonResponse("Series added", sheet, HttpStatus.ACCEPTED), HttpStatus.CREATED);
        } catch (Exception e) {
            return new ResponseEntity<>(JsonResponse.JsonResponse("Failed to add series", "", HttpStatus.CONFLICT), HttpStatus.CONFLICT);
        }
    }

    // duplicate price sheet for next month
    @GetMapping("/check-next-months-sheet")
    @ResponseBody
    public ResponseEntity<JSONObject> checkNextMonthSheet(@RequestParam("locationId") String locationId) {
        Calendar cal = Calendar.getInstance();
        PriceSheet sheet = new PriceSheet();
        try {
            if(cal.get(Calendar.MONTH) == 11) {
                sheet = priceSheetRepository.findByMonthAndYearAndLocationId(0, (cal.get(Calendar.YEAR) + 1), locationId);
            } else {
                sheet = priceSheetRepository.findByMonthAndYearAndLocationId(cal.get(Calendar.MONTH) + 1, cal.get(Calendar.YEAR), locationId);
            }
            return new ResponseEntity<>(JsonResponse.JsonResponse("Series Queried", sheet, HttpStatus.ACCEPTED), HttpStatus.CREATED);
        } catch (Exception e) {
            return new ResponseEntity<>(JsonResponse.JsonResponse("Error checking for price sheet", "", HttpStatus.CONFLICT), HttpStatus.CONFLICT);
        }
    }

    // duplicate price sheet for next month
    @PostMapping("/add-next-sheet")
    @ResponseBody
    public ResponseEntity<JSONObject> addNextSheet(@RequestHeader("Authorization") String token, @RequestBody JSONObject body) {
        try {
            String locationId = body.get("locationId").toString();
            Calendar cal = Calendar.getInstance();
            PriceSheet sheet = priceSheetRepository.findByMonthAndYearAndLocationId(cal.get(Calendar.MONTH), cal.get(Calendar.YEAR), locationId);
            PriceSheet newSheet = priceSheetRepository.save(sheet.duplicateSheetForNextMonth());

            addPriceSheetChange( tokenUtility.getUsernameFromToken(token) + " added price sheet for " + (newSheet.getMonth() + 1) + "/" + newSheet.getYear(), locationId);


            return new ResponseEntity<>(JsonResponse.JsonResponse("Sheet added", sheet, HttpStatus.ACCEPTED), HttpStatus.CREATED);
        } catch (Exception e) {
            return new ResponseEntity<>(JsonResponse.JsonResponse("Failed to add sheet", "", HttpStatus.CONFLICT), HttpStatus.CONFLICT);
        }
    }

    // duplicate last months price sheet for previous month
    @PostMapping("/copy-last-months-sheet")
    @ResponseBody
    public ResponseEntity<JSONObject> copyLastMonthsSheet(@RequestHeader("Authorization") String token, @RequestBody JSONObject body) {
        try {
            String locationId = body.get("locationId").toString();
            Calendar cal = Calendar.getInstance();
            PriceSheet sheet = new PriceSheet();
            PriceSheet newSheet = new PriceSheet();
            if(cal.get(Calendar.MONTH) == 0) {
                sheet = priceSheetRepository.findByMonthAndYearAndLocationId(11, (cal.get(Calendar.YEAR) - 1), locationId);
                newSheet = priceSheetRepository.save(sheet.copyPreviousSheetForCurrentMonth());
                addPriceSheetChange( tokenUtility.getUsernameFromToken(token) + " added price sheet for " + (newSheet.getMonth() + 1) + "/" + newSheet.getYear(), locationId);

            } else {
                sheet = priceSheetRepository.findByMonthAndYearAndLocationId(cal.get(Calendar.MONTH) - 1, cal.get(Calendar.YEAR), locationId);
                System.out.println(sheet.getMonth());
                newSheet = priceSheetRepository.save(sheet.copyPreviousSheetForCurrentMonth());
                addPriceSheetChange( tokenUtility.getUsernameFromToken(token) + " added price sheet for " + (newSheet.getMonth() + 1) + "/" + newSheet.getYear(), locationId);
            }

            return new ResponseEntity<>(JsonResponse.JsonResponse("Sheet added", newSheet, HttpStatus.ACCEPTED), HttpStatus.CREATED);
        } catch (Exception e) {
            return new ResponseEntity<>(JsonResponse.JsonResponse("Failed to add sheet", "", HttpStatus.CONFLICT), HttpStatus.CONFLICT);
        }
    }

    // add series
    @PostMapping("/add-series")
    @ResponseBody
    public ResponseEntity<JSONObject> addSeries(@RequestHeader("Authorization") String token, @RequestBody JSONObject body) {
        try {
            String seriesName = body.get("name").toString();
            String seriesType = body.get("type").toString();
            String seriesManufacturer = body.get("manufacturer").toString();
            String priceSheetId = body.get("priceSheetId").toString();

            PriceSheet sheet = priceSheetRepository.findById(priceSheetId).orElseThrow(() -> new HttpStatusCodeException(HttpStatus.NOT_FOUND) {});

            Series newSeries = new Series();
            newSeries.setSeriesName(seriesName);
            newSeries.setType(seriesType);
            newSeries.setManufacturer(seriesManufacturer);
            sheet.addSeries(newSeries);
            priceSheetRepository.save(sheet);

            addPriceSheetChange( tokenUtility.getUsernameFromToken(token) + " added series " + seriesName, sheet.getLocationId());

            return new ResponseEntity<>(JsonResponse.JsonResponse("Series added", sheet, HttpStatus.ACCEPTED), HttpStatus.CREATED);
        } catch (Exception e) {
            return new ResponseEntity<>(JsonResponse.JsonResponse("Failed to add series", "", HttpStatus.CONFLICT), HttpStatus.CONFLICT);
        }
    }

    private void addPriceSheetChange(String description, String locationId) {
        Change newChange = new Change();
        newChange.setLocationId(locationId);
        newChange.setDescription(description);
        changeRepository.save(newChange);
    }


}
