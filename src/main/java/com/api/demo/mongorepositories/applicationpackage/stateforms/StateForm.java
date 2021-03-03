package com.api.demo.mongorepositories.applicationpackage.stateforms;

import com.api.demo.mongorepositories.BasicEntity;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

@Getter
@Setter
@Document(collection = "stateforms")
public class StateForm extends BasicEntity {
    @Indexed()
    private String state;
    private String url;
    private String modelType;
    private String linkDescription;
}
