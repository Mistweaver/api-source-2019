package com.api.demo.controllers;

import com.api.demo.mongorepositories.DocumentStates;
import com.api.demo.mongorepositories.applicationpackage.changeorders.ChangeOrder;
import com.api.demo.mongorepositories.applicationpackage.changeorders.ChangeOrderRepository;
import com.api.demo.mongorepositories.applicationpackage.deletedObjects.DeletedObjectRepository;
import com.api.demo.mongorepositories.applicationpackage.leads.Lead;
import com.api.demo.mongorepositories.applicationpackage.leads.LeadRepository;
import com.api.demo.mongorepositories.applicationpackage.purchaseagreements.PurchaseAgreement;
import com.api.demo.mongorepositories.applicationpackage.purchaseagreements.PurchaseAgreementRepository;
import com.api.demo.mongorepositories.applicationpackage.salesoffices.SalesOffice;
import com.api.demo.mongorepositories.applicationpackage.salesoffices.SalesOfficeRepository;
import com.api.demo.mongorepositories.applicationpackage.whitelist.WhiteList;
import com.api.demo.mongorepositories.applicationpackage.whitelist.WhiteListRepository;
import com.api.demo.mongorepositories.users.UserRepository;
import com.api.demo.utilityfunctions.JsonResponse;
import com.api.demo.pricedata.repositories.factories.Factory;
import com.api.demo.pricedata.repositories.factories.FactoryRepository;
import com.google.gson.Gson;
import org.json.simple.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.HttpStatusCodeException;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping
public class ApplicationController {
    private static Logger logger = LoggerFactory.getLogger(ApplicationController.class);
    Gson gson = new Gson();
    @Autowired
    private BCryptPasswordEncoder bCryptPasswordEncoder;

    @Autowired
    private LeadRepository leadRepository;
    @Autowired
    private UserRepository applicationUserRepository;
    @Autowired
    private SalesOfficeRepository salesOfficeRepository;
    @Autowired
    private WhiteListRepository whiteListRepository;
    @Autowired
    private PurchaseAgreementRepository purchaseAgreementRepository;
    @Autowired
    private ChangeOrderRepository changeOrderRepository;
    @Autowired
    private DeletedObjectRepository deletedObjectRepository;
    @Autowired
    private FactoryRepository factoryRepository;

    @Value("${addlead.api.key}")
    private String addleadApiKey;

    @PostMapping("/addlead")
    @ResponseBody
    public ResponseEntity<JSONObject> addLead(@RequestHeader("X-API-KEY") String token, @RequestBody JSONObject submittedLead) {
        // logger.info("Adding new lead");
        // logger.info("Auth token: " + token);
        logger.info("Adding lead: " + submittedLead.toJSONString());
        // logger.info(addleadApiKey);

        try {
            Lead newLead = gson.fromJson(submittedLead.toString(), Lead.class);

            /**
             * Default delivery country to US if not set in CRM system.
             */
            if(newLead.getDeliveryCountry() == null || newLead.getDeliveryCountry().equals("")) {
                newLead.setDeliveryCountry("US");
            }

            if(token.equals(addleadApiKey)) {
                // search database for lead with same leadId
                Lead existingLead = leadRepository.findByLeadId(newLead.getLeadId());
                // if found, send back response that lead already exists
                if(existingLead == null) {  // if not found, add lead to database
                    leadRepository.save(newLead);
                    logger.info("New lead added: " + newLead.getId());
                    return new ResponseEntity<>(JsonResponse.JsonResponse("Lead add success", newLead, HttpStatus.ACCEPTED), HttpStatus.CREATED);
                } else {
                    return new ResponseEntity<>(JsonResponse.JsonResponse("Lead already exists", newLead, HttpStatus.ACCEPTED), HttpStatus.ACCEPTED);
                }

            } else {
                logger.error("Lead add failure: auth key provided does not match");
                return new ResponseEntity<>(JsonResponse.JsonResponse("Could not add lead: auth key provided does not match", newLead, HttpStatus.CONFLICT), HttpStatus.CONFLICT);
            }


        } catch (Exception e) {
            logger.error("Lead add failure");
            return new ResponseEntity<>(JsonResponse.JsonResponse("Could not add lead", new Lead(), HttpStatus.CONFLICT), HttpStatus.CONFLICT);
        }
    }

    @GetMapping("/getalloffices")
    @ResponseBody
    public ResponseEntity<List<SalesOffice>> getSalesOffices() {
        List<SalesOffice> offices = new ArrayList<>();
        try {
            offices = salesOfficeRepository.findAll();
            return new ResponseEntity<>(offices, HttpStatus.ACCEPTED);
        } catch (Exception e) {
            logger.error("Failed to get sales offices" + e.getMessage());
            e.printStackTrace();
            return new ResponseEntity<>(offices, HttpStatus.CONFLICT);
        }
    }

