package com.api.demo.pricedata.controllers;

import com.api.demo.mongorepositories.applicationpackage.promotionmodellist.PromotionModelListRepository;
import com.api.demo.mongorepositories.applicationpackage.promotions.PromotionRepository;
import com.api.demo.pricedata.functions.ArizonaDateTimeComponent;
import com.api.demo.pricedata.repositories.models.ModelRepository;
import com.api.demo.pricedata.repositories.pricedata.LocationPriceData;
import com.api.demo.pricedata.repositories.pricedata.LocationSeriesList;
import com.api.demo.pricedata.repositories.pricedata.PriceData;
import com.api.demo.pricedata.repositories.pricedata.PriceDataRepository;
import com.api.demo.mongorepositories.applicationpackage.salesoffices.SalesOffice;
import com.api.demo.mongorepositories.applicationpackage.salesoffices.SalesOfficeRepository;
import com.api.demo.pricedata.restpackages.UpdateExpirationDatePackage;
import com.api.demo.pricedata.restpackages.UpdateResponse;
import com.api.demo.pricedata.repositories.equations.EquationRepository;
import com.api.demo.pricedata.repositories.variables.VariableRepository;
import lombok.Getter;
import lombok.Setter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.HttpStatusCodeException;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;


import static com.api.demo.pricedata.repositories.pricedata.PriceDataStatus.*;


@RestController
@RequestMapping("/pricedata")
public class PriceDataController {
	private static Logger logger = LoggerFactory.getLogger(PriceDataController.class);

	@Autowired
	private ModelRepository modelRepository;
	@Autowired
	private PriceDataRepository priceDataRepository;
	@Autowired
	private EquationRepository equationRepository;
	@Autowired
	private VariableRepository variableRepository;
	@Autowired
	private PromotionRepository promotionRepository;
	@Autowired
	private PromotionModelListRepository promotionModelListRepository;
	@Autowired
	private SalesOfficeRepository salesOfficeRepository;

	SimpleDateFormat format = new SimpleDateFormat("MM/dd/yyyy");


	/**** Get All Price Data For A Location (ACTIVE, PENDING, DRAFTS, EXPIRED) ****/
	@GetMapping("/location")
	@ResponseBody
	public ResponseEntity<Object> getLocationPriceData(@RequestParam("locationId") String locationId) {
		if(locationId.isEmpty()) {
			List<LocationPriceData> data = new ArrayList<>();
			List<SalesOffice> offices = salesOfficeRepository.findAll();
			for(SalesOffice office : offices) {
				LocationPriceData locationPriceData = getLocationData(office);
				data.add(locationPriceData);
			}
			return new ResponseEntity<>(data, HttpStatus.ACCEPTED);
		} else {
			try {
				SalesOffice office = salesOfficeRepository.findById(locationId).orElseThrow(() -> new HttpStatusCodeException(HttpStatus.NOT_FOUND) {});
				LocationPriceData locationPriceData = getLocationData(office);
				return new ResponseEntity<>(locationPriceData, HttpStatus.ACCEPTED);
			} catch (Exception e) {
				logger.error("Office ID " + locationId + " could not be found");
				return new ResponseEntity<>("Office ID " + locationId + " could not be found", HttpStatus.NOT_FOUND);
			}
		}
	}


	/**** Get All ACTIVE Price Data For A Location (with check for pending updates) ****/
	@GetMapping("/location/current")
	@ResponseBody
	public ResponseEntity<Object> getCurrentPricing(@RequestParam("locationId") String locationId) {
		try {
			SalesOffice office = salesOfficeRepository.findById(locationId).orElseThrow(() -> new HttpStatusCodeException(HttpStatus.NOT_FOUND) {});
			LocationPriceData locationPriceData = new LocationPriceData(office);
			String officeID = office.getId();
			List<PriceData> active = priceDataRepository.findByLocationIdAndStatus(officeID, ACTIVE);

			// check pending for any updates today
			List<PriceData> pending = priceDataRepository.findByLocationIdAndStatus(officeID, PENDING);

			for (PriceData _data : active) {
				locationPriceData.addActiveData(_data);
			}

			return new ResponseEntity<>(locationPriceData, HttpStatus.ACCEPTED);
		} catch (Exception e) {
			logger.error("Office ID " + locationId + " could not be found");
			return new ResponseEntity<>("Office ID " + locationId + " could not be found", HttpStatus.NOT_FOUND);
		}
	}

