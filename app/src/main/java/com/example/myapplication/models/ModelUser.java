package com.example.myapplication.models;

import java.util.ArrayList;
import java.util.List;

public class ModelUser {

     // Use same name as in firebase database
    String name, email, search, phone, image, cover, uid, onlineStatus, typingTo;
    Double latitude, longitude;
    Boolean gaming, education, gym;

    public ModelUser() {
    }

    public ModelUser(String name, String email, String search, String phone, String image, String cover, String uid, String onlineStatus, String typingTo, Double latitude, Double longitude, Boolean gaming, Boolean education, Boolean gym) {
        this.name = name;
        this.email = email;
        this.search = search;
        this.phone = phone;
        this.image = image;
        this.cover = cover;
        this.uid = uid;
        this.onlineStatus = onlineStatus;
        this.typingTo = typingTo;
        this.latitude = latitude;
        this.longitude = longitude;
        this.gaming = gaming;
        this.education = education;
        this.gym = gym;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getSearch() {
        return search;
    }

    public void setSearch(String search) {
        this.search = search;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getCover() {
        return cover;
    }

    public void setCover(String cover) {
        this.cover = cover;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getOnlineStatus() {
        return onlineStatus;
    }

    public void setOnlineStatus(String onlineStatus) {
        this.onlineStatus = onlineStatus;
    }

    public String getTypingTo() {
        return typingTo;
    }

    public void setTypingTo(String typingTo) {
        this.typingTo = typingTo;
    }

    public Double getLatitude() {
        return latitude;
    }

    public void setLatitude(Double latitude) {
        this.latitude = latitude;
    }

    public Double getLongitude() {
        return longitude;
    }

    public void setLongitude(Double longitude) {
        this.longitude = longitude;
    }

    public Boolean getGaming() {
        return gaming;
    }

    public void setGaming(Boolean gaming) {
        this.gaming = gaming;
    }

    public Boolean getEducation() {
        return education;
    }

    public void setEducation(Boolean education) {
        this.education = education;
    }

    public Boolean getGym() {
        return gym;
    }

    public void setGym(Boolean gym) {
        this.gym = gym;
    }

    public List<String> getInterests(){
        List<String> list = new ArrayList<>();
        if(gaming == true) list.add("Gaming");
        if(education == true) list.add("Education");
        if(gym == true) list.add("Gym");
        return list;
    }
}
