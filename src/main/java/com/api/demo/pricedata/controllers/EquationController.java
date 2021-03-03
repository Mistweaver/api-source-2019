package com.api.demo.pricedata.controllers;

import com.api.demo.pricedata.repositories.pricedata.PriceData;
import com.api.demo.pricedata.repositories.pricedata.PriceDataRepository;
import com.api.demo.mongorepositories.applicationpackage.salesoffices.SalesOffice;
import com.api.demo.mongorepositories.applicationpackage.salesoffices.SalesOfficeRepository;
import com.api.demo.pricedata.repositories.equations.Equation;
import com.api.demo.pricedata.repositories.equations.EquationData;
import com.api.demo.pricedata.repositories.equations.EquationRepository;
import com.api.demo.pricedata.repositories.pricedata.PriceDataStatus;
import com.api.demo.pricedata.restpackages.PriceDataContainingEquation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.HttpStatusCodeException;

import java.util.*;

@RestController
@RequestMapping("/equations")
public class EquationController {
	private static Logger logger = LoggerFactory.getLogger(EquationController.class);

	@Autowired
	private PriceDataRepository priceDataRepository;
	@Autowired
	private SalesOfficeRepository salesOfficeRepository;
	@Autowired
	private EquationRepository equationRepository;

	@Autowired
	private PriceDataController priceDataController;

	/* Return a list of all equations */
	@GetMapping("/all")
	@ResponseBody
	public ResponseEntity<Object> getAllEquations() {
		List<Equation> allEquations = equationRepository.findAll();
		return new ResponseEntity<>(allEquations, HttpStatus.ACCEPTED);
	}

	/* Creates a new equation */
	@PostMapping("/create")
	@ResponseBody
	public ResponseEntity<Object> createEquation(@RequestBody Equation newEquation) {
		try {
			if(newEquation.getKey().startsWith("[") && newEquation.getKey().endsWith("]")) {
				Equation equation = equationRepository.save(newEquation);
				return new ResponseEntity<>(equation, HttpStatus.ACCEPTED);
			}
			return new ResponseEntity<>("equation key missing delimiters [  ]", HttpStatus.BAD_REQUEST);
		} catch (Exception e) {
			logger.error("Failed to create new equation: " + e.getMessage());
			// e.printStackTrace();
			return new ResponseEntity<>(e.getMessage(), HttpStatus.CONFLICT);
		}
	}

	/* Return all price data that contains a selected equation */
	@GetMapping("/{id}/data")
	@ResponseBody
	public ResponseEntity<Object> getEquationActiveData(@PathVariable String id) {
		try {
			List<SalesOffice> offices = salesOfficeRepository.findAll();
			Map<String, SalesOffice> officeMap = new HashMap<>();
			for (SalesOffice office : offices) {
				officeMap.put(office.getId(), office);
			}

			// get the equation by Id
			Equation equation = equationRepository.findById(id).orElseThrow(() -> new HttpStatusCodeException(HttpStatus.NOT_FOUND) {});

			// create the equation package
			List<PriceDataContainingEquation> listOfDataContainingEquation = new ArrayList<>();

			// get all active and pending price data
			List<PriceData> activeList = priceDataRepository.findByStatus(PriceDataStatus.ACTIVE);
			List<PriceData> pendingList = priceDataRepository.findByStatus(PriceDataStatus.PENDING);
			List<PriceData> draftList = priceDataRepository.findByStatus(PriceDataStatus.DRAFT);

			// check if the equation exists in each price data
			for (PriceData data : activeList) {
				EquationData eqn = data.getEquation(id);
				if(eqn != null) {
					listOfDataContainingEquation.add(new PriceDataContainingEquation(eqn, officeMap.get(data.getLocationId()), data));
				}
			}

			for (PriceData data : pendingList) {
				EquationData eqn = data.getEquation(id);
				if(eqn != null) {
					listOfDataContainingEquation.add(new PriceDataContainingEquation(eqn, officeMap.get(data.getLocationId()), data));

				}
			}

			for (PriceData data : draftList) {
				EquationData eqn = data.getEquation(id);
				if(eqn != null) {
					listOfDataContainingEquation.add(new PriceDataContainingEquation(eqn, officeMap.get(data.getLocationId()), data));
				}
			}

logger.info("listOfDataContainingEquation size: " + listOfDataContainingEquation.size());

			return new ResponseEntity<>(listOfDataContainingEquation, HttpStatus.ACCEPTED);
		} catch (HttpStatusCodeException e) {
			return new ResponseEntity<>("", HttpStatus.NOT_FOUND);
		} catch (Exception e) {
			return new ResponseEntity<>(e.getMessage(), HttpStatus.CONFLICT);
		}
	}

