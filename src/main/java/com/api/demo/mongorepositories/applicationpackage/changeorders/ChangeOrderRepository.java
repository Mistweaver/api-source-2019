package com.api.demo.mongorepositories.applicationpackage.changeorders;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

import java.util.List;

@RepositoryRestResource(collectionResourceRel = "changeorders", path="changeorders")
public interface ChangeOrderRepository extends MongoRepository<ChangeOrder, String> {
    List<ChangeOrder> findByPurchaseAgreementId(@Param("purchaseAgreementId") String purchaseAgreementId);
    List<ChangeOrder> findByCustomerCode(@Param("customerCode") String customerCode);
    Page<ChangeOrder> findByStatus(@Param("status") String status, Pageable pageable);
    List<ChangeOrder> findByLeadId(@Param("leadId") String leadId);
}
