package com.api.demo.pricedata.controllers;

import com.api.demo.mongorepositories.applicationpackage.pricesheets.*;
import com.api.demo.pricedata.KeyConstants;
import com.api.demo.pricedata.functions.ArizonaDateTimeComponent;
import com.api.demo.pricedata.repositories.models.Model;
import com.api.demo.pricedata.repositories.models.ModelRepository;
import com.api.demo.pricedata.repositories.pricedata.PriceData;
import com.api.demo.pricedata.repositories.pricedata.PriceDataRepository;
import com.api.demo.mongorepositories.applicationpackage.salesoffices.SalesOffice;
import com.api.demo.mongorepositories.applicationpackage.salesoffices.SalesOfficeRepository;
import com.api.demo.pricedata.repositories.equations.Equation;
import com.api.demo.pricedata.repositories.equations.EquationData;
import com.api.demo.pricedata.repositories.equations.EquationRepository;
import com.api.demo.pricedata.repositories.variables.Variable;
import com.api.demo.pricedata.repositories.variables.VariableData;
import com.api.demo.pricedata.repositories.variables.VariableRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.HttpStatusCodeException;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.api.demo.pricedata.repositories.pricedata.PriceDataStatus.*;

@RestController
@RequestMapping("/legacydata")
public class LegacyDataController {
	private static Logger logger = LoggerFactory.getLogger(LegacyDataController.class);

	@Autowired
	private PriceSheetRepository priceSheetRepository;
	@Autowired
	private ModelRepository modelRepository;
	@Autowired
	private PriceDataRepository priceDataRepository;
	@Autowired
	private SalesOfficeRepository salesOfficeRepository;

	@Autowired
	private VariableRepository variableRepository;
	@Autowired
	private EquationRepository equationRepository;

	@PostMapping("/deletealldata")
	@ResponseBody
	public ResponseEntity<String> deleteAllData() {
		ArizonaDateTimeComponent arizonaDateTimeComponent = new ArizonaDateTimeComponent();
		// manufacturerRepository.deleteAll();
		// modelRepository.deleteAll();
		// priceDataRepository.deleteAll();
		return new ResponseEntity<>("All data deleted", HttpStatus.ACCEPTED);
	}

	@PostMapping("/expireallactivedata")
	@ResponseBody
	public ResponseEntity<String> expireAllActiveData() {
		ArizonaDateTimeComponent arizonaDateTimeComponent = new ArizonaDateTimeComponent();
		List<PriceData> data = priceDataRepository.findByStatus(ACTIVE);
		for(int i = 0; i < data.size(); i++) {
			PriceData priceData = data.get(i);
			priceData.expireData("12/31/2020");
			priceDataRepository.save(priceData);
			System.out.println("Expired " + priceData.getName() + " " + priceData.getModel().getModelNumber() + " on " + priceData.getExpirationDate());
		}
		return new ResponseEntity<>("All data deleted", HttpStatus.ACCEPTED);
	}


	@PostMapping("/deleteemptymodels")
	@ResponseBody
	public ResponseEntity<String> deleteEmptyModels() {
		List<Model> allModels = modelRepository.findAll();
		int i = 0;
		while(i < allModels.size()) {
			Model model = allModels.get(i);
			System.out.println("Examining " + model.getModelNumber() + " (" + i + "/" + allModels.size() + ")");

			// get price data for each model
			List<PriceData> dataForModel = priceDataRepository.findByModelId(model.getId());
			boolean doesActivePriceDataExist = false;
			for(PriceData data : dataForModel) {
				if(data.isActive() || data.isPending() || data.isDraft()) {
					doesActivePriceDataExist = true;
				}
			}

			if(!doesActivePriceDataExist) {
				logger.warn("Deleting model " + model.getModelNumber());
				modelRepository.delete(model);
			}

			i++;

		}
		return new ResponseEntity<>("All data deleted", HttpStatus.ACCEPTED);
	}


