package com.api.demo.mongorepositories.applicationpackage.optionsheets;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

import java.util.List;

@RepositoryRestResource(collectionResourceRel =  "optionsheets", path="optionsheets")
public interface OptionSheetRepository extends MongoRepository<OptionSheet, String> {
    OptionSheet findByMonthAndYearAndLocationId(@Param("month") int month, @Param("year") int year, @Param("locationId") String locationId);
    Page<OptionSheet> findByLocationId(@Param("locationId") String locationId, Pageable page);
    List<OptionSheet> findByLocationIdAndActive(@Param("locationId") String locationId, @Param("active") boolean active);
    List<OptionSheet> findByLocationIdAndActiveAndSeriesName(@Param("locationId") String locationId, @Param("active") boolean active, @Param("series") String seriesName);
}
