package models;

import android.graphics.Bitmap;

public class UserWithImage {

    private UserDetails userDetails;
    private Bitmap image;
    private String followingStatus;
    private String firebaseId;

    public UserWithImage() {}

    public UserWithImage(UserDetails userDetails, Bitmap image) {
        this.userDetails = userDetails;
        this.image = image;
    }

    public UserDetails getUserDetails() {
        return userDetails;
    }

    public void setUserDetails(UserDetails userDetails) {
        this.userDetails = userDetails;
    }

    public Bitmap getImage() {
        return image;
    }

    public void setImage(Bitmap image) {
        this.image = image;
    }

    public String getFollowingStatus() {
        return followingStatus;
    }

    public void setFollowingStatus(String followingStatus) {
        this.followingStatus = followingStatus;
    }

    public String getFirebaseId() {
        return firebaseId;
    }

    public void setFirebaseId(String firebaseId) {
        this.firebaseId = firebaseId;
    }
}
