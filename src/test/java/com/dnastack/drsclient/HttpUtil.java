package com.dnastack.drsclient;

import com.google.common.reflect.TypeToken;
import com.google.gson.*;
import org.apache.commons.codec.binary.Base64;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.lang.reflect.Type;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Arrays;

public class HttpUtil {
    private final String authHeader;
    private final HttpClient httpClient;
    private final Gson gson;

    public HttpUtil(String username, String password){
        if(username != null && password != null) {
            String auth = username + ":" + password;
            byte[] encodedAuth = Base64.encodeBase64(auth.getBytes(StandardCharsets.ISO_8859_1));
            authHeader = "Basic " + new String(encodedAuth, StandardCharsets.ISO_8859_1);
        }else{
            authHeader = null;
        }
        httpClient = HttpClientBuilder.create().build();
        this.gson = new GsonBuilder().registerTypeAdapter(Instant.class, new JsonDeserializer() {
            @Override
            public Object deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {
                return Instant.parse(jsonElement.getAsString());
            }
        }).create();
    }

    public void delete(URI baseUri, String path){
        try {
            HttpDelete request = new HttpDelete(baseUri.resolve(path));
            //new HttpGet(baseUri.resolve("ga4gh/drs/v1/objects"));
            request.setHeader(HttpHeaders.AUTHORIZATION, authHeader);
            HttpResponse httpResponse = httpClient.execute(request);
            int statusCode = httpResponse.getStatusLine().getStatusCode();
            if (statusCode != 204) {
                throw new IOException(
                        "Delete request to "
                        + request.getURI()
                        + " failed: "
                        + httpResponse.getStatusLine()
                        + "\n"
                        + Arrays.toString(httpResponse.getAllHeaders())
                        + "\n");
            }
            System.out.println("Got OK from DELETE "+request.getURI());
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public <T> T get(URI baseUri, String path, Type responseType){
        try {
            HttpGet request = new HttpGet(baseUri.resolve(path));
            //new HttpGet(baseUri.resolve("ga4gh/drs/v1/objects"));
            request.setHeader("Content-type", "application/json");
            if(authHeader != null) {
                request.setHeader(HttpHeaders.AUTHORIZATION, authHeader);
            }

            HttpResponse httpResponse = httpClient.execute(request);
            String responseBody = EntityUtils.toString(httpResponse.getEntity());
            int statusCode = httpResponse.getStatusLine().getStatusCode();
            if (statusCode != 200) {
                throw new IOException(
                        "GET request to "
                        + request.getURI()
                        + " failed: "
                        + httpResponse.getStatusLine()
                        + "\n"
                        + Arrays.toString(httpResponse.getAllHeaders())
                        + "\n"
                        + responseBody);
            }
            System.out.println("Got OK from GET "+request.getURI());
            return gson.fromJson(responseBody, responseType);

        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public <T> T get(URI baseUri, String path, Class<T> responseType) {
        return get(baseUri, path, new TypeToken<T>(responseType){}.getType());
    }
}
