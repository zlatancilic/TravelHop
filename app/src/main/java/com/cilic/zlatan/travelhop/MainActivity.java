package com.cilic.zlatan.travelhop;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentActivity;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.List;

import models.Post;
import models.PostWithImage;
import utils.UserFeedListAdapter;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;

public class MainActivity extends AppCompatActivity implements UserFeed.OnFragmentInteractionListener {

    ImageView takeImage;
    ImageView pickImage;

    private final int REQUEST_CAMERA_PERMISSION = 111;
    private final int REQUEST_GALLERY_PERMISSION = 222;

    FirebaseAuth firebaseAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        firebaseAuth = FirebaseAuth.getInstance();

        ImageView signOut = (ImageView) findViewById(R.id.user_profile);
        signOut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                firebaseAuth.signOut();
                Intent i = new Intent(MainActivity.this, AuthActivity.class);
                startActivity(i);
                finish();
            }
        });

        takeImage = (ImageView) findViewById(R.id.take_image);
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

        pickImage = (ImageView) findViewById(R.id.pick_image);
        pickImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(Build.VERSION.SDK_INT >= 23 && checkSelfPermission(android.Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                    requestPermissions(new String[]{android.Manifest.permission.READ_EXTERNAL_STORAGE}, REQUEST_GALLERY_PERMISSION);
                }
                else {
                    startUploadActivity("gallery");
                }
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
    public void onFragmentInteraction(Uri uri) {

    }

    private void startUploadActivity(String option) {
        Intent goToImageUploadIntent = new Intent(MainActivity.this, ImageUploadActivity.class);
        goToImageUploadIntent.putExtra("type", option);
        startActivityForResult(goToImageUploadIntent, 1);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CAMERA_PERMISSION) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startUploadActivity("camera");
            }
            else {
                Toast.makeText(this, "Permission not granted", Toast.LENGTH_SHORT).show();
            }
        }
        else if(requestCode == REQUEST_GALLERY_PERMISSION) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startUploadActivity("gallery");
            }
            else {
                Toast.makeText(this, "Permission not granted", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
