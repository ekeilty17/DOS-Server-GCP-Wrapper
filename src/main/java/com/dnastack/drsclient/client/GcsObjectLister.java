package com.dnastack.drsclient.client;

import com.dnastack.drsclient.model.DrsAccessMethod;
import com.dnastack.drsclient.model.DrsAccessUrl;
import com.dnastack.drsclient.model.DrsChecksum;
import com.dnastack.drsclient.model.DrsObject;
import com.google.api.gax.paging.Page;
import com.google.cloud.storage.Blob;
import com.google.cloud.storage.Storage;
import com.google.cloud.storage.Storage.BlobListOption;
import com.google.cloud.storage.StorageException;
import com.google.cloud.storage.StorageOptions;
import com.google.common.collect.ImmutableList;
import lombok.Getter;

import java.io.IOException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import static java.util.Collections.emptyList;
import static java.util.Objects.requireNonNull;

public class GcsObjectLister implements ObjectLister{

    @Getter private String bucketName;
    private Storage storage;
    private String billingProjectId;
    private String region;

    public GcsObjectLister(String bucketName, StorageOptions storageOptions, String billingProjectId) {

        this.bucketName = requireNonNull(bucketName);
        if(this.bucketName.startsWith("gs://")){
            this.bucketName = this.bucketName.substring("gs://".length());
        }
        this.storage = storageOptions.getService();
        this.billingProjectId = billingProjectId;
        this.region = System.getenv("DRS_REGION");
    }

    @Override
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

    private List<DrsChecksum> getChecksums(Blob blob) {
        if (blob.getMd5() != null) {
            return ImmutableList.of(new DrsChecksum(blob.getMd5(), DrsChecksum.Type.md5));
        } else {
            return ImmutableList.of();
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
        String mimeType = translateMimeType(blob.getContentType());
        List<DrsChecksum> checksums = getChecksums(blob);
        String description = blob.getBlobId().getName();
        List<DrsAccessMethod> accessMethods = ImmutableList.of(DrsAccessMethod.builder()
                                                                              .type(DrsAccessMethod.AccessType.https)
                                                                              .region(region)
                                                                              .access_url(DrsAccessUrl.builder()
                                                                                                      .url(blob.getSelfLink() + "?object")
                                                                                                      .build())
                                                                              .build(),
                                                               DrsAccessMethod.builder()
                                                                              .type(DrsAccessMethod.AccessType.gs)
                                                                              .region(region)
                                                                              .access_url(DrsAccessUrl.builder()
                                                                                      .url(String.format("gs://%s/%s", blob.getBucket(), blob.getName()))
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
                description,
                emptyList());
    }
}
