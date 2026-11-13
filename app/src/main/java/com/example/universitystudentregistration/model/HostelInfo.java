package com.example.universitystudentregistration.model;

import com.google.firebase.database.Exclude;
import com.google.firebase.database.IgnoreExtraProperties;
import java.io.Serializable;

@IgnoreExtraProperties
public class HostelInfo implements Serializable {

    private String hostelLocation;
    private String roomType;
    private String isSharable;
    private String studentIdNo;
    private String timestamp;
    private String status;
    private String firebaseKey;

    public HostelInfo() {
    }

    public HostelInfo(String hostelLocation, String roomType, String isSharable, String studentIdNo) {
        this.hostelLocation = hostelLocation;
        this.roomType = roomType;
        this.isSharable = isSharable;
        this.studentIdNo = studentIdNo;
        this.timestamp = String.valueOf(System.currentTimeMillis());
        this.status = "pending";
    }

    public String getHostelLocation() {
        return hostelLocation;
    }

    public void setHostelLocation(String hostelLocation) {
        this.hostelLocation = hostelLocation;
    }

    public String getRoomType() {
        return roomType;
    }

    public void setRoomType(String roomType) {
        this.roomType = roomType;
    }

    public String getIsSharable() {
        return isSharable;
    }

    public void setIsSharable(String isSharable) {
        this.isSharable = isSharable;
    }

    public String getStudentIdNo() {
        return studentIdNo;
    }

    public void setStudentIdNo(String studentIdNo) {
        this.studentIdNo = studentIdNo;
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