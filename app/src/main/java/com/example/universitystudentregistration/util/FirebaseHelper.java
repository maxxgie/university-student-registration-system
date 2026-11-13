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
import java.util.List;

public class FirebaseHelper {

    private static final String TAG = "FirebaseHelper";
    private final DatabaseReference databaseReference;
    private final SharedPrefManager sharedPrefManager;
    private final Context context;
    private final boolean firebaseInitialized;

    public FirebaseHelper(Context context) {
        this.context = context;
        this.sharedPrefManager = new SharedPrefManager(context);
        
        // Check if Firebase is initialized
        this.firebaseInitialized = !FirebaseApp.getApps(context).isEmpty();
        
        if (firebaseInitialized) {
            this.databaseReference = FirebaseDatabase.getInstance().getReference();
            // Enable offline persistence
            FirebaseDatabase.getInstance().setPersistenceEnabled(true);
            Log.d(TAG, "Firebase initialized with offline persistence");
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
        if (!isFirebaseReady() || !NetworkUtil.isConnected(context)) {
            // Save to offline queue
            sharedPrefManager.addOfflineStudent(student);
            if (listener != null) listener.onSuccess();
            return;
        }

        String key = databaseReference.child("students").push().getKey();
        if (key != null) {
            student.setFirebaseKey(key);
            databaseReference.child("students").child(key).setValue(student)
                    .addOnSuccessListener(aVoid -> {
                        Log.d(TAG, "Student saved to Firebase: " + student.getIdNo());
                        sharedPrefManager.removeOfflineStudent(student);
                        if (listener != null) listener.onSuccess();
                    })
                    .addOnFailureListener(e -> {
                        Log.e(TAG, "Failed to save student to Firebase", e);
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

        // Sync students
        List<Student> offlineStudents = sharedPrefManager.getOfflineStudents();
        for (Student student : offlineStudents) {
            String key = databaseReference.child("students").push().getKey();
            if (key != null) {
                student.setFirebaseKey(key);
                databaseReference.child("students").child(key).setValue(student)
                        .addOnSuccessListener(aVoid -> sharedPrefManager.removeOfflineStudent(student));
            }
        }

        // Sync book requests
        List<BookRequest> offlineBookRequests = sharedPrefManager.getOfflineBookRequests();
        for (BookRequest bookRequest : offlineBookRequests) {
            String key = databaseReference.child("bookRequests").push().getKey();
            if (key != null) {
                bookRequest.setFirebaseKey(key);
                databaseReference.child("bookRequests").child(key).setValue(bookRequest)
                        .addOnSuccessListener(aVoid -> sharedPrefManager.removeOfflineBookRequest(bookRequest));
            }
        }

        // Sync hostel infos
        List<HostelInfo> offlineHostelInfos = sharedPrefManager.getOfflineHostelInfos();
        for (HostelInfo hostelInfo : offlineHostelInfos) {
            String key = databaseReference.child("hostelInfos").push().getKey();
            if (key != null) {
                hostelInfo.setFirebaseKey(key);
                databaseReference.child("hostelInfos").child(key).setValue(hostelInfo)
                        .addOnSuccessListener(aVoid -> sharedPrefManager.removeOfflineHostelInfo(hostelInfo));
            }
        }

        // Sync payments
        List<Payment> offlinePayments = sharedPrefManager.getOfflinePayments();
        for (Payment payment : offlinePayments) {
            String key = databaseReference.child("payments").push().getKey();
            if (key != null) {
                payment.setFirebaseKey(key);
                databaseReference.child("payments").child(key).setValue(payment)
                        .addOnSuccessListener(aVoid -> sharedPrefManager.removeOfflinePayment(payment));
            }
        }
    }

    public interface OnFirebaseOperationListener {
        void onSuccess();
        void onFailure(String errorMessage);
    }
}