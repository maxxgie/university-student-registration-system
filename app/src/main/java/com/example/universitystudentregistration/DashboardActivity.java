package com.example.universitystudentregistration;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.example.universitystudentregistration.model.Student;
import com.example.universitystudentregistration.model.HostelInfo;
import com.example.universitystudentregistration.model.Payment;
import com.example.universitystudentregistration.model.Student;
import com.example.universitystudentregistration.util.FirebaseHelper;
import com.example.universitystudentregistration.util.NetworkUtil;
import com.example.universitystudentregistration.util.SharedPrefManager;
import com.google.android.material.navigation.NavigationView;

import java.util.List;

public class DashboardActivity extends AppCompatActivity {

    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private Toolbar toolbar;
    private ActionBarDrawerToggle toggle;

    private SharedPrefManager sharedPrefManager;
    private FirebaseHelper firebaseHelper;

    private TextView tvWelcomeMessage;
    private TextView tvNavStudentName, tvNavStudentId;
    private TextView tvStatRegistration, tvStatFees, tvStatHostel;

    private View cardFeesPayment, cardHostelInfo, cardLibraryBooks, cardMyProfile, cardSyncData, cardSettings;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);

        // Initialize utilities
        sharedPrefManager = new SharedPrefManager(this);
        firebaseHelper = new FirebaseHelper(this);

        // Setup toolbar
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Setup drawer
        drawerLayout = findViewById(R.id.drawerLayout);
        navigationView = findViewById(R.id.navView);

        toggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar,
                R.string.open_drawer, R.string.close_drawer);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        // Navigation item selection
        navigationView.setNavigationItemSelectedListener(this::onNavigationItemSelected);

        // Setup header
        View headerView = navigationView.getHeaderView(0);
        tvNavStudentName = headerView.findViewById(R.id.tvNavStudentName);
        tvNavStudentId = headerView.findViewById(R.id.tvNavStudentId);

        // Connect UI elements
        tvWelcomeMessage = findViewById(R.id.tvWelcomeMessage);
        tvStatRegistration = findViewById(R.id.tvStatRegistration);
        tvStatFees = findViewById(R.id.tvStatFees);
        tvStatHostel = findViewById(R.id.tvStatHostel);

        cardFeesPayment = findViewById(R.id.cardFeesPayment);
        cardHostelInfo = findViewById(R.id.cardHostelInfo);
        cardLibraryBooks = findViewById(R.id.cardLibraryBooks);
        cardMyProfile = findViewById(R.id.cardMyProfile);
        cardSyncData = findViewById(R.id.cardSyncData);
        cardSettings = findViewById(R.id.cardSettings);

        // Load student data and update UI
        loadStudentData();

        // Card click listeners
        cardFeesPayment.setOnClickListener(v -> openFeesPayment());
        cardHostelInfo.setOnClickListener(v -> openHostelInfo());
        cardLibraryBooks.setOnClickListener(v -> openLibraryBooks());
        cardMyProfile.setOnClickListener(v -> openMyProfile());
        cardSyncData.setOnClickListener(v -> syncOfflineData());
        cardSettings.setOnClickListener(v -> openSettings());
    }

    private void loadStudentData() {
        Student student = sharedPrefManager.getStudent();
        if (student != null) {
            tvWelcomeMessage.setText("Welcome, " + student.getFullNames().split("\\s+")[0] + "!");
            tvNavStudentName.setText(student.getFullNames());
            tvNavStudentId.setText("ID: " + student.getIdNo() + " | " + student.getRegistrationNumber());

            // Update stat cards based on completion
            tvStatRegistration.setText("✓");
            tvStatRegistration.setTextColor(getResources().getColor(android.R.color.holo_green_dark));

            // Check fees payment
            List<com.example.universitystudentregistration.model.Payment> payments = sharedPrefManager.getPayments();
            boolean feesPaid = false;
            for (com.example.universitystudentregistration.model.Payment p : payments) {
                if (p.getStudentIdNo().equals(student.getIdNo()) && "completed".equals(p.getStatus())) {
                    feesPaid = true;
                    break;
                }
            }
            if (feesPaid) {
                tvStatFees.setText("✓");
                tvStatFees.setTextColor(getResources().getColor(android.R.color.holo_green_dark));
            } else {
                tvStatFees.setText("₳");
                tvStatFees.setTextColor(getResources().getColor(android.R.color.holo_orange_dark));
            }

            // Check hostel info
            List<com.example.universitystudentregistration.model.HostelInfo> hostels = sharedPrefManager.getHostelInfos();
            boolean hostelDone = false;
            for (com.example.universitystudentregistration.model.HostelInfo h : hostels) {
                if (h.getStudentIdNo().equals(student.getIdNo())) {
                    hostelDone = true;
                    break;
                }
            }
            if (hostelDone) {
                tvStatHostel.setText("✓");
                tvStatHostel.setTextColor(getResources().getColor(android.R.color.holo_green_dark));
            } else {
                tvStatHostel.setText("🏠");
            }
        }
    }

    private boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.nav_dashboard) {
            // Already on dashboard
        } else if (id == R.id.nav_fees_payment) {
            openFeesPayment();
        } else if (id == R.id.nav_hostel_info) {
            openHostelInfo();
        } else if (id == R.id.nav_library_books) {
            openLibraryBooks();
        } else if (id == R.id.nav_my_profile) {
            openMyProfile();
        } else if (id == R.id.nav_sync_data) {
            syncOfflineData();
        } else if (id == R.id.nav_settings) {
            openSettings();
        } else if (id == R.id.nav_logout) {
            logout();
        }
        drawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }

    private void openFeesPayment() {
        Intent intent = new Intent(this, FeesPaymentActivity.class);
        startActivity(intent);
    }

    private void openHostelInfo() {
        Intent intent = new Intent(this, HostelInfoActivity.class);
        startActivity(intent);
    }

    private void openLibraryBooks() {
        Intent intent = new Intent(this, BookRequestActivity.class);
        startActivity(intent);
    }

    private void openMyProfile() {
        Intent intent = new Intent(this, SummaryActivity.class);
        startActivity(intent);
    }

    private void syncOfflineData() {
        if (!NetworkUtil.isConnected(this)) {
            Toast.makeText(this, "No internet connection. Will sync when online.", Toast.LENGTH_SHORT).show();
            return;
        }
        firebaseHelper.syncOfflineData();
        Toast.makeText(this, "Syncing offline data...", Toast.LENGTH_SHORT).show();
    }

    private void openSettings() {
        Toast.makeText(this, "Settings - Coming Soon", Toast.LENGTH_SHORT).show();
    }

    private void logout() {
        sharedPrefManager.clearStudent();
        Intent intent = new Intent(this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    @Override
    public void onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadStudentData();
    }
}