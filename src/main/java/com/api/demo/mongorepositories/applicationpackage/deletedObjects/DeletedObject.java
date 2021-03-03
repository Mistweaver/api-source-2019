package com.api.demo.mongorepositories.applicationpackage.deletedObjects;

import com.api.demo.mongorepositories.BasicEntity;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

@Getter
@Setter
@Document(collection = "deletedobjects")
public class DeletedObject extends BasicEntity {
	@Indexed
	private String objectId;
	@Indexed
	private String objectType;
	private String object;
}
