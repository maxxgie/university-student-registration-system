package com.example.universitystudentregistration.daraja;

import com.google.gson.annotations.SerializedName;

public class DarajaConfig {

    @SerializedName("daraja")
    private DarajaSettings daraja;

    public DarajaSettings getDaraja() {
        return daraja;
    }

    public void setDaraja(DarajaSettings daraja) {
        this.daraja = daraja;
    }

    public static class DarajaSettings {
        @SerializedName("environment")
        private String environment;

        @SerializedName("sandbox")
        private EnvironmentConfig sandbox;

        @SerializedName("production")
        private EnvironmentConfig production;

        @SerializedName("app")
        private AppConfig app;

        public String getEnvironment() {
            return environment;
        }

        public EnvironmentConfig getActiveConfig() {
            return "production".equals(environment) ? production : sandbox;
        }

        public AppConfig getApp() {
            return app;
        }
    }

    public static class EnvironmentConfig {
        @SerializedName("base_url")
        private String baseUrl;

        @SerializedName("consumer_key")
        private String consumerKey;

        @SerializedName("consumer_secret")
        private String consumerSecret;

        @SerializedName("shortcode")
        private String shortcode;

        @SerializedName("passkey")
        private String passkey;

        @SerializedName("callback_url")
        private String callbackUrl;

        public String getBaseUrl() {
            return baseUrl;
        }

        public String getConsumerKey() {
            return consumerKey;
        }

        public String getConsumerSecret() {
            return consumerSecret;
        }

        public String getShortcode() {
            return shortcode;
        }

        public String getPasskey() {
            return passkey;
        }

        public String getCallbackUrl() {
            return callbackUrl;
        }
    }

    public static class AppConfig {
        @SerializedName("account_reference")
        private String accountReference;

        @SerializedName("transaction_desc")
        private String transactionDesc;

        public String getAccountReference() {
            return accountReference;
        }

        public String getTransactionDesc() {
            return transactionDesc;
        }
    }
}