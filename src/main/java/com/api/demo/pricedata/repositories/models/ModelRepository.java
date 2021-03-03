package com.api.demo.pricedata.repositories.models;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

import java.util.List;

@RepositoryRestResource(collectionResourceRel = "models", path = "models")
public interface ModelRepository extends MongoRepository<Model, String> {
	List<Model> findByFactoryId(@Param("factoryId") String factoryId);
	List<Model> findByModelNumber(@Param("modelNumber") String modelNumber);
	List<Model> findByModelNumberLike(@Param("modelNumber") String modelNumber);
	List<Model> findByType(@Param("type") String type);
	List<Model> findByRetired(@Param("retired") boolean retired);
	List<Model> findByModelNumberAndFactoryId(@Param("modelNumber") String modelNumber, @Param("factoryId") String factoryId);
}
