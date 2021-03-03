package com.api.demo.filestore;

import com.api.demo.controllers.DealStates;
import com.api.demo.filestore.service.FileStorageService;
import com.api.demo.mongorepositories.applicationpackage.changeorders.ChangeOrder;
import com.api.demo.mongorepositories.applicationpackage.changeorders.ChangeOrderRepository;
import com.api.demo.mongorepositories.applicationpackage.leads.Lead;
import com.api.demo.mongorepositories.applicationpackage.leads.LeadRepository;
import com.api.demo.mongorepositories.applicationpackage.purchaseagreements.PurchaseAgreement;
import com.api.demo.mongorepositories.applicationpackage.purchaseagreements.PurchaseAgreementRepository;
import com.api.demo.mongorepositories.filestore.StoredFile;
import com.api.demo.mongorepositories.filestore.StoredFileRepository;
import com.api.demo.utilityfunctions.AuditLog;
import org.apache.commons.io.IOUtils;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;


@RestController
public class FsController {

    // Logger
    private static Logger logger = LoggerFactory.getLogger(FsController.class);

    final private SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:sss'Z'");

    @Autowired
    StoredFileRepository storedFileRepository;

    @Autowired
    PurchaseAgreementRepository purchaseAgreementRepository;

    @Autowired
    ChangeOrderRepository changeOrderRepository;

    @Autowired
    LeadRepository leadRepository;

    @Value("${filestore.directory}")
    private String filestoreDirectory;

    @Autowired
    private FileStorageService fileStorageService;
    private MultipartFile file;

    @Autowired
    AuditLog auditLog;

    /**
     * REST API method to upload a file as part of a multi part form. Note: It must
     * have a valid purchase agreement id. Also, if the last modified date is empty,
     * the current date and time will be used.
     *
     * @param file the file to store in the file system
     * @param lastModified the last modified date of the file to be stored
     * @param agreementId the purchase agreement associated with the file
     * @return an HTTP response. "ACCEPTED" if successful, "BAD_REQUEST" if request paramenters missing or incomplete.
     */
    @PostMapping("/uploadFile")
    public ResponseEntity<Object> uploadFile(@RequestParam("file") MultipartFile file,
                                        @RequestParam("lastModified") String lastModified,
                                        @RequestParam("agreementId") String agreementId) {

        logger.info("/uploadFile");

        try {
            /**
             * Make sure the request is valid. It must have at least one reference ID.
             */
            boolean badRequest = true;
            if (!agreementId.isEmpty()) {
                badRequest = false;
            }
            if(badRequest == true) {
                logger.warn("No ID reference set for this file!");
                return new ResponseEntity("No ID reference set for this file!", HttpStatus.BAD_REQUEST);
            }

            /**
             * Create the StoredFile object and populate it with the file metadata.
             */
            StoredFile storedFile = new StoredFile();
            storedFile.setContentType(file.getContentType());
            storedFile.setFileName(file.getOriginalFilename());
            storedFile.setFileSize((int)file.getSize());
            storedFile.setAgreementId(agreementId);

            /**
             * If no last modified date, us the current date and time.
             */
            if(lastModified.isEmpty()) {
                storedFile.setDateTimeLastModified(new Date());
            } else {
                storedFile.setDateTimeLastModified(new Date(Long.parseLong(lastModified)));
            }

            /**
             * Store the uploaded file in the file system.
             */
            String storedFileUri = fileStorageService.storeFile(file);


            /**
             * Assuming the file was written because the method fell through to this point,
             * create the StoredFile object in the database and point it to the stored file.
             */
            storedFile.setFileUri(storedFileUri);
            storedFileRepository.insert(storedFile);


            auditLog.log("/uploadFile", "POST", storedFile);

            /**
             * File stored and database updated successfully. Return a success response.
             */
            JSONObject responseMessage = new JSONObject();
            responseMessage.put("message", "Upload successful!");
            return new ResponseEntity(responseMessage.toString(), HttpStatus.ACCEPTED);

        } catch (Exception e) {
            logger.error("Failure to upload file: " + e.getMessage());
            return new ResponseEntity(e.getMessage(), HttpStatus.CONFLICT);
        }
    }

