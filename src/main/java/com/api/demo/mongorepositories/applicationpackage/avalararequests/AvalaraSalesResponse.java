package com.api.demo.mongorepositories.applicationpackage.avalararequests;

import com.api.demo.mongorepositories.BasicEntity;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

@Getter
@Setter
@Document(collection = "avalarasalesresponse")
public class AvalaraSalesResponse extends BasicEntity {
	@Indexed
	private String user;
	@Indexed
	private String documentId;
	@Indexed
	private String documentType;
	@Indexed
	private String requestType;	// invoice or order
	private String serializedResponse;
}
