package com.api.demo.filestore;

import lombok.Getter;
import lombok.Setter;
import org.springframework.format.annotation.DateTimeFormat;

import java.util.Date;

@Getter
@Setter
public class FileStore {

    private String name;
    private String agreementId;
    private String contentType;
    private String dateTimeLastModified;
    private int size;
    private String contentBytes;
    private String creationTime;
    private String modificationTime;

}
