package com.api.demo.controllers;

import com.api.demo.mongorepositories.applicationpackage.avalararequests.AvalaraSalesResponse;
import com.api.demo.mongorepositories.DocumentStates;
import com.api.demo.mongorepositories.applicationpackage.avalararequests.AvalaraResponseRepository;
import com.api.demo.mongorepositories.applicationpackage.changeorders.ChangeOrder;
import com.api.demo.mongorepositories.applicationpackage.changeorders.ChangeOrderRepository;
import com.api.demo.mongorepositories.applicationpackage.leads.Lead;
import com.api.demo.mongorepositories.applicationpackage.leads.LeadRepository;
import com.api.demo.mongorepositories.applicationpackage.purchaseagreements.PurchaseAgreement;
import com.api.demo.mongorepositories.applicationpackage.purchaseagreements.PurchaseAgreementRepository;
import com.api.demo.pricedata.repositories.models.ModelRepository;
import com.google.gson.Gson;
import com.microsoft.azure.spring.autoconfigure.aad.UserPrincipal;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.springframework.web.client.HttpStatusCodeException;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Base64;
import java.util.Map;

import static com.api.demo.controllers.DealStates.*;
import static com.api.demo.utilityfunctions.JsonResponse.JsonResponse;


@RestController
@RequestMapping("/avalara")
public class AvalaraController {
    private static Logger logger = LoggerFactory.getLogger(AvalaraController.class);
    private static final String USER_AGENT = "Mozilla/5.0";
    Gson g = new Gson();
    JSONParser parser = new JSONParser();

    private String avalaraURI;
    private String companyCode;
    private String avalaraAuthCode;
    private String addleadApiKey;

    @Autowired
    private PurchaseAgreementRepository purchaseAgreementRepository;
    @Autowired
    private ChangeOrderRepository changeOrderRepository;
    @Autowired
    private ModelRepository modelRepository;
    @Autowired
    private AvalaraResponseRepository avalaraResponseRepository;
    @Autowired
    private LeadRepository leadRepository;

    // resolve address
    @GetMapping("/getcompanylocations")
    @ResponseBody
    public ResponseEntity<JSONObject> getCompanyLocations() {
        // logger.info("getting company locations");
        // logger.info(addleadApiKey);
        try {
            URL url = new URL(avalaraURI + "/companies/" + companyCode + "/locations");
            System.out.println(url);
            HttpURLConnection avalaraConnection = (HttpURLConnection) url.openConnection();
            avalaraConnection.setRequestMethod("GET");
            avalaraConnection.setRequestProperty("User-Agent", USER_AGENT);
            avalaraConnection.setRequestProperty("Accept", "application/json");
            avalaraConnection.setRequestProperty("Content-Type", "application/json");
            System.out.println( Base64.getEncoder().encodeToString(avalaraAuthCode.getBytes()));
            avalaraConnection.setRequestProperty("Authorization", "Basic " + Base64.getEncoder().encodeToString(avalaraAuthCode.getBytes()));

            avalaraConnection.setConnectTimeout(5000);
            avalaraConnection.setReadTimeout(5000);

            avalaraConnection.setDoOutput(true);

            StringBuffer response = new StringBuffer();

            int responseCode = avalaraConnection.getResponseCode();
            logger.info("GET Response Code :: " + responseCode);
            try(BufferedReader br = new BufferedReader(
                    new InputStreamReader(avalaraConnection.getInputStream(), "utf-8"))) {
                String responseLine = null;
                while ((responseLine = br.readLine()) != null) {
                    response.append(responseLine.trim());
                }
                System.out.println(response.toString());
            }
            JSONObject jsonResponse = (JSONObject) parser.parse(response.toString());
            return new ResponseEntity<>(JsonResponse("Success", jsonResponse, HttpStatus.resolve(responseCode)), HttpStatus.CREATED);

        } catch (Exception e) {
            e.printStackTrace();
            System.out.println(e);
            logger.error(e.getMessage());
            return new ResponseEntity<>(JsonResponse("Error: " + e.getMessage() , e, HttpStatus.CONFLICT), HttpStatus.CONFLICT);

        }
    }

