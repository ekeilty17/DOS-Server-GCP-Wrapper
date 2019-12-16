package com.dnastack.drsclient.client;

import com.dnastack.drsclient.model.DrsObject;

import java.io.IOException;
import java.util.stream.Stream;

public interface ObjectLister {
    Stream<DrsObject> getDataObjects(String prefix) throws IOException;
    default String translateMimeType(String originalMimeType) {
        if ("text/x-vcard".equals(originalMimeType)) {
            return "application/x-ga4gh-vcf";
        } else if ("text/vcard".equals(originalMimeType)) {
            return "application/x-ga4gh-vcf";
        } else {
            return originalMimeType;
        }
    }
}
