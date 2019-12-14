package com.common.util;

import org.jasypt.encryption.StringEncryptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;


/**
 * @author GQ.Yin
 * @version 1.0
 * @title
 * @date 2019/12/14 11:06
 */
@ComponentScan({"com.ulisesbocchio.jasyptspringboot.*.**" })
public class JasyptEncUtil {
    public static String JASY_SALT = "felix@ygq";
    @Autowired
    private StringEncryptor stringEncryptor;

    @Bean
    private JasyptEncUtil getJasyptEncryptorMain(){
        return new JasyptEncUtil();
    }


    private String enc(String message){
        String result = stringEncryptor.encrypt(message);
        return result;
    }


    private String dec(String message){
        String result = stringEncryptor.decrypt(message);
        return result;
    }

    /**加密
     * @param input
     * @return
     */
    public static String encrypt(String input){
        System.setProperty("jasypt.encryptor.password", JasyptEncUtil.JASY_SALT);
        AnnotationConfigApplicationContext applicationContext = new AnnotationConfigApplicationContext(JasyptEncUtil.class);
        JasyptEncUtil jasyMain = (JasyptEncUtil)applicationContext.getBean("jasyptEncUtil");
        return jasyMain.enc(input);
    }

    /**解密
     * @param input
     * @return
     */
    public static String decrypt(String input){
        System.setProperty("jasypt.encryptor.password", JasyptEncUtil.JASY_SALT);
        AnnotationConfigApplicationContext applicationContext = new AnnotationConfigApplicationContext(JasyptEncUtil.class);
        JasyptEncUtil jasyMain = (JasyptEncUtil)applicationContext.getBean("jasyptEncUtil");
        return jasyMain.dec(input);
    }




    public static void main(String[] args) {
        String aa = "asdfasdf";
        System.out.println(JasyptEncUtil.encrypt(aa));
    }





}
