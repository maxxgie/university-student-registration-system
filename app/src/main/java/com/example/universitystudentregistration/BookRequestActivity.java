package com.example.universitystudentregistration;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.universitystudentregistration.model.BookRequest;
import com.example.universitystudentregistration.model.Student;
import com.example.universitystudentregistration.util.FirebaseHelper;
import com.example.universitystudentregistration.util.NetworkUtil;
import com.example.universitystudentregistration.util.SharedPrefManager;

import java.util.Calendar;

public class BookRequestActivity extends AppCompatActivity {

    private EditText etBookIsbn, etAuthor, etTitle, etDateBorrowed, etDateOfReturn, etStudentIdNoBook;
    private Button btnSubmitBookRequest, btnBackToRegistration, btnNextToHostel;
    private SharedPrefManager sharedPrefManager;
    private FirebaseHelper firebaseHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        super.onCreate(savedInstanceState);

        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_book_request);

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
        etBookIsbn = findViewById(R.id.etBookIsbn);
        etAuthor = findViewById(R.id.etAuthor);
        etTitle = findViewById(R.id.etTitle);
        etDateBorrowed = findViewById(R.id.etDateBorrowed);
        etDateOfReturn = findViewById(R.id.etDateOfReturn);
        etStudentIdNoBook = findViewById(R.id.etStudentIdNoBook);

        // Set placeholders/hints for input fields
        etBookIsbn.setHint("ISBN (e.g., 978-0-123456-78-9)");
        etAuthor.setHint("Author name");
        etTitle.setHint("Book title");
        etDateBorrowed.setHint("Date borrowed (tap to select)");
        etDateOfReturn.setHint("Date of return (tap to select)");
        etStudentIdNoBook.setHint("Student ID (8 or 9 digits)");

        // Pre-fill student ID if available
        Student student = sharedPrefManager.getStudent();
        if (student != null) {
            etStudentIdNoBook.setText(student.getIdNo());
            etStudentIdNoBook.setEnabled(false);
        }

        // Date picker for Date Borrowed
        etDateBorrowed.setFocusable(false);
        etDateBorrowed.setOnClickListener(v -> showDatePicker(etDateBorrowed));

        // Date picker for Date of Return
        etDateOfReturn.setFocusable(false);
        etDateOfReturn.setOnClickListener(v -> showDatePicker(etDateOfReturn));

        // Submit button click
        btnSubmitBookRequest = findViewById(R.id.btnSubmitBookRequest);
        btnBackToRegistration = findViewById(R.id.btnBackToRegistration);
        btnNextToHostel = findViewById(R.id.btnNextToHostel);

        btnSubmitBookRequest.setOnClickListener(v -> validateAndSubmitBookRequest());

        // Navigation buttons
        btnBackToRegistration.setOnClickListener(v -> {
            finish();
        });

        btnNextToHostel.setOnClickListener(v -> {
            Intent intent = new Intent(BookRequestActivity.this, HostelInfoActivity.class);
            startActivity(intent);
        });
    }

    private void showDatePicker(final EditText dateField) {
        final Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(
                this,
                new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int selectedYear, int selectedMonth, int selectedDayOfMonth) {
                        String date = String.format("%04d-%02d-%02d", selectedYear, selectedMonth + 1, selectedDayOfMonth);
                        dateField.setText(date);
                    }
                },
                year, month, day
        );
        datePickerDialog.getDatePicker().setMinDate(System.currentTimeMillis() - 1000);
        datePickerDialog.show();
    }

    private void validateAndSubmitBookRequest() {
        // Retrieve entered values
        String bookIsbn = etBookIsbn.getText().toString().trim();
        String author = etAuthor.getText().toString().trim();
        String title = etTitle.getText().toString().trim();
        String dateBorrowed = etDateBorrowed.getText().toString().trim();
        String dateOfReturn = etDateOfReturn.getText().toString().trim();
        String studentIdNo = etStudentIdNoBook.getText().toString().trim();

        // Validate Book ISBN
        if (bookIsbn.isEmpty()) {
            etBookIsbn.setError("Please enter book ISBN");
            etBookIsbn.requestFocus();
            return;
        }

        // Validate Author
        if (author.isEmpty()) {
            etAuthor.setError("Please enter author name");
            etAuthor.requestFocus();
            return;
        }

        // Validate Title
        if (title.isEmpty()) {
            etTitle.setError("Please enter book title");
            etTitle.requestFocus();
            return;
        }

        // Validate Date Borrowed
        if (dateBorrowed.isEmpty()) {
            etDateBorrowed.setError("Please select date borrowed");
            etDateBorrowed.requestFocus();
            return;
        }

        // Validate Date of Return
        if (dateOfReturn.isEmpty()) {
            etDateOfReturn.setError("Please select date of return");
            etDateOfReturn.requestFocus();
            return;
        }

        // Validate that return date is after borrow date
        if (!dateBorrowed.isEmpty() && !dateOfReturn.isEmpty()) {
            if (dateOfReturn.compareTo(dateBorrowed) < 0) {
                etDateOfReturn.setError("Return date must be after borrow date");
                etDateOfReturn.requestFocus();
                return;
            }
        }

        // Validate Student ID
        if (studentIdNo.isEmpty()) {
            etStudentIdNoBook.setError("Please enter student ID number");
            etStudentIdNoBook.requestFocus();
            return;
        }

        if (!studentIdNo.matches("\\d{8,9}")) {
            etStudentIdNoBook.setError("ID Number must contain 8 or 9 digits");
            etStudentIdNoBook.requestFocus();
            return;
        }

        // All validation passed - create book request object
        BookRequest bookRequest = new BookRequest(bookIsbn, author, title, dateBorrowed, dateOfReturn, studentIdNo);

        // Save locally first
        sharedPrefManager.saveBookRequest(bookRequest);

        // Save to Firebase (handles offline automatically)
        firebaseHelper.saveBookRequest(bookRequest, new FirebaseHelper.OnFirebaseOperationListener() {
            @Override
            public void onSuccess() {
                runOnUiThread(() -> {
                    String statusMsg = NetworkUtil.isConnected(BookRequestActivity.this) ?
                            "Book request submitted successfully!" :
                            "Book request saved offline. Will sync when online.";
                    Toast.makeText(BookRequestActivity.this, statusMsg, Toast.LENGTH_LONG).show();
                    clearFields();
                });
            }

            @Override
            public void onFailure(String errorMessage) {
                runOnUiThread(() ->
                        Toast.makeText(BookRequestActivity.this, "Error: " + errorMessage, Toast.LENGTH_LONG).show()
                );
            }
        });
    }

    private void clearFields() {
        etBookIsbn.setText("");
        etAuthor.setText("");
        etTitle.setText("");
        etDateBorrowed.setText("");
        etDateOfReturn.setText("");
        // Don't clear student ID as it's pre-filled
    }
}