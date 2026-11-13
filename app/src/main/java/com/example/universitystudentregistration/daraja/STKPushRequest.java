package com.example.universitystudentregistration.daraja;

import com.google.gson.annotations.SerializedName;

public class STKPushRequest {
    @SerializedName("BusinessShortCode")
    private String businessShortCode;

    @SerializedName("Password")
    private String password;

    @SerializedName("Timestamp")
    private String timestamp;

    @SerializedName("TransactionType")
    private String transactionType;

    @SerializedName("Amount")
    private String amount;

    @SerializedName("PartyA")
    private String partyA;

    @SerializedName("PartyB")
    private String partyB;

    @SerializedName("PhoneNumber")
    private String phoneNumber;

    @SerializedName("CallBackURL")
    private String callBackURL;

    @SerializedName("AccountReference")
    private String accountReference;

    @SerializedName("TransactionDesc")
    private String transactionDesc;

    // Getters and setters
    public String getBusinessShortCode() { return businessShortCode; }
    public void setBusinessShortCode(String businessShortCode) { this.businessShortCode = businessShortCode; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
    public String getTimestamp() { return timestamp; }
    public void setTimestamp(String timestamp) { this.timestamp = timestamp; }
    public String getTransactionType() { return transactionType; }
    public void setTransactionType(String transactionType) { this.transactionType = transactionType; }
    public String getAmount() { return amount; }
    public void setAmount(String amount) { this.amount = amount; }
    public String getPartyA() { return partyA; }
    public void setPartyA(String partyA) { this.partyA = partyA; }
    public String getPartyB() { return partyB; }
    public void setPartyB(String partyB) { this.partyB = partyB; }
    public String getPhoneNumber() { return phoneNumber; }
    public void setPhoneNumber(String phoneNumber) { this.phoneNumber = phoneNumber; }
    public String getCallBackURL() { return callBackURL; }
    public void setCallBackURL(String callBackURL) { this.callBackURL = callBackURL; }
    public String getAccountReference() { return accountReference; }
    public void setAccountReference(String accountReference) { this.accountReference = accountReference; }
    public String getTransactionDesc() { return transactionDesc; }
    public void setTransactionDesc(String transactionDesc) { this.transactionDesc = transactionDesc; }
}