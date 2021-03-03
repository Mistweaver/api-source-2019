package com.api.demo.mongorepositories.applicationpackage.notes;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

@RepositoryRestResource(collectionResourceRel = "notes", path = "notes")
public interface NoteRepository extends MongoRepository<Note, String> {
    //List operations
    Page<Note> findByUserId(@Param("userId") String userId, Pageable pageable);
    List<Note> findByDeleted(@Param("deleted") boolean deleted);
}