	@PostMapping("/auditdata")
	@ResponseBody
	public ResponseEntity<Object> auditVariablesAndEquationsInPriceData() {
		// get all data
		List<PriceData> priceDataList = priceDataRepository.findAll();
		List<Model> models = modelRepository.findAll();

		// for each data
		// for(PriceData data : priceDataList) {
			// System.out.println("Updating " + data.getId());
			// data.setSeriesName(data.getSeriesName().trim());
			/*if(data.isActive()) {
				data.clearUpdate();
			}*/
			// get the model by Id and update the local copy
			/*try {
				Model model = modelRepository.findById(data.getModelId()).orElseThrow(() -> new HttpStatusCodeException(HttpStatus.NOT_FOUND) {});;
				data.setModel(model);
			} catch (HttpStatusCodeException e) {
				logger.error("Could not find model id " + data.getModelId() + " for data " + data.getId());
			}

			// get all variables
			List<VariableData> variableDataList = data.getVariables();
			for(VariableData var : variableDataList) {
				// find the variable by key
				Variable variable = variableRepository.findByKey(var.getKey());
				if(variable != null) {
					// update the id
					var.updateFromVariable(variable);
				} else {
					logger.error("Could not find variable " + var.getKey() + " by key in data " + data.getId());
				}

			}


			// get all equations
			List<EquationData> equationDataList = data.getEquations();
			for(EquationData eqn : equationDataList) {
				// find the equation by key
				Equation equation = equationRepository.findByKey(eqn.getKey());
				// update the id
				if(equation != null) {
					// update the id
					eqn.updateFromEquation(equation);
				} else {
					logger.error("Could not find equation " + eqn.getKey() + " by key in data " + data.getId());
				}
			}*/

			// priceDataRepository.save(data);
		// }

		// delete all models that do not have price data
		/*int modelDeletionCount = 0;
		for(int i = 0; i < models.size(); i++) {
			List<PriceData> dataForModel = priceDataRepository.findByModelId(models.get(i).getId());
			if(dataForModel.size() == 0) {
				logger.warn("Deleting model " + models.get(i).getId());
				modelRepository.delete(models.get(i));
				modelDeletionCount++;
			}
			System.out.print(i + "/" + models.size() + " models checked");
		}

		logger.warn(modelDeletionCount + " deleted");*/

		/**** QUICK HACK FOR Subtotal / SUBTOTAL issues ****/
		List<PriceData> drafts = priceDataRepository.findByStatus(DRAFT);
		// String[] subtotalKeyVariations = {"[subtotal]", "[Subtotal]", "[SubTotal]"};
		// Equation correctSubtotalEquation = equationRepository.findByKey("[SUBTOTAL]");

		/**
		 * Define Equation key "is" and "should be" strings
		 */
		String isKey = "";
		String sbKey = "[]";

		for(PriceData draft : drafts) {
			// Equation correctFactoryDirectPrice = equationRepository.findByKey(FACTORY_DIRECT_PRICE_KEY);

			/**
			 * Code to Find unbracketed keys, and add brackets to heal the key format.
			 * Perry Spagnola - 2021-01-29
			 */
//			for(EquationData equationData : draft.getEquations()) {
//				String key = equationData.getKey();
//				if(!key.startsWith("[") && !key.endsWith("]")) {
//					logger.info("original key: " + key);
//
//					String newKey = "[" + key + "]";
//					logger.info(("new Key: " + newKey));
//
//					equationData.setKey(newKey);
//
//					// confirm new key has been set correctly
//					if(!equationData.getKey().equals("[" + key + "]")) {
//						logger.error("key failed to set correctly: " + key);
//						return new ResponseEntity<>("key failed to set correctly: " + key, HttpStatus.BAD_REQUEST);
//					}
//				}
//			}
			/**
			 * End - heal key bracket format
			 */

			/**
			 * Key find and replace code
			 * Note: The following code does not work in an obvious way. Be CAREFUL using it.
			 * Perry Spagnola - 2021-01-29
			 */
//			EquationData wrongFactoryDirectPrice = draft.getEquationDataByKey(isKey); // Find the equation definition with the corrupt key
//			if(wrongFactoryDirectPrice != null) {
//				logger.error("iterated to model: " + draft.getName() + " in series: " + draft.getSeriesName());
//
//				logger.error("Modifying incorrect equation key: " + wrongFactoryDirectPrice.getKey());
//				wrongFactoryDirectPrice.setKey(sbKey); // Fix the corrupt key.

//				List<EquationData> equationsInDraft = draft.getEquations();

//				for (int j = 0; j < equationsInDraft.size(); j++) {
//					EquationData draftEquation = equationsInDraft.get(j);
//					String draftEquationString = draftEquation.getEquation();
//
//					Pattern keyPattern = Pattern.compile(Pattern.quote(isKey), Pattern.DOTALL);
//					Matcher keyMatcher = keyPattern.matcher(draftEquationString);
//					List<String> allMatches = new ArrayList<String>();
//
//					while (keyMatcher.find()) {
//						allMatches.add(keyMatcher.group(0));
//						System.out.println("MATCHES " + allMatches.toString());
//						for (String match : allMatches) {
//							System.out.println("Matched: " + match + " in [" + draftEquation.getEquation() + "]");
//							// for each match, swap with the variable value
//							logger.warn("original equation string: " + draftEquation.getEquation());
//
//							String replacementString = draftEquation.getEquation().replace(match, sbKey);
//							logger.info("replacement equation string: " + replacementString);
//							//draftEquation.setEquation(replacementString);
//							logger.warn(draftEquation.getEquation());
//
//						}
//					}
//				}
//			}
			/**
			 * End - Key find and replace code
			 */





			/*for(int i = 0; i < subtotalKeyVariations.length; i++) {
				EquationData subtotalEquation = draft.getEquationDataByKey(subtotalKeyVariations[i]);
				if(subtotalEquation != null) {

					logger.error("Replacing subtotal variant");
					logger.warn(subtotalEquation.getId() + ", " + subtotalEquation.getKey() + ", " + subtotalEquation.getName());
					subtotalEquation.setKey(correctSubtotalEquation.getKey());
					subtotalEquation.setId(correctSubtotalEquation.getId());
					subtotalEquation.setName(correctSubtotalEquation.getName());
					logger.warn(subtotalEquation.getId() + ", " + subtotalEquation.getKey() + ", " + subtotalEquation.getName());


					List<EquationData> equationsIndDraft = draft.getEquations();

					for(int j = 0; j < equationsIndDraft.size(); j++) {
						EquationData draftEquation = equationsIndDraft.get(j);
						String draftEquationString = draftEquation.getEquation();

						Pattern keyPattern = Pattern.compile(Pattern.quote(subtotalKeyVariations[i]), Pattern.DOTALL);
						Matcher keyMatcher = keyPattern.matcher(draftEquationString);
						List<String> allMatches = new ArrayList<String>();

						while (keyMatcher.find()) {
							allMatches.add(keyMatcher.group(0));
							System.out.println("MATCHES " + allMatches.toString());
							for (String match : allMatches) {
								System.out.println("Matched: " + match + " in [" + draftEquation.getEquation() + "]");
								// for each match, swap with the variable value
								logger.warn(draftEquation.getEquation());
								String replacementString = draftEquation.getEquation().replace(match, "[SUBTOTAL]");
								draftEquation.setEquation(replacementString);
								logger.warn(draftEquation.getEquation());

							}
						}

					}
				}
			}*/


//			priceDataRepository.save(draft);
			/**
			 * Remove for use.
			 */
			return new ResponseEntity<>("Disabled!", HttpStatus.ACCEPTED);
		}



		return new ResponseEntity<>("All data audited", HttpStatus.ACCEPTED);

	}

