package com.example.universitystudentregistration.daraja;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Headers;
import retrofit2.http.POST;
import retrofit2.http.Query;

public interface DarajaApiService {

    @GET("oauth/v1/generate")
    @Headers("Authorization: Basic")
    Call<AuthResponse> getAccessToken(
            @Query("grant_type") String grantType
    );

    @POST("mpesa/stkpush/v1/processrequest")
    Call<STKPushResponse> stkPush(@Body STKPushRequest request);

    @POST("mpesa/stkpushquery/v1/query")
    Call<STKPushResponse> stkPushQuery(@Body STKPushRequest request);

    // Callback endpoint - this would be called by Safaricom
    // We include it here for reference but it's handled by a backend
    @POST("api/mpesa/callback")
    Call<ResponseBody> mpesaCallback(@Body MpesaCallback callback);
}