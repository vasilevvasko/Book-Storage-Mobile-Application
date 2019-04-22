package com.example.bookstorageapp;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
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
    private ListView listView;

    // Database references
    private FirebaseDatabase database;
    private DatabaseReference ref;
    // List view and adapter
    private ArrayList<String> list;
    private ArrayList<String> idList;
    private ArrayAdapter<String> adapter;
    // Global instance of object which will store fetched data from the database
    private Book books;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_storarge);


        // Assign values to elements
        books = new Book();
        listView = (ListView) findViewById(R.id.listView);
        database = FirebaseDatabase.getInstance();
        ref = database.getReference("Books"); // Might be Books
        list = new ArrayList<>();
        idList = new ArrayList<>();


        // set adapter
        adapter = new ArrayAdapter<String>(this, R.layout.titles_info, R.id.titlesInfo, list); // not done

        ref.addValueEventListener(new ValueEventListener() {
            // fetch data from database
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for(DataSnapshot ds: dataSnapshot.getChildren()) {
                    books = ds.getValue(Book.class);
                    String book = books.getTitle() + " by " + books.getAuthor();
                list.add(book);
                idList.add(books.getPostId());
                }
                listView.setAdapter(adapter);

                listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                       String bookId = idList.get(position);
                       ref.child(bookId);
                        Intent intent = new Intent(StorargeActivity.this, SingleBookActivity.class);
                        intent.putExtra("bookId", bookId);
                        startActivity(intent);
                    }
                });
            }



            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

}
