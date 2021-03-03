package com.api.demo.pricedata.repositories.factories;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

@RepositoryRestResource(collectionResourceRel = "factories", path = "factories")
public interface FactoryRepository extends MongoRepository<Factory, String> {

}
