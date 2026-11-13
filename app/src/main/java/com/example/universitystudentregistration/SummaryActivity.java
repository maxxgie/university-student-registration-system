package com.example.universitystudentregistration;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.RadioGroup;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.universitystudentregistration.model.BookRequest;
import com.example.universitystudentregistration.model.HostelInfo;
import com.example.universitystudentregistration.model.Payment;
import com.example.universitystudentregistration.model.Student;
import com.example.universitystudentregistration.util.SharedPrefManager;
import com.example.universitystudentregistration.util.SummaryHelper;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class SummaryActivity extends AppCompatActivity {

    private RadioGroup rgSummaryType;
    private TableLayout tblSummary;
    private Button btnRefreshSummary, btnSaveSummary, btnShareSummary, btnBackToFees;
    private SharedPrefManager sharedPrefManager;

    // Store current summary text for save/share
    private String currentSummaryText = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        super.onCreate(savedInstanceState);

        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_summary);

        // Handle system window spacing
        ViewCompat.setOnApplyWindowInsetsListener(
                findViewById(R.id.main), (v, insets) -> {

                    Insets systemBars = insets.getInsets(
                            WindowInsetsCompat.Type.systemBars()
                    );

                    v.setPadding(
                            systemBars.left,
                            systemBars.top,
                            systemBars.right,
                            systemBars.bottom
                    );

                    return insets;
                });

        // Initialize utilities
        sharedPrefManager = new SharedPrefManager(this);

        // Connect UI elements
        rgSummaryType = findViewById(R.id.rgSummaryType);
        tblSummary = findViewById(R.id.tblSummary);
        btnRefreshSummary = findViewById(R.id.btnRefreshSummary);
        btnSaveSummary = findViewById(R.id.btnSaveSummary);
        btnShareSummary = findViewById(R.id.btnShareSummary);
        btnBackToFees = findViewById(R.id.btnBackToFees);

        // Load initial summary
        loadSummary();

        // Radio group listener
        rgSummaryType.setOnCheckedChangeListener((group, checkedId) -> loadSummary());

        // Button listeners
        btnRefreshSummary.setOnClickListener(v -> loadSummary());

        btnSaveSummary.setOnClickListener(v -> saveSummaryToFile());

        btnShareSummary.setOnClickListener(v -> shareSummary());

        btnBackToFees.setOnClickListener(v -> {
            Intent intent = new Intent(SummaryActivity.this, FeesPaymentActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish();
        });
    }

    private void loadSummary() {
        Student student = sharedPrefManager.getStudent();
        List<BookRequest> bookRequests = sharedPrefManager.getBookRequests();
        List<HostelInfo> hostelInfos = sharedPrefManager.getHostelInfos();
        List<Payment> payments = sharedPrefManager.getPayments();

        // Clear existing table
        tblSummary.removeAllViews();

        if (rgSummaryType.getCheckedRadioButtonId() == R.id.rbMySummary) {
            if (student != null) {
                buildMySummaryTable(student, bookRequests, hostelInfos, payments);
                currentSummaryText = SummaryHelper.generateStudentSummary(student, bookRequests, hostelInfos, payments);
            } else {
                addEmptyRow("No student data found. Please complete registration first.");
                currentSummaryText = "No student data found. Please complete registration first.";
            }
        } else {
            // All Students - use text format for better readability
            List<Student> allStudents = sharedPrefManager.getOfflineStudents();
            // Add current student if not in offline list
            if (student != null) {
                boolean exists = false;
                for (Student s : allStudents) {
                    if (s.getIdNo().equals(student.getIdNo())) {
                        exists = true;
                        break;
                    }
                }
                if (!exists) {
                    allStudents.add(0, student);
                }
            }
            if (!allStudents.isEmpty()) {
                currentSummaryText = SummaryHelper.generateAllStudentsSummary(allStudents, bookRequests, hostelInfos, payments);
                // Display as text in a single row for readability
                TableRow row = new TableRow(this);
                TextView tv = new TextView(this);
                tv.setText(currentSummaryText);
                tv.setTextColor(Color.parseColor("#333333"));
                tv.setTextSize(12);
                tv.setPadding(16, 16, 16, 16);
                tv.setGravity(Gravity.START);
                tv.setTypeface(Typeface.MONOSPACE);
                TableRow.LayoutParams params = new TableRow.LayoutParams();
                params.span = 10;
                row.addView(tv, params);
                tblSummary.addView(row);
            } else {
                addEmptyRow("No students registered yet.");
                currentSummaryText = "No students registered yet.";
            }
        }
    }

    private void buildMySummaryTable(Student student, List<BookRequest> bookRequests,
                                     List<HostelInfo> hostelInfos, List<Payment> payments) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());

        // Student Details Section
        addSectionHeader("STUDENT DETAILS");
        addTableHeaderRow("Field", "Value");
        addTableRow("Full Names", student.getFullNames());
        addTableRow("ID Number", student.getIdNo());
        addTableRow("Registration Number", student.getRegistrationNumber());
        addTableRow("Gender", student.getGender());
        addTableRow("Department", student.getDepartment());
        addTableRow("Course", student.getCourse());
        addTableRow("Registration Date", dateFormat.format(new Date(Long.parseLong(student.getTimestamp()))));
        addTableRow("Status", student.getStatus().toUpperCase());

        // Library Book Requests Section
        addSectionHeader("LIBRARY BOOK REQUESTS");
        if (bookRequests != null && !bookRequests.isEmpty()) {
            boolean hasBooks = false;
            for (BookRequest br : bookRequests) {
                if (br.getStudentIdNo().equals(student.getIdNo())) {
                    if (!hasBooks) {
                        addTableHeaderRow("ISBN", "Author", "Title", "Borrowed", "Return", "Status");
                        hasBooks = true;
                    }
                    addTableRow(br.getBookIsbn(), br.getAuthor(), br.getTitle(),
                            br.getDateBorrowed(), br.getDateOfReturn(), br.getStatus().toUpperCase());
                }
            }
            if (!hasBooks) {
                addTableRow("No book requests found.");
            }
        } else {
            addTableRow("No book requests found.");
        }

        // Hostel Information Section
        addSectionHeader("HOSTEL INFORMATION");
        if (hostelInfos != null && !hostelInfos.isEmpty()) {
            boolean hasHostel = false;
            for (HostelInfo hi : hostelInfos) {
                if (hi.getStudentIdNo().equals(student.getIdNo())) {
                    if (!hasHostel) {
                        addTableHeaderRow("Hostel Location", "Room Type", "Sharable", "Status");
                        hasHostel = true;
                    }
                    addTableRow(hi.getHostelLocation(), hi.getRoomType(),
                            hi.getIsSharable(), hi.getStatus().toUpperCase());
                }
            }
            if (!hasHostel) {
                addTableRow("No hostel information found.");
            }
        } else {
            addTableRow("No hostel information found.");
        }

        // Payment Details Section
        addSectionHeader("PAYMENT DETAILS");
        if (payments != null && !payments.isEmpty()) {
            boolean hasPayments = false;
            double totalPaid = 0;
            for (Payment p : payments) {
                if (p.getStudentIdNo().equals(student.getIdNo())) {
                    if (!hasPayments) {
                        addTableHeaderRow("Phone", "Amount (Kshs)", "Transaction Code", "Date", "Status");
                        hasPayments = true;
                    }
                    addTableRow(p.getPhoneNumber(),
                            String.format("%.2f", p.getAmount()),
                            p.getTransactionCode() != null ? p.getTransactionCode() : "N/A",
                            dateFormat.format(new Date(Long.parseLong(p.getTimestamp()))),
                            p.getStatus().toUpperCase());
                    totalPaid += p.getAmount();
                }
            }
            if (hasPayments) {
                addTableRow("TOTAL PAID", String.format("Kshs %.2f", totalPaid), "", "", "");
                // Make total row bold
                TableRow totalRow = (TableRow) tblSummary.getChildAt(tblSummary.getChildCount() - 1);
                for (int i = 0; i < totalRow.getChildCount(); i++) {
                    View child = totalRow.getChildAt(i);
                    if (child instanceof TextView) {
                        ((TextView) child).setTypeface(null, Typeface.BOLD);
                        ((TextView) child).setTextColor(Color.parseColor("#00A651"));
                    }
                }
            } else {
                addTableRow("No payment records found.");
            }
        } else {
            addTableRow("No payment records found.");
        }
    }

    private void addSectionHeader(String title) {
        TableRow headerRow = new TableRow(this);
        headerRow.setBackgroundColor(Color.parseColor("#35145F"));
        headerRow.setPadding(8, 8, 8, 8);

        TextView tv = new TextView(this);
        tv.setText(title);
        tv.setTextColor(Color.WHITE);
        tv.setTextSize(14);
        tv.setTypeface(null, Typeface.BOLD);
        tv.setGravity(Gravity.CENTER);
        tv.setPadding(8, 8, 8, 8);

        // Span across all columns
        TableRow.LayoutParams params = new TableRow.LayoutParams();
        params.span = 10;
        headerRow.addView(tv, params);

        tblSummary.addView(headerRow);
    }

    private void addTableRow(String[] columns, boolean isHeader) {
        TableRow row = new TableRow(this);
        
        // Alternate row colors
        int rowIndex = tblSummary.getChildCount();
        if (!isHeader) {
            row.setBackgroundColor(rowIndex % 2 == 0 ? Color.parseColor("#F7F5FC") : Color.WHITE);
        }

        for (String col : columns) {
            TextView tv = new TextView(this);
            tv.setText(col);
            tv.setTextColor(Color.parseColor("#333333"));
            tv.setTextSize(12);
            tv.setPadding(12, 10, 12, 10);
            tv.setGravity(Gravity.CENTER);
            if (isHeader) {
                tv.setTypeface(null, Typeface.BOLD);
                tv.setTextColor(Color.WHITE);
                tv.setBackgroundColor(Color.parseColor("#35145F"));
            }
            row.addView(tv);
        }

        tblSummary.addView(row);
    }

    private void addTableRow(String... columns) {
        addTableRow(columns, false);
    }

    private void addTableRow(String[] columns, boolean isHeader, boolean unused) {
        addTableRow(columns, isHeader);
    }

    private void addTableHeaderRow(String... columns) {
        addTableRow(columns, true);
    }

    private void addEmptyRow(String message) {
        TableRow row = new TableRow(this);
        TextView tv = new TextView(this);
        tv.setText(message);
        tv.setTextColor(Color.parseColor("#777777"));
        tv.setTextSize(14);
        tv.setGravity(Gravity.CENTER);
        tv.setPadding(16, 32, 16, 32);
        tv.setTypeface(null, Typeface.ITALIC);
        
        TableRow.LayoutParams params = new TableRow.LayoutParams();
        params.span = 10;
        row.addView(tv, params);
        tblSummary.addView(row);
    }

    private void saveSummaryToFile() {
        if (currentSummaryText.isEmpty() || currentSummaryText.contains("No student data")) {
            Toast.makeText(this, "No summary to save", Toast.LENGTH_SHORT).show();
            return;
        }

        String fileName = "RegistrationSummary_" + System.currentTimeMillis() + ".txt";
        boolean success = SummaryHelper.saveSummaryToFile(this, currentSummaryText, fileName);

        if (success) {
            Toast.makeText(this, "Summary saved to Documents/RegistrationSummaries/" + fileName, Toast.LENGTH_LONG).show();
        } else {
            Toast.makeText(this, "Failed to save summary", Toast.LENGTH_SHORT).show();
        }
    }

    private void shareSummary() {
        if (currentSummaryText.isEmpty() || currentSummaryText.contains("No student data")) {
            Toast.makeText(this, "No summary to share", Toast.LENGTH_SHORT).show();
            return;
        }

        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_SUBJECT, "Mwangaza University Registration Summary");
        shareIntent.putExtra(Intent.EXTRA_TEXT, currentSummaryText);
        startActivity(Intent.createChooser(shareIntent, "Share Summary"));
    }
}