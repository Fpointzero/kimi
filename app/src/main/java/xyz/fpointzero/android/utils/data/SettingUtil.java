package xyz.fpointzero.android.utils.data;

import android.content.Context;
import android.util.Log;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

import xyz.fpointzero.android.data.Setting;

public class SettingUtil {
    private static final String TAG = "SettingUtil";
    private static final String SETTING_FILE_NAME = "settings";
    private static SettingUtil instance;
    private Setting setting;

    public synchronized static SettingUtil getInstance() {
        if (instance == null) {
            instance = new SettingUtil();
        }
        return instance;
    }

    public void initSetting(Context context) {
        try {
            FileInputStream in = context.openFileInput(SETTING_FILE_NAME);
            setting = (Setting) SerializationUtil.deserialize(in);
            in.close();
        } catch (IOException e) {
//            Log.e(TAG, "initSetting: ", e);
            setting = new Setting();
            setting.init();
            saveSetting(context);
        }
//        Log.d(TAG, "initSetting: " + RSAKeyGenerator.publicKeyToString(setting.getPublicKey()) + "\n" + RSAKeyGenerator.privateKeyToString(setting.getPrivateKey()));
    }

    public void saveSetting(Context context) {
        try {
            if (setting == null)
                setting = new Setting();
            FileOutputStream out = context.openFileOutput(SETTING_FILE_NAME, Context.MODE_PRIVATE);
            SerializationUtil.serialize(setting, out);
            out.close();
        } catch (IOException e) {
            Log.e(TAG, "saveSetting: ", e);
        }
    }

    public Setting getSetting() {
        return setting;
    }
}
