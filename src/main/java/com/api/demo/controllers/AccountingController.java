package com.api.demo.controllers;

import com.api.demo.mongorepositories.applicationpackage.changeorders.ChangeOrder;
import com.api.demo.mongorepositories.applicationpackage.changeorders.ChangeOrderRepository;
import com.api.demo.mongorepositories.applicationpackage.leads.Lead;
import com.api.demo.mongorepositories.applicationpackage.leads.LeadRepository;
import com.api.demo.mongorepositories.applicationpackage.purchaseagreements.PurchaseAgreement;
import com.api.demo.mongorepositories.applicationpackage.purchaseagreements.PurchaseAgreementRepository;
import org.json.simple.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.*;
import org.springframework.data.repository.query.Param;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


@RestController
@RequestMapping("/accounting")
public class AccountingController {
	private static Logger logger = LoggerFactory.getLogger(AccountingController.class);

	@Autowired
	private PurchaseAgreementRepository purchaseAgreementRepository;
	@Autowired
	private ChangeOrderRepository changeOrderRepository;
	@Autowired
	private LeadRepository leadRepository;

	@GetMapping("/getdeals/executed")
	@ResponseBody
	public ResponseEntity<Map<String, Object>> getDeals(
			@RequestParam("locationId") String locationId,
			@RequestParam("userId") String userId,
			@RequestParam("size") int size,
			@RequestParam("page") int page,
			@RequestParam("sort") String sort) {

		// JSONArray pageList = new JSONArray();
		List<JSONObject> pageList = new ArrayList<>();

		/********* Parse sort property and direction ***********/
		// Initialize new sort properties
		Sort.Direction sortDirection = Sort.Direction.DESC;
		String sortProperty = "creationTime";


		//parse sort direction and field
		if(!sort.equals("")) {  // if sort is not set to blank, break up the string into its components and parse
			try {
				String[] parsedSort = sort.split(",");
				if(parsedSort.length > 1) {
					sortProperty = parsedSort[0];
					if(parsedSort[1].toUpperCase().equals("ASC")) {
						sortDirection = Sort.Direction.ASC;
					}
				}
			} catch (Exception e) {
				logger.warn("Failed to parse sort direction");
			}
		}

		Pageable pageRequest = PageRequest.of(page, size, Sort.by(sortDirection, sortProperty));

		// List<Lead> leads = new ArrayList<>();
		Page<Lead> masterLeadPage = new PageImpl<>(new ArrayList<>());
		try {
			if(locationId != null && !locationId.equals("")) {
				if(userId != null && !userId.equals("")) {
					masterLeadPage = leadRepository.findByLocationIdAndUserId(locationId, userId, pageRequest);
				} else {
					masterLeadPage = leadRepository.findByLocationId(locationId, pageRequest);
				}
			} else {
				masterLeadPage = leadRepository.findAll(pageRequest);
			}
			logger.info(String.valueOf(masterLeadPage.getSize()));

			for(Lead lead : masterLeadPage.getContent()) {
				JSONObject contracts = buildLeadDocuments(lead);
				pageList.add(contracts);
			}

			Map<String, Object> json = new HashMap();


			json.put("totalPages", masterLeadPage.getTotalPages());
			json.put("totalObjects", masterLeadPage.getTotalElements());
			json.put("numberOfElements", masterLeadPage.getNumberOfElements());
			json.put("objects", pageList);

			return new ResponseEntity<>(json, HttpStatus.ACCEPTED);
		} catch(Exception e) {
			logger.error("Error: could not build page contracts");
			Map<String, Object> json = new HashMap();
			json.put("totalPages", 0);
			json.put("totalObjects", 0);
			json.put("numberOfElements", 0);
			return new ResponseEntity<>(json, HttpStatus.NOT_FOUND);
		}
	}

	@GetMapping("/getdeals/ccid")
	@ResponseBody
	public ResponseEntity<JSONObject> getDealsByCCID(@Param("ccid") String ccId) {
		JSONObject contracts = new JSONObject();
		try {
			// JSONArray pageList = new JSONArray();
			List<JSONObject> pageList = new ArrayList<>();

			// get the lead based upon the client consultant ID number
			Lead lead = leadRepository.findByLeadId(ccId);
			// build the lead documents
			contracts = buildLeadDocuments(lead);


			return new ResponseEntity<>(contracts, HttpStatus.ACCEPTED);
		} catch(Exception e) {
			logger.error("Error: could not get lead");
			return new ResponseEntity<>(contracts, HttpStatus.NOT_FOUND);
		}
	}

	private JSONObject buildLeadDocuments(Lead lead) {
		List<PurchaseAgreement> agreements;
		List<ChangeOrder> orders = new ArrayList<>();

		JSONObject contracts = new JSONObject();
		agreements = purchaseAgreementRepository.findByLeadId(lead.getId());
		for(PurchaseAgreement agreement: agreements) {
			orders.addAll(changeOrderRepository.findByPurchaseAgreementId(agreement.getId()));
		}

		contracts.put("lead", lead);
		contracts.put("agreements", agreements);
		contracts.put("changeorders", orders);

		return contracts;
	}

}
