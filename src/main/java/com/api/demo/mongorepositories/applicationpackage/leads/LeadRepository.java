package com.api.demo.mongorepositories.applicationpackage.leads;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

import java.util.List;

@RepositoryRestResource(collectionResourceRel = "leads", path = "leads")
public interface LeadRepository extends MongoRepository<Lead, String> {
    //Search functions
    Page<Lead> findByUserId(@Param("userId") String userId, Pageable pageable);
    Page<Lead> findByUserIdAndFirstNameLike(@Param("userId") String userId, @Param("firstName") String firstName, Pageable pageable);
    Page<Lead> findByUserIdAndLastNameLike(@Param("userId") String userId, @Param("lastName") String lastName, Pageable pageable);
    List<Lead> findByFirstNameLike(@Param("firstName") String firstName);
    List<Lead> findByLastNameLike(@Param("lastName") String lastName);
    List<Lead> findByLeadSourceId(@Param("leadSourceId") String leadSourceId);
    List<Lead> findByLeadStatusId(@Param("leadStatusId") String leadStatusId);
    Lead findByLeadId(@Param("leadId") String leadId);
    Page<Lead> findByLocationId(@Param("locationId") String locationId, Pageable pageable);
    List<Lead> findByIsDeadLead(@Param("isDeadLead") boolean isDeadLead);
    List<Lead> findByDeleted(@Param("deleted") boolean deleted);

    List<Lead> findByPhone(@Param("phone") String phone);
    List<Lead> findByEmailAddress(@Param("email") String emailAddress);
    Page<Lead> findByDeliveryState(@Param("state") String deliveryState, Pageable pageable);

    /*****Manager search functions ******/
    Lead findByLocationIdAndLeadId(@Param("locationId") String locationId, @Param("leadId") String leadId);
    List<Lead> findByLocationIdAndFirstNameLike(@Param("locationId") String locationId, @Param("firstName") String firstName);
    List<Lead> findByLocationIdAndLastNameLike(@Param("locationId") String locationId, @Param("lastName") String lastName);

    /**** Sales search options ********/
    List<Lead> findByLocationIdAndUserIdAndLeadId(@Param("locationId") String locationId, @Param("userId") String userId, @Param("leadId") String leadId);
    List<Lead> findByLocationIdAndUserIdAndFirstNameLike(@Param("locationId") String locationId, @Param("userId") String userId, @Param("firstName") String firstName);
    List<Lead> findByLocationIdAndUserIdAndLastNameLike(@Param("locationId") String locationId, @Param("userId") String userId, @Param("lastName") String lastName);
    Page<Lead> findByLocationIdAndUserId(@Param("locationId") String locationId, @Param("userId") String userId, Pageable pageable);
}
