package com.api.demo.filestore.service;

import com.api.demo.filestore.exception.FsFileNotFoundException;
import com.api.demo.filestore.exception.FileStorageException;
import com.api.demo.filestore.exception.FsFileAlreadyExistsException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class FileStorageService {

    private static final Logger logger = LoggerFactory.getLogger(FileStorageService.class);

    private final Path fileStorageLocation;

    /**
     * Constructor. Takes a file storage directory parameter from the applications property file,
     * resolves it to an absolute path in the file system and attempts to create the directory,
     * if necessary.
     *
     * @param filestoreDirectory the file storage directory in the local file system
     */
    @Autowired
    public FileStorageService(@Value("${filestore.directory}") String filestoreDirectory) {
        logger.info("filestoreDirectory: " + filestoreDirectory);
        this.fileStorageLocation = Paths.get(filestoreDirectory).toAbsolutePath().normalize();

        try {
            Files.createDirectories(this.fileStorageLocation);
        } catch (Exception ex) {
            throw new FileStorageException("Could not create the directory where the uploaded files will be stored.", ex);
        }
    }


    /**
     * Store a multi part file object specified by the file parameter. The file name is
     * made unique by appending it to a generated random UUID.
     *
     * @param file the multi part file to store
     * @return the generated unique file name of the stored file
     */
    public String storeFile(MultipartFile file) {
        /**
         * Normalize the file name.
         */
        String fileName = StringUtils.cleanPath(file.getOriginalFilename());

        /**
         * Make the file name unique by appending it to a generated random UUID.
         */
        String uniqueFileName = UUID.randomUUID() + "." + fileName;


        try {
            /**
             * Check if the file name contains invalid characters.
             */
            if(fileName.contains("..")) {
                throw new FileStorageException("Sorry! File name: " + fileName + " is invalid. Consecutive '..' found in file name");
            } else if(containsIllegals(fileName)) {
                String illegalCharacters = returnIllegalCharacters(fileName);
                throw new FileStorageException("Sorry! File name: " + fileName + " is invalid. Remove the following characters from the file name: " + illegalCharacters);
            }

            System.out.println("All clear");
            /**
             * Copy file to the target location.
             */
            Path targetLocation = this.fileStorageLocation.resolve(uniqueFileName);
            Files.copy(file.getInputStream(), targetLocation);

            /**
             * File successfully stored. Return the generated unique file name.
             */
            return uniqueFileName;
        } catch(FileAlreadyExistsException faee) {
            logger.error("File already exists: " + faee.getMessage());
            throw new FsFileAlreadyExistsException("Could not generate unique file name for file: " + fileName + ". Please try again!", faee);
        } catch (IOException ex) {
            throw new FileStorageException("Could not store file " + fileName + ". Please try again!", ex);
        }
    }


    /**
     * Retrieve the file specified by the fileName parameter as a UrlResource, and return it as a
     * Resource object. The path in the file system is resolved internally by the method.
     *
     * @param fileName the name of the file to retrieve (the full file system path is not required)
     * @return the retrieved file as a Resource object
     */
    public Resource loadFileAsResource(String fileName) {
        try {
            Path filePath = this.fileStorageLocation.resolve(fileName).normalize();
            Resource resource = new UrlResource(filePath.toUri());
            if(resource.exists()) {
                return resource;
            } else {
                throw new FsFileNotFoundException("File not found " + fileName);
            }
        } catch (MalformedURLException ex) {
            throw new FsFileNotFoundException("File not found " + fileName, ex);
        }
    }


    public boolean containsIllegals(String toExamine) {
        // Pattern pattern = Pattern.compile("[~#@*+%{}<>\\[\\]|\"\\_^]");
        // changing this to a whitelist of characters
        Pattern pattern = Pattern.compile("[^a-zA-Z0-9\\s/._-]+");
        Matcher matcher = pattern.matcher(toExamine);
        return matcher.find();
    }

    public String returnIllegalCharacters(String toExamine) {
        Pattern pattern = Pattern.compile("[^a-zA-Z0-9\\s/._-]+");
        Matcher matcher = pattern.matcher(toExamine);
        if(matcher.find()) {
            return toExamine.replaceAll("[a-zA-Z0-9\\s/._-]+", "");
        }
        return toExamine;
    }

}