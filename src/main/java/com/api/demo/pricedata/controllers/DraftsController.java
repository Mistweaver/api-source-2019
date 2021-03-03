package com.api.demo.pricedata.controllers;

import com.api.demo.mongorepositories.applicationpackage.promotions.PromotionRepository;
import com.api.demo.pricedata.functions.ArizonaDateTimeComponent;
import com.api.demo.pricedata.repositories.equations.*;
import com.api.demo.pricedata.repositories.pricedata.PriceData;
import com.api.demo.mongorepositories.applicationpackage.promotionmodellist.PromotionModelListRepository;
import com.api.demo.mongorepositories.applicationpackage.salesoffices.SalesOffice;
import com.api.demo.mongorepositories.applicationpackage.salesoffices.SalesOfficeRepository;
import com.api.demo.pricedata.functions.PriceDataLogger;
import com.api.demo.pricedata.restpackages.*;
import com.api.demo.pricedata.repositories.models.ModelRepository;
import com.api.demo.pricedata.repositories.variables.VariableData;
import com.api.demo.pricedata.repositories.variables.VariableDataUpdatePackage;
import com.api.demo.pricedata.repositories.pricedata.PriceDataRepository;
import com.api.demo.pricedata.repositories.variables.VariableRepository;
import lombok.Getter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.HttpStatusCodeException;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/pricedata/drafts")
public class DraftsController {
	private static final Logger logger = LoggerFactory.getLogger(DraftsController.class);
	private final PriceDataLogger priceDataLogger = new PriceDataLogger();

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
	@Autowired
	private MarketingEquationRepository marketingEquationRepository;




	/**** Create New Price Data DRAFTS ****/
	@PostMapping("/create")
	@ResponseBody
	public ResponseEntity<Object> createDrafts(@RequestBody CreateDraftForPriceDataPackage createDraftForPriceDataPackage) {
		try {
			List<String> locationIds = createDraftForPriceDataPackage.getLocationIds();
			String draftDate = createDraftForPriceDataPackage.getDraftDate();
			List<String> priceDataIds = createDraftForPriceDataPackage.getPriceDataIds();

			// get each price data by id
			for(String id: priceDataIds) {
				try {
					PriceData data = priceDataRepository.findById(id).orElseThrow(() -> new HttpStatusCodeException(HttpStatus.NOT_FOUND) {});
					// If the location Ids is empty, just create the draft in its same location
					if(locationIds.size() == 0) {
						// Check if there is an existing draft of the same model Id, location Id, and date
						PriceData existingDataWithDate = priceDataRepository.findByModelIdAndLocationIdAndActiveDate(data.getModelId(), data.getLocationId(), draftDate);
						if(existingDataWithDate == null || !existingDataWithDate.isDraft()) {
							System.out.println("Creating new data");
							PriceData copy = data.createDraftFromSelf(draftDate);
							priceDataRepository.save(copy);
						} else {
							System.out.println("Existing data already there");
						}
					} else {
						for(String locationId: locationIds) {
							// Check if there is an existing draft of the same model Id, location Id, and date
							SalesOffice office = salesOfficeRepository.findById(locationId).orElseThrow(() -> new HttpStatusCodeException(HttpStatus.NOT_FOUND) {});
							System.out.println("Adding to office: " + office.getOfficeName());
							PriceData existingDataWithDate = priceDataRepository.findByModelIdAndLocationIdAndActiveDate(data.getModelId(), locationId, draftDate);
							if(existingDataWithDate == null || !existingDataWithDate.isDraft()) {
								System.out.println("Creating new data");
								PriceData copy = data.createDraftFromSelfForLocation(locationId, draftDate);
								priceDataRepository.save(copy);
							} else {
								System.out.println("Trying to add " + data.getModel().getModelNumber() + " " + data.getName() + " : " + data.getSeriesName());
								System.out.println("Existing data already there");
								System.out.println(existingDataWithDate.getModel().getModelNumber() + " " + existingDataWithDate.getName() + " : " + existingDataWithDate.getSeriesName());
							}
						}
					}
				} catch (Exception e) {
					logger.error(e.getMessage());
				}
			}
			return new ResponseEntity<>(createDraftForPriceDataPackage, HttpStatus.ACCEPTED);
		} catch (Exception e) {
			logger.error(e.getMessage());
			e.printStackTrace();
			return new ResponseEntity<>(e.getMessage(), HttpStatus.CONFLICT);
		}
	}

