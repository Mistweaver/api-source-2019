package com.api.demo.controllers;

import com.api.demo.mongorepositories.applicationpackage.changeorders.ChangeOrder;
import com.api.demo.mongorepositories.applicationpackage.changeorders.ChangeOrderRepository;
import com.api.demo.mongorepositories.applicationpackage.deletedObjects.DeletedObject;
import com.api.demo.mongorepositories.applicationpackage.deletedObjects.DeletedObjectRepository;
import com.api.demo.mongorepositories.applicationpackage.deletedObjects.DeletedObjectTypes;
import com.api.demo.mongorepositories.applicationpackage.leads.Lead;
import com.api.demo.mongorepositories.applicationpackage.leads.LeadRepository;
import com.api.demo.mongorepositories.applicationpackage.purchaseagreements.PurchaseAgreement;
import com.api.demo.mongorepositories.applicationpackage.purchaseagreements.PurchaseAgreementRepository;
import com.api.demo.utilityfunctions.AuditLog;
import com.api.demo.utilityfunctions.JsonResponse;
import com.google.gson.Gson;
import lombok.Getter;
import lombok.Setter;
import org.json.JSONArray;
import org.json.simple.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.HttpStatusCodeException;

import java.util.*;

@RestController
@RequestMapping("/leads")
public class LeadController {
    private static Logger logger = LoggerFactory.getLogger(LeadController.class);
    @Autowired
    private LeadRepository leadRepository;
    @Autowired
    private PurchaseAgreementRepository purchaseAgreementRepository;
    @Autowired
    private ChangeOrderRepository changeOrderRepository;
    @Autowired
    private DeletedObjectRepository deletedObjectRepository;
    @Autowired
	AuditLog auditLog;

    Gson gson = new Gson();


    @GetMapping("/search/searchfirstname")
    @ResponseBody
    public ResponseEntity<List<Lead>> findByFirstName(@RequestParam("firstName") String firstName) {
        List<Lead> leads = new ArrayList<>();
        try {
            leads = leadRepository.findByFirstNameLike(firstName);
            leads.addAll(leadRepository.findByLastNameLike(firstName));
            return new ResponseEntity<>(leads, HttpStatus.ACCEPTED);
        } catch (Exception e) {
            logger.error("Error finding customers " + e.getMessage());
            return new ResponseEntity<>(leads, HttpStatus.CONFLICT);
        }
    }

    @PostMapping("/reassignlead")
    @ResponseBody
    public ResponseEntity<JSONObject> reassignLead(@RequestBody JSONObject reassignmentData) {
        try {
            String leadId = reassignmentData.get("leadId").toString();
            String reassignedUserKey = reassignmentData.get("reassignedUserKey").toString();

            Lead lead = leadRepository.findById(leadId).orElseThrow(() -> new HttpStatusCodeException(HttpStatus.NOT_FOUND) {});
            lead.setUserId(reassignedUserKey);
            leadRepository.save(lead);

            /**
             * Log the lead reassignment
             */
            auditLog.log("/reassignlead", "POST", lead);

            return new ResponseEntity<>(JsonResponse.JsonResponse("Lead reassigned", lead, HttpStatus.ACCEPTED), HttpStatus.ACCEPTED);
        } catch (Exception e) {
            logger.error("Error reassigning lead " + e.getMessage());
            return new ResponseEntity<>(JsonResponse.JsonResponse("Failed to reassign lead",  e.getMessage(), HttpStatus.CONFLICT), HttpStatus.CONFLICT);
        }
    }

    @GetMapping("/search/searchlastname")
    @ResponseBody
    public ResponseEntity<List<Lead>> findByLastName(@RequestParam("lastName") String lastName) {
        List<Lead> leads = new ArrayList<>();
        try {
            leads = leadRepository.findByFirstNameLike(lastName);
            leads.addAll(leadRepository.findByLastNameLike(lastName));
            return new ResponseEntity<>(leads, HttpStatus.ACCEPTED);
        } catch (Exception e) {
            logger.error("Error finding customers " + e.getMessage());
            return new ResponseEntity<>(leads, HttpStatus.CONFLICT);
        }

    }


