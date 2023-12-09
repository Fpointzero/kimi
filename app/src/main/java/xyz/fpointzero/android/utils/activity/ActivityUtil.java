package xyz.fpointzero.android.utils.activity;

import android.app.Activity;

import java.util.HashMap;

public class ActivityUtil {
    private static ActivityUtil sInstance;
    private Activity mainActivity;
    private static HashMap<String, Activity> map;
    private Activity nowActivity;
    
    public static synchronized ActivityUtil getInstance() {
        if (sInstance == null) {
            map = new HashMap<String, Activity>();
            sInstance = new ActivityUtil();
        }
        return sInstance;
    }

    public HashMap<String, Activity> getMap() {
        return map;
    }

    public void setNowActivity(Activity nowActivity) {
        this.nowActivity = nowActivity;
    }

    public Activity getNowActivity() {
        return nowActivity;
    }
}
