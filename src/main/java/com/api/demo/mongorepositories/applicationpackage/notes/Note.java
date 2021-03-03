package com.api.demo.mongorepositories.applicationpackage.notes;

import com.api.demo.mongorepositories.BasicEntity;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

@Getter
@Setter
@Document(collection="note")
public class Note extends BasicEntity {
    @Indexed()
    private String userId;
    @Indexed()
    private String associatedId;
    @Indexed
    private String noteClassName;
    private String noteTitle;
    private String data;


}

