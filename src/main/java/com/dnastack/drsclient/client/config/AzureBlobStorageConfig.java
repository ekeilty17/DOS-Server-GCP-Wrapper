package com.dnastack.drsclient.client.config;

import lombok.Getter;

import static com.dnastack.drsclient.client.config.Environment.getRequiredEnv;

public class AzureBlobStorageConfig {

    @Getter
    private static final String subscriptionId = getRequiredEnv("AZURE_SUBSCRIPTION_ID");

    @Getter
    private static final String storageAccount = getRequiredEnv("AZURE_STORAGE_ACCOUNT");

    @Getter
    private static final String azConnectionString = getRequiredEnv("AZURE_CONNECTION_STRING");

}
