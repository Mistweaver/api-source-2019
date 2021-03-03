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
import static com.api.demo.mongorepositories.applicationpackage.deletedObjects.DeletedObjectTypes.CHANGE_ORDER;

@RestController
@RequestMapping("/changeorders")
public class ChangeOrderController {
    private static Logger logger = LoggerFactory.getLogger(ChangeOrderController.class);

    @Autowired
    private ChangeOrderRepository changeOrderRepository;
    @Autowired
    private DeletedObjectRepository deletedObjectRepository;
    @Autowired
    private LeadRepository leadRepository;
    @Autowired
    private PurchaseAgreementRepository purchaseAgreementRepository;
    @Autowired
    private SalesOfficeRepository salesOfficeRepository;

    private TokenUtility tokenUtility = new TokenUtility();
    Gson gson = new Gson();

    @GetMapping("/{id}")
    @ResponseBody
    public ResponseEntity<JSONObject> getChangeOrderData(@PathVariable String id) {
        JSONObject data = new JSONObject();
        ChangeOrder order = changeOrderRepository.findById(id).orElseThrow(() -> new HttpStatusCodeException(HttpStatus.NOT_FOUND) {});

        PurchaseAgreement purchaseAgreement = purchaseAgreementRepository.findById(order.getPurchaseAgreementId()).orElseThrow(() -> new HttpStatusCodeException(HttpStatus.NOT_FOUND) {});
        SalesOffice office = salesOfficeRepository.findById(purchaseAgreement.getLocationId()).orElseThrow(() -> new HttpStatusCodeException(HttpStatus.NOT_FOUND) {});
        data.put("agreement", purchaseAgreement);
        data.put("changeorder", order);
        data.put("salesoffice", office);
        return new ResponseEntity<>(data, HttpStatus.ACCEPTED);
    }

    @PutMapping("/{id}")
    @ResponseBody
    public ResponseEntity<JSONObject> saveChangeOrder(@RequestBody ChangeOrder changeOrder) {
        try {
            ChangeOrder order = changeOrderRepository.findById(changeOrder.getId()).orElseThrow(() -> new HttpStatusCodeException(HttpStatus.NOT_FOUND) {});
            if(order.isChangeOrderInProgress()) {
                order = changeOrderRepository.save(changeOrder);
                return new ResponseEntity<>(JsonResponse.JsonResponse("Edit Accepted", order, HttpStatus.ACCEPTED), HttpStatus.CREATED);
            } else {
                return new ResponseEntity<>(JsonResponse.JsonResponse("Cannot edit agreement - unauthorized state", order, HttpStatus.CONFLICT), HttpStatus.CONFLICT);
            }
        } catch (Exception e) {
            return new ResponseEntity<>(JsonResponse.JsonResponse("Unknown error", "", HttpStatus.CONFLICT), HttpStatus.CONFLICT);
        }
    }

    @PostMapping("/submit")
    @ResponseBody
    public ResponseEntity<JSONObject> submitChangeOrder(@RequestBody JSONObject data) {
        try {
            ChangeOrder changeOrder = changeOrderRepository.findById(data.get("changeOrderId").toString()).orElseThrow(() -> new HttpStatusCodeException(HttpStatus.NOT_FOUND) {});
            boolean success = changeOrder.submitChangeOrder();

            if(success) {
                changeOrderRepository.save(changeOrder);

                Lead lead = leadRepository.findById(changeOrder.getLeadId()).orElseThrow(() -> new HttpStatusCodeException(HttpStatus.NOT_FOUND) {});
                lead.setStatus(CHANGE_ORDER_SUBMITTED.name());
                leadRepository.save(lead);

                return new ResponseEntity<>(JsonResponse.JsonResponse("Change Order Submitted", changeOrder, HttpStatus.ACCEPTED), HttpStatus.CREATED);
            }
            else {
                return new ResponseEntity<>(JsonResponse.JsonResponse("Denied - cannot submit change order", "", HttpStatus.CONFLICT), HttpStatus.CONFLICT);
            }
        } catch (Exception e) {
            logger.error("Error submitting change order: " + e.getMessage());
            return new ResponseEntity<>(JsonResponse.JsonResponse("Denied - unknown conflict", "", HttpStatus.CONFLICT), HttpStatus.CONFLICT);
        }
    }

    @PostMapping("/unsubmit")
    @ResponseBody
    public ResponseEntity<JSONObject> unSubmitChangeOrder(@RequestHeader("Authorization") String token, @RequestBody JSONObject data) {
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
                ChangeOrder changeOrder = changeOrderRepository.findById(data.get("changeOrderId").toString()).orElseThrow(() -> new HttpStatusCodeException(HttpStatus.NOT_FOUND) {});
                boolean success = changeOrder.unSubmitChangeOrder();
                if(success) {
                    changeOrderRepository.save(changeOrder);
                    Lead lead = leadRepository.findById(changeOrder.getLeadId()).orElseThrow(() -> new HttpStatusCodeException(HttpStatus.NOT_FOUND) {});
                    lead.setStatus(CHANGE_ORDER_IN_PROGRESS.name());
                    leadRepository.save(lead);
                    return new ResponseEntity<>(JsonResponse.JsonResponse("Change Order Un-Submitted", changeOrder, HttpStatus.ACCEPTED), HttpStatus.CREATED);
                }
                else {
                    return new ResponseEntity<>(JsonResponse.JsonResponse("Denied - cannot un-submit change order", "", HttpStatus.CONFLICT), HttpStatus.CONFLICT);
                }
            } else {
                return new ResponseEntity<>(JsonResponse.JsonResponse("Denied - permission not granted", "", HttpStatus.FORBIDDEN), HttpStatus.FORBIDDEN);
            }
        } catch (Exception e) {
            logger.error("Error un-submitting change order: " + e.getMessage());
            return new ResponseEntity<>(JsonResponse.JsonResponse("Denied - unknown conflict", "", HttpStatus.CONFLICT), HttpStatus.CONFLICT);
        }
    }

    @PreAuthorize("hasRole('ROLE_developer') or hasRole('ROLE_administrator')")
    @DeleteMapping("/delete")
    @ResponseBody ResponseEntity<JSONObject> deleteChangeOrder(@RequestParam("changeOrderId") String changeOrderId) {
        try {

            ChangeOrder changeOrder = changeOrderRepository.findById(changeOrderId).orElseThrow(() -> new HttpStatusCodeException(HttpStatus.NOT_FOUND) {});



            DeletedObject deletedChangeOrder = new DeletedObject();
            deletedChangeOrder.setObjectId(changeOrderId);
            deletedChangeOrder.setObjectType(CHANGE_ORDER);
            deletedChangeOrder.setObject(gson.toJson(deletedChangeOrder));

            deletedObjectRepository.save(deletedChangeOrder);
            changeOrderRepository.delete(changeOrder);


            return new ResponseEntity<>(JsonResponse.JsonResponse("Change order deleted", deletedChangeOrder, HttpStatus.ACCEPTED), HttpStatus.CREATED);
        } catch (Exception e) {
            logger.error("Error deleting change order " + e.getMessage());
            return new ResponseEntity<>(JsonResponse.JsonResponse("Failed to delete change order",  e.getMessage(), HttpStatus.CONFLICT), HttpStatus.CONFLICT);
        }
    }

}
