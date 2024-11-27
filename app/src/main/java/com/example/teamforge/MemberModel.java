package com.example.teamforge;

public class MemberModel {
    private String email;

    public MemberModel() {
        // Default constructor required for calls to DataSnapshot.getValue(MemberModel.class)
    }

    public MemberModel(String email) {
        this.email = email;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
}
