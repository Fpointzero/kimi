package xyz.fpointzero.android.utils.network;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkInfo;
import android.os.Build;
import android.util.Log;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;

import xyz.fpointzero.android.MainActivity;
import xyz.fpointzero.android.utils.activity.ActivityUtil;

public class NetworkUtil {
    public static final String TAG = "NetworkUtil";
    // 获取设备的IP地址
    public static String getDeviceIpAddress(Context context) {
        String ipAddress = "";

        // 检查是否连接了网络
        ConnectivityManager connManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connManager != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                Network network = connManager.getActiveNetwork();
                NetworkCapabilities networkCapabilities = connManager.getNetworkCapabilities(network);
                if (networkCapabilities != null && networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)) {
                    ipAddress = getIPAddress(true);
                }
            } else {
                NetworkInfo networkInfo = connManager.getActiveNetworkInfo();
                if (networkInfo != null && networkInfo.isConnected()) {
                    ipAddress = getIPAddress(false);
                }
            }
        }

        return ipAddress;
    }

    /**
     * 获取IP地址（适用于Android 10及以上和Android 10以下的设备）
     * @param isAndroid10OrAbove
     * @return
     */
    private static String getIPAddress(boolean isAndroid10OrAbove) {
        String ipAddress = "";
        try {
            Enumeration<NetworkInterface> networkInterfaces = NetworkInterface.getNetworkInterfaces();
            while (networkInterfaces.hasMoreElements()) {
                NetworkInterface networkInterface = networkInterfaces.nextElement();
                Enumeration<InetAddress> inetAddresses = networkInterface.getInetAddresses();
                while (inetAddresses.hasMoreElements()) {
                    InetAddress inetAddress = inetAddresses.nextElement();
                    if (!inetAddress.isLoopbackAddress()) {
                        if (isAndroid10OrAbove) {
                            if (inetAddress.isLinkLocalAddress()) {
                                continue;
                            }
                        }
                        ipAddress = inetAddress.getHostAddress();
                        break;
                    }
                }
            }
        } catch (SocketException e) {
            Log.e(TAG, "Failed to get IP address: " + e.getMessage());
            e.printStackTrace();
        }

        return ipAddress;
    }

    /**
     * 获取IPv4地址
     * @return ipv4地址
     */
    public static String getDeviceIPv4Address() {
        Context context = ActivityUtil.getInstance().getMap().get(MainActivity.TAG);
        String ipv4Address = "";

        // 检查是否连接了网络
        ConnectivityManager connManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connManager != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                Network network = connManager.getActiveNetwork();
                NetworkCapabilities networkCapabilities = connManager.getNetworkCapabilities(network);
                if (networkCapabilities != null && networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)) {
                    ipv4Address = getIPv4Address(true);
                }
            } else {
                NetworkInfo networkInfo = connManager.getActiveNetworkInfo();
                if (networkInfo != null && networkInfo.isConnected()) {
                    ipv4Address = getIPv4Address(false);
                }
            }
        }

        return ipv4Address;
    }

    // 获取IPv4地址（适用于Android 10及以上和Android 10以下的设备）
    private static String getIPv4Address(boolean isAndroid10OrAbove) {
        String ipv4Address = "";

        try {
            Enumeration<NetworkInterface> networkInterfaces = NetworkInterface.getNetworkInterfaces();
            while (networkInterfaces.hasMoreElements()) {
                NetworkInterface networkInterface = networkInterfaces.nextElement();
                Enumeration<InetAddress> inetAddresses = networkInterface.getInetAddresses();
                while (inetAddresses.hasMoreElements()) {
                    InetAddress inetAddress = inetAddresses.nextElement();
                    if (!inetAddress.isLoopbackAddress() && inetAddress instanceof InetAddress && !inetAddress.isLinkLocalAddress()) {
                        String hostAddress = inetAddress.getHostAddress();
                        if (isIPv4Address(hostAddress)) {
                            ipv4Address = hostAddress;
                            break;
                        }
                    }
                }
                if (!ipv4Address.isEmpty()) {
                    break;
                }
            }
        } catch (SocketException e) {
            Log.e("NetworkUtils", "Failed to get IPv4 address: " + e.getMessage());
            e.printStackTrace();
        }

        return ipv4Address;
    }

    /**
     * 判断是否IPV4地址
     * @param address
     * @return
     */
    private static boolean isIPv4Address(String address) {
        String[] parts = address.split("\\.");
        if (parts.length != 4) {
            return false;
        }
        for (String part : parts) {
            try {
                int value = Integer.parseInt(part);
                if (value < 0 || value > 255) {
                    return false;
                }
            } catch (NumberFormatException e) {
                return false;
            }
        }
        return true;
    }
}