	/**** Delete Drafts ****/
	@PostMapping("/delete")
	@ResponseBody
	public ResponseEntity<Object> deleteDrafts(@RequestBody CreateDraftForPriceDataPackage createDraftForPriceDataPackage) {
		List<PriceData> failedDeletions = new ArrayList<>();
		try {
			List<String> priceDataIds = createDraftForPriceDataPackage.getPriceDataIds();
			// get each price data by id
			for(String id: priceDataIds) {
				try {
					PriceData data = priceDataRepository.findById(id).orElseThrow(() -> new HttpStatusCodeException(HttpStatus.NOT_FOUND) {});
					// only delete drafts
					if(data.isDraft()) {
						priceDataRepository.deleteById(id);
					} else {
						logger.error("Cannot delete " + id + " : is not a draft");
						failedDeletions.add(data);
					}
				} catch (Exception e) {
					logger.error("Cannot find draft " + id);
					logger.error(e.getMessage());
				}
			}
			return new ResponseEntity<>(failedDeletions, HttpStatus.ACCEPTED);
		} catch (Exception e) {
			logger.error(e.getMessage());
			e.printStackTrace();
			return new ResponseEntity<>(e.getMessage(), HttpStatus.CONFLICT);
		}
	}

	/**** Validate drafts and push them to the pending queue ****/
	@PostMapping("/push")
	@ResponseBody
	public ResponseEntity<Object> pushToPending(@RequestBody DataIdListPackage dataIdListPackage) {
		ArizonaDateTimeComponent arizonaDateTimeComponent = new ArizonaDateTimeComponent();
		List<UpdateResponse> responses = new ArrayList<>();
		try {
			List<String> priceDataIds = dataIdListPackage.getPriceDataIds();
			// get each price data by id
			for(String id: priceDataIds) {
				try {
					PriceData data = priceDataRepository.findById(id).orElseThrow(() -> new HttpStatusCodeException(HttpStatus.NOT_FOUND) {});
					if(data.isDraft()) {
						// check that date is valid
						if(!arizonaDateTimeComponent.isDateStringValid(data.getActiveDate())) {
							responses.add(new UpdateResponse(false, "Invalid date format.  Must be MM/dd/YYYY", data));
						}
						// check that date is not in the past
						if(arizonaDateTimeComponent.isDateBeforeToday(data.getActiveDate())) {
							responses.add(new UpdateResponse(false, "Invalid date.  Draft date must not be before today", data));
						}
						// check that there are no errors
						if(data.isError()) {
							responses.add(new UpdateResponse(false, "Price data contains error(s)", data));
						}
						// check that the data has not been updated
						if(data.isDataUpdated()) {
							responses.add(new UpdateResponse(false, "Price data contains changes and needs recalculation", data));
						}

						data.setToPending();
						logger.info("Set to pending");
						priceDataRepository.save(data);
					} else {
						responses.add(new UpdateResponse(false, "Cannot set to pending.  Data is not a draft.", data));
					}
				} catch (HttpStatusCodeException e) {
					responses.add(new UpdateResponse(false, "Could not find price data with ID " + id, new PriceData()));
				} catch (Exception e) {
					logger.error(e.getMessage());
					responses.add(new UpdateResponse(false, "Error: " + e.getMessage(), new PriceData()));
				}
			}
			return new ResponseEntity<>(responses, HttpStatus.ACCEPTED);
		} catch (Exception e) {
			logger.error(e.getMessage());
			e.printStackTrace();
			return new ResponseEntity<>(e.getMessage(), HttpStatus.CONFLICT);
		}
	}


