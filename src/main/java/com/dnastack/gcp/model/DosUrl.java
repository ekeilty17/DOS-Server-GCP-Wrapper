package com.dnastack.gcp.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DosUrl {

    private String url;
    private Map<String, String> system_metadata;
    private Map<String, String> user_metadata;

}
