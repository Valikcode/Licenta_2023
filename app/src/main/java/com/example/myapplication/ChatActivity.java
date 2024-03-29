package com.example.myapplication;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDialog;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.example.myapplication.adapters.AdapterChat;
import com.example.myapplication.models.ModelChat;
import com.example.myapplication.models.ModelUser;
import com.example.myapplication.notifications.Data;
import com.example.myapplication.notifications.Sender;
import com.example.myapplication.notifications.Token;
import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.gson.Gson;
import com.squareup.picasso.Picasso;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;


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
    ImageButton meetupBtn;

    // Uid from intent
    String hisUID;
    String myUID;
    String hisImage;

    // Volley request queue for notification
    private RequestQueue requestQueue;

    private boolean notify = false;

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
        meetupBtn = findViewById(R.id.meetupBtn);

        // Request queue
        requestQueue = Volley.newRequestQueue(getApplicationContext());

        // Layout (LinearLayout) for RecyclerView
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setStackFromEnd(true);
        // recyclerView properties
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(linearLayoutManager);

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
                String token = String.valueOf(FirebaseMessaging.getInstance().getToken());
            }
        });

        meetupBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Create a dialog
                AppCompatDialog dialog = new AppCompatDialog(ChatActivity.this,R.style.MyDialogStyle);
                dialog.setContentView(R.layout.dialog_meetup);

                // Get references to dialog views
                Spinner interestSpinner = dialog.findViewById(R.id.interestSpinner);
                DatePicker datePicker = dialog.findViewById(R.id.datePicker);
                datePicker.setCalendarViewShown(true);
                Button sendButton = dialog.findViewById(R.id.sendButton);

                // Create an ArrayAdapter with the values
                ArrayAdapter<String> adapter = new ArrayAdapter<String>(getApplicationContext(), android.R.layout.simple_spinner_item);
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

                // Get the interests of the current user
                DatabaseReference currentUserRef = FirebaseDatabase.getInstance().getReference("Users").child(myUID);
                currentUserRef.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()) {
                            ModelUser currentUser = snapshot.getValue(ModelUser.class);
                            List<String> currentUserInterests = currentUser.getInterests();

                            // Get the interests of the other user
                            DatabaseReference otherUserRef = FirebaseDatabase.getInstance().getReference("Users").child(hisUID);
                            otherUserRef.addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot snapshot) {
                                    if (snapshot.exists()) {
                                        ModelUser otherUser = snapshot.getValue(ModelUser.class);
                                        List<String> otherUserInterests = otherUser.getInterests();

                                        // Find the common interests between the two users
                                        List<String> commonInterests = new ArrayList<>();
                                        for (String interest : currentUserInterests) {
                                            if (otherUserInterests.contains(interest)) {
                                                commonInterests.add(interest);
                                            }
                                        }

                                        // Add the common interests to the ArrayAdapter
                                        adapter.addAll(commonInterests);
                                    }
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError error) {
                                    // Handle error
                                }
                            });
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        // Handle error
                    }
                });

                // Set the ArrayAdapter as the adapter for the Spinner
                interestSpinner.setAdapter(adapter);

                // Set up the dialog
                dialog.show();

                // Set an OnClickListener for the MeetUpButton
                sendButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        // Get the selected interest and date
                        String selectedInterest = String.valueOf(interestSpinner.getSelectedItem());
                        int day = datePicker.getDayOfMonth();
                        int month = datePicker.getMonth() + 1; // Month is zero-based
                        int year = datePicker.getYear();
                        String timestamp = String.valueOf(System.currentTimeMillis());

                        List<LatLng> potentialLocations = new ArrayList<>();
                        final LatLng[] senderLocation = new LatLng[1];
                        final LatLng[] receiverLocation = new LatLng[1];

                        getUserLocation(myUID, new UserCallback() {
                            @Override
                            public void onUserLoaded(LatLng userLatLng) {
                                senderLocation[0]=userLatLng;

                            }
                        });

                        getUserLocation(hisUID, new UserCallback() {
                            @Override
                            public void onUserLoaded(LatLng userLatLng) {
                                receiverLocation[0]=userLatLng;
                            }
                        });

                        getLocations(selectedInterest, new LocationCallback() {
                            @Override
                            public void onLocationsLoaded(List<LatLng> locations) {
                                if(locations != null){
                                    potentialLocations.clear();
                                    potentialLocations.addAll(locations);

                                    LatLng recommendedLocation = null;
                                    double minDistanceSender = Double.MAX_VALUE;
                                    double minDistanceReceiver = Double.MAX_VALUE;

                                    for(LatLng location : potentialLocations) {
                                        double distanceToSender = calculateDistance(senderLocation[0], location);
                                        double distanceToReceiver = calculateDistance(receiverLocation[0],location);

                                        if (distanceToSender < minDistanceSender && distanceToReceiver < minDistanceReceiver) {
                                            minDistanceSender = distanceToSender;
                                            minDistanceReceiver = distanceToReceiver;
                                            recommendedLocation = new LatLng(location.latitude, location.longitude);
                                        }
                                    }
                                    final String[] denumireLocatieRecomandata = new String[1];

                                    getLocationNameFromFirebase(recommendedLocation, selectedInterest, new LocationNameCallback() {
                                        @Override
                                        public void onLocationNameReceived(String locationName) {
                                            denumireLocatieRecomandata[0] = locationName;
                                            denumireLocatieRecomandata.toString();

                                            ModelChat meetup = new ModelChat("Meetup",hisUID,myUID,timestamp,false,denumireLocatieRecomandata[0],selectedInterest,"pending",day + "/" + month + "/" + year);
                                            //ModelMeetup meetup = new ModelMeetup("Test",hisUID, myUID, timestamp, false, selectedInterest, "pending", day + "/" + month + "/" + year);
                                            Toast.makeText(ChatActivity.this, "" + meetup.toString(), Toast.LENGTH_SHORT).show();

                                            // Dismiss the dialog
                                            dialog.dismiss();

                                            sendMeetupRequest(meetup);
                                        }
                                    });
                                }
                            }
                        });
                    }
                });
            }
        });


        // Check edit text change listener
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

    private LatLng getUserLocation(String UID, UserCallback callback){
        DatabaseReference userLocation = FirebaseDatabase.getInstance().getReference("Users");
        userLocation.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for(DataSnapshot userSnapshot : snapshot.getChildren()){
                    if(userSnapshot.child("uid").getValue(String.class).equals(UID)){
                        LatLng locatie = new LatLng(userSnapshot.child("latitude").getValue(Double.class),userSnapshot.child("longitude").getValue(Double.class));
                        callback.onUserLoaded(locatie);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
        return null;
    }

    private List<LatLng> getLocations(String interest, LocationCallback callback){
        DatabaseReference locationsReference = FirebaseDatabase.getInstance().getReference("Locations").child(interest);
        locationsReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                List<LatLng> listaLocatii = new ArrayList<>();
                for(DataSnapshot locationSnapshot : snapshot.getChildren()){
                    LatLng locatie = new LatLng(locationSnapshot.child("Lat").getValue(Double.class), locationSnapshot.child("Long").getValue(Double.class));
                    listaLocatii.add(locatie);
                }
                callback.onLocationsLoaded(listaLocatii);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Handle error
                callback.onLocationsLoaded(null);
            }

        });
        return null;
    }

    public interface LocationCallback {
        void onLocationsLoaded(List<LatLng> locations);
    }

    public interface UserCallback {
        void onUserLoaded(LatLng userLatLng);
    }

    public interface LocationNameCallback {
        void onLocationNameReceived(String locationName);
    }

    private String getLocationNameFromFirebase(LatLng recommendedLocation, String category, LocationNameCallback callback) {
        DatabaseReference locationsRef = FirebaseDatabase.getInstance().getReference("Locations").child(category);
        locationsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String locationName = null;
                boolean locationFound = false;

                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    double latitude = dataSnapshot.child("Lat").getValue(Double.class);
                    double longitude = dataSnapshot.child("Long").getValue(Double.class);
                    locationName = dataSnapshot.child("Denumire").getValue(String.class);

                    // Compare the latitude and longitude within a certain threshold
                    double threshold = 0.0001; // Adjust this value based on your desired precision

                    if (Math.abs(latitude - recommendedLocation.latitude) < threshold
                            && Math.abs(longitude - recommendedLocation.longitude) < threshold) {
                        locationFound = true;
                        break;
                    }
                }
                if (locationFound) {
                    callback.onLocationNameReceived(locationName);
                } else {
                    callback.onLocationNameReceived(null);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
        return null;
    }


    private static final int EARTH_RADIUS = 6371; // Earth's radius in kilometers

    private double calculateDistance(LatLng startLatLng, LatLng endLatLng) {
        double startLat = Math.toRadians(startLatLng.latitude);
        double endLat = Math.toRadians(endLatLng.latitude);
        double latDiff = Math.toRadians(endLatLng.latitude - startLatLng.latitude);
        double lngDiff = Math.toRadians(endLatLng.longitude - startLatLng.longitude);

        double a = Math.sin(latDiff / 2) * Math.sin(latDiff / 2)
                + Math.cos(startLat) * Math.cos(endLat)
                * Math.sin(lngDiff / 2) * Math.sin(lngDiff / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        double distance = EARTH_RADIUS * c;

        return distance; // Distance in kilometers
    }

    private void sendMeetupRequest(ModelChat meetup) {
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference();

        String timestamp = String.valueOf(System.currentTimeMillis());

        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("sender", myUID);
        hashMap.put("receiver", hisUID);
        hashMap.put("message", meetup.getMeetupLocation() + " - " + meetup.getMeetupInterest());
        hashMap.put("timestamp", timestamp);
        hashMap.put("isSeen", false);

        hashMap.put("meetupLocation", meetup.getMeetupLocation());
        hashMap.put("meetupInterest", meetup.getMeetupInterest());
        hashMap.put("meetupStatus", meetup.getMeetupStatus());
        hashMap.put("meetupDate", meetup.getMeetupDate());

        databaseReference.child("Chats").push().setValue(hashMap);

        DatabaseReference database = FirebaseDatabase.getInstance().getReference("Users").child(myUID);
        database.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                ModelUser user = snapshot.getValue(ModelUser.class);

                if(notify){
                    sendNotification(hisUID, user.getName(), "New MeetUp!");
                }
                notify = false;
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        // Create chatlist node/child in firebase database
        DatabaseReference chatRef1 = FirebaseDatabase.getInstance().getReference("Chatlist")
                .child(myUID).child(hisUID);
        chatRef1.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(!snapshot.exists()){
                    chatRef1.child("id").setValue(hisUID);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        DatabaseReference chatRef2 = FirebaseDatabase.getInstance().getReference("Chatlist")
                .child(hisUID).child(myUID);
        chatRef2.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(!snapshot.exists()){
                    chatRef2.child("id").setValue(myUID);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

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
                for(DataSnapshot ds: snapshot.getChildren()) {
                        ModelChat chat = ds.getValue(ModelChat.class);
                        if (chat.getReceiver().equals(myUID) && chat.getSender().equals(hisUID) ||
                                chat.getReceiver().equals(hisUID) && chat.getSender().equals(myUID)) {
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

        // Create chatlist node/child in firebase database
        DatabaseReference chatRef1 = FirebaseDatabase.getInstance().getReference("Chatlist")
                .child(myUID).child(hisUID);
        chatRef1.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(!snapshot.exists()){
                    chatRef1.child("id").setValue(hisUID);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        DatabaseReference chatRef2 = FirebaseDatabase.getInstance().getReference("Chatlist")
                .child(hisUID).child(myUID);
        chatRef2.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(!snapshot.exists()){
                    chatRef2.child("id").setValue(myUID);
                }
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
                    Data data = new Data(myUID, name + ": " + message, "New Message", hisUID, R.drawable.ic_default_img);

                    Sender sender = new Sender(data, token.getToken());
                    Log.d("TOKEN", token.getToken());

                    // FCM json object request
                    try {
                        JSONObject senderJsonObj = new JSONObject(new Gson().toJson(sender));
                        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest("https://fcm.googleapis.com/fcm/send",
                                senderJsonObj, new Response.Listener<JSONObject>() {
                            @Override
                            public void onResponse(JSONObject response) {
                                // Response of the request
                                Log.d("JSON_RESPONSE", "onResponse: " + response.toString());

                            }
                        }, new Response.ErrorListener() {
                            @Override
                            public void onErrorResponse(VolleyError error) {
                                Log.d("JSON_RESPONSE", "onResponse: " + error.toString());

                            }
                        }){
                            @Override
                            public Map<String, String> getHeaders() throws AuthFailureError {
                                // Put Params
                                Map<String, String> headers = new HashMap<>();
                                headers.put("Content-Type", "application/json");
                                headers.put("Authorization", "key=AAAAzGSOk5c:APA91bGLhoywuJlK32zOtI_7lgLbTVFOtID6bUjt50n2o2L1ZZjaF0GmcE_hn7Ra2fHn8fEsBF8_pxMRG00JF8gksRs62yxbKQev3QQRdrjmp4XT_fUpt8Y1D-4Eb_KJ8CudOI_91BO7");

                                return headers;
                            }
                        };
                        // Add this request to queue
                        requestQueue.add(jsonObjectRequest);

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
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

        MenuItem searchMenuItem = menu.findItem(R.id.action_profile);
        searchMenuItem.setVisible(false);

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

