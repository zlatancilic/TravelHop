package models;

/**
 * Created by zlatan on 26/06/2017.
 */

public class UserDetails {

    public UserDetails() {
    }

    public UserDetails(String fullName, String username) {
        this.fullName = fullName;
        this.username = username;
    }

    private String fullName;
    private String username;

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }
}
