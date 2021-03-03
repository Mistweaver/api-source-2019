package com.api.demo.mongorepositories.applicationpackage.promomodels;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

@RepositoryRestResource(collectionResourceRel="promomodellist", path="promomodellist")
public interface PromoModelListRepository extends MongoRepository<PromoModelList, String> {
    PromoModelList findByPriceSheetId(@Param("priceSheetId") String priceSheetId);
}
