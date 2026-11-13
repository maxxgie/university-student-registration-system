package com.example.universitystudentregistration.daraja;

import com.google.gson.annotations.SerializedName;

public class STKPushResponse {
    @SerializedName("MerchantRequestID")
    private String merchantRequestID;

    @SerializedName("CheckoutRequestID")
    private String checkoutRequestID;

    @SerializedName("ResponseCode")
    private String responseCode;

    @SerializedName("ResponseDescription")
    private String responseDescription;

    @SerializedName("CustomerMessage")
    private String customerMessage;

    // Getters and setters
    public String getMerchantRequestID() { return merchantRequestID; }
    public void setMerchantRequestID(String merchantRequestID) { this.merchantRequestID = merchantRequestID; }
    public String getCheckoutRequestID() { return checkoutRequestID; }
    public void setCheckoutRequestID(String checkoutRequestID) { this.checkoutRequestID = checkoutRequestID; }
    public String getResponseCode() { return responseCode; }
    public void setResponseCode(String responseCode) { this.responseCode = responseCode; }
    public String getResponseDescription() { return responseDescription; }
    public void setResponseDescription(String responseDescription) { this.responseDescription = responseDescription; }
    public String getCustomerMessage() { return customerMessage; }
    public void setCustomerMessage(String customerMessage) { this.customerMessage = customerMessage; }

    public boolean isSuccess() {
        return "0".equals(responseCode);
    }
}