	@GetMapping("/convertdataformonth")
	@ResponseBody
	public ResponseEntity<String> convertDataForLocation(@RequestParam int month, @RequestParam int year) {
		// return these in the catch block
		String currentOffice = "";
		String currentSeries = "";
		String currentModel = "";
		String currentPriceSheetId = "";
		Calendar calendar = Calendar.getInstance();
		String today = calendar.get(Calendar.MONTH) + "/" + calendar.get(Calendar.DAY_OF_MONTH) + "/" + calendar.get(Calendar.YEAR);

		try {
			List<PriceSheet> priceSheetList = priceSheetRepository.findByMonthAndYear(month, year);

			for(PriceSheet sheet : priceSheetList) {
				currentPriceSheetId = sheet.getId();
				String locationId = sheet.getLocationId();
				// get the sales office
				SalesOffice salesOffice = salesOfficeRepository.findById(locationId).orElseThrow(() -> new HttpStatusCodeException(HttpStatus.NOT_FOUND) {});
				// error handling
				currentOffice = salesOffice.getOfficeName() + ", " + salesOffice.getOfficeCity();
				// get the series list for the price sheet
				List<Series> seriesList = sheet.getSeriesList();
				for(Series series : seriesList) {
					// error handling
					currentSeries = series.getSeriesName();
					// get the table data for the series
					List<SheetRow> rows = series.getTableData().getRows();

					// first row is the headers, so start with the second row
					for(int i = 1; i < rows.size(); i++) {
						List<SheetCell> cells = rows.get(i).getCells();
						String modelName = cells.get(series.getModelNameIndex()).getValue().trim();
						String modelNumber = cells.get(series.getModelNoIndex()).getValue().trim();
						String basePrice = cells.get(series.getBasePriceIndex()).getValue();

						logger.info(salesOffice.getOfficeName() + ", " + salesOffice.getOfficeCity() + " : " + series.getSeriesName() + ", row " + i);
						currentModel = modelName + " " + modelNumber;

						// get model
						// model number must not be blank
						if(!modelNumber.trim().equals("")) {

							Model model = new Model();
							model.setEstimatedSquareFeet(new BigDecimal(cells.get(series.getSqftIndex()).getValue()));
							model.setNotes(cells.get(series.getBedAndBathIndex()).getValue() + " , " + cells.get(series.getSizeIndex()).getValue());
							model.setFactoryId(series.getManufacturer());
							model.setModelNumber(modelNumber);
							model.setType(series.getType());
							model = modelRepository.save(model);

							// check if model is in the inventory class
							// create new Price Data
							PriceData newPriceData = new PriceData();
							// populate the price data
							newPriceData.setLocationId(locationId);
							newPriceData.setModel(model);
							newPriceData.setSeriesName(series.getSeriesName().trim());
							newPriceData.setName(modelName);
							try {
								newPriceData.setBasePrice(new BigDecimal(basePrice));
							} catch (Exception e) {
								logger.error("Could not parse base price value from old sheets");
								newPriceData.setBasePrice(new BigDecimal(0));
								newPriceData.setError("Could not parse base price value from old sheets");
							}

							Map<String, VariableData> rowVariables = new HashMap<>();
							Map<String, EquationData> rowEquations = new HashMap<>();

							try {
								// process each cell in the row
								for (SheetCell cell : cells) {
									analyzeCell(cell, series, rows.get(0), rowVariables, rowEquations);
								}
								// replace SUMS() with the list of equation keys in the sum
								for (Map.Entry<String, EquationData> set : rowEquations.entrySet()) {
									EquationData equation = set.getValue();
									// System.out.println("Replace sums in equations with variable keys");
									Pattern pattern = Pattern.compile("SUM\\((.*?)\\)", Pattern.DOTALL);
									Matcher matcher = pattern.matcher(equation.getEquation());
									while (matcher.find()) {
										// System.out.println("Replacing SUMS in equation " + equation.getEquation());
										String[] keys = matcher.group(1).split(":");
										String startIndex = keys[0].replaceAll("[^A-Z]", "");
										String endIndex = keys[1].replaceAll("[^A-Z]", "");
										// System.out.println("Start Index: " + startIndex);
										// System.out.println("End Index: " + endIndex);
										// array of keys
										List<String> keyArray = new ArrayList<>();

										for (int k = convertIndexStringToNumber(startIndex); k <= convertIndexStringToNumber(endIndex); k++) {
											String headerColumnKey = rows.get(0).getCells().get(k).getValue().replaceAll("[^a-zA-Z0-9]", "").trim();
											if (!headerColumnKey.equals("")) {    // if the key row is not blank, move forward
												keyArray.add(convertIndexToString((k)));
											}
										}


										// new equation string
										String newEquationString = "";
										for (int m = 0; m < keyArray.size(); m++) {
											if (m == keyArray.size() - 1) {
												newEquationString = newEquationString + "#" + keyArray.get(m);
											} else {
												newEquationString = newEquationString + "#" + keyArray.get(m) + " + ";
											}
										}

										// System.out.println("SUM Equation: " + newEquationString);
										// System.out.println("OLD Equation: " + equation.getEquation());
										// System.out.println("Match to replace: " + matcher.group(1));
										String replacementString = equation.getEquation().replace(matcher.group(1), newEquationString);
										// System.out.println("Replacement String: " + replacementString);
										// replacementString = replacementString.replace("SUM(", "");
										replacementString = replacementString.replace("SUM", "");
										// replacementString = replacementString.replace(")", " ) ");
										equation.setEquation(replacementString);
										// System.out.println("New Equation: " + equation.getEquation());
									}
								}

								// Strip the numbers off of all letter combinations
								for (Map.Entry<String, EquationData> set : rowEquations.entrySet()) {
									EquationData equation = set.getValue();
									Pattern keyPattern = Pattern.compile("[A-Z]+[0-9]+", Pattern.DOTALL);
									Matcher keyMatcher = keyPattern.matcher(equation.getEquation());
									List<String> allMatches = new ArrayList<String>();

									// System.out.println("Removing numbers from equation " + equation.getEquation());
									while (keyMatcher.find()) {
										allMatches.add(keyMatcher.group(0));
										// System.out.println("MATCHES " + allMatches.toString());
										for (String match : allMatches) {
											// System.out.println("Matched: " + match + " in [" + equation.getEquation() + "]");
											// for each match, swap with the variable value
											String key = match.replaceAll("[^A-Z]", "");
											// System.out.println("Replacing variable key " + key);
											String replacementString = equation.getEquation().replace(match, "#" + key);
											equation.setEquation(replacementString);
										}
									}
								}
								// print the equation map
								/*System.out.println("Row Variables");
								System.out.println("-------------");
								for (Map.Entry<String, VariableDTO> set : rowVariables.entrySet()) {
									System.out.println(set.getKey() + " : " + set.getValue().getKey() + " = " + set.getValue().getValue());
								}
								System.out.println("-------------");
								// print the equation map
								System.out.println("Row Equations");
								System.out.println("-------------");
								for (Map.Entry<String, EquationDTO> set : rowEquations.entrySet()) {
									System.out.println(set.getKey() + " : " + set.getValue().getKey() + " = " + set.getValue().getEquation());
								}
								System.out.println("-------------");*/

								// Replace all keys (e.g. B, G, DD) with
								for (Map.Entry<String, EquationData> set : rowEquations.entrySet()) {
									EquationData equation = set.getValue();
									Pattern keyPattern = Pattern.compile("#[A-Z]+", Pattern.DOTALL);
									Matcher keyMatcher = keyPattern.matcher(equation.getEquation());
									List<String> allMatches = new ArrayList<String>();

									// System.out.println("Swapping letters for variables in equation " + equation.getEquation());
									while (keyMatcher.find()) {
										allMatches.add(keyMatcher.group(0));
										// System.out.println("MATCHES " + allMatches.toString());
										for (String match : allMatches) {
											// System.out.println(match);
											VariableData variable = rowVariables.get(match.replace("#", ""));
											if (variable != null) {
												// System.out.println("Swapping " + match + " with " + variable.getKey());
												String replacementString = equation.getEquation().replace(match, variable.getKey());
												equation.setEquation(replacementString);
												// System.out.println("Updated Equation: " + equation.getEquation());
											}

											EquationData equationToSwap = rowEquations.get(match.replace("#", ""));
											if (equationToSwap != null) {
												// System.out.println("Swapping " + match + " with " + equationToSwap.getKey());
												String replacementString = equation.getEquation().replace(match, equationToSwap.getKey());
												equation.setEquation(replacementString);
												// System.out.println("Updated Equation: " + equation.getEquation());
											}

											// is this a core data value
											int columnIndex = convertIndexStringToNumber(match.replaceAll("[^A-Z]", ""));

											String variableKey = "";
											if (columnIndex == series.getSqftIndex()) {
												String replacementString = equation.getEquation().replace(match, KeyConstants.SQUARE_FEET_KEY);
												equation.setEquation(replacementString);
											} else if (columnIndex == series.getBasePriceIndex()) {
												String replacementString = equation.getEquation().replace(match, KeyConstants.BASE_PRICE_KEY);
												equation.setEquation(replacementString);
											}
										}
									}
								}

								newPriceData.setVariables(new ArrayList<VariableData>(rowVariables.values()));
								newPriceData.setEquations(new ArrayList<EquationData>(rowEquations.values()));


								newPriceData.updateValues();

								// set these sheets to active
								newPriceData.setToDraft();
								newPriceData.setActiveDate(today);

								ErrorCheck(cells, series, newPriceData);

								// Save new Data
								priceDataRepository.save(newPriceData);
							} catch (Exception e) {
								logger.error(e.getMessage());
								e.printStackTrace();
								PrintData(currentPriceSheetId, series, newPriceData, salesOffice, model);

								return new ResponseEntity<>("Import Error", HttpStatus.CONFLICT);

							}
						}
					}
				}
			}
		} catch (Exception e) {
			logger.error(e.getLocalizedMessage());
			logger.error(e.getMessage());
			e.printStackTrace();
			logger.error(currentOffice);
			logger.error(currentSeries);
			logger.error(currentModel);
			return new ResponseEntity<>("Import Error", HttpStatus.CONFLICT);

		}

		return new ResponseEntity<>("Import Success", HttpStatus.ACCEPTED);
	}


