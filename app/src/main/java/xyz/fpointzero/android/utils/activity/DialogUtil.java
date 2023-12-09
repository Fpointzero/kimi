package xyz.fpointzero.android.utils.activity;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;

import org.litepal.LitePal;

import xyz.fpointzero.android.data.User;
import xyz.fpointzero.android.network.Message;
import xyz.fpointzero.android.utils.network.MessageUtil;

public class DialogUtil {
    public static void showConnectDialog(Context context, Message msg) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setMessage("用户:" + msg.getUserID() + "请求加您为好友！");
        builder.setPositiveButton("同意", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                User user = new User(msg.getUserID(), msg.getIp());
                user.setWhite(true);
                user.save();
            }
        });
        builder.setNegativeButton("拒绝", new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
//                MessageUtil.sendMessage("http://" + msg.getIp() + "/", new Message("reject", ""));
            }
        });
        builder.show();
    }
    
    public static void showSuccessDialog(Context context, String msg) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setMessage(msg);
        builder.setPositiveButton("OK", null);
        builder.show();
    }
}
