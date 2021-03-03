package com.api.demo.pricedata.repositories.models.custom;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

import java.util.List;


@RepositoryRestResource(collectionResourceRel = "custommodels", path = "custommodels")
public interface CustomModelRepository extends MongoRepository<CustomModel, String> {
	List<CustomModel> findByFactoryId(@Param("factoryId") String factoryId);
	List<CustomModel> findByModelNumber(@Param("modelNumber") String modelNumber);
	List<CustomModel> findByType(@Param("type") String type);
	List<CustomModel> findByRetired(@Param("retired") boolean retired);
}