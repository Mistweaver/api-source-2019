package com.api.demo.controllers;

import com.api.demo.mongorepositories.applicationpackage.changeorders.ChangeOrder;
import com.api.demo.mongorepositories.applicationpackage.changeorders.ChangeOrderRepository;
import com.api.demo.mongorepositories.applicationpackage.deletedObjects.DeletedObject;
import com.api.demo.mongorepositories.applicationpackage.deletedObjects.DeletedObjectRepository;
import com.api.demo.mongorepositories.applicationpackage.leads.Lead;
import com.api.demo.mongorepositories.applicationpackage.leads.LeadRepository;
import com.api.demo.mongorepositories.applicationpackage.purchaseagreements.PurchaseAgreement;
import com.api.demo.mongorepositories.applicationpackage.purchaseagreements.PurchaseAgreementRepository;
import com.api.demo.mongorepositories.applicationpackage.salesoffices.SalesOffice;
import com.api.demo.mongorepositories.applicationpackage.salesoffices.SalesOfficeRepository;
import com.api.demo.security.utils.TokenUtility;
import com.api.demo.utilityfunctions.AuditLog;
import com.api.demo.utilityfunctions.JsonResponse;
import com.google.gson.Gson;
import org.json.simple.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.HttpStatusCodeException;

import java.util.List;

import static com.api.demo.controllers.DealStates.*;
import static com.api.demo.mongorepositories.applicationpackage.deletedObjects.DeletedObjectTypes.PURCHASE_AGREEMENT;

@RestController
@RequestMapping("/purchaseagreements")
public class PurchaseAgreementController {
    private static Logger logger = LoggerFactory.getLogger(PurchaseAgreementController.class);

    @Autowired
    private PurchaseAgreementRepository purchaseAgreementRepository;
    @Autowired
    private ChangeOrderRepository changeOrderRepository;
    @Autowired
    private DeletedObjectRepository deletedObjectRepository;
    @Autowired
    private LeadRepository leadRepository;
    @Autowired
    private SalesOfficeRepository salesOfficeRepository;

    @Autowired
    AuditLog auditLog;

    private TokenUtility tokenUtility = new TokenUtility();
    Gson gson = new Gson();

    @PostMapping("/new")
    @ResponseBody public ResponseEntity<JSONObject> createNewPurchaseAgreement(@RequestParam("leadId") String leadId, @RequestBody PurchaseAgreement newPurchaseAgreement) {
        try {
            // get all purchase agreements for the lead
            List<PurchaseAgreement> existingAgreements = purchaseAgreementRepository.findByLeadId(leadId);
            // if any are active, return a 409 (cannot create new purchase agreement until the old one is closed.  Use the manager create to do this)
            for(PurchaseAgreement agreement : existingAgreements) {
                // if the agreement has not been executed or closed, exit
                if(agreement.isAgreementInProgress() || agreement.isAgreementSubmitted()) {
                    return new ResponseEntity<>(JsonResponse.JsonResponse("Failed to create new agreement - existing agreement in progress or submitted",  agreement, HttpStatus.CONFLICT), HttpStatus.CONFLICT);
                }

                // check if there are any active change orders for the agreement
                List<ChangeOrder> previousChangeOrders = changeOrderRepository.findByPurchaseAgreementId(agreement.getId());
                // if previous change orders exist and are open, return a 409
                for(ChangeOrder order : previousChangeOrders) {
                    if(order.isChangeOrderInProgress() || order.isChangeOrderSubmitted()) {
                        return new ResponseEntity<>(JsonResponse.JsonResponse("Failed to create purchase agreement - existing change order in progress or submitted",  order, HttpStatus.CONFLICT), HttpStatus.CONFLICT);
                    }
                }
            }



            // get the lead
            Lead lead = leadRepository.findById(leadId).orElseThrow(() -> new HttpStatusCodeException(HttpStatus.NOT_FOUND) {});
            // get the sales office by its CLIENT CONSULTANT ID
            SalesOffice office = salesOfficeRepository.findByClientConsultantId(Integer.parseInt(lead.getLocationId()));

            // set the documentCode and customerCode from the lead and sales office respectively, and save
            newPurchaseAgreement.setCustomerCode(office.getLocationCode() + "-");
            newPurchaseAgreement.setDocumentCode(lead.getLeadId());

            purchaseAgreementRepository.save(newPurchaseAgreement);
            // update the lead status

            lead.setStatus(PURCHASE_AGREEMENT_IN_PROGRESS.name());
            leadRepository.save(lead);

            /**
             * Log the new agreement.
             */
            auditLog.log("/purchaseagreements/new", "POST", newPurchaseAgreement);

            return new ResponseEntity<>(JsonResponse.JsonResponse("New purchase agreement created", newPurchaseAgreement, HttpStatus.ACCEPTED), HttpStatus.ACCEPTED);
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("Error creating new purchase " + e.getMessage());
            return new ResponseEntity<>(JsonResponse.JsonResponse("Failed to create new purchase agreement",  e.getMessage(), HttpStatus.CONFLICT), HttpStatus.CONFLICT);
        }
    }

