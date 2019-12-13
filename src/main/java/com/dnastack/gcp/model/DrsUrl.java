package com.dnastack.gcp.model;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;

@AllArgsConstructor
@Data
public class DrsUrl {

    /**
     * A URL that can be used to access the file.
     */
    URI url;

    /**
     * These values are reported by the underlying object store.
     * A set of key-value pairs that represent system metadata about the object.
     */
    Map<String, String> systemMetadata = new HashMap<>();

    /**
     * A set of key-value pairs that represent metadata provided by the uploader.
     */
    Map<String, String> userMetadata = new HashMap<>();

    /**
     * A set of key-value pairs that represent sufficient metadata to be granted
     * access to a resource. It may be helpful to provide details about a specific
     * provider, for example.
     */
    Map<String, String> authorizationMetadata = new HashMap<>();

}
