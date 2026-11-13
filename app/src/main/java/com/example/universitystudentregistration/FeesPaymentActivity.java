package com.example.universitystudentregistration;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.universitystudentregistration.model.Payment;
import com.example.universitystudentregistration.model.Student;
import com.example.universitystudentregistration.util.FirebaseHelper;
import com.example.universitystudentregistration.util.NetworkUtil;
import com.example.universitystudentregistration.util.SharedPrefManager;

public class FeesPaymentActivity extends AppCompatActivity {

    private EditText etPhoneNumber, etFeesAmount, etStudentIdNoFees;
    private Button btnMpesaPrompt, btnViewSummary;
    private TextView tvPaymentStatus, tvTransactionCode;
    private SharedPrefManager sharedPrefManager;
    private FirebaseHelper firebaseHelper;
    private boolean paymentCompleted = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        super.onCreate(savedInstanceState);

        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_fees_payment);

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
        firebaseHelper = new FirebaseHelper(this);

        // Connect UI elements
        etPhoneNumber = findViewById(R.id.etPhoneNumber);
        etFeesAmount = findViewById(R.id.etFeesAmount);
        etStudentIdNoFees = findViewById(R.id.etStudentIdNoFees);

        btnMpesaPrompt = findViewById(R.id.btnMpesaPrompt);
        btnViewSummary = findViewById(R.id.btnViewSummary);

        tvPaymentStatus = findViewById(R.id.tvPaymentStatus);
        tvTransactionCode = findViewById(R.id.tvTransactionCode);

        // Pre-fill student ID if available
        Student student = sharedPrefManager.getStudent();
        if (student != null) {
            etStudentIdNoFees.setText(student.getIdNo());
            etStudentIdNoFees.setEnabled(false);
        }

        // Placeholder: MPESA not implemented - show authorized message
        tvPaymentStatus.setText("MPESA integration pending. Tap 'Authorized' to simulate payment.");
        tvPaymentStatus.setTextColor(getResources().getColor(android.R.color.holo_blue_dark));
        btnMpesaPrompt.setText("Authorized (Placeholder)");
        btnMpesaPrompt.setBackgroundTintList(getResources().getColorStateList(android.R.color.holo_orange_dark));

        // Authorized button click - simulate payment
        btnMpesaPrompt.setOnClickListener(v -> simulateAuthorizedPayment());

        btnViewSummary.setOnClickListener(v -> {
            if (paymentCompleted) {
                finish(); // Go back to dashboard
            } else {
                Toast.makeText(this, "Please complete payment first", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void simulateAuthorizedPayment() {
        String phoneNumber = etPhoneNumber.getText().toString().trim();
        String amountStr = etFeesAmount.getText().toString().trim();
        String studentIdNo = etStudentIdNoFees.getText().toString().trim();

        // Validate Phone Number
        if (phoneNumber.isEmpty()) {
            etPhoneNumber.setError("Please enter phone number");
            etPhoneNumber.requestFocus();
            return;
        }

        // Validate Kenyan phone number format (2547xxxxxxxx or 07xxxxxxxx)
        if (!phoneNumber.matches("(254|0)?7\\d{8}")) {
            etPhoneNumber.setError("Enter valid Kenyan phone number (e.g., 254712345678 or 0712345678)");
            etPhoneNumber.requestFocus();
            return;
        }

        // Validate Amount
        if (amountStr.isEmpty()) {
            etFeesAmount.setError("Please enter amount");
            etFeesAmount.requestFocus();
            return;
        }

        double amount;
        try {
            amount = Double.parseDouble(amountStr);
            if (amount <= 0) {
                etFeesAmount.setError("Amount must be greater than 0");
                etFeesAmount.requestFocus();
                return;
            }
            if (amount < 1) {
                etFeesAmount.setError("Minimum amount is Kshs 1");
                etFeesAmount.requestFocus();
                return;
            }
        } catch (NumberFormatException e) {
            etFeesAmount.setError("Invalid amount");
            etFeesAmount.requestFocus();
            return;
        }

        // Validate Student ID
        if (studentIdNo.isEmpty()) {
            etStudentIdNoFees.setError("Please enter student ID number");
            etStudentIdNoFees.requestFocus();
            return;
        }

        if (!studentIdNo.matches("\\d{8}")) {
            etStudentIdNoFees.setError("ID Number must contain exactly 8 digits");
            etStudentIdNoFees.requestFocus();
            return;
        }

        // Simulate authorized payment
        btnMpesaPrompt.setEnabled(false);
        btnMpesaPrompt.setText("Processing...");
        tvPaymentStatus.setText("Authorizing payment...");
        tvPaymentStatus.setTextColor(getResources().getColor(android.R.color.holo_blue_dark));

        new android.os.Handler(android.os.Looper.getMainLooper()).postDelayed(() -> {
            processSuccessfulPayment(studentIdNo, phoneNumber, amount);
        }, 1500); // Short delay for UX
    }

    private void processSuccessfulPayment(String studentIdNo, String phoneNumber, double amount) {
        // Generate transaction code
        String transactionCode = generateTransactionCode();

        // Create payment object
        Payment payment = new Payment(studentIdNo, phoneNumber, amount);
        payment.setTransactionCode(transactionCode);
        payment.setStatus("completed");

        // Save locally
        sharedPrefManager.savePayment(payment);

        // Update student status to registered
        Student student = sharedPrefManager.getStudent();
        if (student != null && student.getIdNo().equals(studentIdNo)) {
            student.setStatus("registered");
            sharedPrefManager.saveStudent(student);
        }

        // Save to Firebase (handles offline automatically)
        firebaseHelper.savePayment(payment, new FirebaseHelper.OnFirebaseOperationListener() {
            @Override
            public void onSuccess() {
                runOnUiThread(() -> {
                    tvPaymentStatus.setText("✓ Authorized! Payment successful. Registration confirmed.");
                    tvPaymentStatus.setTextColor(getResources().getColor(android.R.color.holo_green_dark));
                    tvTransactionCode.setText("Transaction Code: " + transactionCode);
                    tvTransactionCode.setVisibility(View.VISIBLE);
                    btnMpesaPrompt.setText("Payment Completed");
                    btnMpesaPrompt.setBackgroundTintList(getResources().getColorStateList(android.R.color.holo_green_dark));
                    paymentCompleted = true;
                    Toast.makeText(FeesPaymentActivity.this,
                            "Authorized! Registration confirmed.",
                            Toast.LENGTH_LONG).show();
                });
            }

            @Override
            public void onFailure(String errorMessage) {
                runOnUiThread(() -> {
                    tvPaymentStatus.setText("✓ Authorized but offline. Will sync when online.");
                    tvPaymentStatus.setTextColor(getResources().getColor(android.R.color.holo_orange_dark));
                    tvTransactionCode.setText("Transaction Code: " + transactionCode);
                    tvTransactionCode.setVisibility(View.VISIBLE);
                    btnMpesaPrompt.setText("Payment Completed (Offline)");
                    btnMpesaPrompt.setBackgroundTintList(getResources().getColorStateList(android.R.color.holo_orange_dark));
                    paymentCompleted = true;
                    Toast.makeText(FeesPaymentActivity.this,
                            "Authorized! Saved offline. Will sync when online.",
                            Toast.LENGTH_LONG).show();
                });
            }
        });
    }

    private String generateTransactionCode() {
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
        StringBuilder sb = new StringBuilder();
        java.util.Random random = new java.util.Random();
        sb.append("MPS");
        for (int i = 0; i < 10; i++) {
            sb.append(chars.charAt(random.nextInt(chars.length())));
        }
        return sb.toString();
    }
}