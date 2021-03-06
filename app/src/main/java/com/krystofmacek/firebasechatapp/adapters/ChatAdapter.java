package com.krystofmacek.firebasechatapp.adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnSuccessListener;

import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.krystofmacek.firebasechatapp.R;
import com.krystofmacek.firebasechatapp.activity.MessagingActivity;
import com.krystofmacek.firebasechatapp.model.Chat;
import com.krystofmacek.firebasechatapp.model.Message;
import com.krystofmacek.firebasechatapp.model.User;
import com.krystofmacek.firebasechatapp.services.FirestoreService;

import java.util.List;

// Vytvoreni polozky seznamu chatů
public class ChatAdapter extends RecyclerView.Adapter<ChatAdapter.ViewHolder>{

    private Context context;
    private List<Chat> chats;
    private DocumentReference signedUser;
    private FirestoreService firestoreService;


    public ChatAdapter(Context context, List<Chat> chats) {
        this.context = context;
        this.chats = chats;
        firestoreService = new FirestoreService();
        signedUser = firestoreService.getSignedUserDocumentRef();
    }

    @NonNull
    @Override
    public ChatAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Definice ViewHolder layoutu - jeho vytvoreni
        View view = LayoutInflater.from(context).inflate(R.layout.item_chat, parent, false);
        return new ChatAdapter.ViewHolder(view);
    }

    // Inicializace UI elementu
    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView lastMessage;
        TextView username;
        ImageButton item_chat_btnStartChat;

        public ViewHolder(View itemView) {
            super(itemView);
            lastMessage = itemView.findViewById(R.id.item_chat_lastMessage);
            username = itemView.findViewById(R.id.item_chat_username);
            item_chat_btnStartChat = itemView.findViewById(R.id.item_chat_btnStartChat);
        }
    }


    // Napneni UI elementu
    @Override
    public void onBindViewHolder(@NonNull final ChatAdapter.ViewHolder holder, int position) {
        final Chat chat = chats.get(position);
        String userId = chat.getOtherMember(signedUser.getId());
        // nacteni uzivatelskeho jmena
        firestoreService
                .getDocumentReference("Profiles", userId)
                .get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                User user = documentSnapshot.toObject(User.class);
                if(user != null) {
                    holder.username.setText(user.getDisplayName());
                }
            }
        });

        getLastMessage(holder.lastMessage, chat);
        // nastaveni onClick pro spusteni aktivity chat
        holder.itemView.findViewById(R.id.item_chat_btnStartChat).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, MessagingActivity.class);
                intent.putExtra("userid", chat.getOtherMember(signedUser.getId()));
                context.startActivity(intent);
            }
        });

    }

    // nacteni posledni zpravy z chatu
    private void getLastMessage(final TextView lastMessageView, final Chat chat) {
        firestoreService.queryForLastMessage(chat.getUid())
                .get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                if(queryDocumentSnapshots.size() > 0){
                    DocumentSnapshot doc = queryDocumentSnapshots.getDocuments().get(0);
                    Message lastMessage = doc.toObject(Message.class);
                    lastMessageView.setText(lastMessage.getMessageText());
                    if(lastMessage.getAuthorId() != null && !lastMessage.getAuthorId().equals(signedUser.getId())) {
                        lastMessageView
                                .setTextColor(ContextCompat.getColor(context, R.color.colorPrimaryDark));
                    }
                } else {
                    lastMessageView.setText("No Messages");
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return chats.size();
    }


}
