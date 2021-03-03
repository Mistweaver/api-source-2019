package com.api.demo.mongorepositories.applicationpackage.promotions;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

import java.util.List;

@RepositoryRestResource(collectionResourceRel="promos", path="promos")
public interface PromotionRepository extends MongoRepository<Promotion, String> {
    Promotion findByDate(@Param("date") String date);
    List<Promotion> findByYear(@Param("year") int year);
}