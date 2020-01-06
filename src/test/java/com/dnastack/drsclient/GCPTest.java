package com.dnastack.drsclient;

import com.dnastack.drsclient.client.config.DrsServerConfig;
import com.dnastack.drsclient.model.DrsObject;
import com.dnastack.drsclient.model.DrsUrl;
import com.google.api.client.util.Lists;
import com.google.api.gax.paging.Page;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.storage.Blob;
import com.google.cloud.storage.BlobInfo;
import com.google.cloud.storage.Storage;
import com.google.cloud.storage.StorageOptions;
import com.google.common.reflect.TypeToken;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.contrib.java.lang.system.ExpectedSystemExit;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;


public class GCPTest {

    private static final String DRS_SERVER_URL = DrsServerConfig.getDrsServerUrl();
    private static final String DRS_USERNAME = DrsServerConfig.getDrsUsername();
    private static final String DRS_PASSWORD = DrsServerConfig.getDrsPassword();
    private static final URI DRS_BASE_URI;
    private static final String DRS_API_BASE="/ga4gh/drs/v1";
    private static final int NUM_TEST_FILES=5;
    private static final int PAGE_SIZE=NUM_TEST_FILES*10; //should be large enough that all files within the TEST_BUCKET are returned.
    private static final String TEST_BUCKET = Environment.getOptionalEnv("GCS_TEST_BUCKET", "drs-client-test-bucket");
    private static Pattern GCS_URL_PATTERN = Pattern.compile("https://.*google.*/b/(.*)/o/.*");
    private static Pattern GS_URI_PATTERN = Pattern.compile("gs://([a-zA-Z_0-9\\-]+)/(.*)");

    @Rule
    public final ExpectedSystemExit exit = ExpectedSystemExit.none();

    private static final String TEST_PREFIX = RandomStringUtils.randomAlphanumeric(16);

    private static final Storage storage;

    private static final HttpUtil httpUtil = new HttpUtil(DRS_USERNAME, DRS_PASSWORD);

    static{
        try {
            DRS_BASE_URI = new URI(DRS_SERVER_URL);
            StorageOptions storageOptions = StorageOptions.newBuilder()
                                                          .setCredentials(GoogleCredentials.fromStream(new FileInputStream(Environment.getRequiredEnv(
                                                                  "GOOGLE_APPLICATION_CREDENTIALS"))))
                                                          .build();
            storage = storageOptions.getService();

        }catch(URISyntaxException use){
            throw new IllegalArgumentException(use);
        }catch(IOException ie){
            throw new RuntimeException(ie);
        }


    }

    private static String getObjectNameFromGsUri(String gsUri){
        Matcher matcher = GS_URI_PATTERN.matcher(gsUri);
        if(!matcher.find() || matcher.groupCount() > 2){
            throw new RuntimeException("Unexpected fromat for gsUri "+gsUri);
        }
        return matcher.group(2);
    }

    private static byte[] getFileContent(int numLines){
        StringBuilder sb = new StringBuilder();
        for(int i = 0; i < numLines; ++i){
            sb.append(i+"\n");
        }
        return sb.toString().getBytes();
    }

    private static void generateAndUploadObjects(int numObjects){
        for(int i = 0; i < numObjects; ++i){
            String objectName = String.format("%s/%s", TEST_PREFIX, RandomStringUtils.randomAlphanumeric(16));
            BlobInfo blobInfo = BlobInfo.newBuilder(TEST_BUCKET, objectName).build();
            storage.create(blobInfo, getFileContent(100));
            System.out.println("Created file in cloud storage "+String.format("gs://%s/%s", blobInfo.getBucket(), blobInfo.getName()));
        }
    }

