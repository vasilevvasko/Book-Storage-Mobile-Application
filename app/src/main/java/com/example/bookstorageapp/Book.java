package com.example.bookstorageapp;

import com.google.firebase.database.Exclude;
// Class from which an instance is passed to the database
public class Book {
    private String title;
    private String author;
    private String url;
    private String userLocation;
    private String ID;

    private String key;

    public Book() {}


    public String getTitle() {
        return title;
    }

    public String getAuthor() {
        return author;
    }

    public String getUrl() {return url;}

    public String getUserLocation() {return userLocation;}

    public String getID() {return ID;}

    @Exclude
    public String getKey(){ return key;}

    public void setTitle(String title) {
        this.title = title;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public void setUrl(String url) {this.url = url;}

    public void setUserLocation (String userLocation) {this.userLocation = userLocation;}

    public void setID (String ID) {this.ID = ID;}

    @Exclude
    public void setKey(String key) {this.key = key;}
}
