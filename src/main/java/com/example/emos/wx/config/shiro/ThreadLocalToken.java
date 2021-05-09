package com.example.emos.wx.config.shiro;

import org.springframework.stereotype.Component;

/*
    加上@Component注解，在spring框架其他java类就可以获得这个媒介类的引用
 */
@Component
/*
    为什么用ThreadLocal，见教程2-12
    ThreadLocal的作用主要是做数据隔离，填充的数据只属于当前线程，变量的数据对别的线程而言是相对隔离的，在多线程环境下，防止自己的变量被其它线程篡改。
 */
public class ThreadLocalToken {
    private ThreadLocal<String> local = new ThreadLocal<>();

    public void setToken(String token){
        local.set(token);
    }

    public String getToken(){
        return local.get();
    }

    public void clear(){
        local.remove();
    }
}
