package com.example.myapplication;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.text.format.DateFormat;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.myapplication.adapters.AdapterChat;
import com.example.myapplication.models.ModelChat;
import com.example.myapplication.models.ModelUser;
import com.example.myapplication.notifications.APIService;
import com.example.myapplication.notifications.Client;
import com.example.myapplication.notifications.Data;
import com.example.myapplication.notifications.Response;
import com.example.myapplication.notifications.Sender;
import com.example.myapplication.notifications.Token;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;

public class ChatActivity extends AppCompatActivity {

    // Firebase auth
    FirebaseAuth firebaseAuth;

    // Firebase DataBase
    FirebaseDatabase firebaseDatabase;
    DatabaseReference usersDbRef;

    // For checking if user has seen the message or not
    ValueEventListener seenListener;
    DatabaseReference databaseReference;

    List<ModelChat> chatList;
    AdapterChat adapterChat;

    // Views from xml
    Toolbar toolbar;
    RecyclerView recyclerView;
    ImageView profileIv;
    TextView nameTv, userStatusTv;
    EditText messageEt;
    ImageButton sendBtn;

    // Uid from intent
    String hisUID;
    String myUID;
    String hisImage;

    APIService apiService;
    boolean notify = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        // Init Toolbar
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setTitle("");

        // Init Views
        recyclerView = findViewById(R.id.chat_recyclerView);
        profileIv = findViewById(R.id.profileIv);
        nameTv = findViewById(R.id.nameTv);
        userStatusTv = findViewById(R.id.userStatusTv);
        messageEt = findViewById(R.id.messageEt);
        sendBtn = findViewById(R.id.sendBtn);

        // Layout (LinearLayout) for RecyclerView
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setStackFromEnd(true);
        // recyclerView properties
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(linearLayoutManager);

        // Create api Service
        apiService = Client.getRetrofit("https://fcm.googleapis.com/").create(APIService.class);

        // On clicking a user from userList we have passed that user`s UID using intent
        // So we get that uit here to get the profile picture, name and start
        // the chat with that user

        Intent intent = getIntent();
        hisUID = intent.getStringExtra("hisUid");

        // Init FirebaseAuth instance
        firebaseAuth = FirebaseAuth.getInstance();

        // Init FirebaseDatabase instance and reference
        firebaseDatabase = FirebaseDatabase.getInstance();
        usersDbRef = firebaseDatabase.getReference("Users");

