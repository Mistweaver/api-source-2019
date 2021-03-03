package com.api.demo.mongorepositories.users;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

import java.util.List;

@RepositoryRestResource(collectionResourceRel = "users", path = "users")
public interface UserRepository extends MongoRepository<User, String> {
    User findByUsername(@Param("username") String username);
    User findByEmail(@Param("email") String email);
    List<User> findByLocationId(@Param("locationId") String locationId);
    List<User> findByName(@Param("name") String name);
}

