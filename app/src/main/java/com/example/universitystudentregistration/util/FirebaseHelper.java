package com.example.universitystudentregistration.util;

import android.content.Context;
import android.util.Log;
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

    private static final String TAG = "FirebaseHelper";
    private final DatabaseReference databaseReference;
    private final SharedPrefManager sharedPrefManager;
    private final Context context;
    private final boolean firebaseInitialized;
    private static boolean syncInProgress = false;

    public FirebaseHelper(Context context) {
        this.context = context;
        this.sharedPrefManager = new SharedPrefManager(context);
        
        // Check if Firebase is initialized
        this.firebaseInitialized = !FirebaseApp.getApps(context).isEmpty();
        
        if (firebaseInitialized) {
            this.databaseReference = FirebaseDatabase.getInstance().getReference();
            Log.d(TAG, "FirebaseHelper ready - persistence already enabled in Application");
        } else {
            this.databaseReference = null;
            Log.i(TAG, "Firebase not configured - running in offline mode");
        }
    }

    private boolean isFirebaseReady() {
        return firebaseInitialized && databaseReference != null;
    }

    // Save student to Firebase
    public void saveStudent(Student student, OnFirebaseOperationListener listener) {
        boolean ready = isFirebaseReady();
        boolean connected = NetworkUtil.isConnected(context);
        Log.d(TAG, "saveStudent: firebaseReady=" + ready + ", networkConnected=" + connected + ", studentId=" + student.getIdNo());
        if (!ready || !connected) {
            Log.d(TAG, "OFFLINE PATH: Saving to offline queue for " + student.getIdNo());
            sharedPrefManager.addOfflineStudent(student);
            if (listener != null) listener.onSuccess();
            return;
        }
        Log.d(TAG, "ONLINE PATH: Attempting Firebase write for " + student.getIdNo());

        String key = databaseReference.child("students").push().getKey();
        if (key != null) {
            student.setFirebaseKey(key);
            databaseReference.child("students").child(key).setValue(student)
                    .addOnSuccessListener(aVoid -> {
                        Log.d(TAG, "Firebase write SUCCESS for student: " + student.getIdNo());
                        sharedPrefManager.removeOfflineStudent(student);
                        if (listener != null) listener.onSuccess();
                    })
                    .addOnFailureListener(e -> {
                        Log.e(TAG, "Firebase write FAILED for student: " + student.getIdNo(), e);
                        sharedPrefManager.addOfflineStudent(student);
                        if (listener != null) listener.onFailure(e.getMessage());
                    });
        }
    }

    // Save book request to Firebase
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
                        Log.d(TAG, "Book request saved to Firebase: " + bookRequest.getBookIsbn());
                        sharedPrefManager.removeOfflineBookRequest(bookRequest);
                        if (listener != null) listener.onSuccess();
                    })
                    .addOnFailureListener(e -> {
                        Log.e(TAG, "Failed to save book request to Firebase", e);
                        sharedPrefManager.addOfflineBookRequest(bookRequest);
                        if (listener != null) listener.onFailure(e.getMessage());
                    });
        }
    }

    // Save hostel info to Firebase
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
                        Log.d(TAG, "Hostel info saved to Firebase: " + hostelInfo.getStudentIdNo());
                        sharedPrefManager.removeOfflineHostelInfo(hostelInfo);
                        if (listener != null) listener.onSuccess();
                    })
                    .addOnFailureListener(e -> {
                        Log.e(TAG, "Failed to save hostel info to Firebase", e);
                        sharedPrefManager.addOfflineHostelInfo(hostelInfo);
                        if (listener != null) listener.onFailure(e.getMessage());
                    });
        }
    }

    // Save payment to Firebase
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
                        Log.d(TAG, "Payment saved to Firebase: " + payment.getStudentIdNo());
                        sharedPrefManager.removeOfflinePayment(payment);
                        if (listener != null) listener.onSuccess();
                    })
                    .addOnFailureListener(e -> {
                        Log.e(TAG, "Failed to save payment to Firebase", e);
                        sharedPrefManager.addOfflinePayment(payment);
                        if (listener != null) listener.onFailure(e.getMessage());
                    });
        }
    }

    // Sync offline data when connectivity is restored
    public void syncOfflineData() {
        if (!isFirebaseReady() || !NetworkUtil.isConnected(context)) {
            return;
        }

        // Prevent concurrent syncs
        if (syncInProgress) {
            Log.d(TAG, "Sync already in progress, skipping");
            return;
        }
        syncInProgress = true;

        // Copy offline data and clear queues immediately to prevent duplicates
        List<Student> studentsToSync = new ArrayList<>(sharedPrefManager.getOfflineStudents());
        List<BookRequest> bookRequestsToSync = new ArrayList<>(sharedPrefManager.getOfflineBookRequests());
        List<HostelInfo> hostelInfosToSync = new ArrayList<>(sharedPrefManager.getOfflineHostelInfos());
        List<Payment> paymentsToSync = new ArrayList<>(sharedPrefManager.getOfflinePayments());

        // Clear offline queues NOW to prevent re-sync
        sharedPrefManager.clearOfflineStudents();
        sharedPrefManager.clearOfflineBookRequests();
        sharedPrefManager.clearOfflineHostelInfos();
        sharedPrefManager.clearOfflinePayments();

        int totalItems = studentsToSync.size() + bookRequestsToSync.size() + 
                        hostelInfosToSync.size() + paymentsToSync.size();
        Log.d(TAG, "Syncing " + totalItems + " items to Firebase");

        // Sync students
        for (Student student : studentsToSync) {
            String key = databaseReference.child("students").push().getKey();
            if (key != null) {
                student.setFirebaseKey(key);
                databaseReference.child("students").child(key).setValue(student)
                        .addOnSuccessListener(aVoid -> Log.d(TAG, "Synced student: " + student.getIdNo()))
                        .addOnFailureListener(e -> Log.e(TAG, "Failed to sync student: " + student.getIdNo(), e));
            }
        }

        // Sync book requests
        for (BookRequest bookRequest : bookRequestsToSync) {
            String key = databaseReference.child("bookRequests").push().getKey();
            if (key != null) {
                bookRequest.setFirebaseKey(key);
                databaseReference.child("bookRequests").child(key).setValue(bookRequest)
                        .addOnSuccessListener(aVoid -> Log.d(TAG, "Synced book request: " + bookRequest.getBookIsbn()))
                        .addOnFailureListener(e -> Log.e(TAG, "Failed to sync book request", e));
            }
        }

        // Sync hostel infos
        for (HostelInfo hostelInfo : hostelInfosToSync) {
            String key = databaseReference.child("hostelInfos").push().getKey();
            if (key != null) {
                hostelInfo.setFirebaseKey(key);
                databaseReference.child("hostelInfos").child(key).setValue(hostelInfo)
                        .addOnSuccessListener(aVoid -> Log.d(TAG, "Synced hostel info: " + hostelInfo.getStudentIdNo()))
                        .addOnFailureListener(e -> Log.e(TAG, "Failed to sync hostel info", e));
            }
        }

        // Sync payments
        for (Payment payment : paymentsToSync) {
            String key = databaseReference.child("payments").push().getKey();
            if (key != null) {
                payment.setFirebaseKey(key);
                databaseReference.child("payments").child(key).setValue(payment)
                        .addOnSuccessListener(aVoid -> Log.d(TAG, "Synced payment: " + payment.getStudentIdNo()))
                        .addOnFailureListener(e -> Log.e(TAG, "Failed to sync payment", e));
            }
        }

        syncInProgress = false;
        Log.d(TAG, "Auto-sync completed");
    }

    public interface OnFirebaseOperationListener {
        void onSuccess();
        void onFailure(String errorMessage);
    }
}