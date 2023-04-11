package com.example.myapplication.adapters;

import android.content.Context;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplication.R;
import com.example.myapplication.models.ModelChat;
import com.google.android.material.timepicker.TimeFormat;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.squareup.picasso.Picasso;

import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class AdapterChat extends RecyclerView.Adapter<AdapterChat.MyHolder> {

    private  static  final int MSG_TYPE_LEFT = 0;
    private  static  final int MSG_TYPE_RIGHT = 1;
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
        } else {
            View view = LayoutInflater.from(context).inflate(R.layout.row_chat_left, parent, false);
            return new MyHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull MyHolder holder, int position) {
        // Get data
        String message = chatList.get(position).getMessage();
        String timeStamp = chatList.get(position).getTimestamp();


        // Convert timestamp to hh:mm
        Calendar calendar = Calendar.getInstance(Locale.ENGLISH);
        calendar.setTimeInMillis(Long.parseLong(timeStamp));
        String time = DateFormat.format("hh:mm", calendar).toString();

        // Set data
        holder.messageTv.setText(message);
        holder.timeTv.setText(time);
        try{
            Picasso.get().load(imageUrl).placeholder(R.drawable.ic_default_img).into(holder.profileIv);
        } catch ( Exception e){

        }

        // Set seen / delivered stauts of message
        if(position == chatList.size()-1){
            if(chatList.get(position).getSeen()){
                holder.isSeenTv.setText("Seen");
            } else {
                holder.isSeenTv.setText("Delivered");
            }
        }
        else {
            holder.isSeenTv.setText("Sending...");
        }
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
            return MSG_TYPE_RIGHT;
        } else {
            return MSG_TYPE_LEFT;
        }
    }

    // View Holder class
    class MyHolder extends RecyclerView.ViewHolder{

        // Views
        ImageView profileIv;
        TextView messageTv;
        TextView timeTv;
        TextView isSeenTv;

        public MyHolder(@NonNull View itemView) {
            super(itemView);

            // Init views
            profileIv = itemView.findViewById(R.id.profileIv);
            messageTv = itemView.findViewById(R.id.messageTv);
            timeTv = itemView.findViewById(R.id.timeTv);
            isSeenTv = itemView.findViewById(R.id.isSeenTv);

        }
    }
}
