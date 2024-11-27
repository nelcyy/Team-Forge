package com.example.teamforge;

import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class ProjectAdapter extends RecyclerView.Adapter<ProjectAdapter.ProjectViewHolder> {

    private List<ProjectModel> projectList;

    public ProjectAdapter(List<ProjectModel> projectList) {
        this.projectList = projectList;
    }

    @NonNull
    @Override
    public ProjectViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.project_row, parent, false);
        return new ProjectViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ProjectViewHolder holder, int position) {
        ProjectModel project = projectList.get(position);

        // Set text for project and subject
        holder.projectNameTextView.setText(project.getProjectName() + " (" + project.getUserRole() + ")");
        holder.subjectNameTextView.setText(project.getSubjectName());

        // Set the click listener for the item
        holder.itemView.setOnClickListener(v -> {
            // Get the projectId from the current project
            String projectId = project.getProjectId();

            // Create an Intent to navigate to the Member activity
            Intent intent = new Intent(v.getContext(), Member.class);
            intent.putExtra("PROJECT_ID", projectId); // Pass the projectId

            // Start the Member activity
            v.getContext().startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return projectList.size();
    }

    public static class ProjectViewHolder extends RecyclerView.ViewHolder {

        TextView projectNameTextView, subjectNameTextView;

        public ProjectViewHolder(@NonNull View itemView) {
            super(itemView);
            projectNameTextView = itemView.findViewById(R.id.ProjectName);
            subjectNameTextView = itemView.findViewById(R.id.SubjectName);
        }
    }
}