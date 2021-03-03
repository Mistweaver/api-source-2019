package com.api.demo.mongorepositories.filestore;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;
import org.springframework.format.annotation.DateTimeFormat;

import java.util.Date;
import java.util.List;

/**
 * Created by Nick Jones on 4/23/18.
 */
@RepositoryRestResource(collectionResourceRel = "storedfiles", path = "storedfiles")
public interface StoredFileRepository extends MongoRepository<StoredFile, String> {

    List<StoredFile> findByFileName(@Param("fileName") String fileName);

    List<StoredFile> findByDeleted(@Param("deleted") boolean deleted);

    List<StoredFile> findByCreationTime(@Param("creationTime") @DateTimeFormat(pattern="yyyy-MM-dd'T'HH:mm:ss.SSSZ") Date creationTime);
    List<StoredFile> findByCreationTimeBetween(@Param("firstDate") @DateTimeFormat(pattern="yyyy-MM-dd'T'HH:mm:ss.SSSZ") Date from, @Param("secondDate") @DateTimeFormat(pattern="yyyy-MM-dd'T'HH:mm:ss.SSSZ") Date to);

    List<StoredFile> findByModificationTime(@Param("modificationTime") @DateTimeFormat(pattern="yyyy-MM-dd'T'HH:mm:ss.SSSZ") Date modificationTime);
    List<StoredFile> findByModificationTimeBetween(@Param("firstDate") @DateTimeFormat(pattern="yyyy-MM-dd'T'HH:mm:ss.SSSZ") Date from, @Param("secondDate") @DateTimeFormat(pattern="yyyy-MM-dd'T'HH:mm:ss.SSSZ") Date to);

    List<StoredFile> findByContentType(@Param("contentType") String contentType);

    StoredFile findByFileUri(@Param("fileUri") String fileUri);

    List<StoredFile> getFilesByAgreementId(@Param("agreementId") String agreementId);

    List<StoredFile> findFilesByAgreementIdAndDeleted(@Param("agreementId") String agreementId, @Param("deleted") boolean deleted);
}
