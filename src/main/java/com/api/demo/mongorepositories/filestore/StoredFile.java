package com.api.demo.mongorepositories.filestore;

import com.api.demo.mongorepositories.BasicEntity;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Date;

/**
 * Created by Perry Spagnola on 4/23/18.
 */

@Getter
@Setter
@Document(collection="storedfile")
public class StoredFile extends BasicEntity {

    private String agreementId;
    private String fileName;
    private String contentType;
    private Date dateTimeLastModified;
    private int fileSize;
    private String fileUri;


}