	/**** Get All Series For A Location ****/
	@GetMapping("/location/series")
	@ResponseBody
	public ResponseEntity<Object> getCurrentLocationSeries(@RequestParam("locationId") String locationId) {
		try {
			SalesOffice office = salesOfficeRepository.findById(locationId).orElseThrow(() -> new HttpStatusCodeException(HttpStatus.NOT_FOUND) {});
			LocationSeriesList seriesList = new LocationSeriesList(office);

			String officeID = office.getId();
			List<PriceData> active = priceDataRepository.findByLocationIdAndStatus(officeID, ACTIVE);

			for (PriceData _data : active) {
				seriesList.addSeries(_data.getSeriesName());
			}

			return new ResponseEntity<>(seriesList, HttpStatus.ACCEPTED);
		} catch (Exception e) {
			logger.error("Office ID " + locationId + " could not be found");
			return new ResponseEntity<>("Office ID " + locationId + " could not be found", HttpStatus.NOT_FOUND);
		}
	}

	/**
	 * This function sets the expiration date for any price data with the status ACTIVE or DRAFT.  You cannot set the expiration date for
	 * EXPIRED data (for obvious reasons), nor can you set the expiration date for PENDING data, as that data needs to remain in an immutable
	 * state to ensure it is published at the correct time without any conflicts
	 *
	 * @param updateExpirationDatePackage	Object contains a list of ids to update and the new expiration date
	 * @return responseData
	 */
	@PostMapping("/setexpiration")
	@ResponseBody
	public ResponseEntity<Object> setExpirationDate(@RequestBody UpdateExpirationDatePackage updateExpirationDatePackage) {
		// this list returns if any of the updates failed
		List<UpdateResponse> responseData = new ArrayList<>();
		// the date/time component used for validating the dates
		ArizonaDateTimeComponent arizonaDateTimeComponent = new ArizonaDateTimeComponent();


		try {
			// get the list of price data IDs to update from the request package
			List<String> priceDataIds = updateExpirationDatePackage.getPriceDataIds();
			// get the new expiration date from the request package
			String newExpirationDate = updateExpirationDatePackage.getNewExpirationDate();
			// validate the expiration date
			if(!arizonaDateTimeComponent.isDateStringValid(newExpirationDate)) {
				// invalid expiration date
				return new ResponseEntity<>("Invalid Expiration Date", HttpStatus.CONFLICT);
			}


			for (String id : priceDataIds) { // get each price data by id
				try {
					PriceData data = priceDataRepository.findById(id).orElseThrow(() -> new HttpStatusCodeException(HttpStatus.NOT_FOUND) {
					});
					if (data.isDraft()) {
						data.setExpirationDate(newExpirationDate);
						priceDataRepository.save(data);
						responseData.add(new UpdateResponse(true, "", data));
					} else if (data.isActive()) {
						if(!arizonaDateTimeComponent.isFirstDateAfterSecondDate(newExpirationDate, arizonaDateTimeComponent.getTodayDateString())) {
							data.expireData(newExpirationDate); // expiration is on or before today
						} else {
							data.setExpirationDate(newExpirationDate); // expiration is in the future
						}
						priceDataRepository.save(data);
						responseData.add(new UpdateResponse(true, "", data));
					} else {
						// data is not active or in a draft state
						responseData.add(new UpdateResponse(false, "Cannot set expiration for retired/pending data", data));
					}
				} catch (HttpStatusCodeException e) {
					// if you cannot find the id, let the user know
					responseData.add(new UpdateResponse(false, "Could not find ID " + id, new PriceData()));
				} catch (Exception e) {
					// catch any unknown errors
					responseData.add(new UpdateResponse(false, "Unknown error for " + id + ": " + e.getMessage(), new PriceData()));
				}
			}
			// return the response data
			return new ResponseEntity<>(responseData, HttpStatus.ACCEPTED);
		} catch (Exception e) {
			logger.error(e.getMessage());
			// e.printStackTrace();
			return new ResponseEntity<>(e.getMessage(), HttpStatus.CONFLICT);
		}
	}


	@PostMapping("/export")
	@ResponseBody
	public ResponseEntity<Object> exportData(@RequestBody DataExportRequest dataExportRequest) {
		// https://www.baeldung.com/java-csv
		List<String> priceDataIds = dataExportRequest.getPriceDataIds();
		// these data keys are also headers
		// change these to objects.  Pass the header name, the key name, and the type
		List<String> dataKeys = dataExportRequest.getDataKeys();

		// list of comma separated lines
		List<String[]> dataLines = new ArrayList<>();
		dataLines.add(buildHeaderRow());

		try {
			for(String id : priceDataIds) {
				PriceData data = priceDataRepository.findById(id).orElseThrow(() -> new HttpStatusCodeException(HttpStatus.NOT_FOUND) {});
				String[] dataString = buildStringData(data);
				dataLines.add(dataString);
			}

			File csvOuputFile = givenDataArray_whenConvertToCSV_thenOutputCreated(dataLines, "pa_csv_export.csv");
			// for each price data
				// get the data by id
				// initialize the new csv line string
				// for each data key
					// initialize the key value string

					// access properties through reflection
					// https://stackoverflow.com/questions/48285798/java-accessing-the-properties-of-an-object-without-using-the-direct-field-name
					// if MODEL, access the model properties
					// if PRICEDATA, access the price data
					// if CORE, access core data
					// if VAR, search the variables
					// if EQN, search the equations

					// replace the value string for any instance found

					// append to the new csv line with a comma

			// append the header row to the top of the file, with the header titles instead of the keys there

			// return the csv
			return new ResponseEntity<>(csvOuputFile, HttpStatus.ACCEPTED);
		} catch (Exception e) {
			logger.error(e.getMessage());
			// e.printStackTrace();
			return new ResponseEntity<>(e.getMessage(), HttpStatus.CONFLICT);
		}
	}