    private static void deleteObjectsWithPrefix(String prefix){
        Lists.newArrayList(storage.list(TEST_BUCKET, Storage.BlobListOption.prefix(prefix)).iterateAll()).stream()
             .forEach(blob->{
                 try{
                     System.out.println("Deleted object "+blob.getBlobId().getName());
                    storage.delete(blob.getBlobId());
                 }catch(Exception ex){
                     //exceptions shouldn't abort cleanup.
                     ex.printStackTrace();
                     System.err.println("WARNING: Couldn't clean up object "+blob.getBlobId().getName()+" from cloud storage (consider gsutil rm gs://"+TEST_BUCKET+"/"+prefix+"**)");
                 }
             });
    }

    @BeforeClass
    public static void uploadTestFiles(){
        generateAndUploadObjects(NUM_TEST_FILES);
    }


    @AfterClass
    public static void deleteTestFiles(){
        deleteObjectsWithPrefix(TEST_PREFIX);
        deleteRecordsWithPrefix(TEST_PREFIX);
    }


    private static boolean hasPrefix(DrsObject drsObject){
        return getObjectNameFromGsUri(drsObject.getAliases().get(0)).startsWith(TEST_PREFIX);
    }

    private static void deleteRecordsWithPrefix(String prefix){
        TypeToken drsListToken = new TypeToken<DrsObjectList>(){};

        DrsObjectList objectList = httpUtil.get(DRS_BASE_URI,
                                                String.format("%s/%s", DRS_API_BASE, "objects"),
                                                drsListToken.getType());

        List<DrsObject> drsObjects = objectList.getObjects();

        drsObjects.stream()
                 .filter(GCPTest::hasPrefix)
                 .forEach(drsObject->httpUtil.delete(DRS_BASE_URI, String.format("%s/%s/%s", DRS_API_BASE, "objects", drsObject.getId())));


    }

    private void assertWebUrlFormattedCorrectly(String webUrl){
        Matcher matcher = GCS_URL_PATTERN.matcher(webUrl);
        if(!matcher.matches()){
            throw new RuntimeException("Got an object with URL "+webUrl+" which doesn't match expected pattern for a file in Google Cloud Storage");
        }
    }

    @Test
    public void testInsertFiles() {
        final Page<Blob> blobList = storage.list(TEST_BUCKET, Storage.BlobListOption.prefix(TEST_PREFIX));

        if(blobList == null){
            throw new RuntimeException("Couldn't locate any files to insert");
        }
         Set<String> blobGsUris = Lists.newArrayList(blobList.iterateAll()).stream()
                                       .map(blob->String.format("gs://%s/%s", blob.getBucket(), blob.getName()))
                                       .collect(Collectors.toSet());

        try {
            DrsInsert.main(new String[]{"gs://"+TEST_BUCKET, TEST_PREFIX});
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
                  .filter(GCPTest::hasPrefix)
                  .map(drsObject-> {
                    List<DrsUrl> drsUrls = drsObject.getUrls();
                    if(drsUrls == null || drsUrls.isEmpty()){
                        throw new RuntimeException("Expected a non null URL for an inserted DRS Object with id "+drsObject.getId());
                    }

                    String webUrl = drsUrls.get(0).getUrl().toString();
                    assertWebUrlFormattedCorrectly(webUrl);

                    String gsUri = drsObject.getAliases().get(0);
                    if(!blobGsUris.contains(gsUri)){
                        System.out.println(drsObject);
                        throw new RuntimeException("DRS record with URI "+gsUri+" found without a corresponding file in cloud storage"+String.join(",", blobGsUris));
                    }
                    return gsUri;
                  }).collect(Collectors.toSet());

        if(!reportedUris.containsAll(blobGsUris)){
            reportedUris.removeAll(blobGsUris);
            throw new RuntimeException("DRS records with URIs " + StringUtils.join(reportedUris, ",") + " found with no corresponding files in cloud storage.");
        }else if(!blobGsUris.containsAll(reportedUris)){
            blobGsUris.removeAll(reportedUris);
            throw new RuntimeException("Files with URIs " + StringUtils.join(blobGsUris, ",") + " found with no corresponding record in DRS.");
        }
    }

}
