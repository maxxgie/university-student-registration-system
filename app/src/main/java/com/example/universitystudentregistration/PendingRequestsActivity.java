package com.example.universitystudentregistration;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
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
import com.example.universitystudentregistration.util.SharedPrefManager;

import java.util.ArrayList;
import java.util.List;

public class PendingRequestsActivity extends AppCompatActivity {

    private ListView listView;
    private TextView tvEmptyState;
    private SharedPrefManager sharedPrefManager;
    private RequestAdapter adapter;
    private List<RequestItem> requestItems;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_pending_requests);

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

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Pending Requests");
        }

        sharedPrefManager = new SharedPrefManager(this);
        listView = findViewById(R.id.listViewPendingRequests);
        tvEmptyState = findViewById(R.id.tvEmptyState);
        requestItems = new ArrayList<>();
        adapter = new RequestAdapter();
        listView.setAdapter(adapter);

        loadPendingRequests();
    }

    private void loadPendingRequests() {
        String studentIdNo = null;
        com.example.universitystudentregistration.model.Student student = sharedPrefManager.getStudent();
        if (student != null) {
            studentIdNo = student.getIdNo();
        }

        if (studentIdNo == null) {
            Toast.makeText(this, "No student data found", Toast.LENGTH_SHORT).show();
            return;
        }

        // Load pending book requests
        List<BookRequest> bookRequests = sharedPrefManager.getBookRequests();
        for (BookRequest req : bookRequests) {
            if (req.getStudentIdNo().equals(studentIdNo) && "pending".equals(req.getStatus())) {
                requestItems.add(new RequestItem(
                        "Library Book Request",
                        "Title: " + req.getTitle(),
                        "Author: " + req.getAuthor(),
                        "ISBN: " + req.getBookIsbn(),
                        "Borrowed: " + req.getDateBorrowed(),
                        "Return: " + req.getDateOfReturn(),
                        "Pending",
                        req.getTimestamp()
                ));
            }
        }

        // Load pending hostel info requests
        List<HostelInfo> hostelInfos = sharedPrefManager.getHostelInfos();
        for (HostelInfo info : hostelInfos) {
            if (info.getStudentIdNo().equals(studentIdNo) && "pending".equals(info.getStatus())) {
                requestItems.add(new RequestItem(
                        "Hostel Information Request",
                        "Location: " + info.getHostelLocation(),
                        "Room Type: " + info.getRoomType(),
                        "Sharable: " + info.getIsSharable(),
                        "Student ID: " + info.getStudentIdNo(),
                        "",
                        "Pending",
                        info.getTimestamp()
                ));
            }
        }

        // Sort by timestamp (newest first) with null safety
        requestItems.sort((a, b) -> {
            long tsA = a.timestamp != null ? Long.parseLong(a.timestamp) : 0;
            long tsB = b.timestamp != null ? Long.parseLong(b.timestamp) : 0;
            return Long.compare(tsB, tsA);
        });

        adapter.notifyDataSetChanged();

        if (requestItems.isEmpty()) {
            tvEmptyState.setVisibility(View.VISIBLE);
            listView.setVisibility(View.GONE);
            Toast.makeText(this, "No pending requests", Toast.LENGTH_SHORT).show();
        } else {
            tvEmptyState.setVisibility(View.GONE);
            listView.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }

    private static class RequestItem {
        String type;
        String line1;
        String line2;
        String line3;
        String line4;
        String line5;
        String status;
        String timestamp;

        RequestItem(String type, String line1, String line2, String line3, String line4, String line5, String status, String timestamp) {
            this.type = type;
            this.line1 = line1;
            this.line2 = line2;
            this.line3 = line3;
            this.line4 = line4;
            this.line5 = line5;
            this.status = status;
            this.timestamp = timestamp;
        }
    }

    private class RequestAdapter extends BaseAdapter {
        @Override
        public int getCount() {
            return requestItems.size();
        }

        @Override
        public Object getItem(int position) {
            return requestItems.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = LayoutInflater.from(PendingRequestsActivity.this)
                        .inflate(android.R.layout.simple_list_item_2, parent, false);
            }

            RequestItem item = requestItems.get(position);
            TextView text1 = convertView.findViewById(android.R.id.text1);
            TextView text2 = convertView.findViewById(android.R.id.text2);

            text1.setText(item.type + " - " + item.status);
            text1.setTextColor(getResources().getColor(android.R.color.holo_blue_dark));

            StringBuilder details = new StringBuilder();
            if (item.line1 != null && !item.line1.isEmpty()) details.append(item.line1).append(" | ");
            if (item.line2 != null && !item.line2.isEmpty()) details.append(item.line2).append(" | ");
            if (item.line3 != null && !item.line3.isEmpty()) details.append(item.line3).append(" | ");
            if (item.line4 != null && !item.line4.isEmpty()) details.append(item.line4).append(" | ");
            if (item.line5 != null && !item.line5.isEmpty()) details.append(item.line5);
            // Remove trailing " | "
            String detailStr = details.toString();
            if (detailStr.endsWith(" | ")) {
                detailStr = detailStr.substring(0, detailStr.length() - 3);
            }
            text2.setText(detailStr);

            // Color code by status
            if ("pending".equalsIgnoreCase(item.status)) {
                text1.setCompoundDrawablesRelativeWithIntrinsicBounds(
                        android.R.drawable.ic_dialog_alert, 0, 0, 0);
            }

            return convertView;
        }
    }
}