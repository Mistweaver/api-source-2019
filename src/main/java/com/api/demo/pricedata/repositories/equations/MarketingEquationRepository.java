package com.api.demo.pricedata.repositories.equations;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

@RepositoryRestResource(collectionResourceRel = "marketingequations", path = "marketingequations")
public interface MarketingEquationRepository extends MongoRepository<MarketingEquation, String> {

}
