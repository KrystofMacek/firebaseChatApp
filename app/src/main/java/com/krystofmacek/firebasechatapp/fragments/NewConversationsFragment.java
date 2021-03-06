package com.krystofmacek.firebasechatapp.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.krystofmacek.firebasechatapp.R;
import com.krystofmacek.firebasechatapp.adapters.ChatAdapter;
import com.krystofmacek.firebasechatapp.model.Chat;
import com.krystofmacek.firebasechatapp.model.User;
import com.krystofmacek.firebasechatapp.services.FirestoreService;

import java.util.ArrayList;
import java.util.List;


public class NewConversationsFragment extends Fragment {

    private FirestoreService firestoreService;
    private DocumentReference signedUser;
    private RecyclerView newChatRecycler;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_new_conversations, container, false);

        // inicializce ui a fireb obj
        firestoreService = new FirestoreService();
        signedUser = firestoreService.getSignedUserDocumentRef();


        newChatRecycler = view.findViewById(R.id.fChats_New_recycler);

        loadNewChats();

        return view;
    }

    // Metoda pro vytvoreni seznamu novych chatů
    private void loadNewChats() {
        final List<Chat> newChats = new ArrayList<>();

        // nacteme seznam aktivnich chatu
        firestoreService.getSignedUserDocumentRef()
                .get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                final List<String> activeChats =
                        documentSnapshot.toObject(User.class).getActiveChats();

                // nacteme vsechny chaty ve kterych je uzivatel clenem
                firestoreService
                        .queryByArrayContains("Chats", "members", signedUser.getId())
                        .orderBy("lastMessageTime", Query.Direction.DESCENDING)
                        .addSnapshotListener(new EventListener<QuerySnapshot>() {
                            @Override
                            public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {
                                newChats.clear();
                                if(queryDocumentSnapshots != null) {
                                    List<Chat> allChats = queryDocumentSnapshots.toObjects(Chat.class);
                                    // zjistime ktere z nactenych jsou nove - (nejsou mezi aktivními chaty prihlaseneho uzivatele)
                                    for(Chat c : allChats) {
                                        boolean isNewChat = true;
                                        for(String activeChatId : activeChats) {
                                            if(c.getUid().equals(activeChatId)) {
                                                isNewChat = false;
                                            }
                                        }
                                        // nove pridame do seznamu
                                        if(isNewChat && c.getLastMessageTime() != null) {
                                            newChats.add(c);
                                        }
                                    }
                                }
                                // Vytvoreni UI seznamu
                                ChatAdapter adapter = new ChatAdapter(getContext(), newChats);
                                LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
                                layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
                                newChatRecycler.setLayoutManager(layoutManager);
                                newChatRecycler.setAdapter(adapter);
                            }
                        });
            }
        });


    }

    @Override
    public void onResume() {
        super.onResume();
        loadNewChats();
    }
}
