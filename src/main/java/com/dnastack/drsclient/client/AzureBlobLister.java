package com.dnastack.drsclient.client;

import com.azure.storage.blob.BlobContainerClient;
import com.azure.storage.blob.BlobServiceClient;
import com.azure.storage.blob.BlobServiceClientBuilder;
import com.azure.storage.blob.BlobUrlParts;
import com.azure.storage.blob.models.BlobItem;
import com.azure.storage.blob.models.ListBlobsOptions;
import com.dnastack.drsclient.model.*;
import com.google.common.collect.ImmutableList;
import com.google.common.io.BaseEncoding;
import com.microsoft.azure.credentials.AzureCliCredentials;
import com.microsoft.azure.management.Azure;
import com.microsoft.rest.LogLevel;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

public class AzureBlobLister implements ObjectLister {
    private final Azure azure;
    private final AzureCliCredentials cliCredentials;

    private String region;
    private BlobContainerClient containerClient;



    //storage account name should be FULL name, like:
    ///subscriptions/<subscriptionid>>/resourceGroups/DefaultResourceGroup-EUS/providers/Microsoft.Storage/storageAccounts/drstestjv
    public AzureBlobLister(String subscription, String storageAccountName, String containerUrl, String connectionString) throws IOException {
        this.cliCredentials = AzureCliCredentials.create();
        this.azure = Azure.configure().withLogLevel(LogLevel.BASIC)
                          .authenticate(cliCredentials).withSubscription(subscription);
        this.region = azure.storageAccounts().getById(storageAccountName).regionName();

        BlobServiceClient serviceClient = new BlobServiceClientBuilder()
                //.credential(new CliCredential())
                .connectionString(connectionString)
                .endpoint(containerUrl)
                .buildClient();
        BlobUrlParts parts = BlobUrlParts.parse(new URL(containerUrl));
        this.containerClient = serviceClient.getBlobContainerClient(parts.getBlobContainerName());
    }

    private List<DrsChecksum> getChecksums(BlobItem blob) {
        if (blob.getProperties().getContentMd5() != null) {
            return ImmutableList.of(new DrsChecksum(BaseEncoding.base64().encode(blob.getProperties().getContentMd5()), DrsChecksum.Type.md5));
        } else {
            return ImmutableList.of();
        }
    }

    private List<DrsUrl> getUrls(BlobItem blob) {
        try {
           return ImmutableList.of(new DrsUrl(new URI(blob.getName()), null, null, null));
        }catch(URISyntaxException use){
            throw new RuntimeException(use);
        }
    }

    private DrsObject toDrsObject(String prefix, BlobItem blobItem) {
        String id = null;
        String name = prefix == null
                      ? blobItem.getName()
                      : blobItem.getName().substring(prefix.length() + 1);
        Long size = blobItem.getProperties().getContentLength();
        Instant created = blobItem.getProperties().getCreationTime().toInstant();
        Instant updated = blobItem.getProperties().getLastModified().toInstant();
        String version = "1";
        String mimeType = translateMimeType(blobItem.getProperties().getContentType());
        List<DrsChecksum> checksums = getChecksums(blobItem);
        List<DrsUrl> urls = getUrls(blobItem);
        String description = blobItem.getName();
        List<String> aliases = new ArrayList<>();

        String url = containerClient.getBlobClient(blobItem.getName()).getBlobUrl();
        List<DrsAccessMethod> accessMethods = ImmutableList.of(DrsAccessMethod.builder()
                                                                              .type(DrsAccessMethod.AccessType.https)
                                                                              .region(region)
                                                                              .access_url(DrsAccessUrl.builder()
                                                                                                      .url(url)
                                                                                                      .build())
                                                                              .build());
        DrsObject drsObject = new DrsObject(id,
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
        return drsObject;
    }

    @Override
    public Stream<DrsObject> getDataObjects(String prefix) throws IOException {
        ListBlobsOptions options = new ListBlobsOptions();
        if (prefix != null) {
            options.setPrefix(prefix);
        }

        return containerClient.listBlobs(options, Duration.ofSeconds(30)).stream().map(blobItem->toDrsObject(prefix, blobItem));
    }
}