    /*** Helper classes for the get request below ***/

    @Getter
    @Setter
    private class Link {
        private PurchaseAgreement agreement;
        private List<ChangeOrder> changeOrders;
        private boolean revised;

        public Link(PurchaseAgreement _agreement) {
            this.agreement = _agreement;
            this.changeOrders = new ArrayList<>();
            this.revised = false;
        }
    }

    @Getter
    @Setter
    private class DocumentChain {
        private LinkedList<Link> documents;
        public DocumentChain(LinkedList<Link> list) {
            this.documents = list;
        }
    }


    @GetMapping("/contracts/leadId")
    @ResponseBody
    public ResponseEntity<Object> getLeadContractsByLeadId(@RequestParam("leadId") String leadId) {

        try {
            Lead lead = leadRepository.findById(leadId).orElseThrow(() -> new HttpStatusCodeException(HttpStatus.NOT_FOUND) {});
            return buildDocumentChains(lead);

        } catch (Exception e) {
            logger.error("Could not build deals for lead id: " + leadId);
            logger.error(e.getMessage());
            return new ResponseEntity<>(e.getMessage(), HttpStatus.CONFLICT);
        }
    }

    @GetMapping("/contracts/ccId")
    @ResponseBody
    public ResponseEntity<Object> getLeadContractsByCCID(@RequestParam("ccId") String ccId) {

        try {
            Lead lead = leadRepository.findByLeadId(ccId);
            return buildDocumentChains(lead);

        } catch (Exception e) {
            logger.error("Could not build deals for lead id: " + ccId);
            logger.error(e.getMessage());
            return new ResponseEntity<>(e.getMessage(), HttpStatus.CONFLICT);
        }
    }

