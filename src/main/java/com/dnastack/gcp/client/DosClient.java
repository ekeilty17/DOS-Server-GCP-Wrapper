package com.dnastack.gcp.client;

import com.dnastack.gcp.model.Ga4ghDataObject;
import com.google.gson.Gson;
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
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

import static java.util.Collections.singletonMap;
import static java.util.Objects.requireNonNull;

public class DosClient {

    private final URI baseUrl;
    private final String authHeader;
    private final HttpClient httpClient;

    private final Gson gson = new Gson();

    public DosClient(URI baseUrl, String username, String password) throws IOException {
        this.baseUrl = requireNonNull(baseUrl);

        String auth = username + ":" + password;
        byte[] encodedAuth = Base64.encodeBase64(auth.getBytes(StandardCharsets.ISO_8859_1));
        authHeader = "Basic " + new String(encodedAuth, StandardCharsets.ISO_8859_1);

        httpClient = HttpClientBuilder.create().build();
    }

    public void postDataObject(Ga4ghDataObject dataObject) {
        try {
            String postBody = gson.toJson(singletonMap("data_object", dataObject));

            HttpPost request = new HttpPost(baseUrl.resolve("dataobjects"));
            request.setEntity(new StringEntity(postBody));
            request.setHeader("Content-type", "application/json");
            request.setHeader(HttpHeaders.AUTHORIZATION, authHeader);

            HttpResponse httpResponse = httpClient.execute(request);
            String responseBody = EntityUtils.toString(httpResponse.getEntity());
            if (httpResponse.getStatusLine().getStatusCode() != 200) {
                throw new IOException("Post data object to " + request.getURI() + " failed: " + httpResponse.getStatusLine() + "\n" +
                        Arrays.toString(httpResponse.getAllHeaders()) + "\n" +
                        responseBody);
            }
            System.out.println("Posted to DOS server: " + postBody);

        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
