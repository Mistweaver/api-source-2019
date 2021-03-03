package com.api.demo.mongorepositories;

import com.api.demo.security.utils.AuditorAwareImpl;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.*;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.format.annotation.DateTimeFormat.ISO;

import javax.persistence.*;
import javax.persistence.Id;

import java.util.Date;

@Getter
@Setter
@Entity
@EntityListeners(AuditingEntityListener.class)
public abstract class BasicEntity extends AuditorAwareImpl {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private String id;

    @Column(name = "created_by", nullable = false)
    @CreatedBy
    private String createdBy = getCurrentAuditor().orElse("Machine Ghost");


    @Column(name = "created_date", nullable = false, updatable = false)
    @CreatedDate
    @DateTimeFormat(iso = ISO.DATE_TIME)
    private Date creationTime = new Date();

    private boolean deleted;

    @Column(name = "modified_by", nullable = false)
    //@CreatedBy
    @LastModifiedBy
    private String modifiedBy = getCurrentAuditor().orElse("Machine Ghost");


    @Column(name = "modified_date")
    @LastModifiedDate
    @DateTimeFormat(iso = ISO.DATE_TIME)
    private Date modificationTime = new Date();

    public BasicEntity() {
        this.deleted = false;
    }
}