    // get company locations
    @PostMapping("/validateaddress")
    @ResponseBody
    public ResponseEntity<JSONObject> validateAddress(@RequestBody JSONObject address) {
        try {
            String line1 = java.net.URLEncoder.encode(address.get("line1").toString(), "UTF-8").replace("+", "%20");
            String city = java.net.URLEncoder.encode(address.get("city").toString(), "UTF-8").replace("+", "%20");
            String region = java.net.URLEncoder.encode(address.get("region").toString(), "UTF-8").replace("+", "%20");
            String country = java.net.URLEncoder.encode(address.get("country").toString(), "UTF-8").replace("+", "%20");
            String postalCode = java.net.URLEncoder.encode(address.get("postalCode").toString(), "UTF-8").replace("+", "%20");

            try {
                URL url = new URL(avalaraURI + "/addresses/resolve?line1=" + line1 + "&city=" + city + "&region=" + region + "&country=" + country + "&postalCode=" + postalCode);
                HttpURLConnection avalaraConnection = (HttpURLConnection) url.openConnection();
                avalaraConnection.setRequestMethod("GET");
                // request properties

                avalaraConnection.setRequestProperty("User-Agent", USER_AGENT);
                avalaraConnection.setRequestProperty("Accept", "application/json");
                avalaraConnection.setRequestProperty("Content-Type", "application/json");
                avalaraConnection.setRequestProperty("Authorization", "Basic " + Base64.getEncoder().encodeToString(avalaraAuthCode.getBytes()));

                avalaraConnection.setConnectTimeout(20000);
                avalaraConnection.setReadTimeout(20000);

                avalaraConnection.setDoOutput(true);
                StringBuffer response = new StringBuffer();
                int responseCode = avalaraConnection.getResponseCode();
                try(BufferedReader br = new BufferedReader(
                        new InputStreamReader(avalaraConnection.getInputStream(), "utf-8"))) {
                    String responseLine = null;
                    while ((responseLine = br.readLine()) != null) {
                        response.append(responseLine.trim());
                    }
                    // System.out.println(response.toString());
                }
                JSONObject jsonResponse = (JSONObject) parser.parse(response.toString());
                return new ResponseEntity<>(JsonResponse("Success", jsonResponse, HttpStatus.resolve(responseCode)), HttpStatus.CREATED);

            } catch (Exception e) {
                e.printStackTrace();
                logger.error(e.getMessage());
                return new ResponseEntity<>(JsonResponse("Avalara Request Error", "content", HttpStatus.CONFLICT), HttpStatus.CONFLICT);

            }

        } catch (Exception e) {
            return new ResponseEntity<>(JsonResponse("Err", "content", HttpStatus.BAD_REQUEST), HttpStatus.BAD_REQUEST);

        }
    }