    @GetMapping("/getregionaloffices")
    @ResponseBody
    public ResponseEntity<List<SalesOffice>> getRegionalSalesOffices(@RequestParam("regionId") String regionId) {
        List<SalesOffice> offices = new ArrayList<>();
        try {
            offices = salesOfficeRepository.findByRegion(regionId);
            return new ResponseEntity<>(offices, HttpStatus.ACCEPTED);
        } catch (Exception e) {
            logger.error("Failed to get sales offices" + e.getMessage());
            e.printStackTrace();
            return new ResponseEntity<>(offices, HttpStatus.CONFLICT);
        }
    }

    @GetMapping("/getallfactories")
    @ResponseBody
    public ResponseEntity<List<Factory>> getFactories() {
        List<Factory> factories = new ArrayList<>();
        try {
            factories = factoryRepository.findAll();
            return new ResponseEntity<>(factories, HttpStatus.ACCEPTED);
        } catch(Exception e) {
            logger.error("Failed to get list of all factories.");
            return new ResponseEntity<>(factories, HttpStatus.CONFLICT);
        }
    }

    @GetMapping("/initialize")
    @ResponseBody
    public ResponseEntity<String> initialize() {
        try {
            return new ResponseEntity<String>("Initialized!", HttpStatus.OK);
        } catch (Exception e) {
            logger.error("Failed to get sales offices" + e.getMessage());
            e.printStackTrace();
            return new ResponseEntity<String>("Unexpected request!", HttpStatus.BAD_REQUEST);
        }
    }

    @GetMapping("/getwhitelist")
    @ResponseBody
    public ResponseEntity<List<WhiteList>> getWhilteList() {
        List<WhiteList> ipList = new ArrayList<>();
        try {
            ipList = whiteListRepository.findAll();
            return new ResponseEntity<>(ipList, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(ipList, HttpStatus.CONFLICT);
        }
    }

    @PreAuthorize("hasRole('ROLE_pa-developer')")
    @GetMapping("/auditdocuments")
    public ResponseEntity<JSONObject> auditDocuments() {
        try {
            // List<PurchaseAgreement> agreements = purchaseAgreementRepository.findAll();
            List<ChangeOrder> changeOrders = changeOrderRepository.findAll();
            List<String> statuses = new ArrayList<>();
            // audit all agreements
            /*for(PurchaseAgreement agreement : agreements) {
                logger.info("Auditing agreement " + agreement.getId());
                String status = agreement.getStatus();
                if(!statuses.contains(status)) {
                    statuses.add(status);
                }
                agreement.setStatus(this.swapStatus(status));
                purchaseAgreementRepository.save(agreement);
            }*/
            // audit all change orders
            for(ChangeOrder order: changeOrders) {
                logger.info("Auditing change order " + order.getId());
                String status = order.getStatus();
                logger.info("Status accessed");

                //if(!statuses.contains(status)) {
                //    statuses.add(status);
                //}
                logger.info("Status list check complete");

                try {
                    PurchaseAgreement agreement = purchaseAgreementRepository.findById(order.getPurchaseAgreementId()).orElseThrow(() -> new HttpStatusCodeException(HttpStatus.NOT_FOUND) {});
                    if(order.getLeadId() == null || order.getLeadId().equals("")) {
                        order.setLeadId(agreement.getLeadId());
                        logger.info("Added lead ID " + order.getLeadId() + " to change order ");
                        changeOrderRepository.save(order);
                    }


                    // order.setStatus(this.swapStatus(status));
                } catch (HttpStatusCodeException err) {
                    logger.warn("Deleting order");
                    changeOrderRepository.delete(order);
                }

            }


            return new ResponseEntity<>(new JsonResponse().JsonResponse("Success", statuses, HttpStatus.ACCEPTED), HttpStatus.ACCEPTED);
        } catch (Exception e) {
            return new ResponseEntity<>(new JsonResponse().JsonResponse("Fail", e.getMessage(), HttpStatus.CONFLICT), HttpStatus.CONFLICT);
        }
    }

    private String swapStatus(String _status) {
        switch(_status) {
            case "new":
            case "working":
            case "new - revised":
                logger.info("Converting " + _status + " to " + DocumentStates.IN_PROGRESS.name());
                return DocumentStates.IN_PROGRESS.name();
            case "":
            case "submitted":
            case "revised":
            case "revised with change order":
            case "REVISED":
                logger.info("Converting " + _status + " to " + DocumentStates.SUBMITTED.name());
                return DocumentStates.SUBMITTED.name();
            case "executed":
            case "void":
                logger.info("Converting " + _status + " to " + DocumentStates.EXECUTED.name());
                return DocumentStates.EXECUTED.name();
            case "closed":
            case "locked":
                logger.info("Converting " + _status + " to " + DocumentStates.CLOSED.name());
                return DocumentStates.CLOSED.name();
            default:
                logger.error("Status not found " + _status);
                return _status;
        }
    }


}
