package com.common.util;


import org.apache.log4j.Logger;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;

public class CookiesUtil {

    private static Logger log = Logger.getLogger(CookiesUtil.class);

    public static final String LOGINPASSWORD_TOKEN = "u_token";
    public static final String ENABLE_USERS = "1";
    public static final String DEFAULTUSER = "userID";

    /**
     * 根据名字获取cookie
     *
     * @param request
     * @param name    cookie名字
     * @return
     */
    public static Cookie getCookieByName(HttpServletRequest request, String name) {
        Map<String, Cookie> cookieMap = ReadCookieMap(request);
        if (cookieMap.containsKey(name)) {
            Cookie cookie = (Cookie) cookieMap.get(name);
            return cookie;
        } else {
            return null;
        }
    }

    /**
     * 根据名字获取cookie值
     *
     * @param request
     * @param name    cookie名字
     * @return
     */
    public static String getCookieValueByName(HttpServletRequest request, String name) {
        Cookie cookie = getCookieByName(request, name);
        if (org.springframework.util.StringUtils.isEmpty(cookie)) {
            return "";
        } else {
            String value = cookie.getValue();
            return value;
        }
    }

    /**
     * 将cookie封装到Map里面
     *
     * @param request
     * @return
     */
    private static Map<String, Cookie> ReadCookieMap(HttpServletRequest request) {
        Map<String, Cookie> cookieMap = new HashMap<String, Cookie>();
        Cookie[] cookies = request.getCookies();
        if (null != cookies) {
            for (Cookie cookie : cookies) {
                cookieMap.put(cookie.getName(), cookie);
            }
        }
        return cookieMap;
    }

    /**
     * cookie封装
     * @return
     */
    public static Cookie setCookie(String key, String value, String domain, String path, Integer maxAge) {
        //设置cookie
        Cookie cookie = new Cookie(key, value);
        cookie.setDomain(domain);
        cookie.setPath(path != null ? path : "/");
        //30分钟过期  0 为删除，负数为关闭浏览器就失            loginCookie.setMaxAge(1800);//30分钟过期
        // 0 为删除，则表示该Cookie仅在本浏览器窗口以及本窗口打开的子窗口内有效，关闭窗口后该Cookie即失效负数为关闭浏览器就失效，默认-1， 正数保存磁盘效，默认-1， 正数保存磁盘
        cookie.setMaxAge(maxAge != null ? maxAge : -1);
        return cookie;
    }

    /**
     * cookie封装
     * @return
     */
    public static Cookie setCookie(String key, String value) {
        //设置cookie
        Cookie cookie = new Cookie(key, value);
        cookie.setMaxAge(-1);
        return cookie;
    }

}