    // create transactions
    @PostMapping("/transaction")
    @ResponseBody
    public ResponseEntity<JSONObject> createTransaction(@RequestParam("docId") String docId, @RequestParam("docType") String docType, @RequestBody JSONObject newTransaction) {
        // logger.info(docId);
        // logger.info(docType);
        // logger.info("Getting current Auditor");

        String user = "";
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        try {
            UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
            // logger.info(userPrincipal.getName());
            Map<String, Object> map = userPrincipal.getClaims();
            // logger.info(map.toString());
            // logger.info(map.get("name").toString());
            user = map.get("name").toString();
        } catch (Exception e) {
            logger.warn("Could not access user from authentication for Avalara");
        }


        try {
            URL url = new URL(avalaraURI + "/transactions/create");
            System.out.println(url);
            HttpURLConnection avalaraConnection = (HttpURLConnection) url.openConnection();
            avalaraConnection.setRequestMethod("POST");

            // request properties
            avalaraConnection.setRequestProperty("User-Agent", USER_AGENT);
            avalaraConnection.setRequestProperty("Accept", "application/json");
            avalaraConnection.setRequestProperty("Content-Type", "application/json");
            avalaraConnection.setRequestProperty("Authorization", "Basic " + Base64.getEncoder().encodeToString(avalaraAuthCode.getBytes()));

            avalaraConnection.setConnectTimeout(5000);
            avalaraConnection.setReadTimeout(5000);

            // write transaction object
            avalaraConnection.setDoOutput(true);
            OutputStream os = avalaraConnection.getOutputStream();
            os.write(newTransaction.toString().getBytes());
            os.flush();
            os.close();

            StringBuffer response = new StringBuffer();

            int responseCode = avalaraConnection.getResponseCode();
            // logger.info("GET Response Code :: " + responseCode);
            try(BufferedReader br = new BufferedReader(
                    new InputStreamReader(avalaraConnection.getInputStream(), "utf-8"))) {
                String responseLine = null;
                while ((responseLine = br.readLine()) != null) {
                    response.append(responseLine.trim());
                }
            }
            JSONObject jsonResponse = (JSONObject) parser.parse(response.toString());



            String transactionType = newTransaction.get("type").toString();

            // save the response
            AvalaraSalesResponse avalaraResponse = new AvalaraSalesResponse();
            avalaraResponse.setDocumentId(docId);
            avalaraResponse.setDocumentType(docType);
            avalaraResponse.setUser(user);
            avalaraResponse.setRequestType(transactionType);
            avalaraResponse.setSerializedResponse(jsonResponse.toJSONString());
            avalaraResponseRepository.save(avalaraResponse);

            if(transactionType.equals("SalesOrder")) {
                // System.out.println("Sales order found");
                return new ResponseEntity<>(JsonResponse("Success", jsonResponse, HttpStatus.resolve(responseCode)), HttpStatus.CREATED);

            } else if(transactionType.equals("SalesInvoice")) {
                // System.out.println("Invoice found");
                // lock the agreement, save the agreement, and send back the updated agreement
                if(docType.equals("PA")) {
                    PurchaseAgreement purchaseAgreement = purchaseAgreementRepository.findById(docId).orElseThrow(() -> new HttpStatusCodeException(HttpStatus.NOT_FOUND) {});
                    // purchaseAgreement.closeAgreement();
                    purchaseAgreement.setStatus(DocumentStates.CLOSED.name());
                    purchaseAgreement.setTaxBreakdown(jsonResponse);
                    purchaseAgreementRepository.save(purchaseAgreement);

                    Lead lead = leadRepository.findById(purchaseAgreement.getLeadId()).orElseThrow(() -> new HttpStatusCodeException(HttpStatus.NOT_FOUND) {});
                    lead.setStatus(PURCHASE_AGREEMENT_CLOSED.name());
                    // save the updated objects
                    leadRepository.save(lead);

                    return new ResponseEntity<>(JsonResponse("Success", purchaseAgreement, HttpStatus.resolve(responseCode)), HttpStatus.CREATED);

                } else if(docType.equals("CO")) {
                    ChangeOrder changeOrder = changeOrderRepository.findById(docId).orElseThrow(() -> new HttpStatusCodeException(HttpStatus.NOT_FOUND) {});
                    // changeOrder.closeChangeOrder();
                    changeOrder.setStatus(DocumentStates.CLOSED.name());
                    changeOrder.setTaxBreakdown(jsonResponse);
                    changeOrderRepository.save(changeOrder);

                    Lead lead = leadRepository.findById(changeOrder.getLeadId()).orElseThrow(() -> new HttpStatusCodeException(HttpStatus.NOT_FOUND) {});
                    lead.setStatus(CHANGE_ORDER_CLOSED.name());
                    // save the updated objects
                    leadRepository.save(lead);

                    return new ResponseEntity<>(JsonResponse("Success", changeOrder, HttpStatus.resolve(responseCode)), HttpStatus.CREATED);
                } else {
                    return new ResponseEntity<>(JsonResponse("Success but unknown doc type", jsonResponse, HttpStatus.resolve(responseCode)), HttpStatus.resolve(responseCode));
                }
            } else {
                return new ResponseEntity<>(JsonResponse("Unknown response", jsonResponse, HttpStatus.resolve(responseCode)), HttpStatus.resolve(responseCode));
            }
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println(e);
            logger.error(e.getMessage());
            return new ResponseEntity<>(JsonResponse("Err - could not create transaction", "content", HttpStatus.CONFLICT), HttpStatus.CONFLICT);
        }
    }
}