    public ResponseEntity buildDocumentChains(Lead lead) {
        JSONObject data = new JSONObject();
        // get the lead object first

        // get all agreements for the lead
        List<PurchaseAgreement> agreements;
        agreements = purchaseAgreementRepository.findByLeadId(lead.getId());

        // create a list of links for root contracts
        List<Link> links = new ArrayList<>();
        List<Link> lostRecords = new ArrayList<>();

        for(PurchaseAgreement agreement: agreements) {
            // create a new link
            Link link = new Link(agreement);
            // get all the change orders for the agreement, if any
            link.setChangeOrders(changeOrderRepository.findByPurchaseAgreementId(agreement.getId()));
            // add to the links list
            links.add(link);
        }

        // initialize list of chains
        List<LinkedList<Link>> chains = new ArrayList<>();

        // DEBUG
            /*System.out.println("Links: " + links.size());
            System.out.print("[ ");
            for(Link link : links) {
                System.out.print("( " + link.getAgreement().getId() + ", " + link.getAgreement().getContractRevisedFrom() + " ), ");
            }
            System.out.print("]");
            System.out.println();

            System.out.println("Chains: " + chains.size());

            for(LinkedList<Link> chain : chains) {
                System.out.print("[ ");
                for(Link link : chain) {
                    System.out.print("( " + link.getAgreement().getId() + ", " + link.getAgreement().getContractRevisedFrom() + " ), ");
                }
                System.out.print("]");
                System.out.println();
            }*/


        // remove the root nodes for contracts
        int x = 0;
        while(x < links.size()) {
            Link link = links.get(x);
            // is the link a root document?
            if(link.getAgreement().getContractRevisedFrom().isEmpty()) {
                // this is a root document.  make a new chain and add the link, remove it from the links list
                LinkedList<Link> chain = new LinkedList<>();
                chain.addFirst(links.remove(x));
                // add to the list of chains
                chains.add(chain);
                links.remove(link);
                x = 0;
            } else {
                x++;
            }
        }

        // keep track of the number of iterations.  If it is greater than 100, we exit the loop and report an error
        int iterations = 0;
        // at this point the only remaining links should be revised contracts.  Search each chain for its node until all links have been assigned
        while(links.size() > 0) {
            for (int i = 0; i < links.size(); i++) {
                Link link = links.get(i);
                // search each chain
                for(LinkedList<Link> chain : chains) {
                    // search each link in the chain
                    for(int j = 0; j < chain.size(); j++) {
                        Link currentLink = chain.get(j);
                        if(link.getAgreement().getContractRevisedFrom().equals(currentLink.getAgreement().getId())) {
                            // if matched, insert this link into the chain
                            chain.add(j + 1, link);
                            // remove link from list
                            links.remove(link);
                        }
                    }
                }
            }
            iterations++;
            if(iterations > 100) {
                lostRecords = links;
                links = new ArrayList<>();
            }
        }



        // DEBUG
            /*System.out.println("Links: " + links.size());
            System.out.print("[ ");
            for(Link link : links) {
                System.out.print("( " + link.getAgreement().getId() + ", " + link.getAgreement().getContractRevisedFrom() + " ), ");
            }
            System.out.print("]");
            System.out.println();

            System.out.println("Chains: " + chains.size());

            for(LinkedList<Link> chain : chains) {
                System.out.print("[ ");
                for(Link link : chain) {
                    System.out.print("( " + link.getAgreement().getId() + ", " + link.getAgreement().getContractRevisedFrom() + " ), ");
                }
                System.out.print("]");
                System.out.println();
            }*/

        List<DocumentChain> documentChains = new ArrayList<>();
        for(LinkedList<Link> chain : chains) {
            // now for each chain, if there exists more than one link in the chain, then mark all links except the last one as revised
            if(chain.size() > 1) {
                for(int j = 0; j < chain.size() - 1; j++) {
                    chain.get(j).setRevised(true);
                }
            }
            // then add the chain to the document chain array
            documentChains.add(new DocumentChain(chain));
        }

        if(iterations > 100) {
            logger.error("Iteration Max Reached: " + lead.getId());
            data.put("lead", lead);
            data.put("deals", documentChains);
            data.put("lost", lostRecords);
            return new ResponseEntity<>(data, HttpStatus.CONFLICT);
        } else {
            data.put("lead", lead);
            data.put("deals", documentChains);
            return new ResponseEntity<>(data, HttpStatus.ACCEPTED);
        }
    }

