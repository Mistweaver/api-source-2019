package com.api.demo.pricedata.repositories.log;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

import java.util.List;

@RepositoryRestResource(collectionResourceRel = "pricedatalogs", path = "pricedatalogs")
public interface PriceDataLogRepository extends MongoRepository<LogEntry, String> {
    List<LogEntry> findByPriceDataId(@Param("priceDataId") String priceDataId);
}
