package com.api.demo.mongorepositories.applicationpackage.notifications;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;
import org.springframework.format.annotation.DateTimeFormat;

import java.util.Date;
import java.util.List;

@RepositoryRestResource(collectionResourceRel = "notifications", path = "notifications")
public interface NotificationRepository extends MongoRepository<Notification, String> {
        Page<Notification> findByUserId(@Param("userId") String userId, Pageable pageable);
        List<Notification> findByDeleted(@Param("deleted") boolean deleted);
        List<Notification> findByDateBetween(@Param("firstDate") @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSZ") Date from, @Param("secondDate") @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSZ") Date to);
}