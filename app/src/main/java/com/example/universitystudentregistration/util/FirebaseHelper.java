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

    // Find student by ID number in Firebase (for login persistence across reinstall)
    public void findStudentById(String idNo, OnStudentFoundListener listener) {
        if (!isFirebaseReady() || !NetworkUtil.isConnected(context)) {
            if (listener != null) listener.onNotFound("Offline - cannot query Firebase");
            return;
        }

        databaseReference.child("students")
                .orderByChild("idNo")
                .equalTo(idNo)
                .addListenerForSingleValueEvent(new com.google.firebase.database.ValueEventListener() {
                    @Override
                    public void onDataChange(com.google.firebase.database.DataSnapshot dataSnapshot) {
                        if (dataSnapshot.exists()) {
                            for (com.google.firebase.database.DataSnapshot snapshot : dataSnapshot.getChildren()) {
                                Student foundStudent = snapshot.getValue(Student.class);
                                if (foundStudent != null) {
                                    foundStudent.setFirebaseKey(snapshot.getKey());
                                    listener.onFound(foundStudent);
                                    return;
                                }
                            }
                        }
                        if (listener != null) listener.onNotFound("Student not found in database");
                    }

                    @Override
                    public void onCancelled(com.google.firebase.database.DatabaseError databaseError) {
                        if (listener != null) listener.onNotFound(databaseError.getMessage());
                    }
                });
    }

    // Find student by ID number AND registration number in Firebase (for login validation)
    public void findStudentByIdAndRegNo(String idNo, String regNo, OnStudentFoundListener listener) {
        if (!isFirebaseReady() || !NetworkUtil.isConnected(context)) {
            if (listener != null) listener.onNotFound("Offline - cannot query Firebase");
            return;
        }

        databaseReference.child("students")
                .orderByChild("idNo")
                .equalTo(idNo)
                .addListenerForSingleValueEvent(new com.google.firebase.database.ValueEventListener() {
                    @Override
                    public void onDataChange(com.google.firebase.database.DataSnapshot dataSnapshot) {
                        if (dataSnapshot.exists()) {
                            for (com.google.firebase.database.DataSnapshot snapshot : dataSnapshot.getChildren()) {
                                Student foundStudent = snapshot.getValue(Student.class);
                                if (foundStudent != null && foundStudent.getRegistrationNumber().equals(regNo)) {
                                    foundStudent.setFirebaseKey(snapshot.getKey());
                                    listener.onFound(foundStudent);
                                    return;
                                }
                            }
                        }
                        if (listener != null) listener.onNotFound("Student not found in database");
                    }

                    @Override
                    public void onCancelled(com.google.firebase.database.DatabaseError databaseError) {
                        if (listener != null) listener.onNotFound(databaseError.getMessage());
                    }
                });
    }

    // Check if student ID already exists in Firebase (for uniqueness validation)
    public void checkStudentIdExists(String idNo, OnIdCheckListener listener) {
        if (!isFirebaseReady() || !NetworkUtil.isConnected(context)) {
            // When offline, check local SharedPreferences
            Student existing = sharedPrefManager.getStudent();
            if (existing != null && existing.getIdNo().equals(idNo)) {
                listener.onExists(true);
            } else {
                listener.onExists(false);
            }
            return;
        }

        databaseReference.child("students")
                .orderByChild("idNo")
                .equalTo(idNo)
                .addListenerForSingleValueEvent(new com.google.firebase.database.ValueEventListener() {
                    @Override
                    public void onDataChange(com.google.firebase.database.DataSnapshot dataSnapshot) {
                        listener.onExists(dataSnapshot.exists());
                    }

                    @Override
                    public void onCancelled(com.google.firebase.database.DatabaseError databaseError) {
                        listener.onExists(false);
                    }
                });
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

    public interface OnStudentFoundListener {
        void onFound(Student student);
        void onNotFound(String message);
    }

    public interface OnIdCheckListener {
        void onExists(boolean exists);
    }

    // Restore all student data from Firebase after reinstall
    public void restoreStudentData(String idNo, OnDataRestoredListener listener) {
        if (!isFirebaseReady() || !NetworkUtil.isConnected(context)) {
            if (listener != null) listener.onRestored(false, "Offline - cannot restore from Firebase");
            return;
        }

        final int[] pending = {3};
        final boolean[] success = {true};
        final StringBuilder errorMsg = new StringBuilder();

        // Restore book requests
        databaseReference.child("bookRequests")
                .orderByChild("studentIdNo")
                .equalTo(idNo)
                .addListenerForSingleValueEvent(new com.google.firebase.database.ValueEventListener() {
                    @Override
                    public void onDataChange(com.google.firebase.database.DataSnapshot dataSnapshot) {
                        List<BookRequest> requests = new ArrayList<>();
                        for (com.google.firebase.database.DataSnapshot snapshot : dataSnapshot.getChildren()) {
                            BookRequest req = snapshot.getValue(BookRequest.class);
                            if (req != null) {
                                req.setFirebaseKey(snapshot.getKey());
                                requests.add(req);
                            }
                        }
                        sharedPrefManager.clearOfflineBookRequests();
                        for (BookRequest req : requests) {
                            sharedPrefManager.saveBookRequest(req);
                        }
                        checkComplete(pending, success, errorMsg, listener);
                    }

                    @Override
                    public void onCancelled(com.google.firebase.database.DatabaseError databaseError) {
                        success[0] = false;
                        errorMsg.append("Book requests: ").append(databaseError.getMessage()).append("; ");
                        checkComplete(pending, success, errorMsg, listener);
                    }
                });

        // Restore hostel infos
        databaseReference.child("hostelInfos")
                .orderByChild("studentIdNo")
                .equalTo(idNo)
                .addListenerForSingleValueEvent(new com.google.firebase.database.ValueEventListener() {
                    @Override
                    public void onDataChange(com.google.firebase.database.DataSnapshot dataSnapshot) {
                        List<HostelInfo> infos = new ArrayList<>();
                        for (com.google.firebase.database.DataSnapshot snapshot : dataSnapshot.getChildren()) {
                            HostelInfo info = snapshot.getValue(HostelInfo.class);
                            if (info != null) {
                                info.setFirebaseKey(snapshot.getKey());
                                infos.add(info);
                            }
                        }
                        sharedPrefManager.clearOfflineHostelInfos();
                        for (HostelInfo info : infos) {
                            sharedPrefManager.saveHostelInfo(info);
                        }
                        checkComplete(pending, success, errorMsg, listener);
                    }

                    @Override
                    public void onCancelled(com.google.firebase.database.DatabaseError databaseError) {
                        success[0] = false;
                        errorMsg.append("Hostel infos: ").append(databaseError.getMessage()).append("; ");
                        checkComplete(pending, success, errorMsg, listener);
                    }
                });

        // Restore payments
        databaseReference.child("payments")
                .orderByChild("studentIdNo")
                .equalTo(idNo)
                .addListenerForSingleValueEvent(new com.google.firebase.database.ValueEventListener() {
                    @Override
                    public void onDataChange(com.google.firebase.database.DataSnapshot dataSnapshot) {
                        List<Payment> payments = new ArrayList<>();
                        for (com.google.firebase.database.DataSnapshot snapshot : dataSnapshot.getChildren()) {
                            Payment payment = snapshot.getValue(Payment.class);
                            if (payment != null) {
                                payment.setFirebaseKey(snapshot.getKey());
                                payments.add(payment);
                            }
                        }
                        sharedPrefManager.clearOfflinePayments();
                        for (Payment payment : payments) {
                            sharedPrefManager.savePayment(payment);
                        }
                        checkComplete(pending, success, errorMsg, listener);
                    }

                    @Override
                    public void onCancelled(com.google.firebase.database.DatabaseError databaseError) {
                        success[0] = false;
                        errorMsg.append("Payments: ").append(databaseError.getMessage()).append("; ");
                        checkComplete(pending, success, errorMsg, listener);
                    }
                });
    }

    private void checkComplete(int[] pending, boolean[] success, StringBuilder errorMsg, OnDataRestoredListener listener) {
        pending[0]--;
        if (pending[0] <= 0 && listener != null) {
            listener.onRestored(success[0], errorMsg.toString());
        }
    }

    public interface OnDataRestoredListener {
        void onRestored(boolean success, String message);
    }
}