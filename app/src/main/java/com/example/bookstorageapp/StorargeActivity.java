package com.example.bookstorageapp;

import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class StorargeActivity extends AppCompatActivity {

    // View elements
    ListView listView;

    // Database references
    FirebaseDatabase database;
    DatabaseReference ref;
    // List view and adapter
    ArrayList<String> list;
    ArrayAdapter<String> adapter;
    // Global instance of object which will store fetched data from the database
    Titles titles;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_storarge);


        // Assign values to elements
        titles = new Titles();
        listView = (ListView) findViewById(R.id.listView);
        database = FirebaseDatabase.getInstance();
        ref = database.getReference("Books"); // Might be Books
        list = new ArrayList<>();

        // set adapter
        adapter = new ArrayAdapter<String>(this, R.layout.titles_info, R.id.titlesInfo, list); // not done

        ref.addValueEventListener(new ValueEventListener() {
            // fetch data from database
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for(DataSnapshot ds: dataSnapshot.getChildren()) {
                titles = ds.getValue(Titles.class);
                list.add(titles.getTitle());
                }
                listView.setAdapter(adapter);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

}
