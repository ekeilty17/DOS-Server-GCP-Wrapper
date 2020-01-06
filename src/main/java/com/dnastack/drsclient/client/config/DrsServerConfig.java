package com.dnastack.drsclient.client.config;

import lombok.Getter;

import static com.dnastack.drsclient.client.config.Environment.getRequiredEnv;

public class DrsServerConfig {

    @Getter
    private static final String drsServerUrl = getRequiredEnv("DRS_SERVER_URL");

    @Getter
    private static final String drsUsername = getRequiredEnv("DRS_SERVER_USERNAME");

    @Getter
    private static final String drsPassword = getRequiredEnv("DRS_SERVER_PASSWORD");

}
