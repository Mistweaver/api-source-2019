package com.api.demo.pricedata.controllers;

import com.api.demo.pricedata.repositories.models.DuplicateModelPacakge;
import com.api.demo.pricedata.repositories.models.Model;
import com.api.demo.pricedata.repositories.pricedata.PriceData;
import com.api.demo.pricedata.repositories.pricedata.PriceDataRepository;
import com.api.demo.mongorepositories.applicationpackage.salesoffices.SalesOffice;
import com.api.demo.mongorepositories.applicationpackage.salesoffices.SalesOfficeRepository;
import com.api.demo.pricedata.repositories.models.ModelRepository;
import com.api.demo.pricedata.restpackages.NewDataForModelPackage;
import com.api.demo.pricedata.restpackages.PriceDataWithOfficePackage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.HttpStatusCodeException;

import java.math.BigDecimal;
import java.util.*;

import static com.api.demo.pricedata.repositories.pricedata.PriceDataStatus.DRAFT;

@RestController
@RequestMapping("/models")
public class ModelController {
	private static final Logger logger = LoggerFactory.getLogger(ModelController.class);
	/** Required repositories **/
	@Autowired
	private ModelRepository modelRepository;
	@Autowired
	private PriceDataRepository priceDataRepository;
	@Autowired
	private SalesOfficeRepository salesOfficeRepository;


	/**
	 * Returns all models in the repository. Use this sparingly, because it will return over 1000 results
	 * and MongoDB Atlas will send an angry email for wasteful database queries.  In their defense, it is a performance hit.
	 *
	 * @return List<Model> 		List of models found
	 */
	@GetMapping("/all")
	@ResponseBody
	public ResponseEntity<List<Model>> getAllModels() {
		List<Model> models = modelRepository.findAll();
		return new ResponseEntity<>(models, HttpStatus.ACCEPTED);
	}

	/**
	 * Edit a model.  This route will update not only the model, but all price data that is in a draft state as well
	 *
	 * @param id				ID of the model.  Use this to verify the model you are editing is the right one
	 * @param editedModel		The updated model object.  Sits in the request body
	 * @return Model			Return the updated model
	 */
	@PostMapping("/{id}/edit")
	@ResponseBody
	public ResponseEntity<Object> editModel(@PathVariable String id, @RequestBody Model editedModel) {
		try {
			Model model = modelRepository.findById(id).orElseThrow(() -> new HttpStatusCodeException(HttpStatus.NOT_FOUND) {});
			if(!model.getId().equals(editedModel.getId())) {
				return new ResponseEntity<>("Could not edit: edited model ID and path ID do not match", HttpStatus.CONFLICT);
			}
			// save the updates
			model = modelRepository.save(editedModel);
			// get all the price data for the model in draft status and update it
			// do not update price data that is in PENDING, ACTIVE, or RETIRED
			List<PriceData> priceDataList = priceDataRepository.findByModelIdAndStatus(id, DRAFT);
			for(PriceData data : priceDataList) {
				data.setModel(model);
				priceDataRepository.save(data);
			}

			return new ResponseEntity<>(model, HttpStatus.ACCEPTED);
		} catch (Exception e) {
			logger.error("Failed to edit model" + e.getMessage());
			return new ResponseEntity<>("Error editing model: " + e.getMessage(), HttpStatus.CONFLICT);
		}
	}




	@GetMapping("/duplicates")
	@ResponseBody
	public ResponseEntity<Object> findDuplicateModels() {
		List<Model> models = modelRepository.findAll();
		List<DuplicateModelPacakge> duplicateList = new ArrayList<>();

		for(Model model : models) {
			System.out.println("Getting duplicates for " + model.getModelNumber());
			List<Model> similarModels = modelRepository.findByModelNumberLike(model.getModelNumber());
			if(similarModels.size() > 1) {
				DuplicateModelPacakge duplicateModelPacakge = new DuplicateModelPacakge();
				duplicateModelPacakge.setModel(model);
				duplicateModelPacakge.setPossibleDuplicates(similarModels);
				duplicateList.add(duplicateModelPacakge);
			}
		}

		return new ResponseEntity<>(duplicateList , HttpStatus.ACCEPTED);
	}

	@PostMapping("{id}/merge/{modelIdToMerge}")
	@ResponseBody
	public ResponseEntity<Object> mergeModels(@PathVariable String modelId, @PathVariable String modelIdToMerge) {
		Model model = modelRepository.findById(modelId).orElseThrow(() -> new HttpStatusCodeException(HttpStatus.NOT_FOUND) {});
		Model modelToMerge = modelRepository.findById(modelIdToMerge).orElseThrow(() -> new HttpStatusCodeException(HttpStatus.NOT_FOUND) {});
		List<PriceData> mergeModelPriceData = priceDataRepository.findByModelId(modelToMerge.getId());

		for(PriceData data : mergeModelPriceData) {
			// update the model data with the model
			data.setModel(model);
			priceDataRepository.save(data);
		}

		modelRepository.delete(modelToMerge);

		return new ResponseEntity<>(HttpStatus.ACCEPTED);
	}

