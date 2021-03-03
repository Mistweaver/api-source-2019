package com.api.demo.pricedata.controllers;

import com.api.demo.pricedata.repositories.pricedata.PriceData;
import com.api.demo.pricedata.repositories.pricedata.PriceDataRepository;
import com.api.demo.mongorepositories.applicationpackage.salesoffices.SalesOffice;
import com.api.demo.mongorepositories.applicationpackage.salesoffices.SalesOfficeRepository;
import com.api.demo.pricedata.repositories.pricedata.PriceDataStatus;
import com.api.demo.pricedata.repositories.variables.Variable;
import com.api.demo.pricedata.repositories.variables.VariableData;
import com.api.demo.pricedata.repositories.variables.VariableRepository;
import com.api.demo.pricedata.restpackages.PriceDataContainingVariable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.HttpStatusCodeException;

import java.util.*;

@RestController
@RequestMapping("/variables")
public class VariableController {
	private static Logger logger = LoggerFactory.getLogger(VariableController.class);

	@Autowired
	private PriceDataRepository priceDataRepository;
	@Autowired
	private SalesOfficeRepository salesOfficeRepository;
	@Autowired
	private PriceDataController priceDataController;
	@Autowired
	private VariableRepository variableRepository;


	/*** Return a list of all variables ***/
	@GetMapping("/all")
	@ResponseBody
	public ResponseEntity<Object> getAllVariables() {
		List<Variable> allVariables = variableRepository.findAll();
		return new ResponseEntity<>(allVariables, HttpStatus.ACCEPTED);
	}


	/**
	 * Return all price data that contains a selected variable
	 */
	@GetMapping("/{id}/data")
	@ResponseBody
	public ResponseEntity<Object> getVariablesActiveData(@PathVariable String id) {
		try {
			List<SalesOffice> offices = salesOfficeRepository.findAll();
			Map<String, SalesOffice> officeMap = new HashMap<>();
			for (SalesOffice office : offices) {
				officeMap.put(office.getId(), office);
			}

			List<PriceDataContainingVariable> listOfDataContainingVariable = new ArrayList<>();

			// get the variable by Id
			Variable variable = variableRepository.findById(id).orElseThrow(() -> new HttpStatusCodeException(HttpStatus.NOT_FOUND) {});

			// get all active and pending price data
			List<PriceData> activeList = priceDataRepository.findByStatus(PriceDataStatus.ACTIVE);
			List<PriceData> pendingList = priceDataRepository.findByStatus(PriceDataStatus.PENDING);
			List<PriceData> draftList = priceDataRepository.findByStatus(PriceDataStatus.DRAFT);
			// check if the variable exists in each price data
			for (PriceData data : activeList) {
				VariableData var = data.getVariable(id);
				if(var != null) {
					listOfDataContainingVariable.add(new PriceDataContainingVariable(var, officeMap.get(data.getLocationId()), data));
				}
			}

			for (PriceData data : pendingList) {
				VariableData var = data.getVariable(id);
				if(var != null) {
					listOfDataContainingVariable.add(new PriceDataContainingVariable(var, officeMap.get(data.getLocationId()), data));

				}
			}

			for (PriceData data : draftList) {
				VariableData var = data.getVariable(id);
				if(var != null) {
					listOfDataContainingVariable.add(new PriceDataContainingVariable(var, officeMap.get(data.getLocationId()), data));
				}
			}

			return new ResponseEntity<>(listOfDataContainingVariable, HttpStatus.ACCEPTED);
		} catch (HttpStatusCodeException e) {
			return new ResponseEntity<>("", HttpStatus.NOT_FOUND);
		} catch (Exception e) {
			return new ResponseEntity<>(e.getMessage(), HttpStatus.CONFLICT);
		}
	}

	/**
	 *
	 * Variable deletion
	 *
	 * This will not only remove a variable from all price data, but also delete the underlying variable.
	 * Do not use this to remove a variable from price data, use this to remove a variable from the entire system
	 *
	 **/
	@PostMapping("/{id}/delete")
	@ResponseBody
	public ResponseEntity<Object> deleteVariable(@PathVariable String id) {
		List<PriceData> updateList = new ArrayList<>();
		Variable variable;
		try {
			variable = variableRepository.findById(id).orElseThrow(() -> new HttpStatusCodeException(HttpStatus.NOT_FOUND) {});
		} catch (HttpStatusCodeException e) {
			logger.error("Could not find variable with id: " + id);
			return new ResponseEntity<>("Failed to delete variable", HttpStatus.CONFLICT);
		}

		try {
			// remove the variable from each price data it's found in
			List<PriceData> queryResults = new ArrayList<>();
			List<PriceData> deletedFromData = new ArrayList<>();

			queryResults.addAll(priceDataRepository.findByStatus(PriceDataStatus.ACTIVE));
			queryResults.addAll(priceDataRepository.findByStatus(PriceDataStatus.PENDING));
			queryResults.addAll(priceDataRepository.findByStatus(PriceDataStatus.DRAFT));

			// delete variable from all price data
			for(PriceData data : queryResults) {
				try {
					data.removeVariable(variable.getId());
					if(data.isDataUpdated()) {
						deletedFromData.add(data);
					}
				} catch (Exception e) {
					data.setError("Failed to remove equation " + variable.getName());
					e.printStackTrace();
					return new ResponseEntity<>("Failed to remove variable: " + e.getMessage(), HttpStatus.CONFLICT);
				}
			}


			for(PriceData data : deletedFromData) {
				priceDataRepository.save(data);
			}

			variableRepository.delete(variable);
			return new ResponseEntity<>(updateList, HttpStatus.ACCEPTED);

		} catch (Exception e) {
			logger.error("Failed to get variables" + e.getMessage());
			e.printStackTrace();
			return new ResponseEntity<>("Failed to delete variable", HttpStatus.CONFLICT);
		}
	}

