package com.example.emos.wx.config.shiro;

import org.apache.shiro.authc.AuthenticationToken;

/*
    封装类
    客户端提交的token不能直接交给shiro框架，需要先封装成AuthenticationToken类型的对象
 */
public class OAuth2Token implements AuthenticationToken {

    private String token;
    /*
        构造器，对变量赋值
     */
    public OAuth2Token(String token){
        this.token = token;
    }

    @Override
    public Object getPrincipal() {
        return token;
    }

    @Override
    public Object getCredentials() {
        return token;
    }
}
