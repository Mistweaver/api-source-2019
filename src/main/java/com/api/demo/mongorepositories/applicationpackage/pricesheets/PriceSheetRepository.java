package com.api.demo.mongorepositories.applicationpackage.pricesheets;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

import java.util.List;

@RepositoryRestResource(collectionResourceRel = "pricesheets", path = "pricesheets")
public interface PriceSheetRepository extends MongoRepository<PriceSheet, String>{
    PriceSheet findByMonthAndYearAndLocationId(@Param("month") int month, @Param("year") int year, @Param("locationId") String locationId);
    List<PriceSheet> findByMonthAndYear(@Param("month") int month, @Param("year") int year);
    Page<PriceSheet> findByLocationId(@Param("locationId") String locationId, Pageable page);
}
