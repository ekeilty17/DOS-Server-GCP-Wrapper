package com.dnastack.gcp.client;

import com.dnastack.gcp.model.*;
import com.google.api.client.util.DateTime;
import com.google.api.gax.paging.Page;
import com.google.cloud.storage.Blob;
import com.google.cloud.storage.Storage;
import com.google.cloud.storage.Storage.BlobListOption;
import com.google.cloud.storage.StorageException;
import com.google.cloud.storage.StorageOptions;
import com.google.common.collect.ImmutableList;
import lombok.Getter;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import static java.util.Objects.requireNonNull;

public class GcsClient {

    @Getter private String bucketName;
    private Storage storage;
    private String billingProjectId;
    private String region;

    public GcsClient(String bucketName, StorageOptions storageOptions, String billingProjectId) {
        this.bucketName = requireNonNull(bucketName);
        this.storage = storageOptions.getService();
        this.billingProjectId = billingProjectId;
        this.region = System.getenv("DRS_REGION");
    }

    public Stream<DrsObject> getDataObjects(String prefix) throws IOException {
        try {
            List<BlobListOption> blobListOptions = new ArrayList<>();
            if (prefix != null) {
                blobListOptions.add(BlobListOption.prefix(prefix));
            }
            if (billingProjectId != null) {
                blobListOptions.add(BlobListOption.userProject(billingProjectId));
            }

            Page<Blob> blobPage = storage.list(bucketName, blobListOptions.toArray(new BlobListOption[0]));

            return StreamSupport.stream(blobPage.iterateAll().spliterator(), false)
                                .map(blob -> toGa4ghObject(prefix, blob));
        }catch(StorageException se){
            throw new StorageException(se.getCode(), "Can't list data objects with prefix "+prefix+" on bucket "+bucketName, se);
        }
    }

    private String convertDate(Long dateValue) {
        return new DateTime(false, dateValue, 0).toStringRfc3339();
    }

    private List<DrsChecksum> getChecksums(Blob blob) {
        if (blob.getMd5() != null) {
            return ImmutableList.of(new DrsChecksum(blob.getMd5(), DrsChecksum.Type.md5));
        } else {
            return ImmutableList.of();
        }
    }

    private List<DrsUrl> getUrls(Blob blob) {
        try {
            return ImmutableList.of(new DrsUrl(new URI(blob.getMediaLink()), null, null, null));
        }catch(URISyntaxException use){
            throw new RuntimeException(use);
        }
    }

    private String translateMimeType(Blob blob) {
        String originalMimeType = blob.getContentType();
        if ("text/x-vcard".equals(originalMimeType)) {
            return "application/x-ga4gh-vcf";
        } else if ("text/vcard".equals(originalMimeType)) {
            return "application/x-ga4gh-vcf";
        } else {
            return originalMimeType;
        }
    }

    private DrsObject toGa4ghObject(String prefix, Blob blob) {
        String id = null;
        String name =
                prefix == null
                        ? blob.getBlobId().getName()
                        : blob.getBlobId()
                                .getName()
                                .substring(prefix.length() + 1); // include the trailing '/'
        Long size = blob.getSize();
        Instant created = Instant.ofEpochSecond(blob.getCreateTime());
        Instant updated = Instant.ofEpochSecond(blob.getUpdateTime());
        String version = "1";
        String mimeType = translateMimeType(blob);
        List<DrsChecksum> checksums = getChecksums(blob);
        List<DrsUrl> urls = getUrls(blob);
        String description = blob.getBlobId().getName();
        List<String> aliases = new ArrayList<>();
        List<DrsAccessMethod> accessMethods = ImmutableList.of(DrsAccessMethod.builder()
                                                                              .type(DrsAccessMethod.AccessType.gs)
                                                                              .region(region)
                                                                              .access_url(DrsAccessUrl.builder()
                                                                                                      .url(blob.getSelfLink())
                                                                                                      .build())
                                                                              .build());
        return new DrsObject(
                id,
                name,
                null,
                size,
                created,
                updated,
                version,
                mimeType,
                checksums,
                accessMethods,
                urls,
                description,
                aliases);
    }
}