    @PostMapping("/uploadSignedAgreement")
    public ResponseEntity<Object> uploadSignedAgreement(@RequestParam("file") MultipartFile file,
                                        @RequestParam("lastModified") String lastModified,
                                        @RequestParam("agreementId") String agreementId) {
        try {
            /**
             * Update Purchase Agreement State to EXECUTED
             */
            PurchaseAgreement agreement = purchaseAgreementRepository.findById(agreementId).orElseThrow(() -> new HttpStatusCodeException(HttpStatus.NOT_FOUND) {});
            boolean success = agreement.executeAgreement();

            if(success) {
                /**
                 * Make sure the request is valid. It must have at least one reference ID.
                 */
                boolean badRequest = true;
                if (!agreementId.isEmpty()) {
                    badRequest = false;
                }
                if(badRequest == true) {
                    logger.warn("No ID reference set for this file!");
                    return new ResponseEntity("No ID reference set for this file!", HttpStatus.BAD_REQUEST);
                }

                /**
                 * Create the StoredFile object and populate it with the file metadata.
                 */
                StoredFile storedFile = new StoredFile();
                storedFile.setContentType(file.getContentType());
                storedFile.setFileName(file.getOriginalFilename());
                storedFile.setFileSize((int)file.getSize());
                storedFile.setAgreementId(agreementId);

                /**
                 * If no last modified date, us the current date and time.
                 */
                if(lastModified.isEmpty()) {
                    storedFile.setDateTimeLastModified(new Date());
                } else {
                    storedFile.setDateTimeLastModified(new Date(Long.parseLong(lastModified)));
                }

                /**
                 * Store the uploaded file in the file system.
                 */
                String storedFileUri = fileStorageService.storeFile(file);


                /**
                 * Assuming the file was written because the method fell through to this point,
                 * create the StoredFile object in the database and point it to the stored file.
                 */
                storedFile.setFileUri(storedFileUri);
                storedFileRepository.insert(storedFile);

                // update the lead
                Lead lead = leadRepository.findById(agreement.getLeadId()).orElseThrow(() -> new HttpStatusCodeException(HttpStatus.NOT_FOUND) {});
                lead.setStatus(DealStates.PURCHASE_AGREEMENT_EXECUTED.name());
                // save the updated objects
                leadRepository.save(lead);
                agreement = purchaseAgreementRepository.save(agreement);

                /**
                 * Log the file upload event.
                 */
                auditLog.log("/uploadSignedAgreement", "POST", storedFile);

                /**
                 * File stored and database updated successfully. Return a success response.
                 */
                return new ResponseEntity(agreement, HttpStatus.ACCEPTED);
            } else {
                return new ResponseEntity("Could not upload signed document: invalid agreement state", HttpStatus.CONFLICT);
            }


        } catch (Exception e) {
            logger.error("Failure to upload file: " + e.getMessage());
            return new ResponseEntity(e.getMessage(), HttpStatus.CONFLICT);
        }

    }

