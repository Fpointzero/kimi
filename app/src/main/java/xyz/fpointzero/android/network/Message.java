package xyz.fpointzero.android.network;

import androidx.annotation.NonNull;

import com.alibaba.fastjson.JSONObject;

import xyz.fpointzero.android.constants.DataType;
import xyz.fpointzero.android.utils.data.SettingUtil;
import xyz.fpointzero.android.utils.crypto.RSAUtil;
import xyz.fpointzero.android.utils.network.NetworkUtil;

public class Message {
    private int action;
    private String userID;
    private String username;
    private String ip;
    //    private String publicKey;
    private String msg;
//    private String valid;

    private Message() {
    }

    public Message(int action, String msg) {
        this.action = action;
        this.setMsg(msg);
        
        // 根据系统设置自动生成
        this.setUserID(SettingUtil.getInstance().getSetting().getUserID());
        this.username = SettingUtil.getInstance().getSetting().getUsername();
        this.ip = NetworkUtil.getDeviceIPv4Address() + ":" + MockWebServerManager.getInstance().getPort();
//        this.setPublicKey(SettingUtil.getInstance().getSetting().getPublicKey());
        
//        generateValid();
    }

    public Message(int action, String userID, String publicKey, String msg, String valid) {
        this.action = action;
        this.userID = userID;
//        this.publicKey = publicKey;
        this.msg = msg;
//        this.valid = valid;
    }

    public int getAction() {
        return action;
    }

    public void setAction(int action) {
        this.action = action;
    }

    public String getUserID() {
        return userID;
    }

    public void setUserID(String userID) {
        this.userID = userID;
    }

//    public String getPublicKey() {
//        return publicKey;
//    }

//    public void setPublicKey(String publicKey) {
//        this.publicKey = publicKey;
//    }


    public void setUsername(String username) {
        this.username = username;
    }

    public String getUsername() {
        return username;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

//    public String getValid() {
//        return valid;
//    }
//
//    public void setValid(String valid) {
//        this.valid = valid;
//    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }
    
    /*
    public boolean verify() throws NoSuchPaddingException, IllegalBlockSizeException, NoSuchAlgorithmException, BadPaddingException, InvalidKeyException {
        return RSAUtil.verify(RSAUtil.publicKeyFromString(publicKey), this.valid, this.userID);
    }
     */
    

//    public boolean verify() {
//        String stringBuilder = this.action +
//                this.userID +
////        stringBuilder.append(json.getString("publicKey"));
//                this.ip +
//                this.msg +
//                DataType.SALT;
//        return this.valid.equals(MD5Util.stringToMD5(stringBuilder));
//    }

    public static Message getConnectMessage() {
        Message connectMsg = new Message(DataType.DATA_CONNECT, RSAUtil.publicKeyToString(SettingUtil.getInstance().getSetting().getPublicKey()));
        
//        connectMsg.setAction(Type.DATA_CONNECT);
//        connectMsg.setUserID(SettingUtil.getInstance().getSetting().getUserID());
//        connectMsg.setUsername(SettingUtil.getInstance().getSetting().getUsername());
//        connectMsg.setMsg(RSAUtil.publicKeyToString(SettingUtil.getInstance().getSetting().getPublicKey()));
//        this.setPublicKey(SettingUtil.getInstance().getSetting().getPublicKey());
        return connectMsg;
    }

    @NonNull
    @Override
    public String toString() {
        return JSONObject.toJSONString(this);
    }
}
