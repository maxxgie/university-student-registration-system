package com.example.universitystudentregistration;

import android.app.Application;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkRequest;
import android.os.Build;
import android.util.Log;

import com.google.firebase.FirebaseApp;
import com.google.firebase.database.FirebaseDatabase;

public class UniversityApplication extends Application {

    private static final String TAG = "UniversityApplication";
    private ConnectivityManager.NetworkCallback networkCallback;

    @Override
    public void onCreate() {
        super.onCreate();

        // Initialize Firebase conditionally
        initializeFirebase();

        // Register network callback for auto-sync when connectivity restored
        registerNetworkCallback();
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

    private void registerNetworkCallback() {
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivityManager == null) return;

        networkCallback = new ConnectivityManager.NetworkCallback() {
            @Override
            public void onCapabilitiesChanged(Network network, NetworkCapabilities networkCapabilities) {
                super.onCapabilitiesChanged(network, networkCapabilities);
                if (hasInternetCapability(networkCapabilities)) {
                    Log.i(TAG, "Internet connectivity restored - triggering auto-sync");
                    triggerAutoSync();
                }
            }

            @Override
            public void onLost(Network network) {
                super.onLost(network);
                Log.i(TAG, "Internet connectivity lost");
            }
        };

        NetworkRequest request = new NetworkRequest.Builder()
                .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
                .build();

        try {
            connectivityManager.registerNetworkCallback(request, networkCallback);
        } catch (Exception e) {
            Log.e(TAG, "Failed to register network callback", e);
        }
    }

    private boolean hasInternetCapability(NetworkCapabilities capabilities) {
        return capabilities != null &&
                (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) ||
                        capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) ||
                        capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET));
    }

    private void triggerAutoSync() {
        // Use SharedPrefManager and FirebaseHelper to sync offline data
        try {
            com.example.universitystudentregistration.util.SharedPrefManager prefManager = 
                    new com.example.universitystudentregistration.util.SharedPrefManager(this);
            com.example.universitystudentregistration.util.FirebaseHelper firebaseHelper = 
                    new com.example.universitystudentregistration.util.FirebaseHelper(this);
            
            // Only sync if we have offline data
            if (!prefManager.getOfflineStudents().isEmpty() ||
                !prefManager.getOfflineBookRequests().isEmpty() ||
                !prefManager.getOfflineHostelInfos().isEmpty() ||
                !prefManager.getOfflinePayments().isEmpty()) {
                
                firebaseHelper.syncOfflineData();
                Log.i(TAG, "Auto-sync triggered successfully");
            }
        } catch (Exception e) {
            Log.e(TAG, "Error during auto-sync", e);
        }
    }

    @Override
    public void onTerminate() {
        super.onTerminate();
        if (networkCallback != null) {
            ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
            if (connectivityManager != null) {
                try {
                    connectivityManager.unregisterNetworkCallback(networkCallback);
                } catch (Exception e) {
                    Log.e(TAG, "Error unregistering network callback", e);
                }
            }
        }
    }
}