	@GetMapping("/importdata")
	@ResponseBody
	public ResponseEntity<String> importNewData(@RequestParam int month, @RequestParam int year) {
		// return these in the catch block
		String currentOffice = "";
		String currentSeries = "";
		String currentModel = "";
		String currentPriceSheetId = "";
		Calendar calendar = Calendar.getInstance();
		// String today = calendar.get(Calendar.MONTH) + "/" + calendar.get(Calendar.DAY_OF_MONTH) + "/" + calendar.get(Calendar.YEAR);

		try {
			List<PriceSheet> priceSheetList = priceSheetRepository.findByMonthAndYear(month, year);

			for(PriceSheet sheet : priceSheetList) {
				currentPriceSheetId = sheet.getId();
				String locationId = sheet.getLocationId();
				// get the sales office
				SalesOffice salesOffice = salesOfficeRepository.findById(locationId).orElseThrow(() -> new HttpStatusCodeException(HttpStatus.NOT_FOUND) {});
				// error handling
				currentOffice = salesOffice.getOfficeName() + ", " + salesOffice.getOfficeCity();
				// get the series list for the price sheet
				List<Series> seriesList = sheet.getSeriesList();
				for(Series series : seriesList) {
					// error handling
					currentSeries = series.getSeriesName();
					// get the table data for the series
					List<SheetRow> rows = series.getTableData().getRows();

					// first row is the headers, so start with the second row
					for(int i = 1; i < rows.size(); i++) {
						List<SheetCell> cells = rows.get(i).getCells();
						String modelName = cells.get(series.getModelNameIndex()).getValue().trim();
						String modelNumber = cells.get(series.getModelNoIndex()).getValue().trim();
						String basePrice = cells.get(series.getBasePriceIndex()).getValue();
						// logger.info("BASE PRICE: " );
						// logger.info(basePrice);
						logger.info(salesOffice.getOfficeName() + ", " + " : " + series.getSeriesName() + ", row " + i + ", " + modelName + " " + modelNumber);
						currentModel = modelName + " " + modelNumber;

						// get model
						// model number must not be blank
						if(!modelNumber.trim().equals("")) {
							Model model = new Model();
							String manufacturer = series.getManufacturer().trim();
							BigDecimal sqFt = new BigDecimal(cells.get(series.getSqftIndex()).getValue());
							String type = series.getType();
							String info = cells.get(series.getBedAndBathIndex()).getValue() + " , " + cells.get(series.getSizeIndex()).getValue();

							// see if the model number exists
							List<Model> modelQueryByNumberAndManufacturer = modelRepository.findByModelNumberAndFactoryId(modelNumber, manufacturer);
							int queryResultSize = modelQueryByNumberAndManufacturer.size();

							if(queryResultSize > 0) {
								// if the sqFt match, select model.  otherwise create new model
								boolean matchFound = false;
								int k = 0;

								while(k < queryResultSize) {
									Model existingModel = modelQueryByNumberAndManufacturer.get(k);
									if(existingModel.getType().equals(type) && existingModel.getEstimatedSquareFeet().equals(sqFt)) {
										model = existingModel;
										k = queryResultSize;
										matchFound = true;
									}
									k++;
								}

								if(queryResultSize == 1 && !matchFound) {
									Model existingModel = modelQueryByNumberAndManufacturer.get(0);

									logger.warn("Updating model SQFT");
									logger.info("Existing model " + existingModel.getModelNumber() + ", " + existingModel.getFactoryId() + ", " + existingModel.getType() + ", " + existingModel.getEstimatedSquareFeet() + ", " +
											existingModel.getNumberOfBedrooms() + "/" + existingModel.getNumberOfBathrooms() + ", " + existingModel.getWidth() + " x " + existingModel.getLength1());
									logger.info("New model " + modelNumber + ", " + manufacturer + ", " + type + ", " + sqFt + ", " + info);
									// update the square feet of the model found and use that model
									existingModel.setEstimatedSquareFeet(sqFt);
									modelRepository.save(existingModel);
									model = existingModel;
									matchFound = true;
								}

								if(!matchFound) {
									model = createNewModel(sqFt, info, manufacturer, modelNumber, type);
								}
							} else {
								model = createNewModel(sqFt, info, manufacturer, modelNumber, type);
							}



							// create new Price Data
							PriceData newPriceData = new PriceData();
							// populate the price data
							newPriceData.setLocationId(locationId);
							newPriceData.setModel(model);
							newPriceData.setSeriesName(series.getSeriesName().trim());
							newPriceData.setName(modelName);
							try {
								newPriceData.setBasePrice(new BigDecimal(basePrice));
							} catch (Exception e) {
								logger.error("Could not parse base price value from old sheets");
								newPriceData.setBasePrice(new BigDecimal(0));
								newPriceData.setError("Could not parse base price value from old sheets");
							}

							Map<String, VariableData> rowVariables = new HashMap<>();
							Map<String, EquationData> rowEquations = new HashMap<>();

							try {
								// process each cell in the row
								for (SheetCell cell : cells) {
									analyzeCell(cell, series, rows.get(0), rowVariables, rowEquations);
								}
								// replace SUMS() with the list of equation keys in the sum
								for (Map.Entry<String, EquationData> set : rowEquations.entrySet()) {
									EquationData equation = set.getValue();
									// System.out.println("Replace sums in equations with variable keys");
									Pattern pattern = Pattern.compile("SUM\\((.*?)\\)", Pattern.DOTALL);
									Matcher matcher = pattern.matcher(equation.getEquation());
									while (matcher.find()) {
										// System.out.println("Replacing SUMS in equation " + equation.getEquation());
										String[] keys = matcher.group(1).split(":");
										String startIndex = keys[0].replaceAll("[^A-Z]", "");
										String endIndex = keys[1].replaceAll("[^A-Z]", "");
										// System.out.println("Start Index: " + startIndex);
										// System.out.println("End Index: " + endIndex);
										// array of keys
										List<String> keyArray = new ArrayList<>();

										for (int k = convertIndexStringToNumber(startIndex); k <= convertIndexStringToNumber(endIndex); k++) {
											String headerColumnKey = rows.get(0).getCells().get(k).getValue().replaceAll("[^a-zA-Z0-9]", "").trim();
											if (!headerColumnKey.equals("")) {    // if the key row is not blank, move forward
												keyArray.add(convertIndexToString((k)));
											}
										}


										// new equation string
										String newEquationString = "";
										for (int m = 0; m < keyArray.size(); m++) {
											if (m == keyArray.size() - 1) {
												newEquationString = newEquationString + "#" + keyArray.get(m);
											} else {
												newEquationString = newEquationString + "#" + keyArray.get(m) + " + ";
											}
										}

										// System.out.println("SUM Equation: " + newEquationString);
										// System.out.println("OLD Equation: " + equation.getEquation());
										// System.out.println("Match to replace: " + matcher.group(1));
										String replacementString = equation.getEquation().replace(matcher.group(1), newEquationString);
										// System.out.println("Replacement String: " + replacementString);
										// replacementString = replacementString.replace("SUM(", "");
										replacementString = replacementString.replace("SUM", "");
										// replacementString = replacementString.replace(")", " ) ");
										equation.setEquation(replacementString);
										// System.out.println("New Equation: " + equation.getEquation());
									}
								}

								// Strip the numbers off of all letter combinations
								for (Map.Entry<String, EquationData> set : rowEquations.entrySet()) {
									EquationData equation = set.getValue();
									Pattern keyPattern = Pattern.compile("[A-Z]+[0-9]+", Pattern.DOTALL);
									Matcher keyMatcher = keyPattern.matcher(equation.getEquation());
									List<String> allMatches = new ArrayList<String>();

									// System.out.println("Removing numbers from equation " + equation.getEquation());
									while (keyMatcher.find()) {
										allMatches.add(keyMatcher.group(0));
										// System.out.println("MATCHES " + allMatches.toString());
										for (String match : allMatches) {
											// System.out.println("Matched: " + match + " in [" + equation.getEquation() + "]");
											// for each match, swap with the variable value
											String key = match.replaceAll("[^A-Z]", "");
											// System.out.println("Replacing variable key " + key);
											String replacementString = equation.getEquation().replace(match, "#" + key);
											equation.setEquation(replacementString);
										}
									}
								}
								// print the equation map
								/*System.out.println("Row Variables");
								System.out.println("-------------");
								for (Map.Entry<String, VariableDTO> set : rowVariables.entrySet()) {
									System.out.println(set.getKey() + " : " + set.getValue().getKey() + " = " + set.getValue().getValue());
								}
								System.out.println("-------------");
								// print the equation map
								System.out.println("Row Equations");
								System.out.println("-------------");
								for (Map.Entry<String, EquationDTO> set : rowEquations.entrySet()) {
									System.out.println(set.getKey() + " : " + set.getValue().getKey() + " = " + set.getValue().getEquation());
								}
								System.out.println("-------------");*/

								// Replace all keys (e.g. B, G, DD) with
								for (Map.Entry<String, EquationData> set : rowEquations.entrySet()) {
									EquationData equation = set.getValue();
									Pattern keyPattern = Pattern.compile("#[A-Z]+", Pattern.DOTALL);
									Matcher keyMatcher = keyPattern.matcher(equation.getEquation());
									List<String> allMatches = new ArrayList<String>();

									// System.out.println("Swapping letters for variables in equation " + equation.getEquation());
									while (keyMatcher.find()) {
										allMatches.add(keyMatcher.group(0));
										// System.out.println("MATCHES " + allMatches.toString());
										for (String match : allMatches) {
											// System.out.println(match);
											VariableData variable = rowVariables.get(match.replace("#", ""));
											if (variable != null) {
												// System.out.println("Swapping " + match + " with " + variable.getKey());
												String replacementString = equation.getEquation().replace(match, variable.getKey());
												equation.setEquation(replacementString);
												// System.out.println("Updated Equation: " + equation.getEquation());
											}

											EquationData equationToSwap = rowEquations.get(match.replace("#", ""));
											if (equationToSwap != null) {
												// System.out.println("Swapping " + match + " with " + equationToSwap.getKey());
												String replacementString = equation.getEquation().replace(match, equationToSwap.getKey());
												equation.setEquation(replacementString);
												// System.out.println("Updated Equation: " + equation.getEquation());
											}

											// is this a core data value
											int columnIndex = convertIndexStringToNumber(match.replaceAll("[^A-Z]", ""));

											String variableKey = "";
											if (columnIndex == series.getSqftIndex()) {
												String replacementString = equation.getEquation().replace(match, KeyConstants.SQUARE_FEET_KEY);
												equation.setEquation(replacementString);
											} else if (columnIndex == series.getBasePriceIndex()) {
												String replacementString = equation.getEquation().replace(match, KeyConstants.BASE_PRICE_KEY);
												equation.setEquation(replacementString);
											}
										}
									}
								}

								newPriceData.setVariables(new ArrayList<VariableData>(rowVariables.values()));
								newPriceData.setEquations(new ArrayList<EquationData>(rowEquations.values()));


								newPriceData.updateValues();

								// set these sheets to draft
								newPriceData.setToDraft();
								newPriceData.setActiveDate("01/01/2021");

								ErrorCheck(cells, series, newPriceData);

								// Save new Data
								priceDataRepository.save(newPriceData);
							} catch (Exception e) {
								logger.error(e.getMessage());
								e.printStackTrace();
								PrintData(currentPriceSheetId, series, newPriceData, salesOffice, model);

								return new ResponseEntity<>("Import Error", HttpStatus.CONFLICT);

							}
						}
					}
				}
			}
		} catch (Exception e) {
			logger.error(e.getLocalizedMessage());
			logger.error(e.getMessage());
			e.printStackTrace();
			logger.error(currentOffice);
			logger.error(currentSeries);
			logger.error(currentModel);
			return new ResponseEntity<>("Import Error", HttpStatus.CONFLICT);

		}

		return new ResponseEntity<>("Import Success", HttpStatus.ACCEPTED);
	}

