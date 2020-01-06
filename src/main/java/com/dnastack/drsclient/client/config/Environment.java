package com.dnastack.drsclient.client.config;

class Environment {
    public static String getRequiredEnv(String name) {
        String value = System.getenv(name);
        if (value == null) {
            System.err.println("Missing required environment variable " + name);
            System.exit(1);
        }
        return value;
    }


    public static String getOptionalEnv(String name) {
        return System.getenv(name);
    }

    public static String getOptionalEnv(String name, String defaultValue){
        String v = System.getenv(name);
        return (v==null) ? defaultValue : v;
    }
}
