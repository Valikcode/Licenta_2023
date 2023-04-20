package com.example.myapplication.notifications;

public class Token {

    // An FCM Token, or much commonly known as a registrationToken. An ID issued by the GCM
    // connection servers to the client app that allows it to receive messages

    String token;

    public Token() {
    }

    public Token(String token) {
        this.token = token;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }
}
