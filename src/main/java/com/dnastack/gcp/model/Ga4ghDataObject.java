package com.dnastack.gcp.model;

import lombok.Data;
import org.json.JSONObject;

import java.util.*;

@Data
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

    public Ga4ghDataObject() {

    }

    public Ga4ghDataObject(String id, String name, String size, String created, String updated, String version,
                           String mimeType, List<Checksum> checksums, List<DosUrl> urls, String description, List<String> aliases) {
        this.id = id;
        this.name = name;
        this.size = size;
        this.created = created;
        this.updated = updated;
        this.version = version;
        this.mimeType = mimeType;
        this.checksums = checksums;
        this.urls = urls;
        this.description = description;
        this.aliases = aliases;
    }

    public Ga4ghDataObject(JSONObject data) {
        this.id = data.getString("id");
        this.name = data.getString("name");
        this.size = data.getString("size");
        this.created = data.getString("timeCreated");
        this.updated = data.getString("updated");
        this.version = "1.0.0";
        this.mimeType = null;
        this.checksums = new ArrayList<>();

        Map<String, String> system_metadata = new HashMap<>();
        Map<String, String> user_metadata = new HashMap<>();
        this.urls = new ArrayList<>(Arrays.asList(new DosUrl(data.getString("selfLink"), system_metadata, user_metadata)));

        this.description = null;
        this.aliases = new ArrayList<>();
    }

}
