package com.example.universitystudentregistration;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.universitystudentregistration.model.HostelInfo;
import com.example.universitystudentregistration.model.Student;
import com.example.universitystudentregistration.util.FirebaseHelper;
import com.example.universitystudentregistration.util.NetworkUtil;
import com.example.universitystudentregistration.util.SharedPrefManager;

public class HostelInfoActivity extends AppCompatActivity {

    private Spinner spHostelLocation, spRoomType;
    private RadioGroup rgSharable;
    private EditText etStudentIdNoHostel;
    private Button btnSubmitHostel, btnBackToBook, btnNextToFees;
    private SharedPrefManager sharedPrefManager;
    private FirebaseHelper firebaseHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        super.onCreate(savedInstanceState);

        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_hostel_info);

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
        spHostelLocation = findViewById(R.id.spHostelLocation);
        spRoomType = findViewById(R.id.spRoomType);
        rgSharable = findViewById(R.id.rgSharable);
        etStudentIdNoHostel = findViewById(R.id.etStudentIdNoHostel);

        btnSubmitHostel = findViewById(R.id.btnSubmitHostel);
        btnBackToBook = findViewById(R.id.btnBackToBook);
        btnNextToFees = findViewById(R.id.btnNextToFees);

        // Populate Hostel Location dropdown
        ArrayAdapter<CharSequence> locationAdapter = ArrayAdapter.createFromResource(
                this,
                R.array.hostel_locations,
                android.R.layout.simple_spinner_item
        );
        locationAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spHostelLocation.setAdapter(locationAdapter);

        // Populate Room Type dropdown
        ArrayAdapter<CharSequence> roomTypeAdapter = ArrayAdapter.createFromResource(
                this,
                R.array.room_types,
                android.R.layout.simple_spinner_item
        );
        roomTypeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spRoomType.setAdapter(roomTypeAdapter);

        // Pre-fill student ID if available
        Student student = sharedPrefManager.getStudent();
        if (student != null) {
            etStudentIdNoHostel.setText(student.getIdNo());
            etStudentIdNoHostel.setEnabled(false);
        }

        // Submit button click
        btnSubmitHostel.setOnClickListener(v -> validateAndSubmitHostelInfo());

        // Navigation buttons
        btnBackToBook.setOnClickListener(v -> {
            finish();
        });

        btnNextToFees.setOnClickListener(v -> {
            Intent intent = new Intent(HostelInfoActivity.this, FeesPaymentActivity.class);
            startActivity(intent);
        });
    }

    private void validateAndSubmitHostelInfo() {
        // Get selected values
        String hostelLocation = spHostelLocation.getSelectedItem().toString();
        String roomType = spRoomType.getSelectedItem().toString();

        // Validate Sharable selection
        if (rgSharable.getCheckedRadioButtonId() == -1) {
            Toast.makeText(this, "Please select whether room is sharable", Toast.LENGTH_SHORT).show();
            return;
        }

        String isSharable = rgSharable.getCheckedRadioButtonId() == R.id.rbYes ? "Yes" : "No";

        // Validate Student ID
        String studentIdNo = etStudentIdNoHostel.getText().toString().trim();
        if (studentIdNo.isEmpty()) {
            etStudentIdNoHostel.setError("Please enter student ID number");
            etStudentIdNoHostel.requestFocus();
            return;
        }

        if (!studentIdNo.matches("\\d{8}")) {
            etStudentIdNoHostel.setError("ID Number must contain exactly 8 digits");
            etStudentIdNoHostel.requestFocus();
            return;
        }

        // All validation passed - create hostel info object
        HostelInfo hostelInfo = new HostelInfo(hostelLocation, roomType, isSharable, studentIdNo);

        // Save locally first
        sharedPrefManager.saveHostelInfo(hostelInfo);

        // Save to Firebase (handles offline automatically)
        firebaseHelper.saveHostelInfo(hostelInfo, new FirebaseHelper.OnFirebaseOperationListener() {
            @Override
            public void onSuccess() {
                runOnUiThread(() -> {
                    String statusMsg = NetworkUtil.isConnected(HostelInfoActivity.this) ?
                            "Hostel information submitted successfully!" :
                            "Hostel information saved offline. Will sync when online.";
                    Toast.makeText(HostelInfoActivity.this, statusMsg, Toast.LENGTH_LONG).show();
                });
            }

            @Override
            public void onFailure(String errorMessage) {
                runOnUiThread(() ->
                        Toast.makeText(HostelInfoActivity.this, "Error: " + errorMessage, Toast.LENGTH_LONG).show()
                );
            }
        });
    }
}