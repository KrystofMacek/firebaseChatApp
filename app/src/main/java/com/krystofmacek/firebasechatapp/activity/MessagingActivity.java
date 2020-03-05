package com.krystofmacek.firebasechatapp.activity;


import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.krystofmacek.firebasechatapp.R;
import com.krystofmacek.firebasechatapp.adapters.MessageAdapter;
import com.krystofmacek.firebasechatapp.model.Chat;
import com.krystofmacek.firebasechatapp.model.Message;
import com.krystofmacek.firebasechatapp.model.User;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class MessagingActivity extends AppCompatActivity {

    private ImageButton sendMsgBtn;
    private EditText inputMessageText;
    private RecyclerView messageRecycler;
    private TextView heading;

    private FirebaseFirestore firestore;
    private FirebaseUser signedUser;
    private String chatId;

    MessageAdapter adapter;
    String userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_messaging);

        sendMsgBtn = findViewById(R.id.messaging_btnSend);
        inputMessageText = findViewById(R.id.messaging_inputMessage);
        messageRecycler = findViewById(R.id.messaging_recycler);

        firestore = FirebaseFirestore.getInstance();
        signedUser = FirebaseAuth.getInstance().getCurrentUser();

        userId = getIntent().getStringExtra("userid");

        setupTopBar(userId);
        loadMessages(userId);

    }

    private void setupTopBar(String userId) {
        heading = findViewById(R.id.toolbar_heading);
        firestore.collection("Profiles")
                .document(userId)
                .get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                User user = documentSnapshot.toObject(User.class);
                if (user != null) {
                    heading.setText(user.getDisplayName());
                }

            }
        });

        Toolbar toolbar = findViewById(R.id.toolbar_top);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

    }

    private void loadMessages(final String userId){
        final List<Message> messages = new ArrayList<>();

        // nejdrive najdeme chat
        firestore.collection("Chats")
                .whereArrayContains("members", userId)
                .get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        boolean newChat = true;
                        for(DocumentSnapshot snap : queryDocumentSnapshots.getDocuments()) {
                            Chat chat = snap.toObject(Chat.class);
                            if(chat.getOtherMember(userId).equals(signedUser.getUid())) {
                                chatId = chat.getUid();
                                newChat = false;
                                if(chatId != null) {
                                    // nacteme zpravy z chatu
                                    firestore.collection("Chats")
                                            .document(chatId)
                                            .collection("Messages")
                                            .orderBy("timestamp")
                                            .limit(20)
                                            .get()
                                            .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                                                @Override
                                                public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                                                    if(queryDocumentSnapshots != null && queryDocumentSnapshots.getDocuments().size() > 0) {
                                                        for(DocumentSnapshot snap : queryDocumentSnapshots.getDocuments()) {
                                                            messages.add(snap.toObject(Message.class));
                                                        }
                                                        adapter = new MessageAdapter(getApplicationContext(), messages);
                                                        LinearLayoutManager layoutManager = new LinearLayoutManager(getApplicationContext());
                                                        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
                                                        messageRecycler.setLayoutManager(layoutManager);
                                                        messageRecycler.setAdapter(adapter);

                                                    }
                                                }
                                            });
                                    sendMessageSetup(chatId);
                                }

                            }
                        }
                        if(newChat) {
                            DocumentReference ref = firestore.collection("Chats").document();
                            String newId = ref.getId();
                            List<String> members = new ArrayList<>();
                            members.add(signedUser.getUid());
                            members.add(userId);
                            Chat chat = new Chat(
                                    newId,
                                    members
                            );
                            firestore.collection("Chats").document(newId)
                                    .set(chat);
                            sendMessageSetup(chatId);
                        }
                    }
                });
    }

    private void sendMessageSetup(String chatId){
        final DocumentReference newMessageDoc = firestore.collection("Chats")
                .document(chatId)
                .collection("Messages")
                .document();
        sendMsgBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Message newMessage = new Message(signedUser.getUid(), inputMessageText.getText().toString(), Timestamp.now());
                newMessageDoc.set(newMessage);
                loadMessages(userId);
                inputMessageText.setText("");
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        finish();
        return true;
    }
}
