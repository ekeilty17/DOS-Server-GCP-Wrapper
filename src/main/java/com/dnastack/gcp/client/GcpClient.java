package com.dnastack.gcp.client;

import com.dnastack.gcp.model.Ga4ghDataObject;
import lombok.Getter;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static java.util.Objects.requireNonNull;

@Getter
public class GcpClient {

    private String bucketName;
    private URI bucketUrl;

    public GcpClient(String bucketName) {
        this.bucketName = requireNonNull(bucketName);
        this.bucketUrl = URI.create("https://www.googleapis.com/storage/v1/b/" + bucketName + "/");
    }

    public List<Ga4ghDataObject> getDataObjects(String prefix) throws IOException {

        URI queryUrl = bucketUrl.resolve("o?prefix=" + prefix);
        String gcsResults = new BufferedReader(new InputStreamReader(queryUrl.toURL().openStream()))
                .lines()
                .collect(Collectors.joining());

        JSONObject parsedResponse = new JSONObject(gcsResults);
        JSONArray items = parsedResponse.getJSONArray("items");
        System.out.println(bucketName + " " + prefix + ": Found " + items.length() + " items");

        List<Ga4ghDataObject> result = new ArrayList<>(items.length());
        for (int i = 0; i < items.length(); i++) {
            result.add(new Ga4ghDataObject((JSONObject) items.get(i)));
        }
        return result;
    }

}