	// update generic properties for price data
	// variables and/or equations are NOT updated here
	@PostMapping("/update")
	@ResponseBody
	public ResponseEntity<PriceData> updatePriceData(@RequestBody PriceDataBasicUpdatePackage priceDataBasicUpdatePackage) {
		PriceData data = priceDataRepository.findById(priceDataBasicUpdatePackage.getId()).orElseThrow(() -> new HttpStatusCodeException(HttpStatus.NOT_FOUND) {});
		try {
			data.setName(priceDataBasicUpdatePackage.getName());
			data.setSeriesName(priceDataBasicUpdatePackage.getSeriesName());
			data.setActiveDate(priceDataBasicUpdatePackage.getActiveDate());
			data.setExpirationDate(priceDataBasicUpdatePackage.getExpirationDate());
			data = priceDataRepository.save(data);
			return new ResponseEntity<>(data, HttpStatus.ACCEPTED);
		} catch (Exception e) {
			logger.error("Failed to update price data: " + e.getMessage());
			e.printStackTrace();
			return new ResponseEntity<>(data, HttpStatus.CONFLICT);
		}
	}


	/**
	 * Class for the HTTP request to edit the active date on price data
	 */
	@Getter
	public static class EditActiveDateRequest {
		private final List<String> priceDataIds;
		private final String newActiveDate;

		public EditActiveDateRequest() {
			this.priceDataIds = new ArrayList<>();
			this.newActiveDate = "";
		}
	}

