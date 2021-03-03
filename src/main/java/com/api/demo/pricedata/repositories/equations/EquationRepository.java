package com.api.demo.pricedata.repositories.equations;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

@RepositoryRestResource(collectionResourceRel = "equations", path = "equations")
public interface EquationRepository extends MongoRepository<Equation, String> {
	Equation findByKey(@Param("key") String key);
}