    @PreAuthorize("hasRole('ROLE_developer') or hasRole('ROLE_administrator')")
    @PostMapping("/override")
    @ResponseBody public ResponseEntity<JSONObject> overrideNewPurchaseAgreement(@RequestBody PurchaseAgreement newPurchaseAgreement) {
        try {
            purchaseAgreementRepository.save(newPurchaseAgreement);

            Lead lead = leadRepository.findById(newPurchaseAgreement.getLeadId()).orElseThrow(() -> new HttpStatusCodeException(HttpStatus.NOT_FOUND) {});
            lead.setStatus(PURCHASE_AGREEMENT_IN_PROGRESS.name());
            leadRepository.save(lead);

            /**
             * Log the agreement override.
             */
            auditLog.log("/purchaseagreements/override", "POST", newPurchaseAgreement);

            return new ResponseEntity<>(JsonResponse.JsonResponse("New purchase agreement created", newPurchaseAgreement, HttpStatus.ACCEPTED), HttpStatus.ACCEPTED);
        } catch (Exception e) {
            logger.error("Error creating new purchase " + e.getMessage());
            return new ResponseEntity<>(JsonResponse.JsonResponse("Failed to create new purchase agreement",  e.getMessage(), HttpStatus.CONFLICT), HttpStatus.CONFLICT);
        }
    }

    @PostMapping("/submit")
    @ResponseBody
    public ResponseEntity<JSONObject> submitPurchaseAgreement(@RequestBody JSONObject data) {
        try {
            PurchaseAgreement agreement = purchaseAgreementRepository.findById(data.get("agreementId").toString()).orElseThrow(() -> new HttpStatusCodeException(HttpStatus.NOT_FOUND) {});
            boolean success = agreement.submitAgreement();

            if(success) {
                purchaseAgreementRepository.save(agreement);

                Lead lead = leadRepository.findById(agreement.getLeadId()).orElseThrow(() -> new HttpStatusCodeException(HttpStatus.NOT_FOUND) {});
                lead.setStatus(PURCHASE_AGREEMENT_SUBMITTED.name());
                leadRepository.save(lead);

                /**
                 * Log the agreement submit.
                 */
                auditLog.log("/purchaseagreements/submit", "POST", agreement);

                return new ResponseEntity<>(JsonResponse.JsonResponse("Purchase Agreement Submitted", agreement, HttpStatus.ACCEPTED), HttpStatus.CREATED);
            }
            else {
                return new ResponseEntity<>(JsonResponse.JsonResponse("Denied - cannot submit agreement", "", HttpStatus.CONFLICT), HttpStatus.CONFLICT);
            }
        } catch (Exception e) {
            logger.error("Error submitting agreement: " + e.getMessage());
            return new ResponseEntity<>(JsonResponse.JsonResponse("Denied - unknown conflict", "", HttpStatus.CONFLICT), HttpStatus.CONFLICT);
        }
    }


