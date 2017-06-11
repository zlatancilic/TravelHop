package models;


public class Post {

    public Post() {
    }

    public Post(String dateCreated, String downloadPath, String caption, String location) {
        this.dateCreated = dateCreated;
        this.downloadPath = downloadPath;
        this.caption = caption;
        this.location = location;
    }

    private String dateCreated;
    private String downloadPath;
    private String caption;
    private String location;

    public String getDateCreated() {
        return dateCreated;
    }

    public void setDateCreated(String dateCreated) {
        this.dateCreated = dateCreated;
    }

    public String getDownloadPath() {
        return downloadPath;
    }

    public void setDownloadPath(String downloadPath) {
        this.downloadPath = downloadPath;
    }

    public String getCaption() {
        return caption;
    }

    public void setCaption(String caption) {
        this.caption = caption;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }
}
