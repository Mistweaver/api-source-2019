package com.api.demo.mongorepositories.applicationpackage.promotionmodellist;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

import java.util.List;

@RepositoryRestResource(collectionResourceRel="promotionmodellist", path="promotionmodellist")
public interface PromotionModelListRepository extends MongoRepository<PromotionModelList, String> {

	List<PromotionModelList> findByListState(@Param("listState") String listState);

	List<PromotionModelList> findByLocationId(@Param("locationId") String locationId);

	List<PromotionModelList> findByPromotionId(@Param("promotionId") String promotionId);

	PromotionModelList findByLocationIdAndPromotionId(@Param("locationId") String locationId, @Param("promotionId") String promotionId);
}
