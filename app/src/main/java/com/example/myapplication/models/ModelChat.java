package com.example.myapplication.models;

import com.google.firebase.database.PropertyName;

public class ModelChat {
    String message;
    String receiver;
    String sender;
    String timestamp;
    boolean isSeen;

    String meetupLocation;
    String meetupInterest;
    String meetupStatus;
    String meetupDate;

    public ModelChat() {}

    public ModelChat(String message, String receiver, String sender,
                     String timestamp, Boolean isSeen) {
        this.message = message;
        this.receiver = receiver;
        this.sender = sender;
        this.timestamp = timestamp;
        this.isSeen = isSeen;
    }

    public ModelChat(String message, String receiver, String sender, String timestamp, boolean isSeen, String location, String interest, String status, String date) {
        this.message = message;
        this.receiver = receiver;
        this.sender = sender;
        this.timestamp = timestamp;
        this.isSeen = isSeen;
        this.meetupLocation = location;
        this.meetupInterest = interest;
        this.meetupStatus = status;
        this.meetupDate = date;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getReceiver() {
        return receiver;
    }

    public void setReceiver(String receiver) {
        this.receiver = receiver;
    }

    public String getSender() {
        return sender;
    }

    public void setSender(String sender) {
        this.sender = sender;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    @PropertyName("isSeen")
    public boolean isSeen() {
        return isSeen;
    }

    @PropertyName("isSeen")
    public void setSeen(boolean seen) {
        isSeen = seen;
    }

    public String getMeetupLocation() {
        return meetupLocation;
    }

    public void setMeetupLocation(String meetupLocation) {
        this.meetupLocation = meetupLocation;
    }

    public String getMeetupInterest() {
        return meetupInterest;
    }

    public void setMeetupInterest(String meetupInterest) {
        this.meetupInterest = meetupInterest;
    }

    public String getMeetupStatus() {
        return meetupStatus;
    }

    public void setMeetupStatus(String meetupStatus) {
        this.meetupStatus = meetupStatus;
    }

    public String getMeetupDate() {
        return meetupDate;
    }

    public void setMeetupDate(String meetupDate) {
        this.meetupDate = meetupDate;
    }
}
