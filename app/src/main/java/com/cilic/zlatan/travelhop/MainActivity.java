package com.cilic.zlatan.travelhop;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.GenericTypeIndicator;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import models.Post;

public class MainActivity extends AppCompatActivity {

    FirebaseAuth firebaseAuth;
    FirebaseDatabase firebaseDatabase;
    DatabaseReference databaseReference;

    Button takeImage;
    Button pickImage;

    private final int REQUEST_CAMERA_PERMISSION = 111;
    private final int REQUEST_GALLERY_PERMISSION = 222;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        firebaseAuth = FirebaseAuth.getInstance();
        firebaseDatabase  = FirebaseDatabase.getInstance();
        databaseReference = firebaseDatabase.getReference();

        FirebaseUser currentUser = firebaseAuth.getCurrentUser();
        final TextView tv = (TextView) findViewById(R.id.text_view);
        tv.setText(currentUser.getUid());

        Button signOut = (Button) findViewById(R.id.sign_out);
        signOut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                firebaseAuth.signOut();
            }
        });

        takeImage = (Button) findViewById(R.id.take_image);
        takeImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(Build.VERSION.SDK_INT >= 23 && checkSelfPermission(android.Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                    requestPermissions(new String[]{android.Manifest.permission.CAMERA}, REQUEST_CAMERA_PERMISSION);
                }
                else {
                    startUploadActivity("camera");
                }
            }
        });

        pickImage = (Button) findViewById(R.id.pick_image);
        pickImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(Build.VERSION.SDK_INT >= 23 && checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                    requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, REQUEST_GALLERY_PERMISSION);
                }
                else {
                    startUploadActivity("gallery");
                }
            }
        });

        DatabaseReference followingList = firebaseDatabase.getReference("userDetails/" + firebaseAuth.getCurrentUser().getUid() + "/following/");
        followingList.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                GenericTypeIndicator<List<String>> t = new GenericTypeIndicator<List<String>>() {};
                List followingList = dataSnapshot.getValue(t);
                if( followingList == null ) {
                    System.out.println("");
                }
                else {
                    ArrayList<Post> allPosts = new ArrayList<Post>();
                    for(int i = 0; i < followingList.size(); i++) {
                        String tempUser = followingList.get(i).toString();
                        DatabaseReference postsReference = firebaseDatabase.getReference("activityStreamPosts/" + tempUser);
                        postsReference.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                for(DataSnapshot postSnapshot: dataSnapshot.getChildren()) {
                                    Post currentPost = postSnapshot.getValue(Post.class);
                                    //tv.setText(tv.getText() + "|" + currentPost.getDownloadPath());
                                }
                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {

                            }
                        });
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(requestCode == 1 && resultCode == RESULT_OK) {
            Toast.makeText(MainActivity.this, "Image uploaded", Toast.LENGTH_SHORT).show();
        }
        else if(requestCode == 1 && resultCode != RESULT_OK) {
            Toast.makeText(MainActivity.this, "Image upload canceled", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CAMERA_PERMISSION) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startUploadActivity("camera");
            }
            else {
                Toast.makeText(MainActivity.this, "Permission not granted", Toast.LENGTH_SHORT).show();
            }
        }
        else if(requestCode == REQUEST_GALLERY_PERMISSION) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startUploadActivity("gallery");
            }
            else {
                Toast.makeText(MainActivity.this, "Permission not granted", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void startUploadActivity(String option) {
        Intent goToImageUploadIntent = new Intent(MainActivity.this, ImageUploadActivity.class);
        goToImageUploadIntent.putExtra("type", option);
        startActivityForResult(goToImageUploadIntent, 1);
    }
}
