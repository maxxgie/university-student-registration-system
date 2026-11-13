package com.example.universitystudentregistration;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

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
import java.util.Random;

public class FeesPaymentActivity extends AppCompatActivity {

    private EditText etPhoneNumber, etFeesAmount, etStudentIdNoFees;
    private Button btnMpesaPrompt, btnBackToHostel, btnViewSummary;
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
        btnBackToHostel = findViewById(R.id.btnBackToHostel);
        btnViewSummary = findViewById(R.id.btnViewSummary);

        tvPaymentStatus = findViewById(R.id.tvPaymentStatus);
        tvTransactionCode = findViewById(R.id.tvTransactionCode);

        // Pre-fill student ID if available
        Student student = sharedPrefManager.getStudent();
        if (student != null) {
            etStudentIdNoFees.setText(student.getIdNo());
            etStudentIdNoFees.setEnabled(false);
        }

        // MPESA Prompt button click
        btnMpesaPrompt.setOnClickListener(v -> validateAndInitiateMpesa());

        // Navigation buttons
        btnBackToHostel.setOnClickListener(v -> {
            finish();
        });

        btnViewSummary.setOnClickListener(v -> {
            if (paymentCompleted) {
                Intent intent = new Intent(FeesPaymentActivity.this, SummaryActivity.class);
                startActivity(intent);
            } else {
                Toast.makeText(this, "Please complete payment first", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void validateAndInitiateMpesa() {
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

        // All validation passed - simulate MPESA prompt
        simulateMpesaPayment(phoneNumber, amount, studentIdNo);
    }

    private void simulateMpesaPayment(String phoneNumber, double amount, String studentIdNo) {
        // Disable button during simulation
        btnMpesaPrompt.setEnabled(false);
        btnMpesaPrompt.setText("Processing MPESA...");
        tvPaymentStatus.setText("MPESA prompt sent to " + phoneNumber + ". Please enter PIN on your phone.");
        tvPaymentStatus.setTextColor(getResources().getColor(android.R.color.holo_blue_dark));

        // Simulate MPESA processing delay (3-5 seconds)
        int delay = new Random().nextInt(3000) + 3000; // 3-6 seconds

        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            // Simulate success (90% success rate for demo)
            boolean success = new Random().nextInt(100) < 90;

            if (success) {
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

                // Save to Firebase
                firebaseHelper.savePayment(payment, new FirebaseHelper.OnFirebaseOperationListener() {
                    @Override
                    public void onSuccess() {
                        runOnUiThread(() -> {
                            tvPaymentStatus.setText("Payment successful! Registration confirmed.");
                            tvPaymentStatus.setTextColor(getResources().getColor(android.R.color.holo_green_dark));
                            tvTransactionCode.setText("Transaction Code: " + transactionCode);
                            tvTransactionCode.setVisibility(View.VISIBLE);
                            btnMpesaPrompt.setText("Payment Completed");
                            btnMpesaPrompt.setBackgroundTintList(getResources().getColorStateList(android.R.color.holo_green_dark));
                            paymentCompleted = true;
                            Toast.makeText(FeesPaymentActivity.this,
                                    "Payment successful! Registration confirmed.",
                                    Toast.LENGTH_LONG).show();
                        });
                    }

                    @Override
                    public void onFailure(String errorMessage) {
                        runOnUiThread(() -> {
                            tvPaymentStatus.setText("Payment successful but offline. Will sync when online.");
                            tvPaymentStatus.setTextColor(getResources().getColor(android.R.color.holo_orange_dark));
                            tvTransactionCode.setText("Transaction Code: " + transactionCode);
                            tvTransactionCode.setVisibility(View.VISIBLE);
                            btnMpesaPrompt.setText("Payment Completed (Offline)");
                            btnMpesaPrompt.setBackgroundTintList(getResources().getColorStateList(android.R.color.holo_orange_dark));
                            paymentCompleted = true;
                            Toast.makeText(FeesPaymentActivity.this,
                                    "Payment successful! Saved offline. Will sync when online.",
                                    Toast.LENGTH_LONG).show();
                        });
                    }
                });

            } else {
                // Payment failed
                runOnUiThread(() -> {
                    tvPaymentStatus.setText("Payment failed. Please try again.");
                    tvPaymentStatus.setTextColor(getResources().getColor(android.R.color.holo_red_dark));
                    tvTransactionCode.setVisibility(View.GONE);
                    btnMpesaPrompt.setEnabled(true);
                    btnMpesaPrompt.setText("Retry MPESA Payment");
                    btnMpesaPrompt.setBackgroundTintList(getResources().getColorStateList(android.R.color.holo_blue_dark));
                    Toast.makeText(FeesPaymentActivity.this,
                            "Payment failed. Please try again.",
                            Toast.LENGTH_LONG).show();
                });
            }
        }, delay);
    }

    private String generateTransactionCode() {
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
        StringBuilder sb = new StringBuilder();
        Random random = new Random();
        sb.append("MPS");
        for (int i = 0; i < 10; i++) {
            sb.append(chars.charAt(random.nextInt(chars.length())));
        }
        return sb.toString();
    }
}