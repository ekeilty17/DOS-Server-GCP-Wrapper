package com.dnastack.drsclient;

import com.azure.core.http.rest.PagedIterable;
import com.azure.storage.blob.*;
import com.azure.storage.blob.models.BlobItem;
import com.azure.storage.blob.models.ListBlobsOptions;
import com.dnastack.drsclient.client.config.AzureBlobStorageConfig;
import com.dnastack.drsclient.client.config.DrsServerConfig;
import com.dnastack.drsclient.model.DrsObject;
import com.dnastack.drsclient.model.DrsUrl;
import com.google.common.reflect.TypeToken;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.contrib.java.lang.system.ExpectedSystemExit;

import java.io.*;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLDecoder;
import java.nio.charset.Charset;
import java.time.Duration;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;



public class AzureTest {

    private static final String DRS_SERVER_URL = DrsServerConfig.getDrsServerUrl();
    private static final String DRS_USERNAME = DrsServerConfig.getDrsUsername();
    private static final String DRS_PASSWORD = DrsServerConfig.getDrsPassword();
    private static final URI DRS_BASE_URI;
    private static final String DRS_API_BASE="/ga4gh/drs/v1";
    /*
    private static final int NUM_TEST_FILES=5;
    private static final int PAGE_SIZE=NUM_TEST_FILES*10; //should be large enough that all files within the TEST_BUCKET are returned.
    private static final String TEST_CONTAINER_URL = requiredEnv("AZURE_TEST_CONTAINER", "drs-client-test-container");
*/
    private static final String TEST_CONTAINER_URL = Environment.getRequiredEnv("AZURE_TEST_CONTAINER_URL");
    private static final int NUM_TEST_FILES=5;
    // Pattern of Azure URLs.  (Azure uses the same URL as the URI for the object, unlike Google which has both a
    // URL, and a URI of the form gs://...
    private static final Pattern AZURE_URL_PATTERN = Pattern.compile("https://.*.blob.core.windows.net/.*");
    private static Pattern AZURE_URI_PATTERN = Pattern.compile("https://[a-zA-Z_0-9\\-]+.blob.core.windows.net/([a-zA-Z_0-9\\-]+)/(.*)");


    @Rule
    public final ExpectedSystemExit exit = ExpectedSystemExit.none();

    private static final String TEST_PREFIX = RandomStringUtils.randomAlphanumeric(16);

    private static final HttpUtil httpUtil = new HttpUtil(DRS_USERNAME, DRS_PASSWORD);

    private static BlobContainerClient containerClient;

    static{
        try {
            DRS_BASE_URI = new URI(DRS_SERVER_URL);
            BlobServiceClient serviceClient = new BlobServiceClientBuilder()
                    //.credential(new CliCredential())
                    .connectionString(AzureBlobStorageConfig.getAzConnectionString())
                    .endpoint(TEST_CONTAINER_URL)
                    .buildClient();
            BlobUrlParts parts = BlobUrlParts.parse(new URL(TEST_CONTAINER_URL));
            containerClient = serviceClient.getBlobContainerClient(parts.getBlobContainerName());

        }catch(URISyntaxException use){
            throw new IllegalArgumentException(use);
        }catch(IOException ie){
            throw new RuntimeException(ie);
        }
    }

