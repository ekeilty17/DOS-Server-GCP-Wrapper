package com.dnastack.drsclient.model;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class DrsChecksum {
    public enum Type {
        md5("md5"), multipart_md5("multipart-md5"), S3("S3"), sha256("sha256"), sha512("sha512");

        private String val;

        Type(String val) {
            this.val = val;
        }
    }
    /**
     * The hex-string encoded checksum for the Data.
     */
    String checksum;

    /**
     * The digest method used to create the checksum. If left unspecified md5 will be assumed.
     */
    Type type;
}
