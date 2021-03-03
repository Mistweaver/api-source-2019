package com.api.demo.mongorepositories.applicationpackage.whitelist;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

import java.util.List;

@RepositoryRestResource(collectionResourceRel = "whitelist", path = "whitelist")
public interface WhiteListRepository extends MongoRepository<WhiteList, String> {
	List<WhiteList> findByLocationId(@Param("locationId") String locationId);
}
