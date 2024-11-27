package com.example.teamforge;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

public class Member extends AppCompatActivity {

    private RecyclerView recyclerView;
    private MemberAdapter adapter;
    private List<MemberModel> memberModelList;
    private FirebaseFirestore db;

    private TextView projectNameTextView, subjectNameTextView;
    private String loggedInUserEmail; // Email of the currently logged-in user
    private String userRole; // Role of the logged-in user in the project (Leader/Member)

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.member);

        // Initialize views
        recyclerView = findViewById(R.id.recyclerView);
        projectNameTextView = findViewById(R.id.ProjectName);
        subjectNameTextView = findViewById(R.id.SubjectName);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        memberModelList = new ArrayList<>();
        adapter = new MemberAdapter(memberModelList);
        recyclerView.setAdapter(adapter);

        // Initialize Firestore
        db = FirebaseFirestore.getInstance();

        // Retrieve the logged-in user's email from SharedPreferences
        SharedPreferences sharedPreferences = getSharedPreferences("user_session", MODE_PRIVATE);
        loggedInUserEmail = sharedPreferences.getString("email", null);

        String projectId = getIntent().getStringExtra("PROJECT_ID"); // Retrieve the projectId

        if (projectId == null || projectId.isEmpty()) {
            Toast.makeText(this, "Project ID not found.", Toast.LENGTH_SHORT).show();
            finish(); // Close the activity if no projectId is passed
            return;
        }

        // Fetch project data and determine logged-in user's role
        fetchProjectData(projectId);
    }

    private void fetchProjectData(String projectId) {
        // Fetch project data from Firestore using the projectId
        db.collection("projects").document(projectId)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        DocumentSnapshot document = task.getResult();
                        if (document.exists()) {
                            String projectName = document.getString("projectName");
                            String subjectName = document.getString("subjectName");
                            List<String> members = (List<String>) document.get("members");
                            String leaderEmail = document.getString("leaderEmail");

                            // Determine the logged-in user's role
                            if (loggedInUserEmail.equals(leaderEmail)) {
                                userRole = "Leader";
                            } else if (members != null && members.contains(loggedInUserEmail)) {
                                userRole = "Member";
                            } else {
                                userRole = "Unknown";
                            }

                            // Update UI with project information
                            projectNameTextView.setText(projectName);
                            subjectNameTextView.setText(subjectName);

                            // Add project members to the list
                            if (members != null) {
                                for (String email : members) {
                                    memberModelList.add(new MemberModel(email));
                                }
                                adapter.notifyDataSetChanged();
                            }

                            // Notify the user of their role in the project
                            Toast.makeText(this, "Your role: " + userRole, Toast.LENGTH_SHORT).show();

                            // Set the on-click listener for the adapter
                            adapter.setOnMemberClickListener(email -> {
                                // Handle item click, pass both role and clicked member's email
                                navigateToTaskPage(email, projectId);
                            });

                        } else {
                            Toast.makeText(this, "Project data not found.", Toast.LENGTH_SHORT).show();
                            finish(); // Close activity if document doesn't exist
                        }
                    } else {
                        Toast.makeText(this, "Failed to fetch project: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void navigateToTaskPage(String clickedMemberEmail, String projectId) {
        // Pass the logged-in user's role, loggedInUserEmail, and clicked member's email to the Task page
        Intent intent = new Intent(Member.this, Task.class);
        intent.putExtra("MEMBER_EMAIL", clickedMemberEmail);
        intent.putExtra("ROLE", userRole); // Pass the logged-in user's role
        intent.putExtra("PROJECT_ID", projectId);
        intent.putExtra("LOGGED_IN_EMAIL", loggedInUserEmail); // Pass the logged-in user's email
        startActivity(intent);
    }
}
