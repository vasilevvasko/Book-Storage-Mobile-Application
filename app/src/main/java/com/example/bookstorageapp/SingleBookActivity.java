package com.example.bookstorageapp;

import android.content.Intent;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;


import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;


public class SingleBookActivity extends AppCompatActivity {

    // Database references
    private FirebaseStorage firebaseStorage;
    private StorageReference storageReference;
    private StorageReference ref;
    private DatabaseReference db;


    private TextView txtTitle;
    private TextView txtAuthor;
    private TextView txtTimeCreated;
    private TextView txtLocation;
    private ImageView imgBook;


    //Strindz
    private String fetchedAuthor;
    private String fetchedId;
    private String fetchedTitle;
    private String fetchedLocation;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_single_book);

        Intent intent  = getIntent();
        String intentIdInfo  = intent.getStringExtra("bookId");
        db = FirebaseDatabase.getInstance().getReference("Books");

        txtTitle = (TextView) findViewById(R.id.txtTitle);
        txtAuthor = (TextView) findViewById((R.id.txtAuthor));
        txtTimeCreated = (TextView) findViewById((R.id.txtTimeCreated));
        txtLocation = (TextView) findViewById((R.id.txtLocation));
        imgBook = (ImageView) findViewById(R.id.imgBook);

        String titleNotFound = "Title not found.";
        txtTitle.setText(titleNotFound);

        String authorNotFound = "Author not found.";
        txtAuthor.setText(authorNotFound);

        String datePostedNotFound= "Post date not found.";
        txtTimeCreated.setText(datePostedNotFound);

        String locationNotFound = "Location not found.";
        txtLocation.setText(locationNotFound);


        db.child(intentIdInfo)
                .addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                    if(dataSnapshot.exists()) {

                    if (dataSnapshot.hasChild("title")) {
                        fetchedTitle = dataSnapshot.child("title").getValue().toString();
                        txtTitle.setText(fetchedTitle);
                    }

                    if (dataSnapshot.hasChild("author")) {
                        fetchedAuthor = dataSnapshot.child("author").getValue().toString();
                        txtAuthor.setText(fetchedAuthor);
                    }

                    if (dataSnapshot.hasChild("id")) {
                        fetchedId = dataSnapshot.child("id").getValue().toString();
                        String uploadDate = "Uploaded on: " + idToDate(fetchedId);
                        txtTimeCreated.setText(uploadDate);
                    }

                    if (dataSnapshot.hasChild("userLocation")) {
                        fetchedLocation = dataSnapshot.child("userLocation").getValue().toString();
                        String uploadLocation = "Uploaded from: " + fetchedLocation;
                        txtLocation.setText(uploadLocation);
                    }
                        getImage();
                    }

                }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private String idToDate(String id) {
        StringBuilder str = new StringBuilder(id);
        str.insert(4, "-");
        str.insert(7, "-");
        str.insert(10, " ");
        str.insert(13, ":");
        str.insert(16, ":");

        return str.toString();
    }

    private void getImage() {
        storageReference = firebaseStorage.getInstance().getReference();
        ref = storageReference.child(fetchedId);

        ref.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) {
                Picasso.with(SingleBookActivity.this).load(uri).into(imgBook);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {

            }
        });
    }


}
