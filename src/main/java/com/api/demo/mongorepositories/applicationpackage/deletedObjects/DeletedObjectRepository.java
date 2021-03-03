package com.api.demo.mongorepositories.applicationpackage.deletedObjects;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;


@RepositoryRestResource(collectionResourceRel="deletedobjects", path="deletedobjects")
public interface DeletedObjectRepository extends MongoRepository<DeletedObject, String> {
	Page<DeletedObject> findByObjectId(@Param("id") String objectId, Pageable pageable);
	Page<DeletedObject> findByObjectType(@Param("type") String objectType, Pageable pageable);
}
