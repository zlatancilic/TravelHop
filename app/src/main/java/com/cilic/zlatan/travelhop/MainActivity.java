package com.cilic.zlatan.travelhop;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;

public class MainActivity extends AppCompatActivity implements UserFeed.OnFragmentInteractionListener,
        SearchUsers.OnFragmentInteractionListener,
        UserProfile.OnFragmentInteractionListener,
        SinglePost.OnFragmentInteractionListener {

    ImageView takeImage;
    ImageView pickImage;
    ImageView searchUsers;
    ImageView homeUserFeed;
    ImageView userProfile;

    RelativeLayout homeButtonContainer;
    RelativeLayout searchButtonContainer;
    RelativeLayout profileButtonContainer;

    private final int REQUEST_CAMERA_PERMISSION = 111;
    private final int REQUEST_GALLERY_PERMISSION = 222;
    private final String USER_FEED_FRAGMENT_TAG = "HOME_USER_FEED_FRAGMENT";
    private final String SEARCH_USERS_FRAGMENT_TAG = "SEARCH_USERS_FRAGMENT";
    private final String USER_PROFILE_FRAGMENT_TAG = "USER_PROFILE_FRAGMENT";
    private final String OTHER_USER_PROFILE_FRAGMENT_TAG = "OTHER_USER_PROFILE_FRAGMENT";
    private final String SINGLE_POST_FRAGMENT_TAG = "SINGLE_POST_FRAGMENT";

    FirebaseAuth firebaseAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if(findViewById(R.id.fragment_container) != null) {
            if(savedInstanceState != null) {
                return;
            }

            UserFeed userFeedFragment = new UserFeed();
            getSupportFragmentManager().beginTransaction().add(R.id.fragment_container, userFeedFragment, USER_FEED_FRAGMENT_TAG).commit();
        }

        firebaseAuth = FirebaseAuth.getInstance();

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

        searchButtonContainer = (RelativeLayout) findViewById(R.id.search_button_container);
        searchUsers = (ImageView) findViewById(R.id.search_users);
        searchUsers.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SearchUsers searchUsers = (SearchUsers) getSupportFragmentManager().findFragmentByTag(SEARCH_USERS_FRAGMENT_TAG);
                if(searchUsers != null && searchUsers.isVisible()) {
                    //to implement
                }
                else {
                    SearchUsers searchUsersFragment = new SearchUsers();
                    FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
                    transaction.replace(R.id.fragment_container, searchUsersFragment, SEARCH_USERS_FRAGMENT_TAG);
                    transaction.addToBackStack(null);
                    transaction.commit();
                    profileButtonContainer.setBackgroundColor(getResources().getColor(R.color.defaultBackgroundLight));
                    homeButtonContainer.setBackgroundColor(getResources().getColor(R.color.defaultBackgroundLight));
                    searchButtonContainer.setBackgroundColor(getResources().getColor(R.color.lightGray));
                }
            }
        });

        homeButtonContainer = (RelativeLayout) findViewById(R.id.home_button_container);
        homeUserFeed = (ImageView) findViewById(R.id.home_user_feed);
        homeUserFeed.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                UserFeed userFeed = (UserFeed) getSupportFragmentManager().findFragmentByTag(USER_FEED_FRAGMENT_TAG);
                if(userFeed != null && userFeed.isVisible()) {
                    userFeed.scrollToTop();
                }
                else {
                    UserFeed userFeedFragment = new UserFeed();
                    FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
                    transaction.replace(R.id.fragment_container, userFeedFragment, USER_FEED_FRAGMENT_TAG);
                    transaction.addToBackStack(null);
                    transaction.commit();
                    profileButtonContainer.setBackgroundColor(getResources().getColor(R.color.defaultBackgroundLight));
                    homeButtonContainer.setBackgroundColor(getResources().getColor(R.color.lightGray));
                    searchButtonContainer.setBackgroundColor(getResources().getColor(R.color.defaultBackgroundLight));
                }
            }
        });

        profileButtonContainer = (RelativeLayout) findViewById(R.id.profile_button_container);
        userProfile = (ImageView) findViewById(R.id.user_profile);
        userProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                UserProfile userProfileFragment = (UserProfile) getSupportFragmentManager().findFragmentByTag(USER_PROFILE_FRAGMENT_TAG);
                if(userProfileFragment != null && userProfileFragment.isVisible()) {
                    //to implement
                }
                else {
                    UserProfile usersProfileFrag = UserProfile.newInstance(firebaseAuth.getCurrentUser().getUid(), true);
                    FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
                    transaction.replace(R.id.fragment_container, usersProfileFrag, USER_PROFILE_FRAGMENT_TAG);
                    transaction.addToBackStack(null);
                    transaction.commit();
                    profileButtonContainer.setBackgroundColor(getResources().getColor(R.color.lightGray));
                    homeButtonContainer.setBackgroundColor(getResources().getColor(R.color.defaultBackgroundLight));
                    searchButtonContainer.setBackgroundColor(getResources().getColor(R.color.defaultBackgroundLight));
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
        else if(requestCode == 2 && resultCode == RESULT_OK) {
            Toast.makeText(MainActivity.this, "Profile updated", Toast.LENGTH_SHORT).show();
            UserProfile userProfileFragment = (UserProfile) getSupportFragmentManager().findFragmentByTag(USER_PROFILE_FRAGMENT_TAG);
            if(userProfileFragment != null && userProfileFragment.isVisible()) {
                userProfileFragment.loadData();
            }
        }
    }

    @Override
    public void onFragmentInteraction(Uri uri) {

    }

    @Override
    public void openPost(String postId, String userId) {
        SinglePost singlePostFragment = SinglePost.newInstance(postId, userId);
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.add(R.id.fragment_container, singlePostFragment, SINGLE_POST_FRAGMENT_TAG);
        transaction.addToBackStack(null);
        transaction.commit();
    }

    @Override
    public  void openUserProfile(String firebaseId, boolean followingStatus) {
        UserProfile usersProfileFrag = UserProfile.newInstance(firebaseId, followingStatus);
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.fragment_container, usersProfileFrag, OTHER_USER_PROFILE_FRAGMENT_TAG);
        transaction.addToBackStack(null);
        transaction.commit();
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