    @PostMapping("/uploadSignedChangeOrder")
    public ResponseEntity<Object> uploadSignedChangeOrder(@RequestParam("file") MultipartFile file,
                                                   @RequestParam("lastModified") String lastModified,
                                                   @RequestParam("changeOrderId") String changeOrderId) {

        try {
            /**
             * Update Purchase Agreement State to EXECUTED
             */
            System.out.println("Change order id");
            System.out.println(changeOrderId);
            ChangeOrder changeOrder = changeOrderRepository.findById(changeOrderId).orElseThrow(() -> new HttpStatusCodeException(HttpStatus.NOT_FOUND) {});
            boolean success = changeOrder.executeChangeOrder();
            System.out.println("Change order changed: " + success);
            if(success) {
                /**
                 * Make sure the request is valid. It must have at least one reference ID.
                 */
                boolean badRequest = true;
                if (!changeOrderId.isEmpty()) {
                    badRequest = false;
                }
                if(badRequest == true) {
                    logger.warn("No ID reference set for this file!");
                    return new ResponseEntity("No ID reference set for this file!", HttpStatus.BAD_REQUEST);
                }

                /**
                 * Create the StoredFile object and populate it with the file metadata.
                 */
                StoredFile storedFile = new StoredFile();
                storedFile.setContentType(file.getContentType());
                storedFile.setFileName(file.getOriginalFilename());
                storedFile.setFileSize((int)file.getSize());
                storedFile.setAgreementId(changeOrderId);

                /**
                 * If no last modified date, us the current date and time.
                 */
                if(lastModified.isEmpty()) {
                    storedFile.setDateTimeLastModified(new Date());
                } else {
                    storedFile.setDateTimeLastModified(new Date(Long.parseLong(lastModified)));
                }

                /**
                 * Store the uploaded file in the file system.
                 */
                String storedFileUri = fileStorageService.storeFile(file);


                /**
                 * Assuming the file was written because the method fell through to this point,
                 * create the StoredFile object in the database and point it to the stored file.
                 */
                storedFile.setFileUri(storedFileUri);
                storedFileRepository.insert(storedFile);

                // update the lead
                System.out.println("Updating lead");
                System.out.println(changeOrder.getLeadId());
                Lead lead = leadRepository.findById(changeOrder.getLeadId()).orElseThrow(() -> new HttpStatusCodeException(HttpStatus.NOT_FOUND) {});
                lead.setStatus(DealStates.CHANGE_ORDER_EXECUTED.name());
                // save the updated objects
                leadRepository.save(lead);
                changeOrder = changeOrderRepository.save(changeOrder);

                /**
                 * Log the file upload event.
                 */
                auditLog.log("/uploadSignedChangeOrder", "POST", storedFile);

                return new ResponseEntity(changeOrder, HttpStatus.ACCEPTED);

            } else {
                return new ResponseEntity("Could not upload signed document: invalid change order state", HttpStatus.CONFLICT);
            }


        } catch (Exception e) {
            logger.error("Failure to upload file: " + e.getMessage());
            return new ResponseEntity(e.getMessage(), HttpStatus.CONFLICT);
        }


    }


    /**
     * REST API method to upload the shipping directions map file as part of a multi part form.
     * Note: It must have a valid purchase agreement id. Also, if the last modified date is
     * empty, the current date and time will be used.
     *
     * @param file the file to store in the file system
     * @param lastModified the last modified date of the file to be stored
     * @param agreementId the purchase agreement associated with the file
     * @return an HTTP response. "ACCEPTED" if successful, "BAD_REQUEST" if request paramenters missing or incomplete.
     */
    @PostMapping("/uploadMapFile")
    public ResponseEntity<?> uploadMapFile(@RequestParam("file") MultipartFile file,
                                        @RequestParam("lastModified") String lastModified,
                                        @RequestParam("agreementId") String agreementId) {

        logger.info("/uploadMapFile");

        try {
            /**
             * Make sure the request is valid. It must have an agreement reference ID.
             */
            boolean badRequest = true;
            if (!agreementId.isEmpty()) {
                badRequest = false;
            }
            if(badRequest == true) {
                logger.warn("No ID reference set for this file!");
                return new ResponseEntity("No ID reference set for this file!", HttpStatus.BAD_REQUEST);
            }

            /**
             * Create the StoredFile object and populate it with the file metadata.
             */
            StoredFile storedFile = new StoredFile();
            storedFile.setContentType(file.getContentType());
            storedFile.setFileName(file.getOriginalFilename());
            storedFile.setFileSize((int)file.getSize());
            storedFile.setAgreementId(agreementId);

            /**
             * If no last modified date, us the current date and time.
             */
            if(lastModified.isEmpty()) {
                storedFile.setDateTimeLastModified(new Date());
            } else {
                storedFile.setDateTimeLastModified(new Date(Long.parseLong(lastModified)));
            }

            /**
             * Store the uploaded file in the file system.
             */
            String storedFileUri = fileStorageService.storeFile(file);


            /**
             * Assuming the file was written because the method fell through to this point,
             * create the StoredFile object in the database and point it to the stored file.
             */
            storedFile.setFileUri(storedFileUri);
            storedFileRepository.insert(storedFile);

            /**
             * Log the file upload event.
             */
            auditLog.log("/uploadMapFile", "POST", storedFile);

            /**
             * File stored and database updated successfully. Return a success response.
             */
            JSONObject responseMessage = new JSONObject();
            responseMessage.put("message", "Upload successful!");
            responseMessage.put("fileUri", storedFileUri);
            return new ResponseEntity(responseMessage.toString(), HttpStatus.ACCEPTED);

        } catch (Exception e) {
            logger.error("Failure to upload file: " + e.getMessage());
            return new ResponseEntity(e.getMessage(), HttpStatus.CONFLICT);
        }
    }

