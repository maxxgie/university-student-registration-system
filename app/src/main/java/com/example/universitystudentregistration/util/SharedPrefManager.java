package com.example.universitystudentregistration.util;

import android.content.Context;
import android.content.SharedPreferences;
import com.example.universitystudentregistration.model.BookRequest;
import com.example.universitystudentregistration.model.HostelInfo;
import com.example.universitystudentregistration.model.Payment;
import com.example.universitystudentregistration.model.Student;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class SharedPrefManager {

    private static final String PREF_NAME = "UniversityRegistrationPref";
    private static final String KEY_STUDENT = "student_data";
    private static final String KEY_BOOK_REQUESTS = "book_requests";
    private static final String KEY_HOSTEL_INFOS = "hostel_infos";
    private static final String KEY_PAYMENTS = "payments";
    private static final String KEY_OFFLINE_STUDENTS = "offline_students";
    private static final String KEY_OFFLINE_BOOK_REQUESTS = "offline_book_requests";
    private static final String KEY_OFFLINE_HOSTEL_INFOS = "offline_hostel_infos";
    private static final String KEY_OFFLINE_PAYMENTS = "offline_payments";

    private final SharedPreferences sharedPreferences;
    private final Gson gson;

    public SharedPrefManager(Context context) {
        sharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        gson = new Gson();
    }

    // Student methods
    public void saveStudent(Student student) {
        String json = gson.toJson(student);
        sharedPreferences.edit().putString(KEY_STUDENT, json).apply();
    }

    public Student getStudent() {
        String json = sharedPreferences.getString(KEY_STUDENT, null);
        if (json != null) {
            return gson.fromJson(json, Student.class);
        }
        return null;
    }

    public void clearStudent() {
        sharedPreferences.edit().remove(KEY_STUDENT).apply();
    }

    // Book Request methods
    public void saveBookRequest(BookRequest bookRequest) {
        List<BookRequest> requests = getBookRequests();
        requests.add(bookRequest);
        String json = gson.toJson(requests);
        sharedPreferences.edit().putString(KEY_BOOK_REQUESTS, json).apply();
    }

    public List<BookRequest> getBookRequests() {
        String json = sharedPreferences.getString(KEY_BOOK_REQUESTS, null);
        if (json != null) {
            Type type = new TypeToken<List<BookRequest>>(){}.getType();
            return gson.fromJson(json, type);
        }
        return new ArrayList<>();
    }

    // Hostel Info methods
    public void saveHostelInfo(HostelInfo hostelInfo) {
        List<HostelInfo> infos = getHostelInfos();
        infos.add(hostelInfo);
        String json = gson.toJson(infos);
        sharedPreferences.edit().putString(KEY_HOSTEL_INFOS, json).apply();
    }

    public List<HostelInfo> getHostelInfos() {
        String json = sharedPreferences.getString(KEY_HOSTEL_INFOS, null);
        if (json != null) {
            Type type = new TypeToken<List<HostelInfo>>(){}.getType();
            return gson.fromJson(json, type);
        }
        return new ArrayList<>();
    }

    // Payment methods
    public void savePayment(Payment payment) {
        List<Payment> payments = getPayments();
        payments.add(payment);
        String json = gson.toJson(payments);
        sharedPreferences.edit().putString(KEY_PAYMENTS, json).apply();
    }

    public List<Payment> getPayments() {
        String json = sharedPreferences.getString(KEY_PAYMENTS, null);
        if (json != null) {
            Type type = new TypeToken<List<Payment>>(){}.getType();
            return gson.fromJson(json, type);
        }
        return new ArrayList<>();
    }

    // Offline queue methods for Firebase sync
    public void addOfflineStudent(Student student) {
        List<Student> students = getOfflineStudents();
        students.add(student);
        String json = gson.toJson(students);
        sharedPreferences.edit().putString(KEY_OFFLINE_STUDENTS, json).apply();
    }

    public List<Student> getOfflineStudents() {
        String json = sharedPreferences.getString(KEY_OFFLINE_STUDENTS, null);
        if (json != null) {
            Type type = new TypeToken<List<Student>>(){}.getType();
            return gson.fromJson(json, type);
        }
        return new ArrayList<>();
    }

    public void removeOfflineStudent(Student student) {
        List<Student> students = getOfflineStudents();
        students.removeIf(s -> s.getIdNo().equals(student.getIdNo()));
        String json = gson.toJson(students);
        sharedPreferences.edit().putString(KEY_OFFLINE_STUDENTS, json).apply();
    }

    public void addOfflineBookRequest(BookRequest bookRequest) {
        List<BookRequest> requests = getOfflineBookRequests();
        requests.add(bookRequest);
        String json = gson.toJson(requests);
        sharedPreferences.edit().putString(KEY_OFFLINE_BOOK_REQUESTS, json).apply();
    }

    public List<BookRequest> getOfflineBookRequests() {
        String json = sharedPreferences.getString(KEY_OFFLINE_BOOK_REQUESTS, null);
        if (json != null) {
            Type type = new TypeToken<List<BookRequest>>(){}.getType();
            return gson.fromJson(json, type);
        }
        return new ArrayList<>();
    }

    public void removeOfflineBookRequest(BookRequest bookRequest) {
        List<BookRequest> requests = getOfflineBookRequests();
        requests.removeIf(r -> r.getBookIsbn().equals(bookRequest.getBookIsbn()) &&
                r.getStudentIdNo().equals(bookRequest.getStudentIdNo()));
        String json = gson.toJson(requests);
        sharedPreferences.edit().putString(KEY_OFFLINE_BOOK_REQUESTS, json).apply();
    }

    public void addOfflineHostelInfo(HostelInfo hostelInfo) {
        List<HostelInfo> infos = getOfflineHostelInfos();
        infos.add(hostelInfo);
        String json = gson.toJson(infos);
        sharedPreferences.edit().putString(KEY_OFFLINE_HOSTEL_INFOS, json).apply();
    }

    public List<HostelInfo> getOfflineHostelInfos() {
        String json = sharedPreferences.getString(KEY_OFFLINE_HOSTEL_INFOS, null);
        if (json != null) {
            Type type = new TypeToken<List<HostelInfo>>(){}.getType();
            return gson.fromJson(json, type);
        }
        return new ArrayList<>();
    }

    public void removeOfflineHostelInfo(HostelInfo hostelInfo) {
        List<HostelInfo> infos = getOfflineHostelInfos();
        infos.removeIf(h -> h.getStudentIdNo().equals(hostelInfo.getStudentIdNo()) &&
                h.getHostelLocation().equals(hostelInfo.getHostelLocation()));
        String json = gson.toJson(infos);
        sharedPreferences.edit().putString(KEY_OFFLINE_HOSTEL_INFOS, json).apply();
    }

    public void addOfflinePayment(Payment payment) {
        List<Payment> payments = getOfflinePayments();
        payments.add(payment);
        String json = gson.toJson(payments);
        sharedPreferences.edit().putString(KEY_OFFLINE_PAYMENTS, json).apply();
    }

    public List<Payment> getOfflinePayments() {
        String json = sharedPreferences.getString(KEY_OFFLINE_PAYMENTS, null);
        if (json != null) {
            Type type = new TypeToken<List<Payment>>(){}.getType();
            return gson.fromJson(json, type);
        }
        return new ArrayList<>();
    }

    public void removeOfflinePayment(Payment payment) {
        List<Payment> payments = getOfflinePayments();
        payments.removeIf(p -> p.getStudentIdNo().equals(payment.getStudentIdNo()) &&
                p.getTimestamp().equals(payment.getTimestamp()));
        String json = gson.toJson(payments);
        sharedPreferences.edit().putString(KEY_OFFLINE_PAYMENTS, json).apply();
    }

    // Clear all offline queues (used during sync to prevent duplicates)
    public void clearOfflineStudents() {
        sharedPreferences.edit().remove(KEY_OFFLINE_STUDENTS).apply();
    }

    public void clearOfflineBookRequests() {
        sharedPreferences.edit().remove(KEY_OFFLINE_BOOK_REQUESTS).apply();
    }

    public void clearOfflineHostelInfos() {
        sharedPreferences.edit().remove(KEY_OFFLINE_HOSTEL_INFOS).apply();
    }

    public void clearOfflinePayments() {
        sharedPreferences.edit().remove(KEY_OFFLINE_PAYMENTS).apply();
    }
}