	/**
	 *
	 * Equation deletion
	 *
	 * This will not only remove an equation from all price data, but also delete the underlying equation.
	 * Do not use this to remove an equation from price data, use this to remove an equation from the entire system
	 *
	 **/
	@PostMapping("/{id}/delete")
	@ResponseBody
	public ResponseEntity<Object> deleteEquation(@PathVariable String id) {
		List<PriceData> updateList = new ArrayList<>();

		try {
			// get the equation by Id
			Equation equation = equationRepository.findById(id).orElseThrow(() -> new HttpStatusCodeException(HttpStatus.NOT_FOUND) {});

			// remove the equation from each price data it's found in
			List<PriceData> queryResults = new ArrayList<>();
			queryResults.addAll(priceDataRepository.findByStatus(PriceDataStatus.ACTIVE));
			queryResults.addAll(priceDataRepository.findByStatus(PriceDataStatus.PENDING));
			queryResults.addAll(priceDataRepository.findByStatus(PriceDataStatus.DRAFT));
			// delete equation from all price data

			boolean failedRemoval = false;
			for(PriceData data : queryResults) {
				try {
					data.removeEquation(equation.getId());
				} catch (Exception e) {
					failedRemoval = true;
					data.setError("Failed to remove equation " + equation.getName());
				}
				priceDataRepository.save(data);
				updateList.add(data);
			}

			if(!failedRemoval) {
				// delete the equation
				equationRepository.delete(equation);
			}

			return new ResponseEntity<>(updateList, HttpStatus.ACCEPTED);

		} catch (Exception e) {
			logger.error("Failed to get equations" + e.getMessage());
			e.printStackTrace();
			return new ResponseEntity<>("Failed to delete equation", HttpStatus.CONFLICT);
		}
	}

