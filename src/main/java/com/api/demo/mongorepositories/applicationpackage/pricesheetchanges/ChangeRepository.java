package com.api.demo.mongorepositories.applicationpackage.pricesheetchanges;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

@RepositoryRestResource(collectionResourceRel = "changes", path = "changes")
public interface ChangeRepository extends MongoRepository<Change, String> {
    Page<Change> findByLocationId(@Param("locationId") String locationId, Pageable pageable);
}