	private Model createNewModel(BigDecimal sqFt, String notes, String series, String modelNumber, String type) {

		Model newModel = new Model();
		// create new model
		newModel.setEstimatedSquareFeet(sqFt);
		newModel.setNotes(notes);
		newModel.setFactoryId(series.trim());
		newModel.setModelNumber(modelNumber.trim());
		newModel.setType(type);
		newModel = modelRepository.save(newModel);
		logger.warn("New Model Created: " + newModel.getId());
		// System.out.println("SqFt: " + sqFt);
		// System.out.println("Notes: " + notes);
		return newModel;
	}

	private void analyzeCell(SheetCell cell, Series series, SheetRow headerRow, Map<String, VariableData> variables, Map<String, EquationData> equations) {
		// get the column index
		int columnIndex = cell.getColumnIndex();
		// get the header string
		String headerColumnString = headerRow.getCells().get(columnIndex).getValue();


		if(!headerColumnString.isEmpty()) {	// if the key row is not blank, move forward

			String name = headerRow.getCells().get(columnIndex).getValue();
			String indexString = convertIndexToString(columnIndex);

			// format the notes
			String headerNotes = headerRow.getCells().get(columnIndex).getToolTip().trim();
			String cellNotes = cell.getToolTip().trim();

			String notes = "";
			if(!headerNotes.isEmpty()) {
				notes += headerNotes;
			}
			if(!cellNotes.isEmpty()) {
				notes += cellNotes;
			}

			String keyString = "";
			if(columnIndex == series.getFactoryCostIndex()) {
				keyString = KeyConstants.FACTORY_COST_KEY;
				name = "Factory Total Cost";
			} else if (columnIndex == series.getFactoryDirectSaleIndex()) {
				keyString = KeyConstants.FACTORY_DIRECT_PRICE_KEY;
				name = "Factory Direct Price";
			} else if (columnIndex == series.getFirstHalfDiscountIndex()) {
				keyString = KeyConstants.FIRST_HALF_DISCOUNT_KEY;
				name = "First Half Discount";
			} else if (columnIndex == series.getSecondHalfDiscountIndex()) {
				keyString = KeyConstants.SECOND_HALF_DISCOUNT_KEY;
				name = "Second Half Discount";
			} else if (columnIndex == series.getMsrpIndex()) {
				keyString = KeyConstants.MSRP_KEY;
				name = "MSRP";
			} else if (columnIndex == series.getBasePriceIndex()) {
				keyString = KeyConstants.BASE_PRICE_KEY;
				name = "Base Price";
			} else {
				keyString = headerColumnString;
			}


			// is cell an equation?
			if(isValueAnEquation(cell.getValue())) {
				// cell is an equation.  create new equation and at to list
				Equation equation = makeEquation(keyString, name, notes);
				// make equation DTO
				EquationData equationDTO = makeEquationDTO(equation, cell);
				// add to equation list
				equations.put(indexString, equationDTO);
			} else {
				// check that cell is not core data
				if(columnIndex != series.getBedAndBathIndex() &&
					columnIndex !=series.getModelNameIndex() &&
					columnIndex !=series.getModelNoIndex() &&
					columnIndex !=series.getSizeIndex() &&
					columnIndex !=series.getSqftIndex() &&
						columnIndex != series.getBasePriceIndex()
				) {
					// cell is a variable.  make new variable
					Variable variable = makeVariable(keyString, name, notes);
					// make variable DTO
					VariableData variableDTO = makeVariableDTO(variable, cell);
					// add to variable list
					variables.put(indexString, variableDTO);
				}

			}
		}
	}