        // Search user to get his info
        Query userQuery = usersDbRef.orderByChild("uid").equalTo(hisUID);
        // Get user picture and name
        userQuery.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                // Check until required info is received
                for(DataSnapshot ds: snapshot.getChildren()){
                    // Get data
                    String name = ds.child("name").getValue().toString();
                    hisImage = ds.child("image").getValue().toString();
                    String typingStatus = ds.child("typingTo").getValue().toString();

                    // Check typing status
                    if(typingStatus.equals(myUID)){
                        userStatusTv.setText("typing...");
                    } else {
                        // Get value of onlineStatus
                        String onlineStatus = ds.child("onlineStatus").getValue().toString();
                        if(onlineStatus.equals("online")){
                            userStatusTv.setText(onlineStatus);
                        } else {
                            // Convert timestamp to proper time date

                            Calendar calendar = Calendar.getInstance(Locale.ENGLISH);
                            calendar.setTimeInMillis(Long.parseLong(onlineStatus));
                            Date date = calendar.getTime();

                            long timeDiffMillis = System.currentTimeMillis() - date.getTime();

                            if(timeDiffMillis >= 86400000){
                                // Convert timestamp to dd/mm/yyyy hh:mm
                                String time = DateFormat.format("dd/MM/yyyy HH:mm", calendar).toString();
                                userStatusTv.setText("Last seen at: " + time);
                            } else {
                                // Convert timestamp to hh:mm
                                String time = DateFormat.format("HH:mm", calendar).toString();
                                userStatusTv.setText("Last seen at: " + time);
                            }
                        }
                    }

                    // Set data
                    nameTv.setText(name);
                    try{
                        // image received, set it to the imageView in toolbar
                        Picasso.get().load(hisImage).placeholder(R.drawable.ic_default_img_white).into(profileIv);
                    }catch (Exception e){
                        // there is an exception when getting the picture, set default pic
                        Picasso.get().load(R.drawable.ic_default_img_white).into(profileIv);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        // Click button to send msg
        sendBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                notify = true;
                // Get text from EditText
                String message = messageEt.getText().toString().trim();
                // Check if text is empty or not
                if(TextUtils.isEmpty(message)){
                    // text empty
                    Toast.makeText(ChatActivity.this, "Can`t send an empty message.", Toast.LENGTH_SHORT).show();
                } else {
                    // text not empty
                    sendMessage(message);
                }
                // Reset EditText after sending message
                messageEt.setText("");
            }
        });

        // Check edit text change listner
        messageEt.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if(charSequence.toString().trim().length() == 0){
                    checkTypingStatus("noOne");
                } else {
                    checkTypingStatus(hisUID); // UID of receiver
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        readMessages();

        seenMessage();
    }

    private void seenMessage() {
        databaseReference = FirebaseDatabase.getInstance().getReference("Chats");
        seenListener = databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for(DataSnapshot ds: snapshot.getChildren()){
                    ModelChat chat = ds.getValue(ModelChat.class);
                    if(chat.getReceiver().equals(myUID) && chat.getSender().equals(hisUID)){
                        HashMap<String, Object> hasSeenHashMap = new HashMap<>();
                        hasSeenHashMap.put("isSeen", true);
                        ds.getRef().updateChildren(hasSeenHashMap);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void readMessages() {
        chatList = new ArrayList<>();
        DatabaseReference databaseReference = FirebaseDatabase.getInstance()
                .getReference("Chats");
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                chatList.clear();
                for(DataSnapshot ds: snapshot.getChildren()){
                    ModelChat chat = ds.getValue(ModelChat.class);
                    if(chat.getReceiver().equals(myUID) && chat.getSender().equals(hisUID) ||
                            chat.getReceiver().equals(hisUID) && chat.getSender().equals(myUID)){
                        chatList.add(chat);
                    }

                    // Adapter
                    adapterChat = new AdapterChat(ChatActivity.this, chatList, hisImage);
                    adapterChat.notifyDataSetChanged();
                    // Set adapter to RecyclerView
                    recyclerView.setAdapter(adapterChat);
                    // Scroll RecyclerView to the last position after adapter is updated
                    recyclerView.scrollToPosition(chatList.size() - 1);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void sendMessage(String message) {
        // Chats node will be created and it will contain all chats
        // Whenever a user sends a message iw til create a new child in the "Chats" node and that
        // child will contain the folowing key values:
        // sender: UID of sender
        // receiver: UID of receiver
        // message: the actual message

        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference();

        String timestamp = String.valueOf(System.currentTimeMillis());

        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("sender", myUID);
        hashMap.put("receiver", hisUID);
        hashMap.put("message", message);
        hashMap.put("timestamp", timestamp);
        hashMap.put("isSeen", false);
        databaseReference.child("Chats").push().setValue(hashMap);

        DatabaseReference database = FirebaseDatabase.getInstance().getReference("Users").child(myUID);
        database.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                ModelUser user = snapshot.getValue(ModelUser.class);

                if(notify){
                    sendNotification(hisUID, user.getName(), message);
                }
                notify = false;
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }

    private void sendNotification(String hisUID, String name, String message) {
        DatabaseReference allTokens = FirebaseDatabase.getInstance().getReference("Tokens");
        Query query = allTokens.orderByKey().equalTo(hisUID);
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for(DataSnapshot dataSnapshot : snapshot.getChildren()){
                    Token token = dataSnapshot.getValue(Token.class);
                    Data data = new Data(myUID, name + " : " + message, "New Message", hisUID, R.drawable.ic_default_img);

                    Sender sender = new Sender(data, token.getToken());
                    apiService.sendNotification(sender)
                            .enqueue(new Callback<Response>() {
                                @Override
                                public void onResponse(Call<Response> call, retrofit2.Response<Response> response) {
                                    Toast.makeText(ChatActivity.this, "" + response.message(), Toast.LENGTH_SHORT).show();
                                }

                                @Override
                                public void onFailure(Call<Response> call, Throwable t) {

                                }
                            });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }


    private  void checkUserStatus(){
        // Get current user
        FirebaseUser user = firebaseAuth.getCurrentUser();

        if(user != null){
            // User is signed in => stay here
            // Set email of logged in user
            //textViewProfil.setText(user.getEmail());
            myUID = user.getUid(); // currently signed in user`s uid
        }else {
            // User is not signed in => go to main activity
            startActivity(new Intent(this, MainActivity.class));
            finish();
        }
    }

    private void checkOnlineStatus(String status){
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Users").child(myUID);
        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("onlineStatus", status);

        // Update value of onlineStatus of the current user
        databaseReference.updateChildren(hashMap);
    }

    private void checkTypingStatus(String typing){
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Users").child(myUID);
        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("typingTo", typing);

        // Update value of onlineStatus of the current user
        databaseReference.updateChildren(hashMap);
    }

    @Override
    protected void onStart() {
        checkUserStatus();
        // set Online
        checkOnlineStatus("online");
        super.onStart();
    }

    @Override
    protected void onPause() {
        super.onPause();
        // Get timestamp
        String timestamp = String.valueOf(System.currentTimeMillis());

        // set Offline with last seen time stamp
        checkOnlineStatus(timestamp);
        checkTypingStatus("noOne");
        databaseReference.removeEventListener(seenListener);
    }

    @Override
    protected void onResume() {
        // set Online
        checkOnlineStatus("online");
        super.onResume();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        // Hide searchView, as we dont need it here
        menu.findItem(R.id.action_search).setVisible(false);

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        if(id == R.id.action_logut){
            firebaseAuth.signOut();
            checkUserStatus();
        }

        return super.onOptionsItemSelected(item);
    }
}