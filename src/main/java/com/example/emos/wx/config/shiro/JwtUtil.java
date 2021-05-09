package com.example.emos.wx.config.shiro;

import cn.hutool.core.date.DateField;
import cn.hutool.core.date.DateUtil;
import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTCreator;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Date;

@Component
@Slf4j
/*
    根据过期时间expire、密钥scret、用户userId生成令牌token
 */
public class JwtUtil {
    /*
        @Value(“#{}”)表示SpEl表达式通常用来获取bean的属性，或者调用bean的某个方法。当然还有可以表示常量
        @Value(“${}”)从配置文件(application.yml)读取值的用法
     */
    @Value("${emos.jwt.scret}")
    private String scret;

    @Value("${emos.jwt.expire}")
    private int expire;

    public String createToken(int userId){
        Date date = DateUtil.offset(new Date(), DateField.DAY_OF_YEAR, expire); //在当前日期偏移expire的天数，即5天
        Algorithm algorithm = Algorithm.HMAC256(scret);                         //对scret进行加密
        JWTCreator.Builder builder = JWT.create();
        String token = builder.withClaim("userId", userId).withExpiresAt(date).sign(algorithm); //通过这几个方法把过期时间expire、密钥scret、用户userId生成令牌token
        return token;
    }

    public int getUserId(String token){
        DecodedJWT jwt = JWT.decode(token);
        int userId = jwt.getClaim("userId").asInt();
        return userId;
    }

    /*
        验证令牌有效性
     */
    public void verifierToken(String token){
        Algorithm algorithm = Algorithm.HMAC256(scret);
        JWTVerifier verifier = JWT.require(algorithm).build(); //调用build方法创建验证对象
        verifier.verify(token);                                //如果令牌内容正确且没有过期，不会抛出异常；否则抛出Runtime异常（不用捕获）
    }


}
