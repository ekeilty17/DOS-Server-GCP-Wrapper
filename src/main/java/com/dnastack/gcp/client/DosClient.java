package com.dnastack.gcp.client;

import com.dnastack.gcp.model.DrsObject;
import com.google.api.client.util.DateTime;
import com.google.gson.*;
import org.apache.commons.codec.binary.Base64;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.lang.reflect.Type;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAccessor;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static java.util.Objects.requireNonNull;

public class DosClient {

    private final URI baseUrl;
    private final String authHeader;
    private final HttpClient httpClient;

    private final Gson gson;

    private List<DrsObject> bufferedDataObjects = new ArrayList<>();

    public DosClient(URI baseUrl, String username, String password) {
        this.baseUrl = requireNonNull(baseUrl);

        String auth = username + ":" + password;
        byte[] encodedAuth = Base64.encodeBase64(auth.getBytes(StandardCharsets.ISO_8859_1));
        authHeader = "Basic " + new String(encodedAuth, StandardCharsets.ISO_8859_1);

        httpClient = HttpClientBuilder.create().build();


        this.gson = new GsonBuilder().registerTypeAdapter(Instant.class, new JsonSerializer<Instant>() {
            @Override
            public JsonElement serialize(Instant instant, Type type, JsonSerializationContext jsonSerializationContext) {
                String t = new DateTime(false, instant.getEpochSecond(),0).toStringRfc3339();
                return jsonSerializationContext.serialize(t);
            }
        }).create();
    }

    public void postDataObject(DrsObject dataObject) {
        bufferedDataObjects.add(dataObject);
        if (bufferedDataObjects.size() >= 100) {
            flush();
        }
    }

    public void flush() {
        try {
            String postBody = gson.toJson(bufferedDataObjects);
            HttpPost request =
                    new HttpPost(baseUrl.resolve("ga4gh/drs/v1/objects"));
            request.setEntity(new StringEntity(postBody));
            request.setHeader("Content-type", "application/json");
            request.setHeader(HttpHeaders.AUTHORIZATION, authHeader);

            HttpResponse httpResponse = httpClient.execute(request);
            String responseBody = EntityUtils.toString(httpResponse.getEntity());
            int statusCode = httpResponse.getStatusLine().getStatusCode();
            if (statusCode != 200 && statusCode != 201) {
                throw new IOException(
                        "POST object to "
                                + request.getURI()
                                + " failed: "
                                + httpResponse.getStatusLine()
                                + "\n"
                                + Arrays.toString(httpResponse.getAllHeaders())
                                + "\n"
                                + responseBody);
            }
            System.out.println("Posted " + bufferedDataObjects.size() + " objects to DRS server");

            bufferedDataObjects.clear();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