	private Variable makeVariable(String key, String name, String notes) {
		//create new variable
		Variable variable = new Variable();
		variable.setName(name);
		variable.setNotes(notes);
		// make new variable key
		String formattedKey = generateKey(key);
		variable.setKey(formattedKey);

		// check to see if the variable exists already
		Variable existingVariable = variableRepository.findByKey(formattedKey);
		if(existingVariable != null) {
			return existingVariable;
		}
		System.out.println("Creating new variable: " + variable.getName());
		variableRepository.save(variable);
		return variable;
	}

	private Equation makeEquation(String key, String name, String notes) {
		// Make new equation first
		Equation equation = new Equation();
		equation.setName(name);
		equation.setNotes(notes);
		// Make new equation key
		String formattedKey = generateKey(key);
		equation.setKey(formattedKey);

		// check to see if equation exists already
		Equation existingEquation = equationRepository.findByKey(formattedKey);
		if(existingEquation != null) {
			return existingEquation;
		}
		System.out.println("Creating new equation: " + equation.getName());
		equationRepository.save(equation);
		return equation;
	}

	private VariableData makeVariableDTO(Variable variable, SheetCell cell) {
		BigDecimal variableValue;
		try {
			variableValue = new BigDecimal(Float.parseFloat(cell.getValue()));
		} catch (Exception e) {
			logger.error("Could not parse BigDecimal from " + cell.getValue());
			variableValue = new BigDecimal(0);
		}
		VariableData variableDTO = new VariableData();
		variableDTO.initializeFromVariable(variable, variableValue);
		return variableDTO;
	}

