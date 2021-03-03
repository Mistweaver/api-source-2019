package com.api.demo.pricedata.repositories.variables;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

@RepositoryRestResource(collectionResourceRel = "variables", path = "variables")
public interface VariableRepository extends MongoRepository<Variable, String> {
	Variable findByKey(@Param("key") String key);
}
