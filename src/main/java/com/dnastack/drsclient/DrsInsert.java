package com.dnastack.drsclient;

import com.dnastack.drsclient.client.*;
import com.dnastack.drsclient.client.config.AzureBlobStorageConfig;
import com.dnastack.drsclient.client.config.DrsServerConfig;
import com.dnastack.drsclient.client.config.GcsConfig;
import com.dnastack.drsclient.idgen.IdGenerator;
import com.dnastack.drsclient.idgen.UseNameAsId;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.storage.StorageOptions;

import java.io.FileInputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;

public class DrsInsert {

    public static void main(String[] args) throws IOException {
        if (args.length < 1 || args.length > 2) {
            System.err.println("Incorrect number of command line parameters. Got " + args.length + "; expected 1 or 2.");
            System.err.println();
            System.err.println("Usage: DrsInsert <bucket-name or container name> [prefix]");
            System.exit(1);
        }

        String bucketName = args[0];
        String prefix;
        if (args.length == 2) {
            prefix = args[1];
        } else {
            prefix = null;
        }

        final String drsBaseUrlWithTrailingSlash = getServerUrl();

        ObjectLister objectLister;
        if(bucketName.startsWith("gs://")){
            objectLister = createGcsClient(bucketName);
        }else if(bucketName.matches("https://.*.blob.core.windows.net/.*")){
            objectLister = createAzureClient(bucketName);
        }else{
            throw new IllegalArgumentException("Unrecognized bucket/container type.  Expected format is gs://<bucketname> or https://<storageaccount>.blob.core.windows.net/<container>\n");
        }

        DrsClient dosClient = createDosClient(drsBaseUrlWithTrailingSlash);
        IdGenerator idGenerator = new UseNameAsId();


        objectLister.getDataObjects(prefix)
                .map(drsObject -> {
                    String id = idGenerator.generateId(drsObject);
                    // I think its drs://<actual-host-name>/<id> which gets turned into http(s)://<actual-host-name>/api/ga4gh/drs/v1/<id>
                    String self_uri = "drs://"+getHostAndPort(drsBaseUrlWithTrailingSlash)+"/"+id;
                    return drsObject.toBuilder().id(id).self_uri(self_uri).build();
                })
                .forEach(dosClient::postDataObject);

        dosClient.flush();

        System.out.println("Done.");
    }

    private static String getHostAndPort(String url){
        try {
            URL u = new URL(url);
            if (u.getPort() > -1) {
                return String.format("%s:%d", u.getHost(), u.getPort());
            } else {
                return u.getHost();
            }
        }catch(MalformedURLException mue){
            throw new RuntimeException(mue);
        }
    }
    private static AzureBlobLister createAzureClient(String containerURL) throws IOException{
        return new AzureBlobLister(AzureBlobStorageConfig.getSubscriptionId(), AzureBlobStorageConfig.getStorageAccount(), containerURL, AzureBlobStorageConfig
                .getAzConnectionString());
    }

    private static GcsObjectLister createGcsClient(String bucketName) throws IOException {
        StorageOptions storageOptions = StorageOptions.newBuilder()
//                .setCredentials(GoogleCredentials.getApplicationDefault())

                .setCredentials(GoogleCredentials.fromStream(new FileInputStream(GcsConfig.getGoogleApplicationCredentials())))
                .build();

        String billingProjectId = GcsConfig.getBillingProjectId();

        return new GcsObjectLister(bucketName, storageOptions, billingProjectId);
    }

    private static DrsClient createDosClient(String serverUrl) {
        return new DrsClient(
                URI.create(serverUrl),
                DrsServerConfig.getDrsUsername(),
                DrsServerConfig.getDrsPassword());
    }



    private static String getServerUrl(){
        String serverUrl = DrsServerConfig.getDrsServerUrl();
        if (!serverUrl.endsWith("/")) {
            serverUrl += "/";
        }
        return serverUrl;
    }

}
