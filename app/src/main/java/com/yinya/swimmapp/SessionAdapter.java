package com.yinya.swimmapp;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.yinya.swimmapp.models.SessionIndex;

import java.util.List;

public class SessionAdapter extends RecyclerView.Adapter<SessionAdapter.SessionViewHolder> {

    private final List<SessionIndex> sessions;
    private final OnItemClickListener listener;

    public interface OnItemClickListener {
        void onItemClick(SessionIndex session);
    }

    public SessionAdapter(List<SessionIndex> sessions, OnItemClickListener listener) {
        this.sessions = sessions;
        this.listener = listener;
    }

    @NonNull
    @Override
    public SessionViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_session, parent, false);
        return new SessionViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SessionViewHolder holder, int position) {
        SessionIndex session = sessions.get(position);
        holder.tvName.setText(session.getName());
        holder.itemView.setOnClickListener(v -> listener.onItemClick(session));
    }

    @Override
    public int getItemCount() {
        return sessions.size();
    }

    public static class SessionViewHolder extends RecyclerView.ViewHolder {
        TextView tvName;

        public SessionViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.sessionName);
        }
    }
}