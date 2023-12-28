package xyz.fpointzero.android.utils.activity;

import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.widget.EditText;
import android.widget.Toast;

import org.litepal.LitePal;

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
                MockWebServerManager.getInstance().getServerWS(msg.getUserID()).send(new Message(DataType.DATA_ADD, "success").toString());
            }
        });
        builder.setNegativeButton("拒绝", null);
        builder.show();
    }

    public static void showWarningDialog(Context context, String msg, DialogInterface.OnClickListener okListener) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("WARNING");
        builder.setMessage(msg);
        builder.setPositiveButton("确定", okListener);
        builder.setNegativeButton("取消", null);
        builder.show();
    }

    public static void showSuccessDialog(Context context, String msg) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Success");
        builder.setMessage(msg);
        builder.setPositiveButton("OK", null);
        builder.show();
    }

    public static void showEditIPDialog(Context context, User user) {
        // IPv4 格式的正则表达式
        String ipv4Pattern = "^((\\d{1,2}|1\\d{2}|2[0-4]\\d|25[0-5])\\.){3}(\\d{1,2}|1\\d{2}|2[0-4]\\d|25[0-5])$";

// IPv4:端口号 格式的正则表达式
        String ipv4WithPortPattern = "^((\\d{1,2}|1\\d{2}|2[0-4]\\d|25[0-5])\\.){3}(\\d{1,2}|1\\d{2}|2[0-4]\\d|25[0-5]):\\d+$";
        // 创建一个 AlertDialog.Builder 对象
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("修改IP"); // 设置对话框标题

// 创建并设置 EditText
        final EditText editText = new EditText(context);
        editText.setText(user.getIp());
        builder.setView(editText);

// 设置对话框按钮
        builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String inputText = editText.getText().toString();
                // 在这里处理用户输入的内容
                if (inputText.matches(ipv4Pattern) || inputText.matches(ipv4WithPortPattern)) {
                    ContentValues contentValues = new ContentValues();
                    contentValues.put("ip", inputText);
                    LitePal.updateAll(User.class, contentValues, "userid = ?", user.getUserID());
                    user.setIp(inputText);
                    Toast.makeText(context, "修改成功", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(context, "格式错误", Toast.LENGTH_SHORT).show();
                }
            }
        });
        builder.setNegativeButton("取消", null);

        builder.show();
    }
}
