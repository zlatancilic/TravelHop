package models;

import android.graphics.Bitmap;

public class UserWithImage {

    private UserDetails userDetails;
    private Bitmap image;

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
}
