package com.example.universitystudentregistration.model;

import com.google.firebase.database.Exclude;
import com.google.firebase.database.IgnoreExtraProperties;
import java.io.Serializable;

@IgnoreExtraProperties
public class BookRequest implements Serializable {

    private String bookIsbn;
    private String author;
    private String title;
    private String dateBorrowed;
    private String dateOfReturn;
    private String studentIdNo;
    private String timestamp;
    private String status;
    private String firebaseKey;

    public BookRequest() {
    }

    public BookRequest(String bookIsbn, String author, String title,
                       String dateBorrowed, String dateOfReturn, String studentIdNo) {
        this.bookIsbn = bookIsbn;
        this.author = author;
        this.title = title;
        this.dateBorrowed = dateBorrowed;
        this.dateOfReturn = dateOfReturn;
        this.studentIdNo = studentIdNo;
        this.timestamp = String.valueOf(System.currentTimeMillis());
        this.status = "pending";
    }

    public String getBookIsbn() {
        return bookIsbn;
    }

    public void setBookIsbn(String bookIsbn) {
        this.bookIsbn = bookIsbn;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDateBorrowed() {
        return dateBorrowed;
    }

    public void setDateBorrowed(String dateBorrowed) {
        this.dateBorrowed = dateBorrowed;
    }

    public String getDateOfReturn() {
        return dateOfReturn;
    }

    public void setDateOfReturn(String dateOfReturn) {
        this.dateOfReturn = dateOfReturn;
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