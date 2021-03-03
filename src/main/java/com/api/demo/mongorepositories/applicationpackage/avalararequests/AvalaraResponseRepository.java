package com.api.demo.mongorepositories.applicationpackage.avalararequests;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

import java.util.List;
@RepositoryRestResource(collectionResourceRel = "avalararesponses", path="avalararesponses")
public interface AvalaraResponseRepository extends MongoRepository<AvalaraSalesResponse, String>{
	List<AvalaraSalesResponse> findByDocumentId(@Param("documentId") String documentId);
	Page<AvalaraSalesResponse> findByRequestType(@Param("requestType") String requestType, Pageable pageable);
}
