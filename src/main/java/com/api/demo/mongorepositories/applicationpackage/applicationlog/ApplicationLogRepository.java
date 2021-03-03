package com.api.demo.mongorepositories.applicationpackage.applicationlog;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

import java.util.List;

@RepositoryRestResource(collectionResourceRel = "applicationlogs", path = "applicationlogs")
public interface ApplicationLogRepository extends MongoRepository<ApplicationLog, String> {
    List<ApplicationLog> findByUserEmail(@Param("userEmail") String userEmail);
}
