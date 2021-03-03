package com.api.demo.mongorepositories.applicationpackage.purchaseagreements;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

import java.util.List;

@RepositoryRestResource(collectionResourceRel="purchaseagreements", path="purchaseagreements")
public interface PurchaseAgreementRepository extends MongoRepository<PurchaseAgreement, String> {

    Page<PurchaseAgreement> findBySalesPersonId(@Param("salesPersonId") String salesPersonId, Pageable pageable);
    List<PurchaseAgreement> findByLeadId(@Param("leadId") String leadId);
    Page<PurchaseAgreement> findByLocationId(@Param("locationId") String locationId, Pageable pageable);
    List<PurchaseAgreement> findByBuyer1Like(@Param("buyer1") String buyer1);
    List<PurchaseAgreement> findByBuyer2Like(@Param("buyer2") String buyer2);
    List<PurchaseAgreement> findByCustomerCode(@Param("customerCode") String customerCode);
    List<PurchaseAgreement> findByEmailAddress(@Param("email") String emailAddress);
    List<PurchaseAgreement> findByDeliveryState(@Param("deliveryState") String deliveryState);
    List<PurchaseAgreement> findByContractRevisedFrom(@Param("agreementId") String agreementId);
    Page<PurchaseAgreement> findByStatus(@Param("status") String status, Pageable pageable);
    List<PurchaseAgreement> findByModelType(@Param("modelType") String modelType);
    List<PurchaseAgreement> findByCrmLeadId(@Param("crmLeadId") String crmLeadId);

    // my agreements search functions
    PurchaseAgreement findBySalesPersonIdAndId(@Param("salesPersonId") String salesPersonId, @Param("id") String id);
    List<PurchaseAgreement> findBySalesPersonIdAndBuyer1Like(@Param("salesPersonId") String salesPersonId, @Param("buyer1") String buyer1);
    List<PurchaseAgreement> findBySalesPersonIdAndLeadId(@Param("salesPersonId") String salesPersonId, @Param("leadId") String leadId);
    List<PurchaseAgreement> findBySalesPersonIdAndDeliveryState(@Param("salesPersonId") String salesPersonId, @Param("deliveryState") String deliveryState);
    List<PurchaseAgreement> findBySalesPersonIdAndEmailAddress(@Param("salesPersonId") String salesPersonId, @Param("email") String emailAddress);


    // location agreement search functions
    List<PurchaseAgreement> findByLocationIdAndBuyer1Like(@Param("locationId") String locationId, @Param("buyer1") String buyer1);
    List<PurchaseAgreement> findByLocationIdAndLeadId(@Param("locationId") String locationId, @Param("leadId") String leadId);
    List<PurchaseAgreement> findByLocationIdAndDeliveryState(@Param("locationId") String locationId, @Param("deliveryState") String deliveryState);
    List<PurchaseAgreement> findByLocationIdAndEmailAddress(@Param("locationId") String locationId, @Param("email") String emailAddress);

}
