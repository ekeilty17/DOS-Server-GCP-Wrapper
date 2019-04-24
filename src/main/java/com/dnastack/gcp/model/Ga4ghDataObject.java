package com.dnastack.gcp.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Value;
import org.json.JSONObject;

import java.util.*;

@Value
@AllArgsConstructor
@Builder(toBuilder = true)
public class Ga4ghDataObject {

    private String id;
    private String name;
    private String size;
    private String created;
    private String updated;
    private String version;
    private String mimeType;
    private List<Checksum> checksums;
    private List<DosUrl> urls;
    private String description;
    private List<String> aliases;
}
