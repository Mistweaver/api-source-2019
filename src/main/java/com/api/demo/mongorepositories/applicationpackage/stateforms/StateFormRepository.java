package com.api.demo.mongorepositories.applicationpackage.stateforms;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

import java.util.List;

@RepositoryRestResource(collectionResourceRel = "stateforms", path="stateforms")
public interface StateFormRepository extends MongoRepository<StateForm, String> {
    List<StateForm> findByState(@Param("state") String state);
    //@Query("{'state':?0, 'modelType':{?1, 'ALL'}}")
    @Query("{'state':?0, $or: [{'modelType':'ALL'}, {'modelType':?1}]}")
    List<StateForm> findByStateAndModelType(@Param("state") String state, @Param("modelType") String modelType);
}
