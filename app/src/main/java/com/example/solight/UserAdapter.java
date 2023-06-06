package com.example.solight;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class UserAdapter extends RecyclerView.Adapter<UserAdapter.ViewHolder> {

    private ArrayList<User> mUserList;
    private OnUserClickListener mOnUserClickListener;
    private int selectedPos = RecyclerView.NO_POSITION;

    public UserAdapter(ArrayList<User> userList, OnUserClickListener onUserClickListener) {
        this.mUserList = userList;
        this.mOnUserClickListener = onUserClickListener;
    }

    @NonNull
    @Override
    public UserAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.user_item, parent, false);
        return new ViewHolder(view, mOnUserClickListener);
    }

    @Override
    public void onBindViewHolder(@NonNull UserAdapter.ViewHolder holder, int position) {
        holder.userId.setText(mUserList.get(position).getId());
        holder.authorityLevel.setText(mUserList.get(position).getAuthorityLevel());
        holder.itemView.setSelected(selectedPos == position);
        if (selectedPos == position) {
            holder.userId.setTextColor(ContextCompat.getColor(holder.itemView.getContext(), R.color.purple2));
        } else {
            holder.userId.setTextColor(Color.BLACK);
        }
    }

    @Override
    public int getItemCount() {
        return mUserList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        TextView userId, authorityLevel;
        OnUserClickListener onUserClickListener;

        public ViewHolder(@NonNull View itemView, OnUserClickListener onUserClickListener) {
            super(itemView);
            this.userId = itemView.findViewById(R.id.user_id);
            this.authorityLevel = itemView.findViewById(R.id.user_authority);
            this.onUserClickListener = onUserClickListener;
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            if (getAdapterPosition() == RecyclerView.NO_POSITION) return;
            notifyItemChanged(selectedPos);
            selectedPos = getAdapterPosition();
            notifyItemChanged(selectedPos);
            onUserClickListener.onUserClick(getAdapterPosition());
        }
    }

    public interface OnUserClickListener {
        void onUserClick(int position);
    }
}
