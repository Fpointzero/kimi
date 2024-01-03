package xyz.fpointzero.android.data;

import java.io.Serializable;
import java.security.KeyPair;
import java.security.PrivateKey;
import java.security.PublicKey;

import xyz.fpointzero.android.utils.crypto.MD5Util;
import xyz.fpointzero.android.utils.crypto.RSAUtil;

/**
 * 设置
 */
public class Setting implements Serializable {
    private static final String ALG = "RSA";
    private String userID;
    private String username;
    private PublicKey publicKey;
    private PrivateKey privateKey;
    private Integer serverPort;
    
    public void init() {
        if (serverPort == null) {
            serverPort = 10808;
        }
        if (privateKey == null || publicKey == null) {
            KeyPair keyPair = RSAUtil.generateKeyPair();
            if (keyPair != null) {
                publicKey = keyPair.getPublic();
                privateKey = keyPair.getPrivate();
                userID = MD5Util.stringToMD5(RSAUtil.publicKeyToString(keyPair.getPublic()));
            }
        }
        if (username == null)
            username = "unkown";
    }

    public String getUserID() {
        return userID;
    }

    public PublicKey getPublicKey() {
        return publicKey;
    }

    public PrivateKey getPrivateKey() {
        return privateKey;
    }

    public int getServerPort() {
        return serverPort != null ? serverPort : 10808;
    }

    public void setServerPort(int serverPort) {
        this.serverPort = serverPort;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }
}
