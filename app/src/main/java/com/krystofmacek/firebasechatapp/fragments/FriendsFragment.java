package com.krystofmacek.firebasechatapp.fragments;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.krystofmacek.firebasechatapp.R;
import com.krystofmacek.firebasechatapp.adapters.FriendAdapter;
import com.krystofmacek.firebasechatapp.adapters.ProfileAdapter;
import com.krystofmacek.firebasechatapp.model.User;
import com.krystofmacek.firebasechatapp.services.FirestoreService;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;


public class FriendsFragment extends Fragment {

    private RecyclerView recyclerFriends;
    private FirestoreService firestoreService;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_friends, container, false);
        recyclerFriends = view.findViewById(R.id.fFriends_recycler);
        // inicializace firestoreService objektu
        firestoreService = new FirestoreService();

        loadFriendsList();

        return view;
    }
    // metoda pro vytvoreni seznamu pratel
    private void loadFriendsList() {

        final List<User> friendsList = new ArrayList<>();
        final List<String> ids = new ArrayList<>();

        // nacteni pratel
        firestoreService.getSignedUserDocumentRef()
                .addSnapshotListener(new EventListener<DocumentSnapshot>() {
                    @Override
                    public void onEvent(@Nullable DocumentSnapshot documentSnapshot, @Nullable FirebaseFirestoreException e) {
                        ids.clear();
                        if(documentSnapshot !=null) {
                            User user = documentSnapshot.toObject(User.class);
                            ids.addAll(user.getFriends());
                        }

                        if(ids.size() > 0) {
                            // z kolekce uzivatelu nacteme odpovidajici pratele
                            // seradime podle abecendy
                            firestoreService
                                    .queryByWhereIn("Profiles","uid", ids)
                                    .orderBy("displayName", Query.Direction.ASCENDING)
                                    .addSnapshotListener(new EventListener<QuerySnapshot>() {
                                        @Override
                                        public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {
                                            friendsList.clear();
                                            friendsList.addAll(queryDocumentSnapshots.toObjects(User.class));

                                            // Vytvoreni UI seznamu
                                            FriendAdapter adapter = new FriendAdapter(getContext(), friendsList);
                                            LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
                                            layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
                                            recyclerFriends.setLayoutManager(layoutManager);
                                            recyclerFriends.setAdapter(adapter);
                                        }
                                    });
                }
            }
        });

    }

}
