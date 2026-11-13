package com.example.universitystudentregistration.model;

import com.google.firebase.database.Exclude;
import com.google.firebase.database.IgnoreExtraProperties;
import java.io.Serializable;

@IgnoreExtraProperties
public class Payment implements Serializable {

    private String studentIdNo;
    private String phoneNumber;
    private double amount;
    private String transactionCode;
    private String timestamp;
    private String status;
    private String firebaseKey;

    public Payment() {
    }

    public Payment(String studentIdNo, String phoneNumber, double amount) {
        this.studentIdNo = studentIdNo;
        this.phoneNumber = phoneNumber;
        this.amount = amount;
        this.timestamp = String.valueOf(System.currentTimeMillis());
        this.status = "pending";
    }

    public String getStudentIdNo() {
        return studentIdNo;
    }

    public void setStudentIdNo(String studentIdNo) {
        this.studentIdNo = studentIdNo;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public String getTransactionCode() {
        return transactionCode;
    }

    public void setTransactionCode(String transactionCode) {
        this.transactionCode = transactionCode;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    @Exclude
    public String getFirebaseKey() {
        return firebaseKey;
    }

    @Exclude
    public void setFirebaseKey(String firebaseKey) {
        this.firebaseKey = firebaseKey;
    }
}