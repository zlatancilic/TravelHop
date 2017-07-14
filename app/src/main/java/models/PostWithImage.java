package models;

import android.graphics.Bitmap;

public class PostWithImage {

    private Post post;
    private Bitmap image;
    private String firebaseId;

    public PostWithImage() {}

    public PostWithImage(Post post, Bitmap image) {
        this.post = post;
        this.image = image;
    }

    public Post getPost() {
        return post;
    }

    public void setPost(Post post) {
        this.post = post;
    }

    public Bitmap getImage() {
        return image;
    }

    public void setImage(Bitmap image) {
        this.image = image;
    }

    public String getFirebaseId() {
        return firebaseId;
    }

    public void setFirebaseId(String firebaseId) {
        this.firebaseId = firebaseId;
    }
}
