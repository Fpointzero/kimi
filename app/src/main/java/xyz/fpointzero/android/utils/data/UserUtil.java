package xyz.fpointzero.android.utils.data;

import org.litepal.LitePal;

import java.security.PublicKey;
import java.util.ArrayList;

import xyz.fpointzero.android.data.User;
import xyz.fpointzero.android.utils.crypto.MD5Util;
import xyz.fpointzero.android.utils.crypto.RSAUtil;

public class UserUtil {
    public static ArrayList<User> getBlackList() {
        return new ArrayList<User>(LitePal.where("isBlack = ?", "1").find(User.class));
    }

    public static ArrayList<User> getWhiteList() {
        return new ArrayList<User>(LitePal.where("isWhite = ?", "1").find(User.class));
    }
    
    public static boolean isInList(ArrayList<User> list, User user) {
        for (int i = 0; i < list.size(); i++) {
            if (list.get(i).getUserID().equals(user.getUserID())) {
                return true;
            }
        }
        return false;
    }
    
    public static boolean isInBlackList(User user) {
        return !LitePal.where("userID = ? and isBlack = 1", user.getUserID()).find(User.class).isEmpty();
    }
    
    public static boolean isInWhiteList(User user) {
        return !LitePal.where("UserID = ? and isWhite = 1", user.getUserID()).find(User.class).isEmpty();
    }
    
    public static String getUserID(PublicKey publicKey) {
        return MD5Util.stringToMD5(RSAUtil.publicKeyToString(publicKey));
    }
}
