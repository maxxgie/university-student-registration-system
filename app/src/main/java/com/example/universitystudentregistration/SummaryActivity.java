package com.example.universitystudentregistration;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.universitystudentregistration.model.BookRequest;
import com.example.universitystudentregistration.model.HostelInfo;
import com.example.universitystudentregistration.model.Payment;
import com.example.universitystudentregistration.model.Student;
import com.example.universitystudentregistration.util.SharedPrefManager;
import com.example.universitystudentregistration.util.SummaryHelper;

import java.util.List;

public class SummaryActivity extends AppCompatActivity {

    private RadioGroup rgSummaryType;
    private TextView tvSummary;
    private Button btnRefreshSummary, btnSaveSummary, btnShareSummary, btnBackToFees;
    private SharedPrefManager sharedPrefManager;

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
        tvSummary = findViewById(R.id.tvSummary);
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

        btnBackToFees.setOnClickListener(v -> finish());
    }

    private void loadSummary() {
        Student student = sharedPrefManager.getStudent();
        List<BookRequest> bookRequests = sharedPrefManager.getBookRequests();
        List<HostelInfo> hostelInfos = sharedPrefManager.getHostelInfos();
        List<Payment> payments = sharedPrefManager.getPayments();

        String summary;
        if (rgSummaryType.getCheckedRadioButtonId() == R.id.rbMySummary) {
            if (student != null) {
                summary = SummaryHelper.generateStudentSummary(student, bookRequests, hostelInfos, payments);
            } else {
                summary = "No student data found. Please complete registration first.";
            }
        } else {
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
            summary = SummaryHelper.generateAllStudentsSummary(allStudents, bookRequests, hostelInfos, payments);
        }

        tvSummary.setText(summary);
    }

    private void saveSummaryToFile() {
        String summary = tvSummary.getText().toString();
        if (summary.isEmpty() || summary.contains("No student data")) {
            Toast.makeText(this, "No summary to save", Toast.LENGTH_SHORT).show();
            return;
        }

        String fileName = "RegistrationSummary_" + System.currentTimeMillis() + ".txt";
        boolean success = SummaryHelper.saveSummaryToFile(this, summary, fileName);

        if (success) {
            Toast.makeText(this, "Summary saved to Documents/RegistrationSummaries/" + fileName, Toast.LENGTH_LONG).show();
        } else {
            Toast.makeText(this, "Failed to save summary", Toast.LENGTH_SHORT).show();
        }
    }

    private void shareSummary() {
        String summary = tvSummary.getText().toString();
        if (summary.isEmpty() || summary.contains("No student data")) {
            Toast.makeText(this, "No summary to share", Toast.LENGTH_SHORT).show();
            return;
        }

        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_SUBJECT, "Mwangaza University Registration Summary");
        shareIntent.putExtra(Intent.EXTRA_TEXT, summary);
        startActivity(Intent.createChooser(shareIntent, "Share Summary"));
    }
}