	/**
	 * This function sets the active date for any price data with the status DRAFT.  You cannot set the active date for
	 * any other data, as that date needs to remain in an immutable once the data leaves a draft state.
	 *
	 * @param editActiveDateRequest	Object contains a list of ids to update and the new active date
	 * @return responseData
	 */
	@PostMapping("/setactivedate")
	@ResponseBody
	public ResponseEntity<Object> setActiveDate(@RequestBody EditActiveDateRequest editActiveDateRequest) {
		// this list returns if any of the updates failed
		List<UpdateResponse> responseData = new ArrayList<>();
		// the date/time component used for validating the dates
		ArizonaDateTimeComponent arizonaDateTimeComponent = new ArizonaDateTimeComponent();


		try {
			// get the list of price data IDs to update from the request package
			List<String> priceDataIds = editActiveDateRequest.getPriceDataIds();
			// get the new active date from the request package
			String newActiveDate = editActiveDateRequest.getNewActiveDate();
			// validate the active date
			if(!arizonaDateTimeComponent.isDateStringValid(newActiveDate)) {
				return new ResponseEntity<>("Invalid Date", HttpStatus.CONFLICT);
			}


			for (String id : priceDataIds) { // get each price data by id
				try {
					PriceData data = priceDataRepository.findById(id).orElseThrow(() -> new HttpStatusCodeException(HttpStatus.NOT_FOUND) {
					});
					if (data.isDraft()) {
						data.setActiveDate(newActiveDate);
						priceDataRepository.save(data);
						responseData.add(new UpdateResponse(true, "", data));
					} else {
						// data does not have the correct state
						responseData.add(new UpdateResponse(false, "Cannot edit active date for data - incorrect state", data));
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


	// update generic properties for price data
	// variables and/or equations are NOT updated here
	@PostMapping("/updatebaseprice")
	@ResponseBody
	public ResponseEntity<PriceData> updatePriceData(@RequestBody BasePriceUpdatePackage basePriceUpdatePackage) {
		PriceData data = priceDataRepository.findById(basePriceUpdatePackage.getId()).orElseThrow(() -> new HttpStatusCodeException(HttpStatus.NOT_FOUND) {});
		try {
			data.setBasePrice(new BigDecimal(basePriceUpdatePackage.getBasePrice()));
			data = priceDataRepository.save(data);
			return new ResponseEntity<>(data, HttpStatus.ACCEPTED);
		} catch (Exception e) {
			logger.error("Failed to update price data: " + e.getMessage());
			e.printStackTrace();
			return new ResponseEntity<>(data, HttpStatus.CONFLICT);
		}
	}

	// update generic properties for price data
	// variables and/or equations are NOT updated here
	@PostMapping("/template/update")
	@ResponseBody
	public ResponseEntity<Object> updatePriceDataFromTemplate(@RequestBody TemplateUpdatePackage templateUpdatePackage) {
		List<PriceData> failedUpdates = new ArrayList<>();
		try {
			// get the template data
			PriceData template = priceDataRepository.findById(templateUpdatePackage.getTemplateId()).orElseThrow(() -> new HttpStatusCodeException(HttpStatus.NOT_FOUND) {});
			List<String> dataToUpdateFromTemplate = templateUpdatePackage.getDataToUpdateFromTemplate();

			boolean copyVariables = templateUpdatePackage.isCopyVariables();
			boolean copyEquations = templateUpdatePackage.isCopyEquations();
			// get each price data by id
			for(String id: dataToUpdateFromTemplate) {
				try {
					PriceData data = priceDataRepository.findById(id).orElseThrow(() -> new HttpStatusCodeException(HttpStatus.NOT_FOUND) {});
					if(data.isDraft()) {
						// if update variables is true, swap variables
						if(copyVariables) {
							data.setVariables(template.getVariables());
						}
						// if update equations is true, swap equations
						if(copyEquations) {
							data.setEquations(template.getEquations());
						}

						priceDataRepository.save(data);
					} else {
						logger.error("Cannot update draft " + id + " from template");
						failedUpdates.add(data);
					}
				} catch (Exception e) {
					logger.error("Cannot find draft " + id);
					logger.error(e.getMessage());
				}
			}
			return new ResponseEntity<>(failedUpdates, HttpStatus.ACCEPTED);
		} catch (Exception e) {
			logger.error(e.getMessage());
			e.printStackTrace();
			return new ResponseEntity<>(e.getMessage(), HttpStatus.CONFLICT);
		}
	}


	// This function validates the variables/equations in the price data only
	// no data is saved to the database
	@PostMapping("/recalculate")
	@ResponseBody
	public ResponseEntity<Object> recalculatePriceData(@RequestBody DataIdListPackage dataIdListPackage) {
		List<UpdateResponse> responses = new ArrayList<>();

		try {
			List<String> dataList = dataIdListPackage.getPriceDataIds();
			for(String id : dataList) {
				try {
					PriceData data = priceDataRepository.findById(id).orElseThrow(() -> new HttpStatusCodeException(HttpStatus.NOT_FOUND) {});
					// only update if there are changes.  No sense in wasting
					if(data.isDataUpdated() || data.isError()) {
						data.updateValues();
					}
					if(data.isError()) {
						responses.add(new UpdateResponse(false, "Failure recalculating price data: " + data.getErrorDetails(), data));
					}

					priceDataRepository.save(data);

				} catch (HttpStatusCodeException e) {
					responses.add(new UpdateResponse(false, "Could not find price data with ID " + id, new PriceData()));

				} catch (Exception e) {
					responses.add(new UpdateResponse(false, "Error with id " + id + ": " + e.getMessage(), new PriceData()));
				}
			}
			return new ResponseEntity<>(responses, HttpStatus.ACCEPTED);
		} catch (Exception e) {
			logger.error("Failed to update base price in price data: " + e.getMessage());
			e.printStackTrace();
			return new ResponseEntity<>(new PriceData(), HttpStatus.CONFLICT);
		}
	}

	/**
	 *
	 * Add Variable to Price Data
	 *
	 * This function will update a variables notes nad value in a selected list of price data IDs.  This can only update the notes or the value,
	 * it cannot change the variable name or the variable key.  If you need to do either of those actions, use the previous function.
	 *
	 **/
	@PostMapping("/variable/add")
	@ResponseBody
	public ResponseEntity<Object> addVariableToData(@RequestBody VariableDataUpdatePackage variableDataUpdatePackage) {
		List<PriceData> updateList = new ArrayList<>();
		try {
			List<String> priceDataIds = variableDataUpdatePackage.getPriceDataIds();
			VariableData newData = variableDataUpdatePackage.getNewData();
			for(String dataId : priceDataIds) {
				try {
					// get price data
					PriceData data = priceDataRepository.findById(dataId).orElseThrow(() -> new HttpStatusCodeException(HttpStatus.NOT_FOUND) {});
					try {
						data.addVariable(newData);
					} catch (Exception e) {
						// variable failed to update dto for some reason
						data.setError("Failed to add variable data" + newData.getName());
					}
					priceDataRepository.save(data);
					updateList.add(data);
				} catch (HttpStatusCodeException e) {
					logger.error("Could not find price data ID: " + dataId);
				}
			}
			return new ResponseEntity<>(updateList, HttpStatus.ACCEPTED);
		} catch (Exception e) {
			logger.error("Variable add failure: " + e.getMessage());
			e.printStackTrace();
			return new ResponseEntity<>("Variable add failure: " + e.getMessage(), HttpStatus.CONFLICT);
		}
	}

	/**
	 *
	 * Update Variable DTO in Price Data
	 *
	 * This function will update a variables notes nad value in a selected list of price data IDs.  This can only update the notes or the value,
	 * it cannot change the variable name or the variable key.  If you need to do either of those actions, use the previous function.
	 *
	 **/
	@PostMapping("/variable/update")
	@ResponseBody
	public ResponseEntity<Object> updateVariableData(@RequestBody VariableDataUpdatePackage variableDataUpdatePackage) {
		List<PriceData> updateList = new ArrayList<>();

		try {
			System.out.println("Getting package data");
			List<String> priceDataIds = variableDataUpdatePackage.getPriceDataIds();
			System.out.println(priceDataIds.size());
			VariableData newData = variableDataUpdatePackage.getNewData();
			System.out.println(newData.getName() + " : " + newData.getValue());
			for(String dataId : priceDataIds) {
				System.out.println("Updating " + newData.getName() + " in " + dataId + " to " + newData.getValue());
				try {
					// get price data
					PriceData data = priceDataRepository.findById(dataId).orElseThrow(() -> new HttpStatusCodeException(HttpStatus.NOT_FOUND) {});
					System.out.println("Data " + data.getId() + " found");

					try {
						System.out.println("Attempting update");
						data.updateVariableData(newData);
					} catch (Exception e) {
						// variable failed to update dto for some reason
						System.out.println("Update failed");
						data.setError("Failed to update variable Data" + newData.getName());
					}
					System.out.println("Saving data");
					priceDataRepository.save(data);
					updateList.add(data);
				} catch (HttpStatusCodeException e) {
					logger.error("Could not find price data ID: " + dataId);
				}
			}
			return new ResponseEntity<>(updateList, HttpStatus.ACCEPTED);
		} catch (Exception e) {
			logger.error("Variable Data update failure: " + e.getMessage());
			e.printStackTrace();
			return new ResponseEntity<>("Variable Data update failure: " + e.getMessage(), HttpStatus.CONFLICT);
		}
	}

	/**
	 *
	 * Remove Variable
	 *
	 * This function removes a variable from a list of selected price data ids
	 *
	 **/
	@PostMapping("/variable/remove")
	@ResponseBody
	public ResponseEntity<String> removeVariable(@RequestBody VariableDataUpdatePackage updatePackage) {
		List<PriceData> updateList = new ArrayList<>();

		try {
			// get the list of price data ids
			List<String> priceDataIds = updatePackage.getPriceDataIds();
			// get the variable by Id
			VariableData newData = updatePackage.getNewData();
			for(String priceDataId : priceDataIds) {
				PriceData data = priceDataRepository.findById(priceDataId).orElseThrow(() -> new HttpStatusCodeException(HttpStatus.NOT_FOUND) {});
				try {
					data.removeVariable(newData.getId());
				} catch (Exception e) {
					data.setError("Failed to remove " + newData.getKey() + " : " + e.getMessage());
				}
				priceDataRepository.save(data);
				updateList.add(data);
			}
			return new ResponseEntity<>("Variables Removed", HttpStatus.ACCEPTED);
		} catch (Exception e) {
			logger.error("Failed to remove variable" + e.getMessage());
			e.printStackTrace();
			return new ResponseEntity<>("Failed to delete variable", HttpStatus.CONFLICT);
		}
	}

	/**
	 *
	 * Add Equation to Price Data
	 *
	 * This function will update a equations notes nad value in a selected list of price data IDs.  This can only update the notes or the value,
	 * it cannot change the equation name or the equation key.  If you need to do either of those actions, use the previous function.
	 *
	 **/
	@PostMapping("/equation/add")
	@ResponseBody
	public ResponseEntity<Object> addEquationToData(@RequestBody EquationDataUpdatePackage equationDataUpdatePackage) {
		List<PriceData> updateList = new ArrayList<>();
		try {
			List<String> priceDataIds = equationDataUpdatePackage.getPriceDataIds();
			EquationData newEquation = equationDataUpdatePackage.getNewData();
			for(String dataId : priceDataIds) {
				try {
					// get price data
					PriceData data = priceDataRepository.findById(dataId).orElseThrow(() -> new HttpStatusCodeException(HttpStatus.NOT_FOUND) {});
					try {
						data.addEquation(newEquation);
					} catch (Exception e) {
						// equation failed to update dto for some reason
						data.setError("Failed to add equation " + newEquation.getName());
					}
					priceDataRepository.save(data);
					updateList.add(data);
				} catch (HttpStatusCodeException e) {
					logger.error("Could not find price data ID: " + dataId);
				}
			}
			return new ResponseEntity<>(updateList, HttpStatus.ACCEPTED);
		} catch (Exception e) {
			logger.error("Variable add failure: " + e.getMessage());
			e.printStackTrace();
			return new ResponseEntity<>("Variable add failure: " + e.getMessage(), HttpStatus.CONFLICT);
		}
	}

	/**
	 *
	 * Update Equation DTO in Price Data
	 *
	 * This function will update a equations notes nad value in a selected list of price data IDs.  This can only update the notes or the value,
	 * it cannot change the equations name or the equations key.  If you need to do either of those actions, use the previous function.
	 *
	 **/
	@PostMapping("/equation/update")
	@ResponseBody
	public ResponseEntity<Object> updateEquationData(@RequestBody EquationDataUpdatePackage equationDataUpdatePackage) {
		List<UpdateResponse> responses = new ArrayList<>();

		try {
			List<String> priceDataIds = equationDataUpdatePackage.getPriceDataIds();
			EquationData newEquationData = equationDataUpdatePackage.getNewData();

			for(String dataId : priceDataIds) {
				try {
					// get price data
					PriceData data = priceDataRepository.findById(dataId).orElseThrow(() -> new HttpStatusCodeException(HttpStatus.NOT_FOUND) {});
					try {
						logger.info("Updating equation " + newEquationData.getName() + " to: " + newEquationData.getEquation());
						data.clearError();
						data.updateEquationData(newEquationData);
					} catch (Exception e) {
						// equation failed to update dto for some reason
						data.setError("Failed to update equation " + newEquationData.getName());
						responses.add(new UpdateResponse(false, "Failed to update equation " + newEquationData.getName(), new PriceData()));
					}
					if(data.isError()) {
						responses.add(new UpdateResponse(false, "Failed to update equation " + newEquationData.getName() + " : " + data.getErrorDetails(), new PriceData()));
					}
					priceDataRepository.save(data);
				} catch (HttpStatusCodeException e) {
					logger.error("Could not find price data ID: " + dataId);
					responses.add(new UpdateResponse(false, "Could not find price data ID: " + dataId, new PriceData()));

				}
			}
			return new ResponseEntity<>(responses, HttpStatus.ACCEPTED);
		} catch (Exception e) {
			logger.error("Equation DTO update failure: " + e.getMessage());
			e.printStackTrace();
			return new ResponseEntity<>("Equation DTO update failure: " + e.getMessage(), HttpStatus.CONFLICT);
		}
	}

	/**
	 *
	 * Remove Equation
	 *
	 * This function removes an equation from a list of selected price data ids
	 *
	 **/
	@PostMapping("/equation/remove")
	@ResponseBody
	public ResponseEntity<String> removeEquation(@RequestBody EquationDataUpdatePackage updatePackage) {
		List<PriceData> updateList = new ArrayList<>();

		try {
			// get the list of price data ids
			List<String> priceDataIds = updatePackage.getPriceDataIds();
			// get the equation by Id
			// Equation equation = equationRepository.findById(id).orElseThrow(() -> new HttpStatusCodeException(HttpStatus.NOT_FOUND) {});
			EquationData newEquation = updatePackage.getNewData();
			for(String priceDataId : priceDataIds) {
				PriceData data = priceDataRepository.findById(priceDataId).orElseThrow(() -> new HttpStatusCodeException(HttpStatus.NOT_FOUND) {});
				try {
					data.removeEquation(newEquation.getId());
				} catch (Exception e) {
					data.setError("Failed to remove " + newEquation.getKey() + " : " + e.getMessage());
				}
				priceDataRepository.save(data);
				updateList.add(data);
			}
			return new ResponseEntity<>("Equations Removed", HttpStatus.ACCEPTED);
		} catch (Exception e) {
			logger.error("Failed to remove equation" + e.getMessage());
			e.printStackTrace();
			return new ResponseEntity<>("Failed to delete equation", HttpStatus.CONFLICT);
		}
	}


	/**
	 *
	 * Remove Equation
	 *
	 * This function removes an equation from a list of selected price data ids
	 *
	 **/
	@PostMapping("/equation/remove/marketing")
	@ResponseBody
	public ResponseEntity<Object> removeMarketingEquation(@RequestBody RemoveMarketingEquationPackage removeMarketingEquationPackage) {
		List<UpdateResponse> responses = new ArrayList<>();
		try {
			// get the list of price data ids
			List<String> priceDataIds = removeMarketingEquationPackage.getPriceDataIds();
			EquationData equationToRemove = removeMarketingEquationPackage.getEquationToRemove();
			String equationToRemoveId = equationToRemove.getId();

			// get the equation by Id
			Equation equation = null;
			try {
				equation = equationRepository.findById(equationToRemoveId).orElseThrow(() -> new HttpStatusCodeException(HttpStatus.NOT_FOUND) {});
			} catch (HttpStatusCodeException e) {
				return new ResponseEntity<>("Could not find equation with id " + equationToRemoveId, HttpStatus.NOT_FOUND);
			}

			// create the new marketing equation
			MarketingEquation newMarketingEquation = new MarketingEquation();
			newMarketingEquation.buildFromEquation(equationToRemove);

			try {
				marketingEquationRepository.save(newMarketingEquation);
				priceDataLogger.createNewLogEntry("", "Created new marketing equation", newMarketingEquation, "");
			} catch (Exception e) {
				// should only throw for a duplicate key error.  Can ignore
			}

			// remove from price data
			/*for(String priceDataId : priceDataIds) {
				PriceData data = priceDataRepository.findById(priceDataId).orElseThrow(() -> new HttpStatusCodeException(HttpStatus.NOT_FOUND) {});

			}*/

			List<PriceData> allDrafts = new ArrayList<>();
			for(PriceData data : allDrafts) {
				try {
					data.removeEquation(equationToRemoveId);
					priceDataLogger.createNewLogEntry(data.getId(), "Removed marketing equation", equationToRemove, "");
				} catch (Exception e) {
					responses.add(new UpdateResponse(false, "Error removing: " + e.getMessage(), data));
				}
				priceDataRepository.save(data);

			}

			return new ResponseEntity<>(responses , HttpStatus.ACCEPTED);
		} catch (Exception e) {
			logger.error("Failed to remove equation" + e.getMessage());
			e.printStackTrace();
			return new ResponseEntity<>("Failed to delete equation", HttpStatus.CONFLICT);
		}
	}
}
