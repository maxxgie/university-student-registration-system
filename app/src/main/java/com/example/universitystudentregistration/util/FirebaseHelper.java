package com.example.universitystudentregistration.util;

import android.content.Context;
import com.example.universitystudentregistration.model.BookRequest;
import com.example.universitystudentregistration.model.HostelInfo;
import com.example.universitystudentregistration.model.Payment;
import com.example.universitystudentregistration.model.Student;
import com.google.firebase.FirebaseApp;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import java.util.ArrayList;
import java.util.List;

public class FirebaseHelper {

    private final DatabaseReference databaseReference;
    private final SharedPrefManager sharedPrefManager;
    private final Context context;
    private final boolean firebaseInitialized;
    private static boolean syncInProgress = false;

    public FirebaseHelper(Context context) {
        this.context = context;
        this.sharedPrefManager = new SharedPrefManager(context);
        
        this.firebaseInitialized = !FirebaseApp.getApps(context).isEmpty();
        
        if (firebaseInitialized) {
            this.databaseReference = FirebaseDatabase.getInstance().getReference();
        } else {
            this.databaseReference = null;
        }
    }

    private boolean isFirebaseReady() {
        return firebaseInitialized && databaseReference != null;
    }

    public void saveStudent(Student student, OnFirebaseOperationListener listener) {
        if (!isFirebaseReady() || !NetworkUtil.isConnected(context)) {
            sharedPrefManager.addOfflineStudent(student);
            if (listener != null) listener.onSuccess();
            return;
        }

        String key = databaseReference.child("students").push().getKey();
        if (key != null) {
            student.setFirebaseKey(key);
            databaseReference.child("students").child(key).setValue(student)
                    .addOnSuccessListener(aVoid -> {
                        sharedPrefManager.removeOfflineStudent(student);
                        if (listener != null) listener.onSuccess();
                    })
                    .addOnFailureListener(e -> {
                        sharedPrefManager.addOfflineStudent(student);
                        if (listener != null) listener.onFailure(e.getMessage());
                    });
        }
    }

    public void saveBookRequest(BookRequest bookRequest, OnFirebaseOperationListener listener) {
        if (!isFirebaseReady() || !NetworkUtil.isConnected(context)) {
            sharedPrefManager.addOfflineBookRequest(bookRequest);
            if (listener != null) listener.onSuccess();
            return;
        }

        String key = databaseReference.child("bookRequests").push().getKey();
        if (key != null) {
            bookRequest.setFirebaseKey(key);
            databaseReference.child("bookRequests").child(key).setValue(bookRequest)
                    .addOnSuccessListener(aVoid -> {
                        sharedPrefManager.removeOfflineBookRequest(bookRequest);
                        if (listener != null) listener.onSuccess();
                    })
                    .addOnFailureListener(e -> {
                        sharedPrefManager.addOfflineBookRequest(bookRequest);
                        if (listener != null) listener.onFailure(e.getMessage());
                    });
        }
    }

    public void saveHostelInfo(HostelInfo hostelInfo, OnFirebaseOperationListener listener) {
        if (!isFirebaseReady() || !NetworkUtil.isConnected(context)) {
            sharedPrefManager.addOfflineHostelInfo(hostelInfo);
            if (listener != null) listener.onSuccess();
            return;
        }

        String key = databaseReference.child("hostelInfos").push().getKey();
        if (key != null) {
            hostelInfo.setFirebaseKey(key);
            databaseReference.child("hostelInfos").child(key).setValue(hostelInfo)
                    .addOnSuccessListener(aVoid -> {
                        sharedPrefManager.removeOfflineHostelInfo(hostelInfo);
                        if (listener != null) listener.onSuccess();
                    })
                    .addOnFailureListener(e -> {
                        sharedPrefManager.addOfflineHostelInfo(hostelInfo);
                        if (listener != null) listener.onFailure(e.getMessage());
                    });
        }
    }

    public void savePayment(Payment payment, OnFirebaseOperationListener listener) {
        if (!isFirebaseReady() || !NetworkUtil.isConnected(context)) {
            sharedPrefManager.addOfflinePayment(payment);
            if (listener != null) listener.onSuccess();
            return;
        }

        String key = databaseReference.child("payments").push().getKey();
        if (key != null) {
            payment.setFirebaseKey(key);
            databaseReference.child("payments").child(key).setValue(payment)
                    .addOnSuccessListener(aVoid -> {
                        sharedPrefManager.removeOfflinePayment(payment);
                        if (listener != null) listener.onSuccess();
                    })
                    .addOnFailureListener(e -> {
                        sharedPrefManager.addOfflinePayment(payment);
                        if (listener != null) listener.onFailure(e.getMessage());
                    });
        }
    }

    public void syncOfflineData() {
        if (!isFirebaseReady() || !NetworkUtil.isConnected(context)) {
            return;
        }

        if (syncInProgress) {
            return;
        }
        syncInProgress = true;

        List<Student> studentsToSync = new ArrayList<>(sharedPrefManager.getOfflineStudents());
        List<BookRequest> bookRequestsToSync = new ArrayList<>(sharedPrefManager.getOfflineBookRequests());
        List<HostelInfo> hostelInfosToSync = new ArrayList<>(sharedPrefManager.getOfflineHostelInfos());
        List<Payment> paymentsToSync = new ArrayList<>(sharedPrefManager.getOfflinePayments());

        sharedPrefManager.clearOfflineStudents();
        sharedPrefManager.clearOfflineBookRequests();
        sharedPrefManager.clearOfflineHostelInfos();
        sharedPrefManager.clearOfflinePayments();

        for (Student student : studentsToSync) {
            String key = databaseReference.child("students").push().getKey();
            if (key != null) {
                student.setFirebaseKey(key);
                databaseReference.child("students").child(key).setValue(student);
            }
        }

        for (BookRequest bookRequest : bookRequestsToSync) {
            String key = databaseReference.child("bookRequests").push().getKey();
            if (key != null) {
                bookRequest.setFirebaseKey(key);
                databaseReference.child("bookRequests").child(key).setValue(bookRequest);
            }
        }

        for (HostelInfo hostelInfo : hostelInfosToSync) {
            String key = databaseReference.child("hostelInfos").push().getKey();
            if (key != null) {
                hostelInfo.setFirebaseKey(key);
                databaseReference.child("hostelInfos").child(key).setValue(hostelInfo);
            }
        }

        for (Payment payment : paymentsToSync) {
            String key = databaseReference.child("payments").push().getKey();
            if (key != null) {
                payment.setFirebaseKey(key);
                databaseReference.child("payments").child(key).setValue(payment);
            }
        }

        syncInProgress = false;
    }

    public interface OnFirebaseOperationListener {
        void onSuccess();
        void onFailure(String errorMessage);
    }
}