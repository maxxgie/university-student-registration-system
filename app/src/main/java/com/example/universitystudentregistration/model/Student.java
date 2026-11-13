package com.example.universitystudentregistration.model;

import com.google.firebase.database.Exclude;
import com.google.firebase.database.IgnoreExtraProperties;
import java.io.Serializable;

@IgnoreExtraProperties
public class Student implements Serializable {

    private String fullNames;
    private String idNo;
    private String registrationNumber;
    private String gender;
    private String department;
    private String course;
    private String timestamp;
    private String status;
    private String firebaseKey;

    public Student() {
    }

    public Student(String fullNames, String idNo, String registrationNumber,
                   String gender, String department, String course) {
        this.fullNames = fullNames;
        this.idNo = idNo;
        this.registrationNumber = registrationNumber;
        this.gender = gender;
        this.department = department;
        this.course = course;
        this.timestamp = String.valueOf(System.currentTimeMillis());
        this.status = "pending";
    }

    public String getFullNames() {
        return fullNames;
    }

    public void setFullNames(String fullNames) {
        this.fullNames = fullNames;
    }

    public String getIdNo() {
        return idNo;
    }

    public void setIdNo(String idNo) {
        this.idNo = idNo;
    }

    public String getRegistrationNumber() {
        return registrationNumber;
    }

    public void setRegistrationNumber(String registrationNumber) {
        this.registrationNumber = registrationNumber;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public String getDepartment() {
        return department;
    }

    public void setDepartment(String department) {
        this.department = department;
    }

    public String getCourse() {
        return course;
    }

    public void setCourse(String course) {
        this.course = course;
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