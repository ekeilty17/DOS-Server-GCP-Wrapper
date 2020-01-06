package com.dnastack.drsclient.client.config;

import lombok.Getter;

import static com.dnastack.drsclient.client.config.Environment.getOptionalEnv;
import static com.dnastack.drsclient.client.config.Environment.getRequiredEnv;

public class GcsConfig {
    @Getter
    private static final String googleApplicationCredentials = getRequiredEnv("GOOGLE_APPLICATION_CREDENTIALS");

    @Getter
    private static final String billingProjectId = getOptionalEnv("GCS_BILLING_PROJECT_ID");

}
