package com.example.myapplication;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.HashMap;

public class ChatActivity extends AppCompatActivity {

    // Firebase auth
    FirebaseAuth firebaseAuth;

    // Firebase DataBase
    FirebaseDatabase firebaseDatabase;
    DatabaseReference usersDbRef;

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
                    String image = ds.child("image").getValue().toString();

                    // Set data
                    nameTv.setText(name);
                    try{
                        // image received, set it to the imageView in toolbar
                        Picasso.get().load(image).placeholder(R.drawable.ic_default_img_white).into(profileIv);
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

        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("sender", myUID);
        hashMap.put("receivder", hisUID);
        hashMap.put("message", message);
        databaseReference.child("Chats").push().setValue(hashMap);

        // Reset EditText after sending message
        messageEt.setText("");


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

    @Override
    protected void onStart() {
        checkUserStatus();

        super.onStart();
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