package com.dnastack.gcp.http;

import com.dnastack.gcp.model.Ga4ghDataObject;
import com.google.gson.Gson;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClientBuilder;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.stream.Collectors;

public class GcpHttp {

    private String project_id;
    private String bucket_name;
    private String prefix;
    private String url;

    public JSONArray getData() throws IOException {

        // Getting Pagination information
        String gcs_Results = new BufferedReader(new InputStreamReader(new URL(this.url).openStream())).lines().collect(Collectors.joining());

        JSONObject gcs_JSON = new JSONObject(gcs_Results);
        JSONArray gcs_allData = gcs_JSON.getJSONArray("items");

        for (int i = 0; i < gcs_allData.length(); i++) {
            System.out.println(i + " " + gcs_allData.getJSONObject(i));
        }
        System.out.println('\n');

        return gcs_allData;
    }

    public void postDataObject(JSONObject data, String postUrl) throws IOException {

        HttpClient httpClient = HttpClientBuilder.create().build();
        HttpPost post = new HttpPost(postUrl);

        Gson gson = new Gson();
        String json = gson.toJson(new Ga4ghDataObject(data));
        json = "{\"data_object\":" + json + "}";
        //System.out.println(json);

        post.setEntity(new StringEntity(json));
        post.setHeader("Content-type", "application/json");
        httpClient.execute(post);
    }


    // Constructors

    public GcpHttp() {

    }

    public GcpHttp(String bucket_name, String prefix) {
        super();
        this.bucket_name = bucket_name;
        this.prefix = prefix;
        this.url = "https://www.googleapis.com/storage/v1/b/" + bucket_name + "/o?prefix=" + prefix;
    }

    public GcpHttp(String project_id, String bucket_name, String prefix) {
        super();
        this.project_id = project_id;
        this.bucket_name = bucket_name;
        this.prefix = prefix;
        this.url = "https://www.googleapis.com/storage/v1/b/" + bucket_name + "/o?prefix=" + prefix;
    }


    // Getters and setters

    public String getProject_id() {
        return project_id;
    }

    public void setProject_id(String project_id) {
        this.project_id = project_id;
    }

    public String getBucket_name() {
        return bucket_name;
    }

    public void setBucket_name(String bucket_name) {
        this.bucket_name = bucket_name;
        this.url = "https://www.googleapis.com/storage/v1/b/" + this.bucket_name + "/o?prefix=" + this.prefix;
    }

    public String getPrefix() {
        return prefix;
    }

    public void setPrefix(String prefix) {
        this.prefix = prefix;
        this.url = "https://www.googleapis.com/storage/v1/b/" + this.bucket_name + "/o?prefix=" + this.prefix;
    }

    public String getUrl() {
        return url;
    }

}
