package com.krystofmacek.firebasechatapp.adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import com.krystofmacek.firebasechatapp.R;
import com.krystofmacek.firebasechatapp.activity.MessagingActivity;
import com.krystofmacek.firebasechatapp.model.User;
import com.krystofmacek.firebasechatapp.services.FirestoreService;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

// Vytvoreni polozky seznamu nalezenych uzivatel
public class ProfileAdapter extends RecyclerView.Adapter<ProfileAdapter.ViewHolder>{

    private Context context;
    private List<User> profiles;
    private FirestoreService firestoreService;

    public ProfileAdapter(Context context, List<User> profiles) {
        this.context = context;
        this.profiles = profiles;
        firestoreService = new FirestoreService();
    }

    @NonNull
    @Override
    public ProfileAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_profile, parent, false);
        return new ProfileAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ProfileAdapter.ViewHolder holder, int position) {
        // Naplneni ui
        final User profile = profiles.get(position);
        String username = profile.getDisplayName();
        holder.username.setText(username);
        createTagsString(holder.tags, profile);

        holder.itemView.findViewById(R.id.item_profile_btnStartChat).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, MessagingActivity.class);
                intent.putExtra("userid", profile.getUid());
                context.startActivity(intent);
            }
        });

    }
    public class ViewHolder extends RecyclerView.ViewHolder {

        TextView tags;
        TextView username;
        ImageButton item_chat_btnStartChat;

        public ViewHolder(View itemView) {
            super(itemView);

            tags = itemView.findViewById(R.id.item_profile_tags);
            username = itemView.findViewById(R.id.item_profile_username);
            item_chat_btnStartChat = itemView.findViewById(R.id.item_profile_btnStartChat);
        }
    }

    private void createTagsString(TextView output, User profile) {
        output.setText("");
        StringBuilder tagList = new StringBuilder();
        for (String tag : profile.getTags()) {
            tagList.append("#").append(tag).append(" ");
        }
        output.setText(tagList);
    }

    @Override
    public int getItemCount() {
        return profiles.size();
    }


}
