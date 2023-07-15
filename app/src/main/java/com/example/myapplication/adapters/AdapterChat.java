package com.example.myapplication.adapters;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.Typeface;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplication.R;
import com.example.myapplication.models.ModelChat;
import com.google.android.material.timepicker.TimeFormat;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

public class AdapterChat extends RecyclerView.Adapter<AdapterChat.MyHolder> {

    private  static  final int MSG_TYPE_LEFT = 0;
    private  static  final int MSG_TYPE_RIGHT = 1;
    private  static  final int MTP_TYPE_RIGHT = 2;
    private  static  final int MTP_TYPE_LEFT = 3;
    Context context;
    List<ModelChat> chatList;
    String imageUrl;

    FirebaseUser firebaseUser;

    public AdapterChat(Context context, List<ModelChat> chatList, String imageUrl) {
        this.context = context;
        this.chatList = chatList;
        this.imageUrl = imageUrl;
    }

    @NonNull
    @Override
    public MyHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Inflate layouts: row_chat_left.xml for receiver, row_chat_right.xml for sender
        if(viewType == MSG_TYPE_RIGHT){
            View view = LayoutInflater.from(context).inflate(R.layout.row_chat_right, parent, false);
            return new MyHolder(view);
        } else if (viewType == MSG_TYPE_LEFT) {
            View view = LayoutInflater.from(context).inflate(R.layout.row_chat_left, parent, false);
            return new MyHolder(view);
        } else if (viewType == MTP_TYPE_RIGHT) {
            View view = LayoutInflater.from(context).inflate(R.layout.meetup_chat_right,parent,false);
            return  new MyHolder(view);
        } else {
            View view = LayoutInflater.from(context).inflate(R.layout.meetup_chat_left,parent,false);
            return  new MyHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull MyHolder holder, int position) {
        // Get data
        ModelChat chat = chatList.get(position);
        String message = chatList.get(position).getMessage();
        String timeStamp = chatList.get(position).getTimestamp();



        // Convert timestamp to hh:mm
        Calendar calendar = Calendar.getInstance(Locale.ENGLISH);
        calendar.setTimeInMillis(Long.parseLong(timeStamp));
        String time = DateFormat.format("HH:mm", calendar).toString();

        // Set data
        //holder.messageTv.setText(message);
        //holder.timeTv.setText(time);
        try{
            Picasso.get().load(imageUrl).placeholder(R.drawable.ic_default_img).into(holder.profileIv);
        } catch ( Exception e){

        }

        if(holder.getItemViewType() == MTP_TYPE_LEFT || holder.getItemViewType() == MTP_TYPE_RIGHT){
            // Meetup Chat
            String date = chat.getMeetupDate();
            holder.dateTv.setText(date);
            holder.messageTv.setText(message);
            holder.timeTv.setText(time);

            if(!chat.getMeetupStatus().equals("pending")){
                if(holder.acceptBtn != null) holder.acceptBtn.setVisibility(View.GONE);
                if(holder.declineBtn != null) holder.declineBtn.setVisibility(View.GONE);
            }
        } else {
            holder.messageTv.setText(message);
            holder.timeTv.setText(time);
        }

        if(holder.acceptBtn != null) {
            holder.acceptBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    String msgTimeStamp = chatList.get(holder.getAdapterPosition()).getTimestamp();
                    DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Chats");
                    Query query = databaseReference.orderByChild("timestamp").equalTo(msgTimeStamp);
                    query.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            for(DataSnapshot dataSnapshot : snapshot.getChildren()) {
                                String chatId = dataSnapshot.getKey();

                                DatabaseReference chatRef = FirebaseDatabase.getInstance().getReference("Chats").child(chatId);
                                HashMap<String, Object> hashMap = new HashMap<>();
                                hashMap.put("meetupStatus","accepted");
                                chatRef.updateChildren(hashMap);
                            }

                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });
                    Toast.makeText(context, "Meetup Accepted!", Toast.LENGTH_SHORT).show();
                    holder.acceptBtn.setVisibility(View.GONE);
                    holder.declineBtn.setVisibility(View.GONE);
                }
            });
        }

        if(holder.declineBtn !=null) {
            holder.declineBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    String msgTimeStamp = chatList.get(holder.getAdapterPosition()).getTimestamp();
                    DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Chats");
                    Query query = databaseReference.orderByChild("timestamp").equalTo(msgTimeStamp);
                    query.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            for(DataSnapshot dataSnapshot : snapshot.getChildren()) {
                                String chatId = dataSnapshot.getKey();

                                DatabaseReference chatRef = FirebaseDatabase.getInstance().getReference("Chats").child(chatId);
                                HashMap<String, Object> hashMap = new HashMap<>();
                                hashMap.put("meetupStatus","declined");
                                chatRef.updateChildren(hashMap);
                            }

                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });
                    Toast.makeText(context, "Meetup Declined!", Toast.LENGTH_SHORT).show();
                    holder.acceptBtn.setVisibility(View.GONE);
                    holder.declineBtn.setVisibility(View.GONE);
                }
            });
        }

        // Longpress to show delete dialog
        holder.messageLayout.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                // Check if the message is already deleted
                String messageText = chatList.get(holder.getAdapterPosition()).getMessage();
                if (messageText.equals("This message was deleted...")) {
                    return true; // Do not show the dialog
                }

                // Show delete message confirm dialog
                AlertDialog.Builder builder = new AlertDialog.Builder(context);
                builder.setTitle("Delete");
                builder.setMessage("Are you sure you want to delete this message?");

                // Delete button
                builder.setPositiveButton("Delete", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        deleteMessage(holder.getAdapterPosition());
                    }
                });
                // Cancel delete button
                builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        // Dismiss dialog
                        dialogInterface.dismiss();
                    }
                });

                // Set button styles
                AlertDialog dialog = builder.create();
                dialog.setOnShowListener(new DialogInterface.OnShowListener() {
                    @Override
                    public void onShow(DialogInterface dialogInterface) {
                        Button positiveButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
                        Button negativeButton = dialog.getButton(AlertDialog.BUTTON_NEGATIVE);
                        positiveButton.setTextColor(Color.BLACK); // set text color
                        negativeButton.setTextColor(Color.BLACK);
                    }
                });


                // Create and show dialog
                dialog.show();

                return true;
            }
        });

        // Set seen / delivered status of message
        if(position == chatList.size()-1){
            if(chatList.get(position).isSeen()){
                holder.isSeenTv.setText("Seen");
            } else {
                holder.isSeenTv.setText("Delivered");
            }
        }
        else {
            holder.isSeenTv.setVisibility(View.GONE);
        }
    }

    private void deleteMessage(int position) {
        String myUID = FirebaseAuth.getInstance().getCurrentUser().getUid();
        // Logic:
        // Get timestamp of clicked message
        // Compare the timestamp of the clicked message with all messages in Chats
        // Where both values matches delete that message
        // This will allow the sender to delete his and receiver`s message

        String msgTimeStamp = chatList.get(position).getTimestamp();
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Chats");
        Query query = databaseReference.orderByChild("timestamp").equalTo(msgTimeStamp);
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for(DataSnapshot ds: snapshot.getChildren()){
                    // If you want to allow sender to delete only his message then
                    // compare sender value with current user`s UID
                    // if they match it means that it`s the message of sender that is trying to delete
                    if (ds.child("sender").getValue().equals(myUID)){
                        // We can do one of two things here
                        // 1) Remove the message from chats
                        // 2) Set the value of the message to "This message was deleted..."

                        // 1)
                        //ds.getRef().removeValue();

                        // 2)
                        HashMap<String, Object> hashMap = new HashMap<>();
                        hashMap.put("message","This message was deleted...");
                        ds.getRef().updateChildren(hashMap);

                        Toast.makeText(context, "Message deleted!", Toast.LENGTH_SHORT).show();
                   
                    } else {
                        Toast.makeText(context, "You can only delete your message", Toast.LENGTH_SHORT).show();
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    @Override
    public int getItemCount() {
        return chatList.size();
    }

    @Override
    public int getItemViewType(int position) {
        // Get currently signed in user
        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        if(chatList.get(position).getSender().equals(firebaseUser.getUid())){
            if(chatList.get(position).getMeetupDate() != null ){
                return MTP_TYPE_RIGHT;
            } else return MSG_TYPE_RIGHT;
        } else {
            if(chatList.get(position).getMeetupDate() != null){
                return MTP_TYPE_LEFT;
            } else return MSG_TYPE_LEFT;
        }
    }

    // View Holder class
    class MyHolder extends RecyclerView.ViewHolder{

        // Views for message
        ImageView profileIv;
        TextView messageTv;
        TextView timeTv;
        TextView isSeenTv;
        LinearLayout messageLayout; // for click listener to show delete

        // Views for meetup
        TextView dateTv;
        Button acceptBtn;
        Button declineBtn;

        public MyHolder(@NonNull View itemView) {
            super(itemView);

            // Init views
            profileIv = itemView.findViewById(R.id.profileIv);
            messageTv = itemView.findViewById(R.id.messageTv);
            timeTv = itemView.findViewById(R.id.timeTv);
            isSeenTv = itemView.findViewById(R.id.isSeenTv);
            messageLayout = itemView.findViewById(R.id.messageLayout);

            acceptBtn = itemView.findViewById(R.id.acceptButton);
            declineBtn = itemView.findViewById(R.id.declineButton);
            dateTv = itemView.findViewById(R.id.dateTv);

        }
    }
}