	/**
	 * Fetches all the price data for a given model ID
	 * @param id					ID of the model
	 * @return	List<PriceData>		List of the price data for the model
	 */
	@GetMapping("/{id}/data")
	@ResponseBody
	public ResponseEntity<Object> getPriceDataForModel(@PathVariable String id) {
		List<PriceDataWithOfficePackage> existingPriceDataForModel = new ArrayList<>();

		try {
			List<PriceData> modelData = priceDataRepository.findByModelId(id);
			for(PriceData data : modelData) {
				try {
					SalesOffice office = salesOfficeRepository.findById(data.getLocationId()).orElseThrow(() -> new HttpStatusCodeException(HttpStatus.NOT_FOUND) {});
					PriceDataWithOfficePackage record = new PriceDataWithOfficePackage(office, data);
					existingPriceDataForModel.add(record);
				} catch (HttpStatusCodeException e) {
					PriceDataWithOfficePackage record = new PriceDataWithOfficePackage(new SalesOffice(), data);
					existingPriceDataForModel.add(record);
				}
			}
			return new ResponseEntity<>(existingPriceDataForModel, HttpStatus.ACCEPTED);
		} catch (Exception e) {
			logger.error("Failed to get models" + e.getMessage());
			e.printStackTrace();
			return new ResponseEntity<>("Error getting data", HttpStatus.CONFLICT);
		}
	}

	@PostMapping("/data/new")
	@ResponseBody
	public ResponseEntity<Object> newDataForModel(@RequestBody NewDataForModelPackage newDataForModelPackage) {
		try {
			/* Get the Model By ID */
			Model model = modelRepository.findById(newDataForModelPackage.getModelId()).orElseThrow(() -> new HttpStatusCodeException(HttpStatus.NOT_FOUND) {});
			// Create new Price Data for the model
			PriceData newPriceData = new PriceData();
			newPriceData.setModel(model);
			newPriceData.setLocationId(newDataForModelPackage.getLocationId());
			newPriceData.setName(newDataForModelPackage.getName());
			newPriceData.setSeriesName(newDataForModelPackage.getSeriesName());
			newPriceData.setActiveDate(newDataForModelPackage.getDraftDate());

			/* If the existing data id is not empty, copy over the variables and equations from the data ***/
			String existingDataId = newDataForModelPackage.getExistingDataId();
			if(!existingDataId.isEmpty()) {
				PriceData existingData = priceDataRepository.findById(existingDataId).orElseThrow(() -> new HttpStatusCodeException(HttpStatus.NOT_FOUND) {});
				newPriceData.setVariables(existingData.getVariables());
				newPriceData.setEquations(existingData.getEquations());
			}
			/* If the template id is not empty, copy over the variables and equations from the template ***/
			/* This OVERRIDES the existing data id ***/
			String templateId = newDataForModelPackage.getTemplateId();
			if(!templateId.isEmpty()) {
				PriceData template = priceDataRepository.findById(templateId).orElseThrow(() -> new HttpStatusCodeException(HttpStatus.NOT_FOUND) {});
				newPriceData.setVariables(template.getVariables());
				newPriceData.setEquations(template.getEquations());
			}

			/* Update the base price
			 *
			 * Setting this below here for a few reasons.  If a template ID is selected then its base price value will not
			 * be correct.  It also overrides the existing data base price variable.  However if existing data is used then
			 * the base price in the request package will match and be correct.  While I set the base price value directly here,
			 * I also set it in the variables as well.
			 */

			newPriceData.setBasePrice(new BigDecimal(newDataForModelPackage.getBasePrice()));
			System.out.println(newPriceData.getBasePrice());

			PriceData data = priceDataRepository.save(newPriceData);
			return new ResponseEntity<>(data, HttpStatus.ACCEPTED);
		} catch (Exception e) {
			logger.error("Failed to get models" + e.getMessage());
			e.printStackTrace();
			return new ResponseEntity<>("Error getting data", HttpStatus.CONFLICT);
		}
	}

	@PostMapping("/{id}/delete")
	@ResponseBody
	public ResponseEntity<Object> deleteModel(@PathVariable String id) {
		try {
			modelRepository.deleteById(id);
			return new ResponseEntity<>("Deleted!", HttpStatus.ACCEPTED);
		} catch (Exception e) {
			logger.error("Failed to get models" + e.getMessage());
			e.printStackTrace();
			return new ResponseEntity<>("Error getting data", HttpStatus.CONFLICT);
		}
	}

	@PostMapping("/new")
	@ResponseBody
	public ResponseEntity<Object> createNewModel(@RequestBody Model newModel) {
		try {
			Model model = modelRepository.insert(newModel);
			return new ResponseEntity<>(model, HttpStatus.ACCEPTED);
		} catch (Exception e) {
			logger.error("Failed to create model" + e.getMessage());
			e.printStackTrace();
			return new ResponseEntity<>("Failed to create model", HttpStatus.CONFLICT);
		}
	}
}

