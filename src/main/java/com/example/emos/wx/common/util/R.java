/*
    创建自定义异常类
 */
package com.example.emos.wx.common.util;

import org.apache.http.HttpStatus;
import java.util.HashMap;
import java.util.Map;

/*
    重写(override)是子类对父类的允许访问的方法的实现过程进行重新编写, 返回值和形参都不能改变。
    重载(overloading) 是在一个类里面，方法名字相同，而参数不同。返回类型可以相同也可以不同。
 */
public class R extends HashMap<String,Object> {

    public R(){
        put("code", HttpStatus.SC_OK); //HttpStatus.SC_OK代表状态码200，请求成功
        put("msg","success");
    }

    public R put(String key,Object value){
        super.put(key,value);          //重写Override，子类实现父类的方法
        return this;
    }

    public static R ok(String msg){
        R r = new R();
        r.put("msg",msg);
        return r;
    }

    public static R ok(Map<String,Object> map){
        R r = new R();
        r.putAll(map);                 //r是继承HashMap,调用HashMap的putAll方法
        return r;
    }

    public static R ok(){
        R r = new R();
        return r;
    }

    public static R error(int code,String msg){                   //表示错误的状态码有很多，如404，500
        R r = new R();
        r.put("code",code);
        r.put("msg",msg);
        return r;
    }

    public static R error(String msg){                            //重载Overload，使用上面的方法，只有一个参数msg，code默认为500
        return error(HttpStatus.SC_INTERNAL_SERVER_ERROR, msg);
    }

    public static R error(){
        return error(HttpStatus.SC_INTERNAL_SERVER_ERROR, "未知异常，请联系管理员");
    }

}
