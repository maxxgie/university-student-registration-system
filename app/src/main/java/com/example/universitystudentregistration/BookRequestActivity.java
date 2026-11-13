package com.example.universitystudentregistration;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

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

        btnSubmitBookRequest = findViewById(R.id.btnSubmitBookRequest);
        btnBackToRegistration = findViewById(R.id.btnBackToRegistration);
        btnNextToHostel = findViewById(R.id.btnNextToHostel);

        // Pre-fill student ID if available
        Student student = sharedPrefManager.getStudent();
        if (student != null) {
            etStudentIdNoBook.setText(student.getIdNo());
            etStudentIdNoBook.setEnabled(false);
        }

        // Submit button click
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
            etDateBorrowed.setError("Please enter date borrowed");
            etDateBorrowed.requestFocus();
            return;
        }

        // Validate Date of Return
        if (dateOfReturn.isEmpty()) {
            etDateOfReturn.setError("Please enter date of return");
            etDateOfReturn.requestFocus();
            return;
        }

        // Validate Student ID
        if (studentIdNo.isEmpty()) {
            etStudentIdNoBook.setError("Please enter student ID number");
            etStudentIdNoBook.requestFocus();
            return;
        }

        if (!studentIdNo.matches("\\d{8}")) {
            etStudentIdNoBook.setError("ID Number must contain exactly 8 digits");
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