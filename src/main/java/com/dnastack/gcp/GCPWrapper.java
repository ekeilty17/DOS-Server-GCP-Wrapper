package com.dnastack.gcp;

import java.io.IOException;

import org.json.JSONArray;

import com.dnastack.gcp.http.GCPHttp;

public class GCPWrapper {
	
	public static void main(String[] args) throws IOException {
		
		GCPHttp gcpHttp = new GCPHttp("genomics-public-data", "genomics-public-data", "1000-genomes/bam");
		
		JSONArray allData = gcpHttp.getData();
		
		for (int i = 0; i < allData.length(); i++) {
			gcpHttp.postDataObject(allData.getJSONObject(i), "http://localhost:8080/dataobjects");
		}
		
		gcpHttp.setPrefix("1000-genomes/vcf");
		
		allData = gcpHttp.getData();
		
		for (int i = 0; i < allData.length(); i++) {
			gcpHttp.postDataObject(allData.getJSONObject(i), "http://localhost:8080/dataobjects");
		}
		
		System.out.println("Done.");
		
	}
	
}
