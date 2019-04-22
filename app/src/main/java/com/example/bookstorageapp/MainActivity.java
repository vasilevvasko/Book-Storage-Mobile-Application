package com.example.bookstorageapp;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Build;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

// dangerous shit below
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.MultiplePermissionsReport;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.DexterError;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.PermissionRequestErrorListener;
import com.karumi.dexter.listener.multi.MultiplePermissionsListener;


import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;


public class MainActivity extends AppCompatActivity
{
    //Database references
    private FirebaseStorage storage;
    private StorageReference storageRef;
    private DatabaseReference dbRef;

    //Location Client
    private FusedLocationProviderClient client;

    //View Elements
    private EditText txtTitle, txtAuthor;
    private Button  btnUpload, btnStore;
    private ImageView imgBook;

    // Global book instance
    private Book book = new Book();

    // Image Uri
    private Uri filePath;

    // Global variables used for storing in the database
    private String title;
    private String author;
    private String timeID;
    private String locaiton;

    // Passed Image Identifier
    private final int PICK_IMAGE_REQUEST = 71;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        requestMultiplePermissions(); //Get all needed requests

        //Database References
        storage = FirebaseStorage.getInstance();
        storageRef = storage.getReference();
        dbRef = FirebaseDatabase.getInstance().getReference().child("Books");

        //View elements
        txtTitle = (EditText) findViewById(R.id.txtTitle);
        txtAuthor = (EditText) findViewById((R.id.txtAuthor));
        btnUpload = (Button) findViewById(R.id.btnUpload);
        btnStore = (Button) findViewById(R.id.btnStorage);
        imgBook = (ImageView) findViewById(R.id.imgBook);


        // Get user location and store it, update all global variables, upload image by pushing a Book class instance,
        btnUpload.setOnClickListener(new View.OnClickListener() {
             @Override
             public void onClick(View v) {
                 getLocation();
                 title = txtTitle.getText().toString().trim();
                 author = txtAuthor.getText().toString().trim();
                 uploadImage();
             }
         });


        // Choose image trough a new screen
        imgBook.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                choseImage();
            }
        });

        // Send the user to the Storageactivity
        btnStore.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent (MainActivity.this, StorargeActivity.class);
                startActivity(intent);
            }
        });
    }

    // After geting needed permissions, pass the lat and long to a function which returns the city name, pass the city name in the object to be sotred
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

        switch (requestCode) {
            case 1000:{
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    LocationManager locationManager = (LocationManager) getSystemService (Context.LOCATION_SERVICE);
                    Location location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
                    try{
                        String city = getCityLocation(location.getLatitude(), location.getLongitude());
                        book.setUserLocation(city);
                    } catch (Exception e) {
                        e.printStackTrace();
                        Toast.makeText(MainActivity.this, "Not found", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(this, "Permission not granted!", Toast.LENGTH_SHORT).show();
                }
                break;
            }
        }
    }

    //Choose image from folder
    private void choseImage() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent,"Select Picture"), PICK_IMAGE_REQUEST);
    }

    // Check if image is requested, upload image to image view
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode,resultCode,data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK
            && data !=null && data.getData() != null)
        {
            filePath = data.getData();
            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), filePath);
                imgBook.setImageBitmap(bitmap);
            }
            catch (IOException e){
                e.printStackTrace();
            }
        }

    }

    //push an object as child in the database with the year-month-day-hour-minute-second as an unique ID
    private void uploadImage() {
        if (filePath != null) {
            final ProgressDialog progressDialog = new ProgressDialog(this);
            progressDialog.setTitle("Uploading...");
            progressDialog.show();

            timeID = getCurrentTimeUsingCalendar();

            StorageReference ref = storageRef.child(timeID);

            ref.putFile(filePath)
                    .addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> taskSnapshot) {
                            progressDialog.dismiss();
                            Toast.makeText(MainActivity.this, "Uploaded Successfully", Toast.LENGTH_LONG).show();
                            book.setTitle(title);
                            book.setAuthor(author);
                            book.setID(timeID);
                            book.setUserLocation(locaiton);

                            String postId = dbRef.push().getKey();
                            book.setPostId(postId);
                            dbRef.child(postId).setValue(book);

                        }
                    })

                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            progressDialog.dismiss();
                            Toast.makeText(MainActivity.this, "Failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    })
                    .addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                            double progress = (100.0*taskSnapshot.getBytesTransferred()/taskSnapshot.getTotalByteCount());
                            progressDialog.setMessage("Uploaded " + (int)progress+"%" );

                        }
                    });
        }
    }

    // Get the city name from lat and lon
    private String getCityLocation(double lat, double lon){
        String city = " ";
        Geocoder geocoder = new Geocoder(this, Locale.getDefault());
        List<Address> addresses;
        try{
            addresses = geocoder.getFromLocation(lat, lon, 10);
            if(addresses.size() > 0){
                for(Address adr: addresses){
                    if(adr.getLocality() != null && adr.getLocality().length() > 0){
                        city = adr.getLocality();
                        break;
                    }
                }
            }
        } catch (IOException e){
            e.printStackTrace();
        }
        return city;
    }

    // Get the current t ime and remove all empty spaces and -
    private static String getCurrentTimeUsingCalendar() {
        Calendar cal = Calendar.getInstance();
        Date date = cal.getTime();
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String formattedDate = dateFormat.format(date).replaceAll("[^\\d.]", "");
        return formattedDate;
    }

    //Request all needed permissions
    private void  requestMultiplePermissions(){
        Dexter.withActivity(this)
                .withPermissions(
                        Manifest.permission.CAMERA,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE,
                        Manifest.permission.READ_EXTERNAL_STORAGE)
                .withListener(new MultiplePermissionsListener() {
                    @Override
                    public void onPermissionsChecked(MultiplePermissionsReport report) {
                        // check if all permissions are granted
                        if (report.areAllPermissionsGranted()) {
                            Toast.makeText(getApplicationContext(), "All permissions are granted by user!", Toast.LENGTH_SHORT).show();
                        }
                        // check for permanent denial of any permission
                        if (report.isAnyPermissionPermanentlyDenied()) {
                            // show alert dialog navigating to Settings
                        }
                    }

                    @Override
                    public void onPermissionRationaleShouldBeShown(List<PermissionRequest> permissions, PermissionToken token) {
                        token.continuePermissionRequest();
                    }
                }).
                withErrorListener(new PermissionRequestErrorListener() {
                    @Override
                    public void onError(DexterError error) {
                        Toast.makeText(getApplicationContext(), "Some Error! ", Toast.LENGTH_SHORT).show();
                    }
                })
                .onSameThread()
                .check();
    }

    // Get the user location after permission checks and store it in the book object
    private void getLocation() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1000);
        } else {
            LocationManager locationManager = (LocationManager) getSystemService (Context.LOCATION_SERVICE);
            Location location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
            try{
                String city = getCityLocation(location.getLatitude(), location.getLongitude());
                locaiton = city.trim();
            } catch (Exception e) {
                e.printStackTrace();
                Toast.makeText(MainActivity.this, "Not found", Toast.LENGTH_SHORT).show();
            }

        }
    }
}



