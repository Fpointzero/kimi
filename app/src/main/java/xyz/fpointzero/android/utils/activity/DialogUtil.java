package xyz.fpointzero.android.utils.activity;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;

import xyz.fpointzero.android.constants.DataType;
import xyz.fpointzero.android.data.User;
import xyz.fpointzero.android.fragments.ContactFragment;
import xyz.fpointzero.android.data.Message;
import xyz.fpointzero.android.network.MockWebServerManager;

public class DialogUtil {
    public static void showConnectDialog(Context context, Message msg) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("通知");
        builder.setMessage("用户:" + msg.getUsername() + "(" + msg.getUserID() + ")请求加您为好友！");
        builder.setPositiveButton("同意", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                User user = new User(msg.getUserID(), msg.getUsername(), msg.getIp());
                user.setWhite(true);
                user.saveOrUpdate("userid = ?", user.getUserID());
                ContactFragment.flushContactList();
                MockWebServerManager.getInstance().getServerWS(msg.getUserID()).send(new Message(DataType.DATA_ADD,"success").toString());
            }
        });
        builder.setNegativeButton("拒绝", null);
        builder.show();
    }

    public static void showErrorDialog(Context context, Message msg) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("警告");
        builder.setMessage("用户和你已经是好友了");
        builder.setPositiveButton("OK", null);
//        builder.setNegativeButton("拒绝", null);
        builder.show();
    }
    
    public static void showSuccessDialog(Context context, String msg) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Success");
        builder.setMessage(msg);
        builder.setPositiveButton("OK", null);
        builder.show();
    }
}