    @PostMapping("/unsubmit")
    @ResponseBody
    public ResponseEntity<JSONObject> unsubmitPurchaseAgreement(@RequestHeader("Authorization") String token, @RequestBody JSONObject data) {
        try {
            List<String> roles = tokenUtility.returnMicrosoftRolesFromToken(token);
            // if user is not authorized (lower than a manager), reject the request
            boolean accessAllowed = false;
            for(String role: roles) {
                if(role.equals("MAN") || role.equals("ADMIN") || role.equals("DEV")) {
                    accessAllowed = true;
                }
            }

            if(accessAllowed) {
                PurchaseAgreement agreement = purchaseAgreementRepository.findById(data.get("agreementId").toString()).orElseThrow(() -> new HttpStatusCodeException(HttpStatus.NOT_FOUND) {});
                boolean success = agreement.unSubmitAgreement();
                if(success) {
                    purchaseAgreementRepository.save(agreement);
                    Lead lead = leadRepository.findById(agreement.getLeadId()).orElseThrow(() -> new HttpStatusCodeException(HttpStatus.NOT_FOUND) {});
                    lead.setStatus(PURCHASE_AGREEMENT_IN_PROGRESS.name());
                    leadRepository.save(lead);

                    /**
                     * Log the agreement unsubmit.
                     */
                    auditLog.log("/purchaseagreements/unsubmit", "POST", agreement);

                    return new ResponseEntity<>(JsonResponse.JsonResponse("Purchase Agreement Un-Submitted", agreement, HttpStatus.ACCEPTED), HttpStatus.CREATED);
                }
                else {
                    return new ResponseEntity<>(JsonResponse.JsonResponse("Denied - cannot un-submit agreement", "", HttpStatus.CONFLICT), HttpStatus.CONFLICT);
                }
            } else {
                return new ResponseEntity<>(JsonResponse.JsonResponse("Denied - permission not granted", "", HttpStatus.FORBIDDEN), HttpStatus.FORBIDDEN);
            }
        } catch (Exception e) {
            logger.error("Error un-submitting agreement: " + e.getMessage());
            return new ResponseEntity<>(JsonResponse.JsonResponse("Denied - unknown conflict", "", HttpStatus.CONFLICT), HttpStatus.CONFLICT);
        }
    }

    @PutMapping("/editagreement")
    @ResponseBody
    public ResponseEntity<JSONObject> editPurchaseAgreement(@RequestBody PurchaseAgreement purchaseAgreement) {
        try {
            PurchaseAgreement agreement = purchaseAgreementRepository.findById(purchaseAgreement.getId()).orElseThrow(() -> new HttpStatusCodeException(HttpStatus.NOT_FOUND) {});
            if(agreement.isAgreementInProgress()) {
                agreement = purchaseAgreementRepository.save(purchaseAgreement);
                return new ResponseEntity<>(JsonResponse.JsonResponse("Edit Accepted", agreement, HttpStatus.ACCEPTED), HttpStatus.CREATED);
            } else {
                return new ResponseEntity<>(JsonResponse.JsonResponse("Cannot edit agreement - unauthorized state", agreement, HttpStatus.CONFLICT), HttpStatus.CONFLICT);
            }
        } catch (Exception e) {
            return new ResponseEntity<>(JsonResponse.JsonResponse("Unknown error", "", HttpStatus.CONFLICT), HttpStatus.CONFLICT);
        }

    }

    /**
     * Function accepts an agreement ID as a request parameter, and returns the agreement, all associated changes orders,
     * any revised agreements, and the sales office.
     * @param agreementId       Id of the agreement
     * @return  JSONObject      Object containing each of the relevant documents for the requested agreement
     */
    @GetMapping("/getagreementdata")
    @ResponseBody
    public ResponseEntity<Object> getAgreementData(@RequestParam("agreementId") String agreementId) {
        try {
            JSONObject data = new JSONObject();
            /** Get all documents for an agreement ID ***/
            PurchaseAgreement purchaseAgreement;
            List<ChangeOrder> orders;
            SalesOffice office;

            try {
                purchaseAgreement = purchaseAgreementRepository.findById(agreementId).orElseThrow(() -> new HttpStatusCodeException(HttpStatus.NOT_FOUND) {});
            } catch (HttpStatusCodeException e) {
                return new ResponseEntity<>("Error: could not find purchase agreement: " + e.getMessage(), HttpStatus.NOT_FOUND);
            }

            // Get any change orders for the agreement
            orders = changeOrderRepository.findByPurchaseAgreementId(agreementId);
            // Get any revised agreements
            List<PurchaseAgreement> revisedAgreements = purchaseAgreementRepository.findByContractRevisedFrom(agreementId);

            try {
                office = salesOfficeRepository.findById(purchaseAgreement.getLocationId()).orElseThrow(() -> new HttpStatusCodeException(HttpStatus.NOT_FOUND) {});
            } catch (HttpStatusCodeException e) {
                return new ResponseEntity<>("Error: could not find locationId for purchase agreement: " + e.getMessage(), HttpStatus.NOT_FOUND);
            }


            data.put("agreement", purchaseAgreement);
            data.put("changeorders", orders);
            data.put("revisions", revisedAgreements);
            data.put("office", office);

            return new ResponseEntity<>(data, HttpStatus.ACCEPTED);
        } catch (Exception e) {
            return new ResponseEntity<>("Unknown error: " + e.getMessage(), HttpStatus.CONFLICT);
        }

    }

