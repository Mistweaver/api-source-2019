package com.api.demo.controllers;

import com.api.demo.mongorepositories.applicationpackage.changeorders.ChangeOrder;
import com.api.demo.mongorepositories.applicationpackage.changeorders.ChangeOrderRepository;
import com.api.demo.mongorepositories.applicationpackage.deletedObjects.DeletedObject;
import com.api.demo.mongorepositories.applicationpackage.deletedObjects.DeletedObjectRepository;
import com.api.demo.mongorepositories.applicationpackage.leads.Lead;
import com.api.demo.mongorepositories.applicationpackage.leads.LeadRepository;
import com.api.demo.mongorepositories.applicationpackage.purchaseagreements.PurchaseAgreement;
import com.api.demo.mongorepositories.applicationpackage.purchaseagreements.PurchaseAgreementRepository;
import com.google.gson.Gson;
import org.json.simple.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.HttpStatusCodeException;

@RestController
@RequestMapping("/deletedobjects")
public class DeletedObjectController {
	private static Logger logger = LoggerFactory.getLogger(DeletedObjectController.class);

	@Autowired
	private DeletedObjectRepository deletedObjectRepository;
	@Autowired
	private LeadRepository leadRepository;
	@Autowired
	private PurchaseAgreementRepository purchaseAgreementRepository;
	@Autowired
	private ChangeOrderRepository changeOrderRepository;

	Gson gson = new Gson();

	@PostMapping("/restore")
	@ResponseBody
	public ResponseEntity<String> restoreObject(@RequestBody JSONObject data) {
		try {
			DeletedObject object = deletedObjectRepository.findById(data.get("deletedObjectId").toString()).orElseThrow(() -> new HttpStatusCodeException(HttpStatus.NOT_FOUND) {});

			// check object type
			switch(object.getObjectType()) {
				case "lead":
					// cast new object
					Lead lead = gson.fromJson(object.getObject(), Lead.class);
					// save to database
					leadRepository.save(lead);
					// delete from deleted object repository
					deletedObjectRepository.delete(object);
					return new ResponseEntity<>("Lead Restoration Successful", HttpStatus.ACCEPTED);
				case "purchaseAgreement":
					PurchaseAgreement agreement = gson.fromJson(object.getObject(), PurchaseAgreement.class);
					purchaseAgreementRepository.save(agreement);
					deletedObjectRepository.delete(object);
					return new ResponseEntity<>("Purchase Agreement Restoration Successful", HttpStatus.ACCEPTED);
				case "changeOrder":
					ChangeOrder changeOrder = gson.fromJson(object.getObject(), ChangeOrder.class);
					changeOrderRepository.save(changeOrder);
					deletedObjectRepository.delete(object);
					return new ResponseEntity<>("Change Order Restoration Successful", HttpStatus.ACCEPTED);
				default:
					return new ResponseEntity<>("Could not restore object of unknown type", HttpStatus.CONFLICT);

			}


		} catch (Exception e) {
			logger.error("Restore failed: " + e.getMessage());
			return new ResponseEntity<>("Restoration Failed: " + e.getMessage(), HttpStatus.CONFLICT);
		}

	}
}
