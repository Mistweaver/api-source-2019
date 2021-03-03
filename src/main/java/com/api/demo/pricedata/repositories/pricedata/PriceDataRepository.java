package com.api.demo.pricedata.repositories.pricedata;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

import java.util.List;

@RepositoryRestResource(collectionResourceRel = "pricedata", path = "pricedata")
public interface PriceDataRepository extends MongoRepository<PriceData, String> {
	List<PriceData> findByModelId(@Param("modelId") String modelId);
	List<PriceData> findByStatus(@Param("status") String status);
	List<PriceData> findByLocationId(@Param("locationId") String locationId);
	List<PriceData> findByLocationIdAndSeriesName(@Param("locationId") String locationId, @Param("seriesName") String seriesName);
	PriceData findByModelIdAndLocationId(@Param("modelId") String modelId, @Param("locationId") String locationId);
	List<PriceData> findByLocationIdAndStatus(@Param("locationId") String locationId, @Param("status") String status);
	List<PriceData> findByModelIdAndStatus(@Param("modelId") String modelId, @Param("status") String status);

	PriceData findByModelIdAndLocationIdAndActiveDate(@Param("modelId") String modelId, @Param("locationId") String locationId, @Param("activeDate") String activeDate);
}