    @PostMapping("/reassign")
    @ResponseBody
    public ResponseEntity<JSONObject> reassignLead(@RequestBody JSONObject reassignmentData) {
        try {
            String agreementId = reassignmentData.get("agreementId").toString();
            String reassignedUserKey = reassignmentData.get("reassignedUserKey").toString();

            PurchaseAgreement agreement = purchaseAgreementRepository.findById(agreementId).orElseThrow(() -> new HttpStatusCodeException(HttpStatus.NOT_FOUND) {});
            agreement.setSalesPersonId(reassignedUserKey);
            purchaseAgreementRepository.save(agreement);

            /**
             * Log the agreement reassign.
             */
            auditLog.log("/purchaseagreements/reassign", "POST", agreement);

            return new ResponseEntity<>(JsonResponse.JsonResponse("Agreement reassigned", agreement, HttpStatus.ACCEPTED), HttpStatus.ACCEPTED);
        } catch (Exception e) {
            logger.error("Error reassigning agreement " + e.getMessage());
            return new ResponseEntity<>(JsonResponse.JsonResponse("Failed to reassign agreement",  e.getMessage(), HttpStatus.CONFLICT), HttpStatus.CONFLICT);
        }
    }

    @PostMapping("/revise")
    @ResponseBody
    public ResponseEntity<JSONObject> reviseAgreement(@RequestParam("agreementId") String agreementId, @RequestBody PurchaseAgreement revisedAgreement) {
        try {
            PurchaseAgreement agreementToRevise = purchaseAgreementRepository.findById(agreementId).orElseThrow(() -> new HttpStatusCodeException(HttpStatus.NOT_FOUND) {});

            // check for any existing revisions or open purchase agreements
            List<PurchaseAgreement> existingRevisions = purchaseAgreementRepository.findByContractRevisedFrom(agreementId);
            // if an existing revision is present, exit
            if(existingRevisions.size() != 0) {
                return new ResponseEntity<>(JsonResponse.JsonResponse("Revision not created - existing revision present", revisedAgreement, HttpStatus.CONFLICT), HttpStatus.CONFLICT);
            }

            // if the agreement can be revised, continue.  Otherwise return
            if(agreementToRevise.canAgreementBeRevised()) {

                // update the revised agreement with relevant data
                agreementToRevise.setContractRevisedFrom(revisedAgreement.getId());
                // double check where this date comes from
                // agreementToRevise.setContractRevisedFromDate(revisedAgreement.getDate());

                // save the revised agreement
                revisedAgreement.initializeRevisedAgreementState();
                revisedAgreement = purchaseAgreementRepository.save(revisedAgreement);


                Lead lead = leadRepository.findById(agreementToRevise.getLeadId()).orElseThrow(() -> new HttpStatusCodeException(HttpStatus.NOT_FOUND) {});
                lead.setStatus(REVISED_AGREEMENT_IN_PROGRESS.name());
                leadRepository.save(lead);

                /**
                 * Log the agreement revise.
                 */
                auditLog.log("/purchaseagreements/revise", "POST", revisedAgreement);

                // return the revised agreement
                return new ResponseEntity<>(JsonResponse.JsonResponse("Agreement revised", revisedAgreement, HttpStatus.ACCEPTED), HttpStatus.CREATED);

            } else {
                return new ResponseEntity<>(JsonResponse.JsonResponse("Cannot revise agreement - invalid state", agreementToRevise, HttpStatus.CONFLICT), HttpStatus.CONFLICT);
            }

        } catch (Exception e) {
            logger.error("Error revising agreement " + e.getMessage());
            return new ResponseEntity<>(JsonResponse.JsonResponse("Failed to revise agreement",  e.getMessage(), HttpStatus.CONFLICT), HttpStatus.CONFLICT);
        }
    }
    @PostMapping("/createchangeorder")
    @ResponseBody public ResponseEntity<JSONObject> createChangeOrder(@RequestParam("agreementId") String agreementId) {
        System.out.println(agreementId);
        try {
            PurchaseAgreement agreementToCreateChangeOrderWith = purchaseAgreementRepository.findById(agreementId).orElseThrow(() -> new HttpStatusCodeException(HttpStatus.NOT_FOUND) {});

            // if the agreement is closed, only an admin should be able to create a new change order (currently controlled via client only
            // if the agreement is executed, your standard user should be able to create a new change order
            if(agreementToCreateChangeOrderWith.isAgreementExecuted() || agreementToCreateChangeOrderWith.isAgreementClosed()) {
                // check existing change orders
                List<ChangeOrder> previousChangeOrders = changeOrderRepository.findByPurchaseAgreementId(agreementId);
                // if previous change orders exist and are open, return a 409
                for(ChangeOrder order : previousChangeOrders) {
                    if(order.isChangeOrderInProgress() || order.isChangeOrderSubmitted()) {
                        return new ResponseEntity<>(JsonResponse.JsonResponse("Failed to create change order - open change order already exists",  order, HttpStatus.CONFLICT), HttpStatus.CONFLICT);
                    }
                }

                ChangeOrder newChangeOrder = new ChangeOrder();
                newChangeOrder.setPurchaseAgreementId(agreementId);
                newChangeOrder.setLeadId(agreementToCreateChangeOrderWith.getLeadId());
                newChangeOrder.setChangeOrderNumber(previousChangeOrders.size() + 1);
                newChangeOrder.setCustomerCode(agreementToCreateChangeOrderWith.getCustomerCode());

                changeOrderRepository.save(newChangeOrder);

                Lead lead = leadRepository.findById(agreementToCreateChangeOrderWith.getLeadId()).orElseThrow(() -> new HttpStatusCodeException(HttpStatus.NOT_FOUND) {});
                lead.setStatus(CHANGE_ORDER_IN_PROGRESS.name());
                leadRepository.save(lead);

                /**
                 * Log the new change order.
                 */
                auditLog.log("/purchaseagreements/createchangeorder", "POST", newChangeOrder);

                return new ResponseEntity<>(JsonResponse.JsonResponse("Change order created", newChangeOrder, HttpStatus.ACCEPTED), HttpStatus.ACCEPTED);
            } else {
                // if agreement is new or working, exit and return a 409
                return new ResponseEntity<>(JsonResponse.JsonResponse("Failed to create change order - invalid agreement state",  agreementToCreateChangeOrderWith, HttpStatus.CONFLICT), HttpStatus.CONFLICT);
            }
        } catch (Exception e) {
            logger.error("Error creating change order " + e.getMessage());
            return new ResponseEntity<>(JsonResponse.JsonResponse("Failed to create change order",  e.getMessage(), HttpStatus.CONFLICT), HttpStatus.CONFLICT);
        }
    }

