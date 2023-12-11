package xyz.fpointzero.android.data;

import org.litepal.annotation.Column;
import org.litepal.crud.LitePalSupport;

public class User extends LitePalSupport {
    @Column(unique = true, defaultValue = "unknown")
    private String userID;
    // TODO: 增加用户名功能
    private String username;
    private String ip;
    private boolean isBlack;
    private boolean isWhite;

    public User(String userID, String ip) {
        this.userID = userID;
        username = "";
        this.ip = ip;
    }

    public User(String userID, String username, String ip) {
        this.userID = userID;
        this.username = username;
        this.ip = ip;
    }

    public String getUserID() {
        return userID;
    }

    public String getIp() {
        return ip;
    }

    public boolean isBlack() {
        return isBlack;
    }

    public boolean isWhite() {
        return isWhite;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public void setUserID(String userID) {
        this.userID = userID;
    }

    public void setBlack(boolean black) {
        isBlack = black;
    }

    public void setWhite(boolean white) {
        isWhite = white;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getUsername() {
        return username;
    }
}