	public String[] buildHeaderRow() {
		String[] headers = {"id", "factoryId", "name", "modelNumber", "seriesName",
			"type",
			"width",
			"length1",
			"length2",
			"numberOfBathrooms",
			"numberOfBedrooms",
			"numberOfDens",
			"extraFeatures",
			"notes",
			"estimatedSquareFeet",
			"activeDate",
			"expirationDate",
			"basePrice",
			"factoryTotalCost",
			"msrp",
			"factoryDirectPrice",
			"firstHalfAdvertisingPrice",
			"secondHalfAdvertisingPrice"
		};

		return headers;
	}

	public String[] buildStringData(PriceData data) {
		String[] dataString = {
			data.getId(),
			data.getModel().getFactoryId(),
				data.getName(),
				data.getModel().getModelNumber(),
				data.getSeriesName(),
				data.getModel().getType(),
				String.valueOf(data.getModel().getWidth()),
				String.valueOf(data.getModel().getLength1()),
				String.valueOf(data.getModel().getLength2()),
				String.valueOf(data.getModel().getNumberOfBathrooms()),
				String.valueOf(data.getModel().getNumberOfBedrooms()),
				String.valueOf(data.getModel().getNumberOfDens()),
				data.getModel().getExtraFeatures(),
				data.getModel().getNotes(),
				String.valueOf(data.getModel().getEstimatedSquareFeet()),
				data.getActiveDate(),
				data.getExpirationDate(),
				String.valueOf(data.getBasePrice()),
				String.valueOf(data.getFactoryTotalCost()),
				String.valueOf(data.getMsrp()),
				String.valueOf(data.getFactoryDirectPrice()),
				String.valueOf(data.getFirstHalfAdvertisingPrice()),
				String.valueOf(data.getSecondHalfAdvertisingPrice())

		};
		return dataString;
	}

	public String convertToCSV(String[] data) {
		return Stream.of(data).map(this::escapeSpecialCharacters).collect(Collectors.joining(","));
	}

	public File givenDataArray_whenConvertToCSV_thenOutputCreated(List<String[]> dataLines, String fileName) throws IOException {
		File csvOutputFile = new File(fileName);
		try (PrintWriter pw = new PrintWriter(csvOutputFile)) {
			dataLines.stream()
					.map(this::convertToCSV)
					.forEach(pw::println);
		}

		// assertTrue(csvOutputFile.exists());

		return csvOutputFile;
	}

	public String escapeSpecialCharacters(String data) {
		String escapedData = data.replaceAll("\\R", " ");
		if (data.contains(",") || data.contains("\"") || data.contains("'")) {
			data = data.replace("\"", "\"\"");
			escapedData = "\"" + data + "\"";
		}
		return escapedData;
	}

	@Getter
	@Setter
	public static class DataExportRequest {
		// the list of IDs for data you wish to export
		List<String> priceDataIds;
		// the list of keys for what fields in the data you wish to return
		List<String> dataKeys;
		public DataExportRequest() {
			this.priceDataIds = new ArrayList<>();
			this.dataKeys = new ArrayList<>();
		}
	}


	private LocationPriceData getLocationData(SalesOffice office) {
		LocationPriceData locationPriceData = new LocationPriceData(office);
		String officeID = office.getId();
		List<PriceData> active = priceDataRepository.findByLocationIdAndStatus(officeID, ACTIVE);
		List<PriceData> pending = priceDataRepository.findByLocationIdAndStatus(officeID, PENDING);
		List<PriceData> drafts = priceDataRepository.findByLocationIdAndStatus(officeID, DRAFT);

		for (PriceData _data : active) {
			locationPriceData.addActiveData(_data);
		}

		for (PriceData _data : pending) {
			locationPriceData.addPendingData(_data);
		}

		for (PriceData _data : drafts) {
			locationPriceData.addDraftData(_data);
		}

		return locationPriceData;
	}



}
