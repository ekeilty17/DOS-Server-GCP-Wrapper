package com.dnastack.gcp.client;

import static java.util.Objects.requireNonNull;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;
import com.dnastack.gcp.model.Checksum;
import com.dnastack.gcp.model.Checksum.Type;
import com.dnastack.gcp.model.DosUrl;
import com.dnastack.gcp.model.Ga4ghDataObject;
import com.google.api.client.util.DateTime;
import com.google.api.gax.paging.Page;
import com.google.cloud.storage.Blob;
import com.google.cloud.storage.Storage;
import com.google.cloud.storage.Storage.BlobListOption;
import com.google.cloud.storage.StorageOptions;
import com.google.common.collect.ImmutableList;
import lombok.Getter;

public class GcpClient {

    @Getter private String bucketName;
    private Storage storage;

    public GcpClient(String bucketName) {
        this.bucketName = requireNonNull(bucketName);
        this.storage = StorageOptions.getDefaultInstance().getService();
    }

    public List<Ga4ghDataObject> getDataObjects(String prefix) throws IOException {
        Page<Blob> blobPage =
                prefix == null
                        ? storage.list(bucketName)
                        : storage.list(bucketName, BlobListOption.prefix(prefix));

        return StreamSupport.stream(blobPage.iterateAll().spliterator(), false)
                .map(blob -> toGa4ghObject(prefix, blob))
                .collect(Collectors.toList());
    }

    private String convertDate(Long dateValue) {
        return new DateTime(dateValue).toString();
    }

    private List<Checksum> getChecksums(Blob blob) {
        if (blob.getMd5() != null) {
            return ImmutableList.of(new Checksum(blob.getMd5(), Type.md5));
        } else {
            return ImmutableList.of();
        }
    }

    private List<DosUrl> getUrls(Blob blob) {
        return ImmutableList.of(new DosUrl(blob.getMediaLink(), null, null));
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

    private Ga4ghDataObject toGa4ghObject(String prefix, Blob blob) {
        String id = null;
        String name =
                prefix == null
                        ? blob.getBlobId().getName()
                        : blob.getBlobId()
                                .getName()
                                .substring(prefix.length() + 1); // include the trailing '/'
        String size = blob.getSize().toString();
        String created = convertDate(blob.getCreateTime());
        String updated = convertDate(blob.getUpdateTime());
        String version = "1";
        String mimeType = translateMimeType(blob);
        List<Checksum> checksums = getChecksums(blob);
        List<DosUrl> urls = getUrls(blob);
        String description = blob.getBlobId().getName();
        List<String> aliases = new ArrayList<>();
        return new Ga4ghDataObject(
                id,
                name,
                size,
                created,
                updated,
                version,
                mimeType,
                checksums,
                urls,
                description,
                aliases);
    }
}
