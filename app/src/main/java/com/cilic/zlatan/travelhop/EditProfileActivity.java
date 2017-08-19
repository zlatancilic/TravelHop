package com.cilic.zlatan.travelhop;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.graphics.drawable.RoundedBitmapDrawable;
import android.support.v4.graphics.drawable.RoundedBitmapDrawableFactory;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;

import utils.ProgressDialogHandler;

public class EditProfileActivity extends AppCompatActivity {

    ImageView userPhotoImageView;
    TextView changePhotoTextView;
    ImageView cancelChanges;
    ImageView confirmChanges;
    EditText editUserName;
//    EditText editUserUsername;

    ProgressDialog progressDialog;
    ProgressDialogHandler progressDialogHandler;
    FirebaseAuth firebaseAuth;
    StorageReference storageReference = FirebaseStorage.getInstance().getReference();
    FirebaseDatabase firebaseDatabase;
    DatabaseReference databaseReference;

    private final int REQUEST_CAMERA_PERMISSION = 111;
    private final int REQUEST_GALLERY_PERMISSION = 222;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);

        firebaseAuth = FirebaseAuth.getInstance();
        firebaseDatabase  = FirebaseDatabase.getInstance();
        databaseReference = firebaseDatabase.getReference();
        progressDialogHandler = new ProgressDialogHandler();
        userPhotoImageView = (ImageView) findViewById(R.id.user_profile_editing);
        changePhotoTextView = (TextView) findViewById(R.id.change_photo_label);
        changePhotoTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final CharSequence options[] = new CharSequence[] {"Camera", "Gallery"};
                new AlertDialog.Builder(EditProfileActivity.this)
                        .setTitle("How do you want to upload the photo?")
                        .setItems(options, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                startPhotoActivity(String.valueOf(options[which]));
                            }
                        })
                        .show();
            }
        });

        Bitmap icon = BitmapFactory.decodeResource(getResources(), R.drawable.default_user_avatar);
        setImage(icon);

        editUserName = (EditText) findViewById(R.id.edit_user_name);
//        editUserUsername = (EditText) findViewById(R.id.edit_username);

        Intent i = getIntent();
        if(i.hasExtra(UserProfile.USER_NAME_EXTRA_TAG)) {
            editUserName.setText(i.getStringExtra(UserProfile.USER_NAME_EXTRA_TAG));
        }
//        if(i.hasExtra(UserProfile.USER_USERNAME_EXTRA_TAG)) {
//            editUserUsername.setText(i.getStringExtra(UserProfile.USER_USERNAME_EXTRA_TAG));
//        }
        if(i.hasExtra(UserProfile.USER_PICTURE_EXTRA_TAG)) {
            setImage((Bitmap) i.getParcelableExtra(UserProfile.USER_PICTURE_EXTRA_TAG));
        }

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
                progressDialogHandler.showProgressDialog(progressDialog, EditProfileActivity.this);

                databaseReference.child("userDetails/" + firebaseAuth.getCurrentUser().getUid() + "/fullName").setValue(String.valueOf(editUserName.getText()));
                UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                        .setDisplayName(String.valueOf(editUserName.getText())).build();
                firebaseAuth.getCurrentUser().updateProfile(profileUpdates);
//                databaseReference.child("userDetails/" + firebaseAuth.getCurrentUser().getUid() + "/username").setValue(String.valueOf(editUserUsername.getText()));

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
                        progressDialogHandler.hideProgressDialog(progressDialog);
                        setResult(Activity.RESULT_OK);
                        finish();
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

    private void startPhotoActivity(String option) {
        if(option.equals("Camera")) {
            if(Build.VERSION.SDK_INT >= 23 && checkSelfPermission(android.Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{android.Manifest.permission.CAMERA}, REQUEST_CAMERA_PERMISSION);
            }
            else {
                openCamera();
            }
        }
        else if(option.equals("Gallery")) {
            if(Build.VERSION.SDK_INT >= 23 && checkSelfPermission(android.Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{android.Manifest.permission.READ_EXTERNAL_STORAGE}, REQUEST_GALLERY_PERMISSION);
            }
            else {
                openGallery();
            }
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
