package com.api.demo.mongorepositories.applicationpackage.salesoffices;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

import java.util.List;

@RepositoryRestResource(collectionResourceRel = "salesoffices", path = "salesoffices")
public interface SalesOfficeRepository extends MongoRepository<SalesOffice, String> {
    SalesOffice findByClientConsultantId(@Param("clientConsultantId") int clientConsultantId);
    List<SalesOffice> findByRegion(@Param("region") String region);

}
