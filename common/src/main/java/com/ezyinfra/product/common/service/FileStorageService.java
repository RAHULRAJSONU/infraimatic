package com.ezyinfra.product.common.service;

import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

/**
 * Service abstraction for storing uploaded files.  Implementations may store
 * files on a local filesystem, in a database or in a cloud storage provider.
 * The returned string is a URL or identifier that can later be resolved to
 * retrieve the file.
 */
public interface FileStorageService {

    /**
     * Store an uploaded file on behalf of a tenant.
     *
     * @param tenantId the tenant for whom the file is being stored
     * @param file     the uploaded file
     * @return a string identifier or URL referencing the stored file
     * @throws IOException if the file cannot be stored
     */
    String store(String tenantId, MultipartFile file) throws IOException;
}