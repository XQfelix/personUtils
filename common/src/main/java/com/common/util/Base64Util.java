package com.common.util;

import sun.misc.BASE64Decoder;
import sun.misc.BASE64Encoder;

import java.util.Base64;


/**DIX工具类
 * @author GQ.Yin
 */
public class Base64Util {
 
 
    public static void main(String[] args) throws Exception {
        String str = "helloDix";
 
        System.out.println(base64(str));
        System.out.println(base64dec(base64(str)));
    }
 
 
    /**
     * Base64
     *加密
     */
    public static String base64(String str) {
        byte[] bytes = str.getBytes();
        //Base64 加密
        String encoded = Base64.getEncoder().encodeToString(bytes);
        return encoded;
    }
    
    /**
     * Base64
     *解密
     */
    
    public static String base64dec(String str) {
    	//Base64 解密
        byte[] decoded = Base64.getDecoder().decode(str);
 
        String decodeStr = new String(decoded);
        return decodeStr;
    }
 
    /**
     * BASE64加密解密
     */
    public static void enAndDeCode(String str) throws Exception {
        String data = encryptBASE64(str.getBytes());
        System.out.println("sun.misc.BASE64 加密后：" + data);
 
        byte[] byteArray = decryptBASE64(data);
        System.out.println("sun.misc.BASE64 解密后：" + new String(byteArray));
    }
 
    /**
     * BASE64解密
     * @throws Exception
     */
    public static byte[] decryptBASE64(String key) throws Exception {
        return (new BASE64Decoder()).decodeBuffer(key);
    }
 
    /**
     * BASE64加密
     */
    public static String encryptBASE64(byte[] key) throws Exception {
        return (new BASE64Encoder()).encodeBuffer(key);
    }
}