    @PreAuthorize("hasRole('ROLE_developer') or hasRole('ROLE_administrator')")
    @DeleteMapping("/delete")
    @ResponseBody ResponseEntity<JSONObject> deleteAgreement(@RequestParam("agreementId") String agreementId) {
        // System.out.println(agreementId);
        try {
            PurchaseAgreement agreementToDelete = purchaseAgreementRepository.findById(agreementId).orElseThrow(() -> new HttpStatusCodeException(HttpStatus.NOT_FOUND) {});
            // System.out.println(agreementToDelete.toString());
            // check for any existing revisions from this agreement
            List<PurchaseAgreement> existingRevisions = purchaseAgreementRepository.findByContractRevisedFrom(agreementId);
            if(existingRevisions.size() != 0) {
                return new ResponseEntity<>(JsonResponse.JsonResponse("Failed to delete agreement - this agreement has existing revisions",  existingRevisions, HttpStatus.CONFLICT), HttpStatus.CONFLICT);
            }
            // check to see that the agreement is not executed
            // System.out.println(agreementToDelete.getStatus());
            if(agreementToDelete.isAgreementExecuted()) {
                return new ResponseEntity<>(JsonResponse.JsonResponse("Failed to delete agreement - agreement has been executed",  agreementToDelete, HttpStatus.CONFLICT), HttpStatus.CONFLICT);
            }

            // find any previous change orders tied to the agreement
            List<ChangeOrder> associatedChangeOrders = changeOrderRepository.findByPurchaseAgreementId(agreementId);
            if(associatedChangeOrders.size() != 0) {
                return new ResponseEntity<>(JsonResponse.JsonResponse("Failed to delete agreement - there are associated change orders with this agreement",  associatedChangeOrders, HttpStatus.CONFLICT), HttpStatus.CONFLICT);
            }

            DeletedObject deletedAgreement = new DeletedObject();
            deletedAgreement.setObjectId(agreementId);
            deletedAgreement.setObjectType(PURCHASE_AGREEMENT);
            deletedAgreement.setObject(gson.toJson(agreementToDelete));

            deletedObjectRepository.save(deletedAgreement);
            purchaseAgreementRepository.delete(agreementToDelete);

            /**
             * Log the deleted object.
             */
            auditLog.log("/purchaseagreements/delete", "DELETE", agreementToDelete);

            return new ResponseEntity<>(JsonResponse.JsonResponse("Agreement deleted", deletedAgreement, HttpStatus.ACCEPTED), HttpStatus.CREATED);
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("Error deleting agreement " + e.getMessage());
            return new ResponseEntity<>(JsonResponse.JsonResponse("Failed to delete agreement",  e.getMessage(), HttpStatus.CONFLICT), HttpStatus.CONFLICT);
        }
    }

