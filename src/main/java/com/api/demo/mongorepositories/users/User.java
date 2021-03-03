package com.api.demo.mongorepositories.users;

import com.api.demo.mongorepositories.BasicEntity;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import javax.persistence.Entity;

@Entity
@Getter
@Setter
@ToString
@Document(collection="users")
public class User extends BasicEntity {
    private String username;    // don't use anymore, but it's baked into the security so deleting will be a days work
    @Indexed
    private String name;
    @Indexed
    private String locationId;
    private String location;
    private String licenseNumber;
    private String profilePictureUrl;
    @Indexed(unique = true)
    private String email;
}