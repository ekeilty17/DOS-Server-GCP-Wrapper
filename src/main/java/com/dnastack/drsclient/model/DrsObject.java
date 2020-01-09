package com.dnastack.drsclient.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder(toBuilder=true)
public class DrsObject {

    /**
     * An identifier unique to this Data Object.
     */
    private String id;

    /**
     * A string that can be optionally used to name a Data Object.
     */
    private String name;

    /**
     * A drs:// URI, as defined in the DRS documentation, that tells clients how to access this object. The intent of
     * this field is to make DRS objects self-contained, and therefore easier for clients to store and pass around.
     */
    private String self_uri;

    /**
     * The computed size in bytes.
     */
    private Long size;

    /**
     * Timestamp of object creation in RFC3339.
     */
    private Instant created_time;

    /**
     * Timestamp of update in RFC3339, identical to create timestamp in systems that do not support updates.
     */
    private Instant updated_time;

    /**
     * A string representing a version.
     */
    private String version;

    /**
     * A string providing the mime-type of the Data Object. For example, "application/json".
     */
    private String mime_type;

    /**
     * The checksum of the Data Object. At least one checksum must be provided.
     */
    private List<DrsChecksum> checksums;

    private List<DrsAccessMethod> access_methods;

    /**
     * A human readable description of the contents of the Data Object.
     */
    private String description;

    /**
     * A list of strings that can be used to find this Data Object. These aliases can be used to represent the Data Object's location in a directory (e.g. "bucket/folder/file.name") to make Data Objects more discoverable.
     */
    private List<String> aliases;

}