	private EquationData makeEquationDTO(Equation equation, SheetCell cell) {
		String equationString = cell.getValue().replace("=", "");
		EquationData equationDTO = new EquationData();
		equationDTO.initializeEquationDTO(equation, equationString);
		return equationDTO;
	}



	private String generateKey(String keyString) {
		return "[" + keyString.replaceAll("[^a-zA-Z0-9]","").trim() + "]";
	}



	/*private List<Integer> buildCoreIndexes(Series series) {
		List<Integer> coreIndexes = new ArrayList<>();
		coreIndexes.add(series.getBasePriceIndex());
		coreIndexes.add(series.getFactoryCostIndex());
		// coreIndexes.add(series.getFactoryDirectDiscountIndex());
		coreIndexes.add(series.getFactoryDirectSaleIndex());
		coreIndexes.add(series.getFirstHalfDiscountIndex());
		coreIndexes.add(series.getSecondHalfDiscountIndex());
		coreIndexes.add(series.getMsrpIndex());

		coreIndexes.add(series.getBedAndBathIndex());
		coreIndexes.add(series.getModelNameIndex());
		coreIndexes.add(series.getModelNoIndex());
		coreIndexes.add(series.getSizeIndex());
		coreIndexes.add(series.getSqftIndex());
		
		// System.out.println("Core Data Indexes built");
		for(Integer index : coreIndexes) {
			// System.out.println("Core Data Index: " + index);
		}
		return coreIndexes;
	}*/
	
	/*private void isModelInInventory(Model model, Inventory inventory) {
		// check if model exists in inventory for location
		if(!inventory.getModelIds().contains(model.getId())) {
			// System.out.println("Adding model " + model.getModelNumber() + " to inventory");
			inventory.addModelToInventory(model.getId());
			inventoryRepository.save(inventory);
		}
	}*/

	private boolean isValueAnEquation(String value) {
		return value.startsWith("=");
	}

	private String convertIndexToString(int num) {
		String[] ALPHABET = "ABCDEFGHIJKLMNOPQRSTUVWXYZ".split("(?!^)");
		String letterCode = "";
		if(num / 25.0 <= 1) {
			letterCode = ALPHABET[num];
		} else {
			int alphabetIndex = (int) (num - 26 * (Math.floor(num / 26)));
			for(int i = 0; i <= (Math.floor(num / 26)); i++) {
				letterCode = letterCode + ALPHABET[alphabetIndex];
			}
		}
		return letterCode;
	}

	private int convertIndexStringToNumber(String index) {
		int stringLength = index.length();
		int alphabetIndex = Character.codePointAt(index,0) - 65;
		return (stringLength - 1) * 26 + alphabetIndex;
	}

	/*private Variable findVariableBySheetIndex(List<Variable> variables, String sheetIndex) {
		String index = sheetIndex.replaceAll("[^A-Z]","");
		for(Variable variable : variables) {
			if(variable.getSheetStringIndex().equals(index)) {
				return variable;
			}
		}
		return null;
	}*/

