package com.example.teamforge;

public class ProjectModel {
    private String projectName;
    private String subjectName;
    private String userRole;
    private String projectId; // Add projectId

    public ProjectModel(String projectName, String subjectName, String userRole, String projectId) {
        this.projectName = projectName;
        this.subjectName = subjectName;
        this.userRole = userRole;
        this.projectId = projectId; // Initialize projectId
    }

    public String getProjectName() {
        return projectName;
    }

    public String getSubjectName() {
        return subjectName;
    }

    public String getUserRole() {
        return userRole;
    }

    public String getProjectId() {
        return projectId; // Getter for projectId
    }
}
