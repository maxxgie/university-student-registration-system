package com.example.universitystudentregistration;

import android.app.Application;
import android.util.Log;

import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.database.FirebaseDatabase;

public class UniversityApplication extends Application {

    private static final String TAG = "UniversityApplication";

    @Override
    public void onCreate() {
        super.onCreate();

        // Initialize Firebase conditionally
        initializeFirebase();
    }

    private void initializeFirebase() {
        try {
            // Check if google-services.json exists by trying to get default app
            if (FirebaseApp.getApps(this).isEmpty()) {
                // No Firebase configured - this is expected without google-services.json
                Log.i(TAG, "Firebase not configured - running in offline mode");
            } else {
                // Firebase is configured, enable offline persistence
                FirebaseDatabase.getInstance().setPersistenceEnabled(true);
                Log.i(TAG, "Firebase initialized with offline persistence");
            }
        } catch (IllegalStateException e) {
            // Firebase not configured - run in offline mode
            Log.w(TAG, "Firebase not configured: " + e.getMessage());
        } catch (Exception e) {
            Log.e(TAG, "Error initializing Firebase", e);
        }
    }
}