    @GetMapping("/master")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> getLeadMasterObject(
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

    @GetMapping("/master/search/findByName")
    @ResponseBody
    public ResponseEntity<JSONArray> getLeadMasterObjectByName(
            @RequestParam("locationId") String locationId,
            @RequestParam("userId") String userId,
            @RequestParam("name") String name) {

        JSONArray pageList = new JSONArray();


        /********* Parse sort property and direction ***********/
        // Initialize new sort properties
        Sort.Direction sortDirection = Sort.Direction.DESC;
        String sortProperty = "creationTime";



        // List<Lead> leads = new ArrayList<>();
        List<Lead> masterLeadList = new ArrayList<>();
        try {
            if(locationId != null && !locationId.equals("")) {
                if(userId != null && !userId.equals("")) {
                    masterLeadList.addAll(leadRepository.findByLocationIdAndUserIdAndLastNameLike(locationId, userId, name));
                    masterLeadList = leadRepository.findByLocationIdAndUserIdAndFirstNameLike(locationId, userId, name);
                } else {
                    masterLeadList = leadRepository.findByLocationIdAndFirstNameLike(locationId, name);
                    masterLeadList = leadRepository.findByLocationIdAndLastNameLike(locationId, name);
                }
            } else {
                masterLeadList = leadRepository.findByFirstNameLike(name);
                masterLeadList = leadRepository.findByLastNameLike(name);

            }

            for(Lead lead : masterLeadList) {
                JSONObject contracts = buildLeadDocuments(lead);
                pageList.put(contracts);
            }
            return new ResponseEntity<>(pageList, HttpStatus.ACCEPTED);
        } catch(Exception e) {
            logger.error("Error: could not build page contracts");
            return new ResponseEntity<>(pageList, HttpStatus.ACCEPTED);
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

    @PreAuthorize("hasRole('ROLE_developer') or hasRole('ROLE_administrator')")
    @DeleteMapping("/delete")
    @ResponseBody ResponseEntity<JSONObject> deleteAgreement(@RequestParam("leadId") String leadId) {
        try {

            Lead lead = leadRepository.findById(leadId).orElseThrow(() -> new HttpStatusCodeException(HttpStatus.NOT_FOUND) {});

            // check for any existing agreements from this lead
            List<PurchaseAgreement> agreements = purchaseAgreementRepository.findByLeadId(leadId);
            if(agreements.size() != 0) {
                return new ResponseEntity<>(JsonResponse.JsonResponse("Failed to delete lead - there are existing contracts with this lead",  agreements, HttpStatus.CONFLICT), HttpStatus.CONFLICT);
            }

            DeletedObject deletedLead = new DeletedObject();
            deletedLead.setObjectId(leadId);
            deletedLead.setObjectType(DeletedObjectTypes.LEAD);
            deletedLead.setObject(gson.toJson(lead));

            deletedObjectRepository.save(deletedLead);
            leadRepository.delete(lead);

            return new ResponseEntity<>(JsonResponse.JsonResponse("Lead deleted", deletedLead, HttpStatus.ACCEPTED), HttpStatus.CREATED);
        } catch (Exception e) {
            logger.error("Error deleting lead " + e.getMessage());
            return new ResponseEntity<>(JsonResponse.JsonResponse("Failed to delete lead",  e.getMessage(), HttpStatus.CONFLICT), HttpStatus.CONFLICT);
        }
    }

    @PreAuthorize("hasRole('ROLE_developer') or hasRole('ROLE_administrator')")
    @DeleteMapping("/masterdelete")
    @ResponseBody ResponseEntity<JSONObject> masterDeleteAgreement(@RequestParam("leadId") String leadId) {
        try {

            Lead lead = leadRepository.findById(leadId).orElseThrow(() -> new HttpStatusCodeException(HttpStatus.NOT_FOUND) {});


            DeletedObject deletedLead = new DeletedObject();
            deletedLead.setObjectId(leadId);
            deletedLead.setObjectType(DeletedObjectTypes.LEAD);
            deletedLead.setObject(gson.toJson(lead));

            deletedObjectRepository.save(deletedLead);
            leadRepository.delete(lead);

            // delete all purchase agreements
            List<PurchaseAgreement> existingPurchaseAgreements = purchaseAgreementRepository.findByLeadId(leadId);
            for(PurchaseAgreement agreement : existingPurchaseAgreements) {
                // find all change orders for each agreement and delete them as well
                List<ChangeOrder> associatedChangeOrders = changeOrderRepository.findByPurchaseAgreementId(agreement.getId());
                for(ChangeOrder changeOrder : associatedChangeOrders) {
                    changeOrderRepository.delete(changeOrder);
                }

                purchaseAgreementRepository.delete(agreement);
            }

            return new ResponseEntity<>(JsonResponse.JsonResponse("Lead deleted", deletedLead, HttpStatus.ACCEPTED), HttpStatus.CREATED);
        } catch (Exception e) {
            logger.error("Error deleting lead (MASTER) " + e.getMessage());
            return new ResponseEntity<>(JsonResponse.JsonResponse("Failed to delete lead",  e.getMessage(), HttpStatus.CONFLICT), HttpStatus.CONFLICT);
        }
    }
}
