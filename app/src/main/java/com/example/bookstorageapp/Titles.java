package com.example.bookstorageapp;

// Class whose objects stores fetched data from databse
public class Titles {

    Titles() {}
    Titles(String title) {
        this.title = title;
    }

    private String title;

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }
}
