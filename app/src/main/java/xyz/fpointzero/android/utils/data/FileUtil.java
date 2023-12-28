package xyz.fpointzero.android.utils.data;

import static android.text.TextUtils.substring;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import xyz.fpointzero.android.MainActivity;
import xyz.fpointzero.android.utils.activity.ActivityUtil;

public class FileUtil {
    public static final String TAG = "FileUtil";
    public static File getInternalStorageDir() {
        return ActivityUtil.getInstance().getMap().get(MainActivity.TAG).getFilesDir();
    }
    public static boolean createNewImg(String filename, Bitmap bitmap) {
        File internalStorageDir = getInternalStorageDir();
        String folderName = filename.substring(0,filename.lastIndexOf("/"));
        String fileName = filename.substring(filename.lastIndexOf("/"));
        // 创建文件夹
        File folder = new File(internalStorageDir, folderName);
        if (!folder.exists()) {
            if (!folder.mkdirs()) {
                return false;
            }
        }
        
        try {
            // 创建文件
            File file = new File(folder, fileName);
            Log.d(TAG, "createNewImg: " + file.getPath());
            if (!file.exists()) {
                file.createNewFile();
            }
            FileOutputStream fileOutputStream = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, fileOutputStream);
            fileOutputStream.flush();
            fileOutputStream.close();
            return true;
        } catch (IOException e) {
            Log.e(TAG, "createNewImg: ", e);
            return false;
        } 
        
    }
}
