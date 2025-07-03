package com.wastesmart.admin;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.wastesmart.R;
import com.wastesmart.models.User;

import java.util.List;

/**
 * Adapter for displaying user information in the ManageUsersActivity
 */
public class UserAdapter extends RecyclerView.Adapter<UserAdapter.UserViewHolder> {

    private List<User> usersList;
    private Context context;
    private UserActionListener listener;

    public interface UserActionListener {
        void onViewProfileClicked(User user, int position);
        void onBlockUserClicked(User user, int position);
    }

    public UserAdapter(List<User> usersList, Context context, UserActionListener listener) {
        this.usersList = usersList;
        this.context = context;
        this.listener = listener;
    }

    @NonNull
    @Override
    public UserViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_user, parent, false);
        return new UserViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull UserViewHolder holder, int position) {
        User user = usersList.get(position);
        
        // Set user data
        holder.tvUserName.setText(user.getName() != null ? user.getName() : "User");
        holder.tvUserEmail.setText(user.getEmail() != null ? user.getEmail() : "No email");
        holder.tvUserPhone.setText(user.getPhone() != null ? user.getPhone() : "No phone number");
        
        // Set status based on userType
        boolean isBlocked = "blocked".equals(user.getUserType());
        
        if (isBlocked) {
            holder.tvUserStatus.setText("BLOCKED");
            holder.tvUserStatus.setTextColor(ContextCompat.getColor(context, R.color.error));
            holder.btnBlockUser.setText("Unblock User");
            holder.btnBlockUser.setBackgroundTintList(ContextCompat.getColorStateList(context, R.color.primary));
        } else {
            holder.tvUserStatus.setText("ACTIVE");
            holder.tvUserStatus.setTextColor(ContextCompat.getColor(context, R.color.success));
            holder.btnBlockUser.setText("Block User");
            holder.btnBlockUser.setBackgroundTintList(ContextCompat.getColorStateList(context, R.color.error));
        }
        
        // Set click listeners
        holder.btnViewProfile.setOnClickListener(v -> {
            if (listener != null) {
                listener.onViewProfileClicked(user, position);
            }
        });
        
        holder.btnBlockUser.setOnClickListener(v -> {
            if (listener != null) {
                listener.onBlockUserClicked(user, position);
            }
        });
    }

    @Override
    public int getItemCount() {
        return usersList != null ? usersList.size() : 0;
    }

    static class UserViewHolder extends RecyclerView.ViewHolder {
        ImageView ivUserAvatar;
        TextView tvUserName, tvUserEmail, tvUserPhone, tvUserStatus;
        Button btnViewProfile, btnBlockUser;

        UserViewHolder(@NonNull View itemView) {
            super(itemView);
            ivUserAvatar = itemView.findViewById(R.id.ivUserAvatar);
            tvUserName = itemView.findViewById(R.id.tvUserName);
            tvUserEmail = itemView.findViewById(R.id.tvUserEmail);
            tvUserPhone = itemView.findViewById(R.id.tvUserPhone);
            tvUserStatus = itemView.findViewById(R.id.tvUserStatus);
            btnViewProfile = itemView.findViewById(R.id.btnViewProfile);
            btnBlockUser = itemView.findViewById(R.id.btnBlockUser);
        }
    }
}