	/*private Equation findEquationBySheetIndex(List<Equation> equations, String sheetIndex) {
		String index = sheetIndex.replaceAll("[^A-Z]","");
		for(Equation equation: equations) {
			if(equation.getSheetStringIndex().equals(index)) {
				return equation;
			}
		}
		return null;
	}*/

	private void ErrorCheck(List<SheetCell> cells, Series series, PriceData newPriceData ) throws Exception {
		/**************** ERROR CHECKS *******************/
		BigDecimal originalBasePrice = new BigDecimal(cells.get(series.getBasePriceIndex()).getDisplayValue().replaceAll("[$,]", "")).setScale(2, RoundingMode.HALF_UP);
		BigDecimal diff = newPriceData.getBasePrice().setScale(2, RoundingMode.HALF_UP).subtract(originalBasePrice).abs();

		if(diff.doubleValue() == 0.01) {
			// logger.warn("Base Price -> " + newPriceData.getBasePrice().setScale(2) + " != " + originalBasePrice);
		} else if(diff.doubleValue() > 0.01) {
			logger.error("Base Price -> " + newPriceData.getBasePrice().setScale(2) + " != " + originalBasePrice);
			throw new Exception();
		}

		BigDecimal originalMSRP = new BigDecimal(cells.get(series.getMsrpIndex()).getDisplayValue().replaceAll("[$,]", "")).setScale(2, RoundingMode.HALF_UP);
		diff = newPriceData.getMsrp().setScale(2, RoundingMode.HALF_UP).subtract(originalMSRP).abs();
		if(diff.doubleValue() == 0.01) {
			// logger.warn("MSRP -> " + newPriceData.getMsrp().setScale(2, RoundingMode.HALF_UP) + " != " + originalMSRP);
		} else if(diff.doubleValue() > 0.01) {
			logger.error("MSRP -> " + newPriceData.getMsrp().setScale(2, RoundingMode.HALF_UP) + " != " + originalMSRP);
			throw new Exception();
		}

		BigDecimal originalFactoryDirectPrice = new BigDecimal(cells.get(series.getFactoryDirectSaleIndex()).getDisplayValue().replaceAll("[$,]", "")).setScale(2, RoundingMode.HALF_UP);
		diff = newPriceData.getFactoryDirectPrice().setScale(2, RoundingMode.HALF_UP).subtract(originalFactoryDirectPrice).abs();
		if(diff.doubleValue() == 0.01) {
			// logger.warn("Direct Price -> " + newPriceData.getFactoryDirectPrice().setScale(2, RoundingMode.HALF_UP) + " != " + originalFactoryDirectPrice);
		} else if(diff.doubleValue() > 0.01) {
			logger.error("Direct Price -> " + newPriceData.getFactoryDirectPrice().setScale(2, RoundingMode.HALF_UP) + " != " + originalFactoryDirectPrice);
			throw new Exception();

		}


		BigDecimal firstHalfAdvertPrice = new BigDecimal(cells.get(series.getFirstHalfDiscountIndex()).getDisplayValue().replaceAll("[$,]", "")).setScale(2, RoundingMode.HALF_UP);
		diff = newPriceData.getFirstHalfAdvertisingPrice().setScale(2, RoundingMode.HALF_UP).subtract(firstHalfAdvertPrice).abs();
		if(diff.doubleValue() == 0.01) {
			// logger.warn("First Half Advert -> " + newPriceData.getFirstHalfAdvertisingPrice().setScale(2, RoundingMode.HALF_UP) + " != " + firstHalfAdvertPrice);
		} else if(diff.doubleValue() > 0.01) {
			logger.error("First Half Advert -> " + newPriceData.getFirstHalfAdvertisingPrice().setScale(2, RoundingMode.HALF_UP) + " != " + firstHalfAdvertPrice);
			throw new Exception();
		}

		BigDecimal secondHalfAdvertPrice = new BigDecimal(cells.get(series.getSecondHalfDiscountIndex()).getDisplayValue().replaceAll("[$,]", "")).setScale(2, RoundingMode.HALF_UP);
		diff = newPriceData.getSecondHalfAdvertisingPrice().setScale(2, RoundingMode.HALF_UP).subtract(secondHalfAdvertPrice).abs();
		if(diff.doubleValue() == 0.01) {
			// logger.warn("Second Half Advert -> " +newPriceData.getSecondHalfAdvertisingPrice().setScale(2, RoundingMode.HALF_UP) + " != " + secondHalfAdvertPrice);
		} else if(diff.doubleValue() > 0.01) {
			logger.error("Second Half Advert -> " + newPriceData.getSecondHalfAdvertisingPrice().setScale(2, RoundingMode.HALF_UP) + " != " + secondHalfAdvertPrice);
			throw new Exception();
		}

	}

	private void PrintData(String priceSheetId, Series series, PriceData newPriceData, SalesOffice office, Model model) {
		System.out.println("-----------------");
		System.out.println();
		System.out.println("Price Sheet Id: " + priceSheetId);
		System.out.println("Series " + series.getSeriesName());
		System.out.println("Model " + newPriceData.getName() + " " + model.getModelNumber() + " (ID " + model.getId() + ")");
		System.out.println("Manufacturer: " + series.getManufacturer());
		System.out.println("Sales Office: " + office.getOfficeName() + " (ID: " + office.getId() + ")");
		System.out.println("BASE PRICE: " + newPriceData.getBasePrice());
		System.out.println("SQFT: " + newPriceData.getModel().getEstimatedSquareFeet() + " ( " + model.getEstimatedSquareFeet() + " ) ");
		System.out.println("Notes: " + model.getNotes());
		System.out.println("Variables");
		System.out.println("-----------------");
		for(VariableData var : newPriceData.getVariables()) {
			System.out.println(var.getKey() + " = " + var.getValue());
		}
		System.out.println("Equations");
		System.out.println("-----------------");
		for(EquationData eqn: newPriceData.getEquations()) {
			System.out.println(eqn.getKey() + " = " + eqn.getEquation());
		}
		System.out.println("-----------------");
		newPriceData.EquationDebug();
	}
}