    /**
     * REST API method to download the stored file specified by the file URI in the
     * path variable.
     *
     * @param uri the file URI of the file to be downloaded
     * @param request the HTTP file download request
     * @return the HTTP repsonse reporting the success or failure of the request
     */
    @GetMapping("/downloadFile/{uri:.+}")
    public ResponseEntity<Resource> downloadFile(@PathVariable String uri, HttpServletRequest request) {
        /**
         * Load the stored file as Resource object.
         */
        Resource resource = fileStorageService.loadFileAsResource(uri);

        /**
         * Determine file's content type.
         */
        String contentType = null;
        try {
            contentType = request.getServletContext().getMimeType(resource.getFile().getAbsolutePath());
        } catch (IOException ex) {
            logger.info("Could not determine file type.");
        }

        /**
         * If the file content type cannot be determined, use the default content type.
         */
        if(contentType == null) {
            contentType = "application/octet-stream";
        }

        /**
         * Log the file download event.
         */
        try {
            auditLog.log("/downloadFile/" + uri, "GET", storedFileRepository.findByFileUri(uri));
        } catch (org.json.simple.parser.ParseException e) {
            logger.error("Failed to log the file download attempt: " + e.getMessage());
        }

        /**
         * Download the loaded resource, and report success or failure.
         */
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(contentType))
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + getFileNameFromUri(resource.getFilename()) + "\"")
                .body(resource);
    }


    private String getFileNameFromUri(String uri) {
        StringBuffer fileName = new StringBuffer();

        String[] fn = uri.split("\\.");

        for(int i=1; i<fn.length; i++) {
            fileName.append(fn[i]);
            if(i < fn.length-1) {
                fileName.append(".");
            }
        }

        String result = "";

        try {
            result = java.net.URLDecoder.decode(fileName.toString(), StandardCharsets.UTF_8.name());
        } catch (UnsupportedEncodingException e) {
            logger.warn("Couldn't URL decode filename string: " + fileName.toString());
            result = fileName.toString();
        }
        logger.info("Extracted file name: " + result);

        return result;
    }

    @PostMapping(path = "/filestore", consumes = "application/json")
    public ResponseEntity<?> saveFile(@RequestBody String jsonBody) {

        logger.info("Single file upload...");
        // logger.info("JSON Body: " + jsonBody.toString());
        JSONObject JSONbody = new JSONObject(jsonBody);

        String contentBytes = JSONbody.getString("contentBytes");
        if (contentBytes == null || contentBytes.isEmpty()) {
            return new ResponseEntity("No file data detected!", HttpStatus.NO_CONTENT);
        }
        /**
         * Verify there is a reference to a customer, lead, or policy. If not, return a
         * BAD_REQUEST status.
         */
        String customerId;
        String insuranceLeadId;
        String policyId;

        boolean badRequest = true;
        try {
            customerId = JSONbody.getString("customerId");
            if(!customerId.isEmpty()) {
                badRequest = false;
            }
        } catch(JSONException je) {
            customerId = "";
            logger.warn("Customer Id empty");
        }
        try {
            insuranceLeadId = JSONbody.getString("insuranceLeadId");
            if(!insuranceLeadId.isEmpty()) {
                badRequest = false;
            }
        } catch(JSONException je) {
            insuranceLeadId = "";
            logger.warn("Insurance lead Id empty");
        }
        try {
            policyId = JSONbody.getString("policyId");
            if(!policyId.isEmpty()) {
                badRequest = false;
            }
        } catch(JSONException je) {
            policyId = "";
            logger.warn("Policy Id empty");
        }
        if(badRequest == true) {
            logger.warn("No ID reference set for this file!");
            return new ResponseEntity("No ID reference set for this file!", HttpStatus.BAD_REQUEST);
        }
        StoredFile storedFile = new StoredFile();
        storedFile.setContentType(JSONbody.getString("contentType"));
        storedFile.setFileName(JSONbody.getString("name"));
        storedFile.setFileSize(JSONbody.getInt("size"));
        // storedFile.setCustomerId(customerId);
        // storedFile.setPolicyId(policyId);
        // storedFile.setInsuranceLeadId(insuranceLeadId);

        Date dateLastModified;
        try {
            dateLastModified = formatter.parse(JSONbody.getString("dateTimeLastModified"));
        } catch(ParseException pe) {
            logger.warn("Provided date could not be parsed: " + JSONbody.getString("dateTimeLastModified"));
            logger.warn("Setting file last modified date to now!");
            dateLastModified = new Date();
        }
        storedFile.setDateTimeLastModified(dateLastModified);

        String storedFileUri;
        try {
            // Get the file and save it somewhere
            byte[] bytes = JSONbody.getString("contentBytes").getBytes();
            File directory = new File(filestoreDirectory);
            File f = File.createTempFile("File-", ".file", directory);
            FileOutputStream outputStream = new FileOutputStream(f.getAbsolutePath());
            outputStream.write(bytes);

            storedFileUri = f.getAbsolutePath();
        } catch(IOException ioe) {
            logger.warn("IO Exception on file write: " + ioe.getMessage());
            ioe.printStackTrace();
            return new ResponseEntity(ioe.getMessage(), HttpStatus.BAD_REQUEST);
        }

        /**
         * Assuming the file was written because the method fell through to this point,
         * create the StoredFile object and point it to the stored file.
         */
        storedFile.setFileUri(storedFileUri);

        storedFileRepository.insert(storedFile);
        logger.info("File created");
        JSONObject responseMessage = new JSONObject();
        responseMessage.put("message", "Upload successful!");
        return new ResponseEntity(responseMessage.toString(), HttpStatus.ACCEPTED);

    }

    @GetMapping("/filestore/{id}")
    public ResponseEntity<FileStore> retrieveFile(@PathVariable final String id) {

        StoredFile storedFile = storedFileRepository.findById(id).orElseThrow(() -> new HttpStatusCodeException(HttpStatus.NOT_FOUND) {});

        String fileName = storedFile.getFileUri();

        String contentBytesString;

        try {
            FileInputStream inputStream = new FileInputStream(fileName);
            byte[] b = IOUtils.toByteArray(inputStream);

            contentBytesString = new String(b, "UTF-8");
        } catch(FileNotFoundException fnfe) {
            logger.warn(fnfe.getMessage() + ": " + fileName);
            return new ResponseEntity(fnfe.getMessage(), HttpStatus.NOT_FOUND);
        } catch(IOException ioe) {
            return new ResponseEntity(ioe.getMessage(), HttpStatus.NOT_FOUND);
        }

        /**
         * Create the FileStore object to return.
         */
        FileStore fileStore = new FileStore();

        /**
         * Populate the FileStore object.
         */
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ");

        // fileStore.setCustomerId(storedFile.getCustomerId());
        // fileStore.setInsuranceLeadId(storedFile.getInsuranceLeadId());
        // fileStore.setPolicyId(storedFile.getPolicyId());
        fileStore.setName(storedFile.getFileName());
        fileStore.setContentType(storedFile.getContentType());
        fileStore.setSize(storedFile.getFileSize());
        fileStore.setDateTimeLastModified(dateFormat.format(storedFile.getDateTimeLastModified()));
        fileStore.setContentBytes(contentBytesString);

        fileStore.setCreationTime(dateFormat.format(storedFile.getCreationTime()));
        fileStore.setModificationTime(dateFormat.format(storedFile.getModificationTime()));

        return new ResponseEntity<FileStore>(fileStore, HttpStatus.OK);
    }


    /**
     * Return the request headers as a JSON object.
     * @param headers  the MAP of the headers of the request
     * @return the request headers in a JSONObject
     */
    private JSONObject getRequestHeaders(Map<String, String> headers) {
        JSONObject header = new JSONObject();
        for (Map.Entry<String, String> entry : headers.entrySet()) {
            try {
                header.put(entry.getKey(), entry.getValue());
                //logger.info("key: " + entry.getKey() + "  - value: " + entry.getValue());
            } catch (Exception e) {
                logger.error(e.getMessage());
            }
        }
        return header;
    }
}
