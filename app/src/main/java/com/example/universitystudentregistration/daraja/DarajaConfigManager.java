package com.example.universitystudentregistration.daraja;

import android.content.Context;
import android.util.Log;

import com.google.gson.Gson;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

public class DarajaConfigManager {

    private static final String TAG = "DarajaConfigManager";
    private static final String CONFIG_FILE = "daraja_config.json";

    private final DarajaConfig darajaConfig;

    public DarajaConfigManager(Context context) {
        darajaConfig = loadConfig(context);
    }

    private DarajaConfig loadConfig(Context context) {
        try {
            InputStream inputStream = context.getAssets().open(CONFIG_FILE);
            InputStreamReader reader = new InputStreamReader(inputStream, StandardCharsets.UTF_8);
            Gson gson = new Gson();
            return gson.fromJson(reader, DarajaConfig.class);
        } catch (Exception e) {
            Log.e(TAG, "Failed to load Daraja config", e);
            return null;
        }
    }

    public DarajaConfig.DarajaSettings getSettings() {
        return darajaConfig != null ? darajaConfig.getDaraja() : null;
    }

    public DarajaConfig.EnvironmentConfig getActiveEnvironment() {
        DarajaConfig.DarajaSettings settings = getSettings();
        return settings != null ? settings.getActiveConfig() : null;
    }

    public DarajaConfig.AppConfig getAppConfig() {
        DarajaConfig.DarajaSettings settings = getSettings();
        return settings != null ? settings.getApp() : null;
    }

    public boolean isConfigured() {
        DarajaConfig.EnvironmentConfig env = getActiveEnvironment();
        return env != null &&
                env.getConsumerKey() != null &&
                !env.getConsumerKey().startsWith("YOUR_") &&
                env.getConsumerSecret() != null &&
                !env.getConsumerSecret().startsWith("YOUR_");
    }
}