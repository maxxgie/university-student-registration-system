package com.example.universitystudentregistration.util;

import android.content.Context;
import android.os.Environment;
import android.util.Log;
import com.example.universitystudentregistration.model.BookRequest;
import com.example.universitystudentregistration.model.HostelInfo;
import com.example.universitystudentregistration.model.Payment;
import com.example.universitystudentregistration.model.Student;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class SummaryHelper {

    private static final String TAG = "SummaryHelper";

    public static String generateStudentSummary(Student student, List<BookRequest> bookRequests,
                                                List<HostelInfo> hostelInfos, List<Payment> payments) {
        StringBuilder summary = new StringBuilder();
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());

        summary.append("========================================\n");
        summary.append("       MWANGAZA UNIVERSITY\n");
        summary.append("    STUDENT REGISTRATION SUMMARY\n");
        summary.append("========================================\n\n");

        summary.append("STUDENT DETAILS:\n");
        summary.append("----------------\n");
        summary.append(String.format("Full Names: %s\n", student.getFullNames()));
        summary.append(String.format("ID Number: %s\n", student.getIdNo()));
        summary.append(String.format("Registration Number: %s\n", student.getRegistrationNumber()));
        summary.append(String.format("Gender: %s\n", student.getGender()));
        summary.append(String.format("Department: %s\n", student.getDepartment()));
        summary.append(String.format("Course: %s\n", student.getCourse()));
        summary.append(String.format("Registration Date: %s\n",
                dateFormat.format(new Date(Long.parseLong(student.getTimestamp())))));
        summary.append(String.format("Status: %s\n\n", student.getStatus().toUpperCase()));

        summary.append("LIBRARY BOOK REQUESTS:\n");
        summary.append("----------------------\n");
        if (bookRequests != null && !bookRequests.isEmpty()) {
            for (BookRequest br : bookRequests) {
                if (br.getStudentIdNo().equals(student.getIdNo())) {
                    summary.append(String.format("ISBN: %s\n", br.getBookIsbn()));
                    summary.append(String.format("Author: %s\n", br.getAuthor()));
                    summary.append(String.format("Title: %s\n", br.getTitle()));
                    summary.append(String.format("Date Borrowed: %s\n", br.getDateBorrowed()));
                    summary.append(String.format("Date of Return: %s\n", br.getDateOfReturn()));
                    summary.append(String.format("Status: %s\n\n", br.getStatus().toUpperCase()));
                }
            }
        } else {
            summary.append("No book requests found.\n\n");
        }

        summary.append("HOSTEL INFORMATION:\n");
        summary.append("-------------------\n");
        if (hostelInfos != null && !hostelInfos.isEmpty()) {
            for (HostelInfo hi : hostelInfos) {
                if (hi.getStudentIdNo().equals(student.getIdNo())) {
                    summary.append(String.format("Hostel Location: %s\n", hi.getHostelLocation()));
                    summary.append(String.format("Room Type: %s\n", hi.getRoomType()));
                    summary.append(String.format("Sharable: %s\n", hi.getIsSharable()));
                    summary.append(String.format("Status: %s\n\n", hi.getStatus().toUpperCase()));
                }
            }
        } else {
            summary.append("No hostel information found.\n\n");
        }

        summary.append("PAYMENT DETAILS:\n");
        summary.append("----------------\n");
        if (payments != null && !payments.isEmpty()) {
            double totalPaid = 0;
            for (Payment p : payments) {
                if (p.getStudentIdNo().equals(student.getIdNo())) {
                    summary.append(String.format("Phone Number: %s\n", p.getPhoneNumber()));
                    summary.append(String.format("Amount: Kshs %.2f\n", p.getAmount()));
                    summary.append(String.format("Transaction Code: %s\n", p.getTransactionCode() != null ? p.getTransactionCode() : "N/A"));
                    summary.append(String.format("Date: %s\n",
                            dateFormat.format(new Date(Long.parseLong(p.getTimestamp())))));
                    summary.append(String.format("Status: %s\n\n", p.getStatus().toUpperCase()));
                    totalPaid += p.getAmount();
                }
            }
            summary.append(String.format("TOTAL PAID: Kshs %.2f\n\n", totalPaid));
        } else {
            summary.append("No payment records found.\n\n");
        }

        summary.append("========================================\n");
        summary.append("Generated on: ");
        summary.append(dateFormat.format(new Date()));
        summary.append("\n========================================");

        return summary.toString();
    }

    public static String generateAllStudentsSummary(List<Student> students,
                                                    List<BookRequest> bookRequests,
                                                    List<HostelInfo> hostelInfos,
                                                    List<Payment> payments) {
        StringBuilder summary = new StringBuilder();
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());

        summary.append("========================================\n");
        summary.append("       MWANGAZA UNIVERSITY\n");
        summary.append("    ALL STUDENTS REGISTRATION SUMMARY\n");
        summary.append("========================================\n\n");
        summary.append(String.format("Total Registered Students: %d\n\n", students.size()));

        int studentNum = 1;
        for (Student student : students) {
            summary.append(String.format("STUDENT %d:\n", studentNum++));
            summary.append("-----------\n");
            summary.append(String.format("Full Names: %s\n", student.getFullNames()));
            summary.append(String.format("ID Number: %s\n", student.getIdNo()));
            summary.append(String.format("Registration Number: %s\n", student.getRegistrationNumber()));
            summary.append(String.format("Gender: %s\n", student.getGender()));
            summary.append(String.format("Department: %s\n", student.getDepartment()));
            summary.append(String.format("Course: %s\n", student.getCourse()));
            summary.append(String.format("Registration Date: %s\n",
                    dateFormat.format(new Date(Long.parseLong(student.getTimestamp())))));
            summary.append(String.format("Status: %s\n", student.getStatus().toUpperCase()));

            // Book requests
            if (bookRequests != null) {
                for (BookRequest br : bookRequests) {
                    if (br.getStudentIdNo().equals(student.getIdNo())) {
                        summary.append(String.format("  Book: %s by %s (ISBN: %s)\n",
                                br.getTitle(), br.getAuthor(), br.getBookIsbn()));
                        summary.append(String.format("  Borrowed: %s, Return: %s\n",
                                br.getDateBorrowed(), br.getDateOfReturn()));
                    }
                }
            }

            // Hostel info
            if (hostelInfos != null) {
                for (HostelInfo hi : hostelInfos) {
                    if (hi.getStudentIdNo().equals(student.getIdNo())) {
                        summary.append(String.format("  Hostel: %s, Room: %s, Sharable: %s\n",
                                hi.getHostelLocation(), hi.getRoomType(), hi.getIsSharable()));
                    }
                }
            }

            // Payments
            double studentTotal = 0;
            if (payments != null) {
                for (Payment p : payments) {
                    if (p.getStudentIdNo().equals(student.getIdNo())) {
                        studentTotal += p.getAmount();
                    }
                }
            }
            summary.append(String.format("  Total Paid: Kshs %.2f\n", studentTotal));
            summary.append("\n");
        }

        summary.append("========================================\n");
        summary.append("Generated on: ");
        summary.append(dateFormat.format(new Date()));
        summary.append("\n========================================");

        return summary.toString();
    }

    public static boolean saveSummaryToFile(Context context, String summary, String fileName) {
        try {
            File directory = new File(context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS), "RegistrationSummaries");
            if (!directory.exists()) {
                directory.mkdirs();
            }

            File file = new File(directory, fileName);
            FileOutputStream fos = new FileOutputStream(file);
            fos.write(summary.getBytes());
            fos.close();

            Log.d(TAG, "Summary saved to: " + file.getAbsolutePath());
            return true;
        } catch (IOException e) {
            Log.e(TAG, "Error saving summary to file", e);
            return false;
        }
    }
}