    @PreAuthorize("hasRole('ROLE_developer') or hasRole('ROLE_administrator')")
    @DeleteMapping("/masterdelete")
    @ResponseBody ResponseEntity<JSONObject> masterDeleteAgreement(@RequestParam("agreementId") String agreementId) {
        try {
            PurchaseAgreement agreementToDelete = purchaseAgreementRepository.findById(agreementId).orElseThrow(() -> new HttpStatusCodeException(HttpStatus.NOT_FOUND) {});

            DeletedObject deletedAgreement = new DeletedObject();
            deletedAgreement.setObjectId(agreementId);
            deletedAgreement.setObjectType(PURCHASE_AGREEMENT);
            deletedAgreement.setObject(gson.toJson(agreementToDelete));

            deletedObjectRepository.save(deletedAgreement);
            purchaseAgreementRepository.delete(agreementToDelete);

            /**
             * Log the deleted object.
             */
            auditLog.log("/purchaseagreements/masterdelete", "DELETE", agreementToDelete);

            // delete all revisions
            List<PurchaseAgreement> existingRevisions = purchaseAgreementRepository.findByContractRevisedFrom(agreementId);
            for(PurchaseAgreement agreement : existingRevisions) {
                purchaseAgreementRepository.delete(agreement);

                /**
                 * Log the deleted object.
                 */
                auditLog.log("/purchaseagreements/masterdelete", "DELETE", agreement);
            }

            // delete all change orders
            List<ChangeOrder> associatedChangeOrders = changeOrderRepository.findByPurchaseAgreementId(agreementId);
            for(ChangeOrder changeOrder : associatedChangeOrders) {
                changeOrderRepository.delete(changeOrder);

                /**
                 * Log the deleted object.
                 */
                auditLog.log("/purchaseagreements/masterdelete", "DELETE", changeOrder);
            }

            return new ResponseEntity<>(JsonResponse.JsonResponse("Agreement deleted", deletedAgreement, HttpStatus.ACCEPTED), HttpStatus.CREATED);
        } catch (Exception e) {
            logger.error("Error deleting agreement (MASTER) " + e.getMessage());
            return new ResponseEntity<>(JsonResponse.JsonResponse("Failed to delete agreement",  e.getMessage(), HttpStatus.CONFLICT), HttpStatus.CONFLICT);
        }
    }
}
