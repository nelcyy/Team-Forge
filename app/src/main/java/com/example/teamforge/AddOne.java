package com.example.teamforge;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AddOne extends AppCompatActivity {

    private Spinner memberSpinner;
    private TableLayout memberTable;
    private EditText projectNameEditText, subjectNameEditText;
    private Button nextButton;
    private FirebaseFirestore db;
    private String creatorEmail; // To hold the creator's email
    private List<String> selectedMembers = new ArrayList<>(); // List to track selected members

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.add_one);

        // Retrieve the creator's email passed from MainActivity
        Intent intent = getIntent();
        creatorEmail = intent.getStringExtra("CREATOR_EMAIL");

        // Initialize Firestore, views, and table layout
        db = FirebaseFirestore.getInstance();
        memberSpinner = findViewById(R.id.spinner);
        memberTable = findViewById(R.id.MemberTable);
        projectNameEditText = findViewById(R.id.ProjectName);
        subjectNameEditText = findViewById(R.id.SubjectName);
        nextButton = findViewById(R.id.button);

        // Load emails into the spinner, excluding the creator's email
        loadEmailsIntoSpinner();

        // Set up item selected listener for spinner
        memberSpinner.setOnItemSelectedListener(new android.widget.AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(android.widget.AdapterView<?> parent, View view, int position, long id) {
                String selectedEmail = (String) parent.getItemAtPosition(position);
                if (selectedEmail != null && !selectedEmail.equals(creatorEmail) && !selectedMembers.contains(selectedEmail)) {
                    addMemberToTable(selectedEmail);
                }
            }

            @Override
            public void onNothingSelected(android.widget.AdapterView<?> parent) {
                // Handle case when nothing is selected
            }
        });

        // Set up click listener for the "Next" button
        nextButton.setOnClickListener(v -> saveProjectData());
    }

    private void loadEmailsIntoSpinner() {
        db.collection("users")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        List<String> emailList = new ArrayList<>();
                        for (com.google.firebase.firestore.DocumentSnapshot document : task.getResult().getDocuments()) {
                            String email = document.getString("email");
                            if (email != null && !email.equals(creatorEmail)) {
                                emailList.add(email);
                            }
                        }
                        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, emailList);
                        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                        memberSpinner.setAdapter(adapter);
                    } else {
                        Toast.makeText(this, "Failed to fetch users: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void addMemberToTable(String email) {
        TableRow row = new TableRow(this);
        TextView memberText = new TextView(this);
        memberText.setText(email);
        memberText.setPadding(20, 10, 20, 10);
        memberText.setOnClickListener(v -> removeMemberFromTable(row, email));
        row.addView(memberText);
        memberTable.addView(row);
        selectedMembers.add(email);
    }

    private void removeMemberFromTable(TableRow row, String email) {
        memberTable.removeView(row);
        selectedMembers.remove(email);
        Toast.makeText(this, email + " removed from members.", Toast.LENGTH_SHORT).show();
    }

    private void saveProjectData() {
        String projectName = projectNameEditText.getText().toString().trim();
        String subjectName = subjectNameEditText.getText().toString().trim();

        if (projectName.isEmpty() || subjectName.isEmpty() || selectedMembers.isEmpty()) {
            Toast.makeText(this, "Please fill in all fields and select at least one member.", Toast.LENGTH_SHORT).show();
            return;
        }

        Map<String, Object> projectData = new HashMap<>();
        projectData.put("projectName", projectName);
        projectData.put("subjectName", subjectName);
        projectData.put("leaderEmail", creatorEmail);
        projectData.put("members", selectedMembers);

        db.collection("projects")
                .add(projectData)
                .addOnSuccessListener(documentReference -> {
                    String documentId = documentReference.getId(); // Get the created document's ID
                    Toast.makeText(this, "Project saved successfully!", Toast.LENGTH_SHORT).show();

                    // Create intent to navigate back to MainActivity and send a signal to refresh
                    Intent intent = new Intent(AddOne.this, MainActivity.class);
                    intent.putExtra("REFRESH", true);  // Flag to indicate refresh
                    startActivity(intent);

                    Intent intentt = new Intent(AddOne.this, Member.class);
                    intentt.putExtra("PROJECT_ID", documentId);
                    startActivity(intentt);

                    finish(); // Close AddOne activity
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Failed to save project: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

}