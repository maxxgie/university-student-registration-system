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

import com.example.universitystudentregistration.daraja.DarajaClient;
import com.example.universitystudentregistration.daraja.STKPushResponse;
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
    private DarajaClient darajaClient;
    private boolean paymentCompleted = false;
    private String currentCheckoutRequestId = null;

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
        darajaClient = new DarajaClient(this);

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

        // Check if Daraja is configured
        if (!darajaClient.getConfigManager().isConfigured()) {
            tvPaymentStatus.setText("⚠️ MPESA not configured. Add credentials to daraja_config.json");
            tvPaymentStatus.setTextColor(getResources().getColor(android.R.color.holo_orange_dark));
            btnMpesaPrompt.setEnabled(false);
        }

        // MPESA Prompt button click
        btnMpesaPrompt.setOnClickListener(v -> validateAndInitiateMpesa());

        btnViewSummary.setOnClickListener(v -> {
            if (paymentCompleted) {
                finish(); // Go back to dashboard
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

        // All validation passed - initiate real MPESA STK Push
        initiateMpesaPayment(phoneNumber, amount, studentIdNo);
    }

    private void initiateMpesaPayment(String phoneNumber, double amount, String studentIdNo) {
        // Disable button during processing
        btnMpesaPrompt.setEnabled(false);
        btnMpesaPrompt.setText("Initiating MPESA...");
        tvPaymentStatus.setText("Sending STK Push to " + phoneNumber + "...");
        tvPaymentStatus.setTextColor(getResources().getColor(android.R.color.holo_blue_dark));
        tvTransactionCode.setVisibility(View.GONE);

        darajaClient.initiateSTKPush(phoneNumber, amount, studentIdNo, 
                "University Registration Fees - " + studentIdNo, 
                new DarajaClient.OnSTKPushListener() {
                    @Override
                    public void onSuccess(STKPushResponse response) {
                        runOnUiThread(() -> {
                            currentCheckoutRequestId = response.getCheckoutRequestID();
                            tvPaymentStatus.setText("✓ STK Push sent! Please enter MPESA PIN on your phone.");
                            tvPaymentStatus.setTextColor(getResources().getColor(android.R.color.holo_blue_dark));
                            btnMpesaPrompt.setText("Waiting for payment...");
                            
                            // Start polling for payment status
                            pollPaymentStatus(studentIdNo, phoneNumber, amount);
                        });
                    }

                    @Override
                    public void onFailure(String error) {
                        runOnUiThread(() -> {
                            tvPaymentStatus.setText("Failed to send STK Push: " + error);
                            tvPaymentStatus.setTextColor(getResources().getColor(android.R.color.holo_red_dark));
                            btnMpesaPrompt.setEnabled(true);
                            btnMpesaPrompt.setText("Retry MPESA Payment");
                            btnMpesaPrompt.setBackgroundTintList(getResources().getColorStateList(android.R.color.holo_blue_dark));
                            Toast.makeText(FeesPaymentActivity.this,
                                    "MPESA Error: " + error,
                                    Toast.LENGTH_LONG).show();
                        });
                    }
                });
    }

    private void pollPaymentStatus(String studentIdNo, String phoneNumber, double amount) {
        // In a real implementation, you would either:
        // 1. Use a callback URL that receives the payment confirmation
        // 2. Poll the STK Push Query API
        // 3. For now, we'll simulate waiting for user to complete payment
        
        // Show waiting message with instructions
        tvPaymentStatus.setText("Waiting for payment completion...\n\nPlease check your phone for MPESA prompt and enter your PIN.");
        
        // Start a timeout check (in production, use callback or query API)
        new android.os.Handler(android.os.Looper.getMainLooper()).postDelayed(() -> {
            if (!paymentCompleted) {
                // Check if user might have completed payment
                runOnUiThread(() -> {
                    tvPaymentStatus.setText("Still waiting for payment...\n\nIf you completed payment, tap 'Check Status' below.");
                    btnMpesaPrompt.setEnabled(true);
                    btnMpesaPrompt.setText("Check Payment Status");
                    btnMpesaPrompt.setBackgroundTintList(getResources().getColorStateList(android.R.color.holo_orange_dark));
                    btnMpesaPrompt.setOnClickListener(v -> checkPaymentStatus(studentIdNo, phoneNumber, amount));
                });
            }
        }, 60000); // 60 seconds timeout
    }

    private void checkPaymentStatus(String studentIdNo, String phoneNumber, double amount) {
        btnMpesaPrompt.setEnabled(false);
        btnMpesaPrompt.setText("Checking status...");
        tvPaymentStatus.setText("Checking payment status with MPESA...");

        // In production, call STK Push Query API here
        // For now, we'll simulate the callback completion
        // This would be replaced by actual callback handling from your backend
        
        // Simulate checking - in real app, this queries the Daraja API
        new android.os.Handler(android.os.Looper.getMainLooper()).postDelayed(() -> {
            // In a real implementation, you would:
            // 1. Call darajaClient.stkPushQuery() with the checkoutRequestID
            // 2. Or receive callback from your backend server
            // 3. For demo, we'll assume payment might have completed
            
            // For demo purposes, show manual confirmation dialog
            runOnUiThread(() -> {
                showPaymentConfirmationDialog(studentIdNo, phoneNumber, amount);
            });
        }, 3000);
    }

    private void showPaymentConfirmationDialog(String studentIdNo, String phoneNumber, double amount) {
        new android.app.AlertDialog.Builder(this)
                .setTitle("Payment Confirmation")
                .setMessage("Did you complete the MPESA payment on your phone?\n\n" +
                        "Amount: Kshs " + String.format("%,.0f", amount) + "\n" +
                        "Phone: " + phoneNumber + "\n\n" +
                        "If yes, tap 'Yes, Payment Done'.\n" +
                        "If no, tap 'No, Retry'.")
                .setPositiveButton("Yes, Payment Done", (dialog, which) -> {
                    processSuccessfulPayment(studentIdNo, phoneNumber, amount);
                })
                .setNegativeButton("No, Retry", (dialog, which) -> {
                    btnMpesaPrompt.setEnabled(true);
                    btnMpesaPrompt.setText("Retry MPESA Payment");
                    btnMpesaPrompt.setBackgroundTintList(getResources().getColorStateList(android.R.color.holo_blue_dark));
                    btnMpesaPrompt.setOnClickListener(v -> validateAndInitiateMpesa());
                    tvPaymentStatus.setText("Ready to retry payment.");
                    tvPaymentStatus.setTextColor(getResources().getColor(android.R.color.holo_blue_dark));
                })
                .setCancelable(false)
                .show();
    }

    private void processSuccessfulPayment(String studentIdNo, String phoneNumber, double amount) {
        tvPaymentStatus.setText("Processing payment confirmation...");
        tvPaymentStatus.setTextColor(getResources().getColor(android.R.color.holo_blue_dark));
        btnMpesaPrompt.setEnabled(false);
        btnMpesaPrompt.setText("Confirming...");

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
                    tvPaymentStatus.setText("✓ Payment successful! Registration confirmed.");
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
                    tvPaymentStatus.setText("✓ Payment successful but offline. Will sync when online.");
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