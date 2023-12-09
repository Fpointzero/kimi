package xyz.fpointzero.android.utils;

import android.annotation.SuppressLint;

import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.LocalDate;
import java.util.Date;

public class DateUtil {
    /**
     * @param timestamp 时间戳
     * @return 得到 Y-M-D 类型
     */
    public static String toYMD(long timestamp) {
        // 将时间戳转换成毫秒数
        long millis = timestamp * 1000;
        // 使用 Date 对象来格式化毫秒数
        Date date = new Date(millis);
        // 获取年月日
        @SuppressLint("SimpleDateFormat") SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        return sdf.format(date);
    }
}
