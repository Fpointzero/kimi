package xyz.fpointzero.android.data;

import java.io.Serializable;
import java.security.KeyPair;
import java.security.PrivateKey;
import java.security.PublicKey;

import xyz.fpointzero.android.utils.crypto.MD5Util;
import xyz.fpointzero.android.utils.crypto.RSAUtil;

public class Setting implements Serializable {
    private static final String ALG = "RSA";
    private String userID;
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
}
