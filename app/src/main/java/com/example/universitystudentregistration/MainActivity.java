
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

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.universitystudentregistration.model.Student;
import com.example.universitystudentregistration.util.FirebaseHelper;
import com.example.universitystudentregistration.util.NetworkUtil;
import com.example.universitystudentregistration.util.SharedPrefManager;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        super.onCreate(savedInstanceState);

        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

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

        // Connect the department and course dropdowns
        Spinner spDepartment = findViewById(R.id.spDepartment);
        Spinner spCourse = findViewById(R.id.spCourse);

        // Populate the Department dropdown
        ArrayAdapter<CharSequence> departmentAdapter =
                ArrayAdapter.createFromResource(
                        this,
                        R.array.departments,
                        android.R.layout.simple_spinner_item
                );

        departmentAdapter.setDropDownViewResource(
                android.R.layout.simple_spinner_dropdown_item
        );

        spDepartment.setAdapter(departmentAdapter);

        // Update courses based on the selected department
        spDepartment.setOnItemSelectedListener(
                new AdapterView.OnItemSelectedListener() {

                    @Override
                    public void onItemSelected(
                            AdapterView<?> parent,
                            View view,
                            int position,
                            long id) {

                        int courseArrayId;

                        switch (position) {

                            case 0:
                                courseArrayId = R.array.technology_courses;
                                break;

                            case 1:
                                courseArrayId = R.array.engineering_courses;
                                break;

                            case 2:
                                courseArrayId = R.array.commerce_courses;
                                break;

                            case 3:
                                courseArrayId = R.array.nursing_courses;
                                break;

                            case 4:
                                courseArrayId = R.array.food_science_courses;
                                break;

                            default:
                                courseArrayId = R.array.technology_courses;
                                break;
                        }

                        ArrayAdapter<CharSequence> courseAdapter =
                                ArrayAdapter.createFromResource(
                                        MainActivity.this,
                                        courseArrayId,
                                        android.R.layout.simple_spinner_item
                                );

                        courseAdapter.setDropDownViewResource(
                                android.R.layout.simple_spinner_dropdown_item
                        );

                        spCourse.setAdapter(courseAdapter);
                    }

                    @Override
                    public void onNothingSelected(AdapterView<?> parent) {
                        // No department selected
                    }
                });

        // Connect the Register button
        Button btnRegister = findViewById(R.id.btnRegister);

        btnRegister.setOnClickListener(v -> validateRegistration());

        // Initialize utilities
        sharedPrefManager = new SharedPrefManager(this);
        firebaseHelper = new FirebaseHelper(this);
    }

    private SharedPrefManager sharedPrefManager;
    private FirebaseHelper firebaseHelper;

    // Validate student registration details
    private void validateRegistration() {

        EditText etFullNames = findViewById(R.id.etFullNames);
        EditText etIdNo = findViewById(R.id.etIdNo);
        EditText etRegistrationNumber =
                findViewById(R.id.etRegistrationNumber);

        RadioGroup rgGender = findViewById(R.id.rgGender);
        Spinner spDepartment = findViewById(R.id.spDepartment);
        Spinner spCourse = findViewById(R.id.spCourse);

        // Retrieve entered values
        String fullNames = etFullNames.getText().toString().trim();
        String idNo = etIdNo.getText().toString().trim();
        String registrationNumber =
                etRegistrationNumber.getText().toString().trim();
        String department = spDepartment.getSelectedItem().toString();
        String course = spCourse.getSelectedItem().toString();

        // Validate Full Names: exactly three names
        String[] names = fullNames.split("\\s+");

        if (names.length != 3 ||
                fullNames.isEmpty()) {

            etFullNames.setError(
                    "Please enter exactly three names"
            );
            etFullNames.requestFocus();
            return;
        }

        // Ensure names contain valid letters
        for (String name : names) {

            if (!name.matches("[\\p{L}]+(?:[-'][\\p{L}]+)*")) {

                etFullNames.setError(
                        "Names should contain letters only"
                );
                etFullNames.requestFocus();
                return;
            }
        }

        // Validate ID Number: exactly 8 digits
        if (!idNo.matches("\\d{8}")) {

            etIdNo.setError(
                    "ID Number must contain exactly 8 digits"
            );
            etIdNo.requestFocus();
            return;
        }

        // Validate Registration Number
        // Expected format: C026-01-0777/2024
        if (!registrationNumber.matches(
                "[A-Za-z]\\d{3}-\\d{2}-\\d{4}/\\d{4}")) {

            etRegistrationNumber.setError(
                    "Use format: C026-01-0777/2024"
            );
            etRegistrationNumber.requestFocus();
            return;
        }

        // Validate Gender
        if (rgGender.getCheckedRadioButtonId() == -1) {

            Toast.makeText(
                    this,
                    "Please select your gender",
                    Toast.LENGTH_SHORT
            ).show();

            return;
        }

        String gender = rgGender.getCheckedRadioButtonId() == R.id.rbMale ? "Male" : "Female";

        // All validation checks have passed - create student object
        Student student = new Student(fullNames, idNo, registrationNumber, gender, department, course);

        // Save locally first
        sharedPrefManager.saveStudent(student);

        // Save to Firebase (handles offline automatically)
        firebaseHelper.saveStudent(student, new FirebaseHelper.OnFirebaseOperationListener() {
            @Override
            public void onSuccess() {
                runOnUiThread(() -> {
                    String statusMsg = NetworkUtil.isConnected(MainActivity.this) ?
                            "Registration successful! Proceeding to library book request..." :
                            "Registration saved offline. Will sync when online. Proceeding to library book request...";
                    Toast.makeText(MainActivity.this, statusMsg, Toast.LENGTH_LONG).show();

                    // Navigate to Book Request Activity
                    Intent intent = new Intent(MainActivity.this, BookRequestActivity.class);
                    startActivity(intent);
                });
            }

            @Override
            public void onFailure(String errorMessage) {
                runOnUiThread(() ->
                        Toast.makeText(MainActivity.this, "Error: " + errorMessage, Toast.LENGTH_LONG).show()
                );
            }
        });
    }
}