	/**
	 *
	 * Update Equation
	 *
	 * This function will update a equations properties, and if necessary, reflect that update in all of the existing price data
	 * where the equation is used e.g. a change in the equation key or equation name will be updated everywhere, where notes being
	 * added/edited will only show up in the specific equation details.
	 *
	 **/
	@PostMapping("/{id}/update/")
	@ResponseBody
	public ResponseEntity<Object> updateEquation(@PathVariable String id, @RequestBody Equation updatedEquation) {
		try {
			// get the equation by Id
			Equation equation = equationRepository.findById(id).orElseThrow(() -> new HttpStatusCodeException(HttpStatus.NOT_FOUND) {});

			// clean the equation key before checking
			String formattedKey = cleanKeyString(updatedEquation.getKey());
			updatedEquation.setKey(formattedKey);

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
					EquationData eqn = priceData.getEquation(equation.getId());
					if (eqn != null) {
						try {
							// data contains equation.  Update the equation with the DTO
							priceData.updateDataFromEquation(updatedEquation);
							// ...and then search the equations to update any keys
							// might leave this blank and trust the users to remove the keys from the equations themselves
						} catch (Exception e) {
							// if the update fails, mark the price data with an error
							didUpdateFailureOccur = true;
							logger.error("Update failure: " + priceData.getId());
							priceData.setError("Failed to update " + equation.getKey() + "/" + equation.getName() + " to " + updatedEquation.getKey() + "/" + updatedEquation.getName() + " : " + e.getMessage());
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
					equationRepository.save(updatedEquation);
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

	/*private PriceData updateEquation(PriceData data, JSONObject updatePackage) {
		String equationKey = updatePackage.get("equationKey").toString();
		System.out.println("Updating: " + equationKey);

		// retrieve all equations from the data
		List<EquationDTO> equations = data.getEquations();
		// list of indexes where the equation key was found
		List<Integer> indexList = new ArrayList<>();
		// flag for if the price data has updates to save.  Use to improve response time
		boolean changesMade = false;

		for(int i = 0; i < equations.size(); i++) {
			EquationDTO equation = equations.get(i);
			// if the keys match, add the index to the indexList
			if(equation.getKey().equals(equationKey)) {
				indexList.add(i);
			}
		}

		// get the new equation values from the update package
		String newName = updatePackage.get("newName").toString();
		String newKey = updatePackage.get("newKey").toString();
		String newNotes = updatePackage.get("newNotes").toString();

		String newEquation = updatePackage.get("newEquation").toString();
		String newRoundingPosition = updatePackage.get("newRoundingPosition").toString();
		String newRoundingDirection = updatePackage.get("newRoundingDirection").toString();

		// iterate through the index list and update each equation
		for(Integer index : indexList) {
			EquationDTO equationToUpdate = equations.get(index);

			// update the equation name if applicable.
			if (!newName.isEmpty()) { equationToUpdate.setName(newName); }
			// update the equation notes if applicable
			if (!newNotes.isEmpty()) { equationToUpdate.setNotes(newNotes); }
			// update the equation value if applicable
			if (!newEquation.isEmpty()) { equationToUpdate.setEquation(newEquation); }
			// update the rounding position if applicable
			if (!newRoundingPosition.isEmpty()) { equationToUpdate.setRoundingPosition(Integer.parseInt(newRoundingPosition)); }
			// update the rounding direction if applicable
			if (!newRoundingDirection.isEmpty()) { equationToUpdate.setRoundingDirection(newRoundingDirection); }
			// update the key if applicable
			if (!newKey.isEmpty()) { equationToUpdate.setKey(newKey); }

			// update the equation list in the price data
			data.setEquations(equations);

			// only need to set the flag to true here.  If this loop executes, changes have occurred
			changesMade = true;
		}

		// if the equation was found in the equation list (indexList.size() > 0), update all the equations if applicable
		if(indexList.size() > 0) {

			// if the key has been updated, reflect this in all the equations
			if(!newKey.isEmpty()) {
				for(int j = 0; j < equations.size(); j++) {
					EquationDTO equation = equations.get(j);
					// update any references to the equation
					if(equation.getEquation().contains(equationKey)) {
						String equationString = equation.getEquation();
						System.out.println("Equation key found in equation string");
						System.out.println(equationString);
						String newEquationString = equationString.replaceAll(equationKey, newKey);
						equation.setEquation(newEquationString);
						System.out.println("New equation string");
						System.out.println(equation.getEquation());
					}
				}
				// update the equation list in the price data
				data.setEquations(equations);
			}

			// if the equation value has been updated, reflect this in all the equations
			if(!newEquation.isEmpty()) {
				System.out.println("Updating equation values");
				data.updateValues();
			}
		}


		if(changesMade) {
			data = priceDataRepository.save(data);
			System.out.println("Price data " + data.getId() + " done");
		}
		return data;

	}

	private PriceData removeEquation(PriceData data, Equation equationToRemove) {
		String equationKey = equationToRemove.getKey();

		List<EquationDTO> equations = data.getEquations();

		// build new equation list
		List<EquationDTO> newEquations = new ArrayList<>();
		// flag for if equation has been found.  Using it to save processing time
		boolean equationFound = false;
		// holder for the original equation that is being deleted
		EquationDTO equationToDelete = null;

		// if equation exists, flag it
		for(int i = 0; i < equations.size(); i++) {
			EquationDTO eqn = equations.get(i);
			if(equationKey.equals(eqn.getKey())) {
				equationFound = true;
				equationToDelete = eqn;
			} else {
				// all other equations are added to the new list
				newEquations.add(eqn);
			}
		}
		System.out.println("Equation for loop completed");

		// if the equation exists
		if(equationFound) {
			// if the equation exists in another equation, remove it
			for(int j = 0; j < equations.size(); j++) {
				EquationDTO equation = equations.get(j);
				if(equation.getEquation().contains(equationKey)) {
					String equationString = equation.getEquation();
					// System.out.println("Equation key found in equation string");
					// System.out.println(equationString);
					// String newEquationString = equationString.replaceAll(equationKey, "( " + equationToDelete.getEquation() + " ) ");
					String newEquationString = removeVariableFromEquation(equationKey, equationString);
					equation.setEquation(newEquationString);
					// System.out.println("New equation string");
					// System.out.println(equation.getEquation());
				}
				// System.out.println("EXIT equation found while loop");

			}
			// System.out.println("EXIT equation found for loop");
			data.setEquations(newEquations);

			// recalculate all data
			data.updateValues();
			// save the new data
			data = priceDataRepository.save(data);
		}

		// System.out.println("Price data " + data.getId() + " done");

		return data;
	}
*/
	/*
	private String removeVariableFromEquation(String variableToRemove, String equationToRemoveFrom) {

		StringBuilder equationString = new StringBuilder(equationToRemoveFrom);
		int variableIndex = equationString.indexOf(variableToRemove);
		// System.out.println(variableIndex);
		if(variableIndex != -1) {
			System.out.println("Variable exists in equation [" + equationString + "] at index " + variableIndex);
			// find the first character to the left and the first character to the right of the variable
			int idxLeft = this.firstNonWhiteSpaceCharLeft(equationString.toString(), variableIndex);

			if(idxLeft != -1) {
				char characterLeft = equationString.charAt(idxLeft);
				System.out.println("Character Left: " + characterLeft + " ( index: " + idxLeft + " )");
				// character to the left is an operator, remove the operator
				System.out.println("Deleting chars");
				equationString.deleteCharAt(idxLeft);
			} else {
				// else, get the character to the right
				int idxRight = this.firstNonWhiteSpaceCharRight(equationString.toString(), variableIndex + variableToRemove.length());

				// if operator, remove variable and the operator
				if(idxRight != -1) {
					char characterRight = equationString.charAt(idxRight);
					System.out.println("Character Right: " + characterRight + " ( index: " + idxRight + " )");
					System.out.println("Deleting chars right");
					equationString.deleteCharAt(idxRight);
				}
			}
			// remove the variable
			equationString = new StringBuilder(equationString.toString().replace(variableToRemove, ""));
			variableIndex = equationString.indexOf(variableToRemove);
			System.out.println("New equation [" + equationString.toString().replaceAll("\\s\\s+", " ") + "]");

			// check for variable occurring more than once
			if(variableIndex != -1) {
				equationString = new StringBuilder(this.removeVariableFromEquation(variableToRemove, equationToRemoveFrom));
			}

		}

		return equationString.toString().replaceAll("\\s\\s+", "");

	}

	private int firstNonWhiteSpaceCharLeft(String equationString, int variableIndex) {
		for(int i = 1; i < variableIndex; i++) {
			char characterLeft = equationString.charAt(variableIndex - i);
			System.out.println("Character " + i + " indexes left of " + variableIndex + " : " + characterLeft);
			if(!Character.isWhitespace(characterLeft)) {
				return variableIndex - i;
			}
		}
		return -1;
	}

	private int firstNonWhiteSpaceCharRight(String equationString, int searchIndex) {
		for(int i = searchIndex; i < equationString.length(); i++) {
			char characterRight = equationString.charAt(i);
			System.out.println("Character " + i + " indexes right of " + searchIndex + " : " + characterRight);
			if(!Character.isWhitespace(characterRight)) {
				return i;
			}
		}
		return -1;
	}*/
}