	/**
	 *
	 * Update Variable
	 *
	 * This function will update a variables properties, and if necessary, reflect that update in all of the existing price data
	 * where the variable is used e.g. a change in the variable key or variable name will be updated everywhere, where notes being
	 * added/edited will only show up in the specific variable details.
	 *
	 **/
	@PostMapping("/{id}/update/")
	@ResponseBody
	public ResponseEntity<Object> updateVariable(@PathVariable String id, @RequestBody Variable updatedVariable) {
		try {
			// get the variable by Id
			Variable variable = variableRepository.findById(id).orElseThrow(() -> new HttpStatusCodeException(HttpStatus.NOT_FOUND) {});

			// clean the variable key before checking
			String formattedKey = cleanKeyString(updatedVariable.getKey());
			updatedVariable.setKey(formattedKey);

			try {
				// get a list of all active, pending, and drafts
				List<PriceData> queryResults = new ArrayList<>();
				List<PriceData> updatedData = new ArrayList<>();
				queryResults.addAll(priceDataRepository.findByStatus(PriceDataStatus.ACTIVE));
				queryResults.addAll(priceDataRepository.findByStatus(PriceDataStatus.PENDING));
				queryResults.addAll(priceDataRepository.findByStatus(PriceDataStatus.DRAFT));

				// flag for any update failures
				boolean didUpdateFailureOccur = false;

				for (PriceData priceData : queryResults) {
					VariableData var = priceData.getVariable(variable.getId());
					if (var != null) {
						try {
							// data contains variable.  Update the variable with the DTO
							priceData.updateDataFromVariable(updatedVariable);
							// ...and then search the equations to update any keys
							// might leave this blank and trust the users to remove the keys from the equations themselves
						} catch (Exception e) {
							// if the update fails, mark the price data with an error
							didUpdateFailureOccur = true;
							logger.error("Update failure: " + priceData.getId());
							priceData.setError("Failed to update " + variable.getKey() + "/" + variable.getName() + " to " + updatedVariable.getKey() + "/" + updatedVariable.getName() + " : " + e.getMessage());
						}
						updatedData.add(priceData);
					}
				}

				if(didUpdateFailureOccur) {
					return new ResponseEntity<>(updatedData, HttpStatus.CONFLICT);
				} else {
					for(PriceData data : updatedData) {
						priceDataRepository.save(data);
					}
					variableRepository.save(updatedVariable);
					return new ResponseEntity<>(updatedData, HttpStatus.ACCEPTED);
				}
			} catch (Exception e) {
				logger.error("Query failure: " + e.getMessage());
				e.printStackTrace();
				return new ResponseEntity<>("Unknown error: " + e.getMessage(), HttpStatus.CONFLICT);
			}


		} catch (Exception e) {
			logger.error("Variable update failure: " + e.getMessage());
			e.printStackTrace();
			return new ResponseEntity<>("Unknown error: " + e.getMessage(), HttpStatus.CONFLICT);
		}
	}





	@PostMapping("/create")
	@ResponseBody
	public ResponseEntity<Object> createVariable(@RequestBody Variable newVariable) {
		try {
			Variable variable = variableRepository.save(newVariable);
			return new ResponseEntity<>(variable, HttpStatus.ACCEPTED);
		} catch (Exception e) {
			logger.error("Failed to create new variable: " + e.getMessage());
			e.printStackTrace();
			return new ResponseEntity<>(newVariable, HttpStatus.CONFLICT);
		}
	}

	/********* Addition, Update, Deletion Variables **************/

	private String cleanKeyString(String key) {
		// strip all non alphabetical characters
		key = key.replaceAll("[^a-zA-Z]", "");
		// strip all white space
		key = key.replaceAll("\\s", "");
		// add key delimiters
		key = "[" + key + "]";
		System.out.println(key);
		return key;
	}
}
