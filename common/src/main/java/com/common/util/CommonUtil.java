package com.common.util;


import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

public class CommonUtil {


    /**
     * 格式化时间格式
     *      yyyy-MM-dd HH:mm:ss
     *
     */
    public static String DATE_FORMAT_24H = "yyyy-MM-dd HH:mm:ss";



    /**
     * 将时间戳转为字符串
     *
     * @param time
     * @return
     */
    public static String conLong2Date(long time, String format) {

        if (format == null || (format = format.trim()).length() == 0) {
            format = "yyyy-MM-dd HH:mm:ss";
        }

        String returnValue = "";
        SimpleDateFormat sdf = new SimpleDateFormat(format);
        try {
            Date date = new Date(time);
            returnValue = sdf.format(date);
        } catch (Exception e) {

        }

        return returnValue;
    }

    public static Date formatDate (Object dateObject, String format) throws Exception {
        if (format == null || (format = format.trim()).length() == 0) {
            format = "yyyy-MM-dd HH:mm:ss";
        }


        SimpleDateFormat sdf = new SimpleDateFormat(format);
        Date date = new Date();

        String dateStr = "";

        if (dateObject == null || (dateStr = String.valueOf(dateObject).trim()).length() == 0) {
            return date;
        }

        if (dateObject instanceof  String) {
            date = sdf.parse(dateStr);
        } else if (dateObject instanceof Double) {
            long dateTime = (long) Double.parseDouble(dateStr);
            date =sdf.parse(conLong2Date(dateTime, format));
        } else if (dateObject instanceof Long) {
            date =sdf.parse(conLong2Date(Long.parseLong(dateStr), format));
        } else {
            throw new Exception("The expected time format is illegal!");
        }
        return date;
    }

    public static Map<String, String> convertWeekByDate(Date time) {
        Map<String, String> reMap = new HashMap<String, String>();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd"); // 设置时间格式
        Calendar cal = Calendar.getInstance();
        cal.setTime(time);
        // 判断要计算的日期是否是周日，如果是则减一天计算周六的，否则会出问题，计算到下一周去了
        int dayWeek = cal.get(Calendar.DAY_OF_WEEK);// 获得当前日期是一个星期的第几天
        if (1 == dayWeek) {
            cal.add(Calendar.DAY_OF_MONTH, -1);
        }
        cal.setFirstDayOfWeek(Calendar.MONDAY);// 设置一个星期的第一天，按中国的习惯一个星期的第一天是星期一
        int day = cal.get(Calendar.DAY_OF_WEEK);// 获得当前日期是一个星期的第几天
        cal.add(Calendar.DATE, cal.getFirstDayOfWeek() - day);// 根据日历的规则，给当前日期减去星期几与一个星期第一天的差值
        String imptimeBegin = sdf.format(cal.getTime());
        reMap.put("START", imptimeBegin);
        cal.add(Calendar.DATE, 6);
        String imptimeEnd = sdf.format(cal.getTime());
        return reMap;
    }

    public static int converMin2Hour(int min) {
        if (min % 60 == 0) {
            return min / 60;
        }
        return min / 60 + 1;
    }

    public static long conDate2Long(String time, String format) {
        long returnValue = 0L;
        try {
            SimpleDateFormat sdf = new SimpleDateFormat(format);
            Date dt = sdf.parse(time);
            returnValue = dt.getTime();
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return returnValue;
    }

    public static String timestampToStr(Timestamp timestamp, String format) {
        DateFormat df = new SimpleDateFormat(format);
        String str = df.format(timestamp);
        return str;
    }


}
