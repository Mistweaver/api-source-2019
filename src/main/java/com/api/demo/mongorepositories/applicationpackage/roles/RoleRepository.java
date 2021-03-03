package com.api.demo.mongorepositories.applicationpackage.roles;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

@RepositoryRestResource(collectionResourceRel = "roles", path = "roles")
public interface RoleRepository extends MongoRepository<Role, String> {

}