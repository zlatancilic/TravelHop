package com.cilic.zlatan.travelhop;

import android.*;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Build;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.StringDef;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.UserInfo;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.naver.android.helloyako.imagecrop.view.ImageCropView;

import java.io.ByteArrayOutputStream;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Map;

import models.Post;
import models.PostWithImage;
import models.UserDetails;

public class ImageUploadActivity extends AppCompatActivity {

//    ImageView imageView;
    Button openGalleryButton;
    Button openCameraButton;
    Button uploadImageButton;
    EditText captionEditText;
    EditText locationEditText;
    ImageCropView imageCropView;

    StorageReference storageReference = FirebaseStorage.getInstance().getReference();
    FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
    FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
    DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference();

    private static final SimpleDateFormat sdf = new SimpleDateFormat("yyyy_MM_dd_HH_mm_ss");
    private static final SimpleDateFormat creation_date_format = new SimpleDateFormat("MM.dd.yyyy HH:mm:ss");

    private final int REQUEST_CAMERA_PERMISSION = 111;
    private final int REQUEST_GALLERY_PERMISSION = 222;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_upload);

//        imageView = (ImageView) findViewById(R.id.selected_image_imageview);
        openGalleryButton = (Button) findViewById(R.id.gallery_button);
        openCameraButton = (Button) findViewById(R.id.camera_button);
        uploadImageButton = (Button) findViewById(R.id.upload_image_button);
        captionEditText = (EditText) findViewById(R.id.caption_edittext);
        locationEditText = (EditText) findViewById(R.id.location_edittext);
        imageCropView = (ImageCropView) findViewById(R.id.selected_image_imageview_offset);


        uploadImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Bitmap bitmapImage = imageCropView.getViewBitmap();
                if(bitmapImage != null) {
                    uploadImage(bitmapImage);
                }
                else {
                    Toast.makeText(ImageUploadActivity.this, "You have not selected an image yet", Toast.LENGTH_SHORT);
                }
            }
        });

        openGalleryButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(Build.VERSION.SDK_INT >= 23 && checkSelfPermission(android.Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                    requestPermissions(new String[]{android.Manifest.permission.READ_EXTERNAL_STORAGE}, REQUEST_GALLERY_PERMISSION);
                }
                else {
                    openGallery();
                }
            }
        });

        openCameraButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(Build.VERSION.SDK_INT >= 23 && checkSelfPermission(android.Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                    requestPermissions(new String[]{android.Manifest.permission.CAMERA}, REQUEST_CAMERA_PERMISSION);
                }
                else {
                    openCamera();
                }
            }
        });

        Intent mainActivityIntent = getIntent();
        if(mainActivityIntent.hasExtra("type")) {
            String type = mainActivityIntent.getStringExtra("type");
            if(type.equals("camera")) {
                openCameraButton.callOnClick();
            }
            else if(type.equals("gallery")) {
                openGalleryButton.callOnClick();
            }
        }


    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(requestCode == 2 && resultCode == RESULT_OK) {
            try {
                Bundle extras = data.getExtras();
                Bitmap imageBitmap = (Bitmap) extras.get("data");
//                imageView.setImageBitmap(imageBitmap);
                imageCropView.setImageBitmap(imageBitmap);
            }
            catch (Exception e) {
                Log.i("tag", "error");
            }
        }
        else if(requestCode == 4 && resultCode == RESULT_OK && data != null) {
            try {
                Uri selectedImage = data.getData();
                Bitmap bitmapImage = MediaStore.Images.Media.getBitmap(this.getContentResolver(), selectedImage);
//                imageView.setImageBitmap(bitmapImage);
                imageCropView.setImageBitmap(bitmapImage);
            }
            catch (Exception e) {
                Log.i("tag", "error");
            }
        }
        else if((requestCode == 2 || requestCode == 4) && resultCode != RESULT_OK) {
            Toast.makeText(ImageUploadActivity.this, "Image selection canceled", Toast.LENGTH_SHORT).show();
        }
    }

    private void openCamera() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(takePictureIntent, 2);
        }
    }

    private void openGallery() {
        Intent pickFromGalleryIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(pickFromGalleryIntent, 4);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CAMERA_PERMISSION) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                openCamera();
            }
            else {
                Toast.makeText(ImageUploadActivity.this, "Permission not granted", Toast.LENGTH_SHORT);
            }
        }
        else if(requestCode == REQUEST_GALLERY_PERMISSION) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                openGallery();
            }
            else {
                Toast.makeText(ImageUploadActivity.this, "Permission not granted", Toast.LENGTH_SHORT);
            }
        }
    }

    private void uploadImage(final Bitmap image) {
        final String caption = captionEditText.getText().toString();
        final String location = locationEditText.getText().toString();

        DatabaseReference postsReference = firebaseDatabase.getReference("userDetails/" + firebaseAuth.getCurrentUser().getUid());
        postsReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                    final UserDetails currentUser = dataSnapshot.getValue(UserDetails.class);

                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    image.compress(Bitmap.CompressFormat.JPEG, 100, baos);
                    byte[] data = baos.toByteArray();
                    final Timestamp timestamp = new Timestamp(System.currentTimeMillis());
                    final String img_path = "activityStreamImages/" + sdf.format(timestamp) + "_" + firebaseAuth.getCurrentUser().getUid();

                    StorageReference imageRef = storageReference.child(img_path);
                    UploadTask uploadTask = imageRef.putBytes(data);
                    uploadTask.addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception exception) {
                            Toast.makeText(ImageUploadActivity.this, "ERROR WITH UPLOAD", Toast.LENGTH_SHORT).show();
                        }
                    }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            Map<String, String> map = new HashMap<String, String>();
                            map.put("downloadPath", img_path);
                            map.put("caption", caption);
                            map.put("location", location);
                            map.put("username", currentUser.getUsername());
                            map.put("fullName", currentUser.getFullName());
                            map.put("dateCreated", creation_date_format.format(timestamp));
                            String key = databaseReference.child("activityStreamPosts").push().getKey();
                            databaseReference.child("activityStreamPosts").child(firebaseAuth.getCurrentUser().getUid()).child(key).setValue(map);

                            Toast.makeText(ImageUploadActivity.this, "SUCCESS", Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }
}