    private static String getObjectNameFromAzureUri(String azureUri){
        Matcher matcher = AZURE_URI_PATTERN.matcher(azureUri);
        if(!matcher.find() || matcher.groupCount() > 2){
            throw new RuntimeException("Unexpected format for Azure URI "+azureUri);
        }
        String objectName = matcher.group(2);

        try {
            return URLDecoder.decode(objectName, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
    }

    private static byte[] getFileContent(int numLines){
        StringBuilder sb = new StringBuilder();
        for(int i = 0; i < numLines; ++i){
            sb.append(i+"\n");
        }
        return sb.toString().getBytes();
    }

    private static void generateAndUploadObjects(int numObjects) {
        for (int i = 0; i < numObjects; ++i) {
            File tmpFile = null;
            try {

                String objectName = String.format("%s/%s", TEST_PREFIX, RandomStringUtils.randomAlphanumeric(16));
                BlobClient blobClient = containerClient.getBlobClient(objectName);
                tmpFile = File.createTempFile(RandomStringUtils.randomAlphanumeric(16), null);
                try (FileOutputStream fos = new FileOutputStream(tmpFile)) {
                    fos.write(getFileContent(100));
                }

                blobClient.uploadFromFile(tmpFile.getAbsolutePath());

                System.out.println("Created file in cloud storage " + objectName);
            } catch (IOException ie) {
                if(tmpFile != null && tmpFile.exists()){
                    tmpFile.delete();
                }
                throw new UncheckedIOException(ie);
            }
        }

    }

    private static void deleteObjectsWithPrefix(String prefix){
        PagedIterable<BlobItem> blobItems = containerClient.listBlobs(new ListBlobsOptions().setPrefix(prefix), Duration.ofSeconds(60));
        blobItems.stream().forEach(blob->{
            containerClient.getBlobClient(blob.getName()).delete();
        });
    }

    @BeforeClass
    public static void uploadTestFiles(){
        generateAndUploadObjects(NUM_TEST_FILES);
    }


    @AfterClass
    public static void deleteTestFiles(){
        try {
            System.err.println("Deleting objects with prefix " + TEST_PREFIX);
            deleteObjectsWithPrefix(TEST_PREFIX);
        }finally{
            System.err.println("Deleting records with prefix "+TEST_PREFIX);
            deleteRecordsWithPrefix(TEST_PREFIX);
        }
    }


    private static boolean hasPrefix(DrsObject drsObject){
        if(drsObject == null){
            System.err.println("drsObject is null!");
        }

        return getObjectNameFromAzureUri(drsObject.getAccess_methods().get(0).getAccess_url().getUrl()).startsWith(TEST_PREFIX);
    }

    private static void deleteRecordsWithPrefix(String prefix){
        TypeToken drsListToken = new TypeToken<DrsObjectList>(){};

        DrsObjectList objectList = httpUtil.get(DRS_BASE_URI,
                                                String.format("%s/%s", DRS_API_BASE, "objects"),
                                                drsListToken.getType());

        List<DrsObject> drsObjects = objectList.getObjects();

        drsObjects.stream()
                  .filter(AzureTest::hasPrefix)
                  .forEach(drsObject->httpUtil.delete(DRS_BASE_URI, String.format("%s/%s/%s", DRS_API_BASE, "objects", drsObject.getId())));


    }


    /*
    private void assertWebUrlFormattedCorrectly(String webUrl){
        Matcher matcher = AZURE_URL_PATTERN.matcher(webUrl);
        if(!matcher.matches()){
            throw new RuntimeException("Got an object with URL "+webUrl+" which doesn't match expected pattern for a file in Azure Blob Storage");
        }
    }*/

    @Test
    public void testInsertFiles() {

        final PagedIterable<BlobItem> blobItems = containerClient.listBlobs(new ListBlobsOptions().setPrefix(TEST_PREFIX), Duration.ofSeconds(60));

        final Iterator<BlobItem> blobItemIterator = blobItems.iterator();
        if(!blobItemIterator.hasNext()){
            throw new RuntimeException("Couldn't locate any files to insert");
        }

        Set<String> blobURLs = blobItems.stream()
                                        .map(blobItem -> containerClient.getBlobClient(blobItem.getName()).getBlobUrl())
                                        .collect(Collectors.toSet());



        try {
            // Use the main method to insert all contents of the container at containerClient.getBlobContainerUrl()
            // with object names starting with TEST_PREFIX
            DrsInsert.main(new String[]{containerClient.getBlobContainerUrl(), TEST_PREFIX});
        }catch(IOException ie){
            throw new UncheckedIOException(ie);
        }


        TypeToken drsListToken = new TypeToken<DrsObjectList>(){};

        //were files actually inserted?
        DrsObjectList objectList = httpUtil.get(DRS_BASE_URI,
                                                String.format("%s/%s", DRS_API_BASE, "objects"),
                                                drsListToken.getType());

        List<DrsObject> drsObjects = objectList.getObjects();

        Set<String> reportedUris = drsObjects.stream()
                                             .filter(AzureTest::hasPrefix)
                                             .map(drsObject-> {
                                                 List<DrsUrl> drsUrls = drsObject.getUrls();
                                                 if(drsUrls == null || drsUrls.isEmpty()){
                                                     throw new RuntimeException("Expected a non null URL for an inserted DRS Object with id "+drsObject.getId());
                                                 }

                                                 String webUrl = drsUrls.get(0).getUrl().toString();
                                                 //assertWebUrlFormattedCorrectly(webUrl);

                                                 String blobURL = drsObject.getAccess_methods().get(0).getAccess_url().getUrl();
                                                 if(!blobURLs.contains(blobURL)){
                                                     System.out.println(drsObject);
                                                     throw new RuntimeException("DRS record with URI "+blobURL+" found without a corresponding file in cloud storage"+String.join(",", blobURLs));
                                                 }
                                                 return blobURL;
                                             }).collect(Collectors.toSet());

        if(!reportedUris.containsAll(blobURLs)){
            reportedUris.removeAll(blobURLs);

            throw new RuntimeException("DRS records with URIs " + StringUtils.join(reportedUris, ",") + " found with no corresponding files in cloud storage.");
        }else if(!blobURLs.containsAll(reportedUris)){
            blobURLs.removeAll(reportedUris);
            throw new RuntimeException("Files with URIs " + StringUtils.join(blobURLs, ",") + " found with no corresponding record in DRS.");
        }
    }
}
