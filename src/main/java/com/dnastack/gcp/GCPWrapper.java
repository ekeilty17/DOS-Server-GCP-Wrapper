package com.dnastack.gcp;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClientBuilder;
import org.json.JSONArray;
import org.json.JSONObject;

import com.dnastack.gcp.model.Ga4ghDataObject;
import com.google.gson.Gson;

public class GCPWrapper {
	private static final String PROJECT_ID = "genomics-public-data";
	private static final String BUCKET_NAME = "genomics-public-data";
	private static final String PREFIX = "1000-genomes/bam";
	 
	public static JSONArray getData(String url) throws IOException {
		
		// Getting Pagination information
    	String gcs_Results = new BufferedReader(new InputStreamReader(new URL(url).openStream())).lines().collect(Collectors.joining());
    	
    	JSONObject gcs_JSON = new JSONObject(gcs_Results);
    	JSONArray gcs_allData = gcs_JSON.getJSONArray("items");
    	
    	for (int i = 0; i < gcs_allData.length(); i++) {
    	    System.out.println(String.valueOf(i) + " " + gcs_allData.getJSONObject(i));
    	}
    	System.out.println('\n');
    	
    	return gcs_allData;
	}
	
	public static void postDataObject(JSONObject data, String url) throws ClientProtocolException, IOException {
    	
    	HttpClient httpClient = HttpClientBuilder.create().build();
		HttpPost post = new HttpPost(url);
		
		Gson gson = new Gson();
		String json = gson.toJson(new Ga4ghDataObject(data));
		json = "{\"data_object\":" + json + "}";
		//System.out.println(json);
		
		post.setEntity(new StringEntity(json));
		post.setHeader("Content-type", "application/json");
		httpClient.execute(post);
    }
	
	public static void main(String[] args) throws IOException {
		
		JSONArray allData = getData("https://www.googleapis.com/storage/v1/b/" + BUCKET_NAME + "/o?prefix=" + PREFIX);
		
		for (int i = 0; i < allData.length(); i++) {
			postDataObject(allData.getJSONObject(i), "http://localhost:8080/dataobjects");
		}
		
		System.out.println("Done.");
		
	}
	
}
