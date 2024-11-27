package com.example.teamforge;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.view.Gravity;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

public class Task extends AppCompatActivity {

    private TableLayout taskTable;
    private Button addTaskButton;
    private EditText deadlineEditText;
    private TextView memberEmailTextView, taskTextView, statusTextView;
    private Button saveButton;

    private boolean isLeader;
    private String role, loggedInEmail, memberEmail;
    private FirebaseFirestore db;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.task);
        // Initialize views
        taskTable = findViewById(R.id.TaskTable);
        addTaskButton = findViewById(R.id.AddTask);
        deadlineEditText = findViewById(R.id.Deadline);
        memberEmailTextView = findViewById(R.id.MembersEmail);
        saveButton = findViewById(R.id.Save);

        // Get role and email from intent
        role = getIntent().getStringExtra("ROLE");
        loggedInEmail = getIntent().getStringExtra("LOGGED_IN_EMAIL");
        memberEmail = getIntent().getStringExtra("MEMBER_EMAIL");
        isLeader = "Leader".equals(role);

        db = FirebaseFirestore.getInstance();

        // Display member email
        if (memberEmail != null) {
            memberEmailTextView.setText(memberEmail);
        } else {
            Toast.makeText(this, "No member email received", Toast.LENGTH_SHORT).show();
        }

        setupRoleBasedUI();

        addTaskButton.setOnClickListener(v -> addTaskRow(memberEmail)); // Pass the member's email

        deadlineEditText.setOnClickListener(v -> {
            if (isLeader) showDatePicker();
        });

        saveButton.setOnClickListener(v -> saveTaskData());
    }

    private void setupRoleBasedUI() {
        if (isLeader) {
            // Leader: Enable all actions
            addTaskButton.setEnabled(true);
            deadlineEditText.setEnabled(true);
            saveButton.setVisibility(Button.VISIBLE);
        } else {
            // Member: Check if logged-in email matches assigned email
            addTaskButton.setVisibility(Button.GONE);
            deadlineEditText.setEnabled(false); // Members can't edit deadline

            if (loggedInEmail.equals(memberEmail)) {
                saveButton.setVisibility(Button.VISIBLE); // Allow saving for assigned member
            } else {
                saveButton.setVisibility(Button.GONE); // Hide save button otherwise
            }
        }
    }

    private void showDatePicker() {
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        new DatePickerDialog(this, (view, selectedYear, selectedMonth, selectedDay) -> {
            String selectedDate = selectedDay + "/" + (selectedMonth + 1) + "/" + selectedYear;
            deadlineEditText.setText(selectedDate);
        }, year, month, day).show();
    }

    private void addTaskRow(String assignedEmail) {
        TableRow newRow = new TableRow(this);
        newRow.setLayoutParams(new TableRow.LayoutParams(
                TableRow.LayoutParams.MATCH_PARENT,
                TableRow.LayoutParams.WRAP_CONTENT
        ));

        // Create an EditText for the task
        EditText taskEditText = createTaskEditText(assignedEmail);
        newRow.addView(taskEditText);

        // Create a RadioGroup for the status
        RadioGroup statusGroup = createStatusRadioGroup(assignedEmail);
        newRow.addView(statusGroup);

        // Add the new row to the table
        taskTable.addView(newRow);

        Toast.makeText(this, "New task row added!", Toast.LENGTH_SHORT).show();
    }

    private EditText createTaskEditText(String assignedEmail) {
        EditText taskEditText = new EditText(this);
        taskEditText.setLayoutParams(new TableRow.LayoutParams(
                0,
                TableRow.LayoutParams.WRAP_CONTENT,
                1
        ));
        taskEditText.setHint("Enter task");
        taskEditText.setPadding(10, 25, 10, 25);

        // Disable task editing for members unless they are leaders
        if (!isLeader) {
            taskEditText.setEnabled(false);
        }
        return taskEditText;
    }

    private RadioGroup createStatusRadioGroup(String assignedEmail) {
        RadioGroup statusGroup = new RadioGroup(this);
        statusGroup.setLayoutParams(new TableRow.LayoutParams(
                0,
                TableRow.LayoutParams.WRAP_CONTENT,
                1
        ));
        statusGroup.setOrientation(RadioGroup.HORIZONTAL);
        statusGroup.setGravity(Gravity.CENTER);

        // Add "On Going" radio button
        RadioButton onGoingRadio = new RadioButton(this);
        onGoingRadio.setText("On Going");

        // Add "Done" radio button
        RadioButton doneRadio = new RadioButton(this);
        doneRadio.setText("Done");

        // Disable interaction for leaders and members not assigned to the task
        if (isLeader) {
            onGoingRadio.setEnabled(false);
            doneRadio.setEnabled(false);
        } else if (!loggedInEmail.equals(assignedEmail)) {
            // Members can't interact if it's not their assigned task
            onGoingRadio.setEnabled(false);
            doneRadio.setEnabled(false);
        }

        statusGroup.addView(onGoingRadio);
        statusGroup.addView(doneRadio);

        return statusGroup;
    }

    private void saveTaskData() {
        // Prepare deadline and project ID
        String deadline = deadlineEditText.getText().toString();
        String projectId = getIntent().getStringExtra("PROJECT_ID");

        for (int i = 1; i < taskTable.getChildCount(); i++) { // Start at 1 to skip the header row
            TableRow row = (TableRow) taskTable.getChildAt(i);

            // Retrieve task description
            EditText taskEditText = (EditText) row.getChildAt(0);
            String taskDesc = taskEditText.getText().toString();

            // Retrieve task status
            RadioGroup statusGroup = (RadioGroup) row.getChildAt(1);
            String status = getStatusFromRadioGroup(statusGroup);

            // Ensure task description is not empty
            if (taskDesc.isEmpty()) {
                Toast.makeText(this, "Task description cannot be empty!", Toast.LENGTH_SHORT).show();
                return;
            }

            // Create a unique document ID based on projectId and taskDesc
            String documentId = projectId + "_" + taskDesc.replace(" ", "_").toLowerCase();

            // Prepare task data
            Map<String, Object> taskData = new HashMap<>();
            taskData.put("taskDesc", taskDesc);
            taskData.put("status", status); // Can be null
            taskData.put("projectId", projectId);
            taskData.put("memberEmail", memberEmail);
            taskData.put("deadline", deadline);

            // Save or update task in Firestore
            db.collection("tasks").document(documentId)
                    .set(taskData) // .set() will overwrite if document exists, create if not
                    .addOnSuccessListener(aVoid ->
                            Toast.makeText(this, "Task saved/updated successfully!", Toast.LENGTH_SHORT).show())
                    .addOnFailureListener(e ->
                            Toast.makeText(this, "Failed to save/update task: " + e.getMessage(), Toast.LENGTH_SHORT).show());
        }
    }

    private String getStatusFromRadioGroup(RadioGroup group) {
        int checkedId = group.getCheckedRadioButtonId();
        if (checkedId != -1) {
            RadioButton selected = findViewById(checkedId);
            return selected.getText().toString();
        }
        return null; // No selection
    }

}
