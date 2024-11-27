package com.example.teamforge;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class MemberAdapter extends RecyclerView.Adapter<MemberAdapter.MemberViewHolder> {

    private List<MemberModel> memberList;
    private OnMemberClickListener onMemberClickListener;

    // Constructor to initialize the list of members
    public MemberAdapter(List<MemberModel> memberList) {
        this.memberList = memberList;
    }

    // Setter method for click listener
    public void setOnMemberClickListener(OnMemberClickListener listener) {
        this.onMemberClickListener = listener;
    }

    @NonNull
    @Override
    public MemberViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Use LayoutInflater to inflate the item view
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.member_row, parent, false);
        return new MemberViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MemberViewHolder holder, int position) {
        // Get the current member at the given position
        MemberModel member = memberList.get(position);

        // Set the email of the member in the TextView
        holder.memberEmail.setText(member.getEmail());

        // Handle item click by triggering the click listener
        holder.itemView.setOnClickListener(v -> {
            // Check if the listener is not null, then call the onMemberClick method
            if (onMemberClickListener != null) {
                onMemberClickListener.onMemberClick(member.getEmail());
            }
        });
    }

    @Override
    public int getItemCount() {
        // Return the size of the member list
        return memberList != null ? memberList.size() : 0; // Avoid null pointer if memberList is null
    }

    // ViewHolder class to hold references to the views in the layout
    public static class MemberViewHolder extends RecyclerView.ViewHolder {
        // Declare the TextView for member's email
        TextView memberEmail;

        public MemberViewHolder(@NonNull View itemView) {
            super(itemView);
            // Initialize the TextView by finding the view from the layout
            memberEmail = itemView.findViewById(R.id.MembersEmail); // Make sure this ID matches the layout
        }
    }

    // Interface to handle member click events
    public interface OnMemberClickListener {
        void onMemberClick(String email);
    }
}
