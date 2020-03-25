package com.common.util;


import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Map;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.Claim;
import com.auth0.jwt.interfaces.DecodedJWT;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DIToken {
    private static Logger logger = LoggerFactory.getLogger(DIToken.class);

    /**
     * 根据用户名计算token
     * @param userName 用户名
     * @return
     */
    public static String createToken(String userName) {
        Date nowDate = new Date();
        Date expireDate = getAfterDate(nowDate, 0, 0, 0, 0, 30, 0); //30 min 有效
        Map<String, Object> map = new HashMap<String, Object>();
        map.put("alg", "HS256");
        map.put("typ", "JWT");
        Algorithm algorithm = Algorithm.HMAC256("personToken");
        String token = JWT.create().withHeader(map)
                .withClaim("user", userName)
                .withIssuer("DIC")
                .withSubject("dixToken")
                .withAudience("DIPanel")
                .withIssuedAt(nowDate)
                .withExpiresAt(expireDate)
                .sign(algorithm);
        return token;
    }

    /**
     * token校验
     * @param token
     * @return
     */
    public static boolean verifyToken(String token){
        try {
            Algorithm algorithm = Algorithm.HMAC256("personToken");
            JWTVerifier verifier = JWT.require(algorithm).withIssuer("DIC").build(); // Reusable verifier instance
            DecodedJWT jwt = verifier.verify(token);
            Map<String, Claim> claims = jwt.getClaims();
            Claim claim = claims.get("user");
            logger.info("Current operating user: {}", claim.asString());
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * 返回一定时间后的日期
     * @param date 开始计时的时间
     * @param year 增加的年
     * @param month 增加的月
     * @param day 增加的日
     * @param hour 增加的小时
     * @param minute 增加的分钟
     * @param second 增加的秒
     * @return
     */
    private static Date getAfterDate(Date date, int year, int month, int day, int hour, int minute, int second){
        if(date == null){
            date = new Date();
        }
        Calendar cal = new GregorianCalendar ();
        cal.setTime(date);
        if(year != 0){
            cal.add(Calendar.YEAR, year);
        }
        if(month != 0){
            cal.add(Calendar.MONTH, month);
        }
        if(day != 0){
            cal.add(Calendar.DATE, day);
        }
        if(hour != 0){
            cal.add(Calendar.HOUR_OF_DAY, hour);
        }
        if(minute != 0){
            cal.add(Calendar.MINUTE, minute);
        }
        if(second != 0){
            cal.add(Calendar.SECOND, second);
        }
        return cal.getTime();
    }
}
