package com.dnastack.gcp;

import java.io.IOException;
import java.net.URI;
import java.util.List;
import com.dnastack.gcp.client.DosClient;
import com.dnastack.gcp.client.GcpClient;
import com.dnastack.gcp.model.Ga4ghDataObject;

public class GCPWrapper {

    public static void main(String[] args) throws IOException {

        String serverUrl = requiredEnv("DOS_SERVER_URL");
        if (!serverUrl.endsWith("/")) {
            serverUrl += "/";
        }

        DosClient dosClient =
                new DosClient(
                        URI.create(serverUrl),
                        requiredEnv("DOS_SERVER_USERNAME"),
                        requiredEnv("DOS_SERVER_PASSWORD"),
                        10000);

        GcpClient gcpHttp = new GcpClient("genomics-public-data");

        gcpHttp.getDataObjects("1000-genomes/bam").forEach(dosClient::postDataObject);

        gcpHttp.getDataObjects("1000-genomes/vcf").forEach(dosClient::postDataObject);

        gcpHttp = new GcpClient("pgc-data");

        List<Ga4ghDataObject> objects = gcpHttp.getDataObjects(null);

        objects.forEach(dosClient::postDataObject);

        System.out.println("Done.");
    }

    private static String requiredEnv(String name) {
        String value = System.getenv(name);
        if (value == null) {
            System.err.println("Missing required environment variable " + name);
            System.exit(1);
        }
        return value;
    }
}
