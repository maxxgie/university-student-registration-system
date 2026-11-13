package com.example.universitystudentregistration.daraja;

import android.content.Context;
import android.util.Base64;
import android.util.Log;

import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class DarajaClient {
    private static final String TAG = "DarajaClient";
    private final DarajaApiService apiService;
    private final DarajaConfigManager configManager;
    private String cachedAccessToken = null;
    private long tokenExpiryTime = 0;

    public DarajaClient(Context context) {
        this.configManager = new DarajaConfigManager(context);
        this.apiService = buildApiService();
    }

    public DarajaConfigManager getConfigManager() {
        return configManager;
    }

    private DarajaApiService buildApiService() {
        HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
        logging.setLevel(HttpLoggingInterceptor.Level.BODY);

        OkHttpClient client = new OkHttpClient.Builder()
                .addInterceptor(logging)
                .addInterceptor(new Interceptor() {
                    @Override
                    public Response intercept(Chain chain) throws IOException {
                        Request original = chain.request();
                        Request.Builder builder = original.newBuilder();
                        
                        // Add Bearer token for authenticated endpoints
                        if (cachedAccessToken != null) {
                            builder.header("Authorization", "Bearer " + cachedAccessToken);
                        }
                        
                        Request request = builder.build();
                        return chain.proceed(request);
                    }
                })
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .writeTimeout(30, TimeUnit.SECONDS)
                .build();

        DarajaConfig.EnvironmentConfig env = configManager.getActiveEnvironment();
        String baseUrl = env != null ? env.getBaseUrl() : "https://sandbox.safaricom.co.ke";

        return new Retrofit.Builder()
                .baseUrl(baseUrl + "/")
                .addConverterFactory(GsonConverterFactory.create())
                .client(client)
                .build()
                .create(DarajaApiService.class);
    }

    // Get access token from Daraja OAuth API
    public synchronized void fetchAccessToken(OnTokenFetchListener listener) {
        // Check if cached token is still valid
        if (cachedAccessToken != null && System.currentTimeMillis() < tokenExpiryTime) {
            if (listener != null) listener.onSuccess(cachedAccessToken);
            return;
        }

        DarajaConfig.EnvironmentConfig env = configManager.getActiveEnvironment();
        if (env == null) {
            if (listener != null) listener.onFailure("Daraja not configured");
            return;
        }

        String credentials = env.getConsumerKey() + ":" + env.getConsumerSecret();
        String encodedCredentials = Base64.encodeToString(credentials.getBytes(), Base64.NO_WRAP);

        OkHttpClient authClient = new OkHttpClient.Builder()
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .build();

        Request request = new Request.Builder()
                .url(env.getBaseUrl() + "/oauth/v1/generate?grant_type=client_credentials")
                .header("Authorization", "Basic " + encodedCredentials)
                .build();

        authClient.newCall(request).enqueue(new okhttp3.Callback() {
            @Override
            public void onFailure(okhttp3.Call call, IOException e) {
                Log.e(TAG, "Failed to fetch access token", e);
                if (listener != null) listener.onFailure(e.getMessage());
            }

            @Override
            public void onResponse(okhttp3.Call call, Response response) throws IOException {
                if (response.isSuccessful() && response.body() != null) {
                    String responseBody = response.body().string();
                    Log.d(TAG, "Token response: " + responseBody);
                    
                    com.google.gson.Gson gson = new com.google.gson.Gson();
                    AuthResponse authResponse = gson.fromJson(responseBody, AuthResponse.class);
                    
                    if (authResponse != null && authResponse.getAccessToken() != null) {
                        cachedAccessToken = authResponse.getAccessToken();
                        // Token expires in ~1 hour, cache for 50 minutes
                        tokenExpiryTime = System.currentTimeMillis() + (50 * 60 * 1000);
                        if (listener != null) listener.onSuccess(cachedAccessToken);
                    } else {
                        if (listener != null) listener.onFailure("Invalid token response");
                    }
                } else {
                    String error = response.body() != null ? "HTTP " + response.code() : "Unknown error";
                    if (response.body() != null) response.body().close();
                    Log.e(TAG, "Token fetch failed: " + error);
                    if (listener != null) listener.onFailure("Token fetch failed: " + error);
                }
            }
        });
    }

    // Initiate STK Push
    public void initiateSTKPush(String phoneNumber, double amount, String accountReference, 
                                String transactionDesc, OnSTKPushListener listener) {
        fetchAccessToken(new OnTokenFetchListener() {
            @Override
            public void onSuccess(String accessToken) {
                performSTKPush(phoneNumber, amount, accountReference, transactionDesc, listener);
            }

            @Override
            public void onFailure(String error) {
                if (listener != null) listener.onFailure("Auth failed: " + error);
            }
        });
    }

    private void performSTKPush(String phoneNumber, double amount, String accountReference,
                                String transactionDesc, OnSTKPushListener listener) {
        DarajaConfig.EnvironmentConfig env = configManager.getActiveEnvironment();
        DarajaConfig.AppConfig appConfig = configManager.getAppConfig();

        if (env == null || appConfig == null) {
            if (listener != null) listener.onFailure("Daraja not configured");
            return;
        }

        // Format phone number to 2547XXXXXXXX
        String formattedPhone = formatPhoneNumber(phoneNumber);
        if (formattedPhone == null) {
            if (listener != null) listener.onFailure("Invalid phone number format");
            return;
        }

        // Generate timestamp
        String timestamp = new SimpleDateFormat("yyyyMMddHHmmss", Locale.getDefault()).format(new Date());

        // Generate password: Base64(ShortCode + Passkey + Timestamp)
        String password = generatePassword(env.getShortcode(), env.getPasskey(), timestamp);

        STKPushRequest request = new STKPushRequest();
        request.setBusinessShortCode(env.getShortcode());
        request.setPassword(password);
        request.setTimestamp(timestamp);
        request.setTransactionType("CustomerPayBillOnline");
        request.setAmount(String.valueOf((int) amount)); // Amount in whole shillings
        request.setPartyA(formattedPhone);
        request.setPartyB(env.getShortcode());
        request.setPhoneNumber(formattedPhone);
        request.setCallBackURL(env.getCallbackUrl());
        request.setAccountReference(accountReference != null ? accountReference : appConfig.getAccountReference());
        request.setTransactionDesc(transactionDesc != null ? transactionDesc : appConfig.getTransactionDesc());

        apiService.stkPush(request).enqueue(new Callback<STKPushResponse>() {
            @Override
            public void onResponse(Call<STKPushResponse> call, retrofit2.Response<STKPushResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    STKPushResponse stkResponse = response.body();
                    Log.d(TAG, "STK Push response: " + stkResponse.getResponseCode() + " - " + stkResponse.getResponseDescription());
                    
                    if (stkResponse.isSuccess()) {
                        if (listener != null) listener.onSuccess(stkResponse);
                    } else {
                        if (listener != null) listener.onFailure(stkResponse.getResponseDescription());
                    }
                } else {
                    String error = "STK Push failed";
                    try {
                        if (response.errorBody() != null) {
                            error = response.errorBody().string();
                        }
                    } catch (IOException e) {
                        Log.e(TAG, "Error reading error body", e);
                    }
                    Log.e(TAG, "STK Push error: " + error);
                    if (listener != null) listener.onFailure("STK Push error: " + error);
                }
            }

            @Override
            public void onFailure(Call<STKPushResponse> call, Throwable t) {
                Log.e(TAG, "STK Push network error", t);
                if (listener != null) listener.onFailure("Network error: " + t.getMessage());
            }
        });
    }

    private String formatPhoneNumber(String phone) {
        if (phone == null) return null;
        
        // Remove non-digits
        String digits = phone.replaceAll("[^0-9]", "");
        
        // Convert to 2547XXXXXXXX format
        if (digits.startsWith("254") && digits.length() == 12) {
            return digits;
        } else if (digits.startsWith("07") && digits.length() == 10) {
            return "254" + digits.substring(1);
        } else if (digits.startsWith("7") && digits.length() == 9) {
            return "254" + digits;
        }
        
        return null;
    }

    private String generatePassword(String shortCode, String passkey, String timestamp) {
        String data = shortCode + passkey + timestamp;
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(data.getBytes());
            return Base64.encodeToString(hash, Base64.NO_WRAP);
        } catch (NoSuchAlgorithmException e) {
            Log.e(TAG, "Failed to generate password", e);
            return "";
        }
    }

    public interface OnTokenFetchListener {
        void onSuccess(String accessToken);
        void onFailure(String error);
    }

    public interface OnSTKPushListener {
        void onSuccess(STKPushResponse response);
        void onFailure(String error);
    }
}