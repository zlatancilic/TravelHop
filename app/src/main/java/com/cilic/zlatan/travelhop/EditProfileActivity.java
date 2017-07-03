package com.cilic.zlatan.travelhop;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.graphics.drawable.RoundedBitmapDrawable;
import android.support.v4.graphics.drawable.RoundedBitmapDrawableFactory;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.sql.Timestamp;

public class EditProfileActivity extends AppCompatActivity {

    ImageView userPhotoImageView;
    ImageView takeImage;
    ImageView pickImage;
    ImageView cancelChanges;
    ImageView confirmChanges;

    FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
    StorageReference storageReference = FirebaseStorage.getInstance().getReference();

    private final int REQUEST_CAMERA_PERMISSION = 111;
    private final int REQUEST_GALLERY_PERMISSION = 222;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);

        userPhotoImageView = (ImageView) findViewById(R.id.user_profile_editing);

        Bitmap icon = BitmapFactory.decodeResource(getResources(), R.drawable.default_user_avatar);
        setImage(icon);

        takeImage = (ImageView) findViewById(R.id.take_image_profile_photo);
        takeImage.setOnClickListener(new View.OnClickListener() {
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

        pickImage = (ImageView) findViewById(R.id.pick_image_profile_photo);
        pickImage.setOnClickListener(new View.OnClickListener() {
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

        cancelChanges = (ImageView) findViewById(R.id.profile_edit_cancel);
        cancelChanges.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        confirmChanges = (ImageView) findViewById(R.id.profile_edit_done);
        confirmChanges.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                RoundedBitmapDrawable temp = (RoundedBitmapDrawable) userPhotoImageView.getDrawable();
                Bitmap imageForUpload = temp.getBitmap();
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                imageForUpload.compress(Bitmap.CompressFormat.JPEG, 100, baos);
                byte[] data = baos.toByteArray();
                final String img_path = "userProfileImages/" + firebaseAuth.getCurrentUser().getUid();

                StorageReference imageRef = storageReference.child(img_path);
                UploadTask uploadTask = imageRef.putBytes(data);
                uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        Toast.makeText(EditProfileActivity.this, "Image upload successful", Toast.LENGTH_SHORT).show();

                        new Handler().postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                EditProfileActivity.this.finish();
                            }
                        }, 2000);
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(EditProfileActivity.this, "Image upload failure. Try again later.", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
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
                Toast.makeText(EditProfileActivity.this, "Permission not granted", Toast.LENGTH_SHORT);
            }
        }
        else if(requestCode == REQUEST_GALLERY_PERMISSION) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                openGallery();
            }
            else {
                Toast.makeText(EditProfileActivity.this, "Permission not granted", Toast.LENGTH_SHORT);
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(requestCode == 2 && resultCode == RESULT_OK) {
            try {
                Bundle extras = data.getExtras();
                Bitmap imageBitmap = (Bitmap) extras.get("data");
                setImage(imageBitmap);
            }
            catch (Exception e) {
                Log.i("tag", "error");
            }
        }
        else if(requestCode == 4 && resultCode == RESULT_OK && data != null) {
            try {
                Uri selectedImage = data.getData();
                Bitmap bitmapImage = MediaStore.Images.Media.getBitmap(this.getContentResolver(), selectedImage);
                setImage(bitmapImage);
            }
            catch (Exception e) {
                Log.i("tag", "error");
            }
        }
        else if((requestCode == 2 || requestCode == 4) && resultCode != RESULT_OK) {
//            Toast.makeText(ImageUploadActivity.this, "Image selection canceled", Toast.LENGTH_SHORT).show();
        }
    }

    private void setImage(Bitmap bitmapImage) {
        bitmapImage = cropToSquare(bitmapImage);
        bitmapImage = Bitmap.createScaledBitmap(bitmapImage, 120, 120, false);
        RoundedBitmapDrawable roundedBitmapDrawable = RoundedBitmapDrawableFactory.create(getResources(), bitmapImage);
        final float roundPx = (float) bitmapImage.getWidth() * 0.6f;
        roundedBitmapDrawable.setCornerRadius(roundPx);
        userPhotoImageView.setImageDrawable(roundedBitmapDrawable);
    }

    public static Bitmap cropToSquare(Bitmap bitmap){
        int width  = bitmap.getWidth();
        int height = bitmap.getHeight();
        int newWidth = (height > width) ? width : height;
        int newHeight = (height > width)? height - ( height - width) : height;
        int cropW = (width - height) / 2;
        cropW = (cropW < 0)? 0: cropW;
        int cropH = (height - width) / 2;
        cropH = (cropH < 0)? 0: cropH;
        Bitmap cropImg = Bitmap.createBitmap(bitmap, cropW, cropH, newWidth, newHeight);

        return cropImg;
    }



}
