package com.example.teamforge;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class MainActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private ProjectAdapter adapter;
    private List<ProjectModel> projectModelList;
    private FirebaseFirestore db;
    private String creatorEmail; // To hold the creator's email
    private ImageView addButton, logoutButton; // Add logoutButton
    private Set<String> projectIdSet; // For quick duplicate checks
    private TextView username; // Declare TextView for username

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        // Initialize views
        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        projectModelList = new ArrayList<>();
        adapter = new ProjectAdapter(projectModelList); // Pass click listener
        recyclerView.setAdapter(adapter);

        // Initialize Firestore
        db = FirebaseFirestore.getInstance();
        projectIdSet = new HashSet<>();
        username = findViewById(R.id.username); // Initialize TextView

        // Retrieve the logged-in user's email from SharedPreferences
        SharedPreferences sharedPreferences = getSharedPreferences("user_session", MODE_PRIVATE);
        creatorEmail = sharedPreferences.getString("email", null);

        // Fetch projects and user data from Firestore
        fetchProjectData();

        // Fetch user data (username) from Firestore and set the TextView
        if (creatorEmail != null) {
            db.collection("users") // Assuming "users" collection holds the user data
                    .whereEqualTo("email", creatorEmail)
                    .get()
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful() && !task.getResult().isEmpty()) {
                            DocumentSnapshot userDoc = task.getResult().getDocuments().get(0);
                            String username = userDoc.getString("username"); // Adjust field name accordingly
                            this.username.setText(username);
                        } else {
                            // Handle case where user data is not found
                            Toast.makeText(this, "Failed to fetch user data", Toast.LENGTH_SHORT).show();
                        }
                    });
        }

        // Set click listener for Add button
        addButton = findViewById(R.id.add);
        addButton.setOnClickListener(v -> {
            if (creatorEmail != null) {
                // Navigate to AddOne activity and pass the creator's email
                Intent intent = new Intent(MainActivity.this, AddOne.class);
                intent.putExtra("CREATOR_EMAIL", creatorEmail);
                startActivity(intent);
            } else {
                Toast.makeText(MainActivity.this, "Please log in first!", Toast.LENGTH_SHORT).show();
            }
        });

        // Set click listener for Logout button
        logoutButton = findViewById(R.id.logout);
        logoutButton.setOnClickListener(v -> {
            // Clear saved session data
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.clear();
            editor.apply();

            // Notify user and redirect to login screen
            Toast.makeText(MainActivity.this, "Logged out successfully", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(MainActivity.this, Login.class);
            startActivity(intent);
            finish();
        });
    }

    @Override
    protected void onResume() {
        super.onResume();

        // Check if refresh is required (e.g., after adding a project)
        if (getIntent().getBooleanExtra("REFRESH", false)) {
            fetchProjectData();
            getIntent().removeExtra("REFRESH"); // Reset the flag
        }
    }

    private void fetchProjectData() {
        projectModelList.clear();
        projectIdSet.clear();

        // Fetch projects where the user is a member
        db.collection("projects")
                .whereArrayContains("members", creatorEmail)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        for (DocumentSnapshot document : task.getResult()) {
                            addProjectToList(document);
                        }
                        adapter.notifyDataSetChanged();
                    } else {
                        Toast.makeText(this, "Failed to fetch projects: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });

        // Fetch projects where the user is the leader
        db.collection("projects")
                .whereEqualTo("leaderEmail", creatorEmail)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        for (DocumentSnapshot document : task.getResult()) {
                            addProjectToList(document);
                        }
                        adapter.notifyDataSetChanged();
                    } else {
                        Toast.makeText(this, "Failed to fetch projects: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    /**
     * Add a project to the list if it is not a duplicate.
     */
    private void addProjectToList(DocumentSnapshot document) {
        String projectId = document.getId();
        if (!projectIdSet.contains(projectId)) {
            projectIdSet.add(projectId);

            String projectName = document.getString("projectName");
            String subjectName = document.getString("subjectName");
            String leaderEmail = document.getString("leaderEmail");
            String userRole = creatorEmail.equals(leaderEmail) ? "Leader" : "Member";

            projectModelList.add(new ProjectModel(projectName, subjectName, userRole, projectId));
        }
    }

    /**
     * Handle project selection and navigate to Member.java.
     */
    private void onProjectSelected(ProjectModel project) {
        Intent intent = new Intent(MainActivity.this, Member.class);
        intent.putExtra("PROJECT_ID", project.getProjectId());
        intent.putExtra("ROLE", project.getUserRole());
        startActivity(intent);
    }
}