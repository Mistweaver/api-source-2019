package com.api.demo.mongorepositories.applicationpackage.roles;

import com.api.demo.mongorepositories.BasicEntity;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

@Getter
@Setter
@Document(collection="role")
public class Role extends BasicEntity {
    @Indexed(unique=true)
    private String roleName;
}
