package com.dnastack.gcp.model;

import java.util.Map;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class URL {
	
	private String url;
	private Map<String, String> system_metadata;
	private Map<String, String> user_metadata;
	
}
