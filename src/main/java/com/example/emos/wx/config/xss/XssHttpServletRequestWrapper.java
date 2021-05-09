package com.example.emos.wx.config.xss;

import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HtmlUtil;
import cn.hutool.json.JSONUtil;

import javax.servlet.ReadListener;
import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import java.io.*;
import java.nio.charset.Charset;
import java.util.LinkedHashMap;
import java.util.Map;

public class XssHttpServletRequestWrapper extends HttpServletRequestWrapper {

    public XssHttpServletRequestWrapper(HttpServletRequest request) {
        super(request);
    }

    @Override
    public String getParameter(String name) {
        String value = super.getParameter(name);
        //StrUtil.hasEmpty判断字符串是否为空，为空返回true
        if(!StrUtil.hasEmpty(value)){
            //HtmlUtil.filter 过滤HTML文本，防止XSS攻击
            value = HtmlUtil.filter(value);
        }
        return value;
    }

    @Override
    public String[] getParameterValues(String name) {
        //return super.getParameterValues(name);     这是未经过转义的，返回的是String[]，需要对其中的每个字符转义后再返回
        String[] values = super.getParameterValues(name);
        if(values != null){
            for(int i=0; i< values.length; i++){
                String value = values[i];
                value = HtmlUtil.filter(value);
                values[i] = value;
            }
        }
        return values;
    }

    @Override
    public Map<String, String[]> getParameterMap() {
        //return super.getParameterMap();
        Map<String,String[]> parameters = super.getParameterMap();
        //如果用HashMap，里面的数据是乱序的；用LinkedHashMap，可以保证插入的顺序
        LinkedHashMap<String,String[]> map = new LinkedHashMap<>();
        if(parameters != null){
            for(String key:parameters.keySet()){      //keySet()获取key集合对象
                String[] values = parameters.get(key);
                for(int i=0; i < values.length; i++ ){
                    String value = values[i];
                    if(!StrUtil.hasEmpty(value)){
                        value = HtmlUtil.filter(value);
                    }
                    values[i] = value;
                }
                map.put(key,values);
            }
        }
        return map;
    }

    @Override
    public String getHeader(String name) {
        //return super.getHeader(name);
        String value = super.getHeader(name);
        if(!StrUtil.hasEmpty(value)){
            value = HtmlUtil.filter(value);
        }
        return value;
    }

    /*
        InputStream: 是所有字节输入流的超类，一般使用它的子类：FileInputStream等，它能输出字节流；
        InputStreamReader: 是字节流与字符流之间的桥梁，能将字节流输出为字符流，并且能为字节流指定字符集，可输出一个个的字符；
        BufferedReader: 提供通用的缓冲方式文本读取，readLine读取一个文本行， 从字符输入流中读取文本，缓冲各个字符，从而提供字符、数组和行的高效读取。
     */
    /*
        String: 在String类中没有用来改变已有字符串中的某个字符的方法，由于不能改变一个java字符串中的某个单独字符，所以在JDK文档中称String类的对象是不可改变的。
        当对字符串进行修改的时候，特别是字符串对象经常改变的情况下，需要使用StringBuffer和StringBuilder类。
        StringBuilder相较于StringBuffer有速度优势，但是StringBuffer是线程安全的，StringBuilder不是线程安全的（不能同步访问）
     */
    @Override
    public ServletInputStream getInputStream() throws IOException {
        //return super.getInputStream();
        InputStream in = super.getInputStream();
        InputStreamReader reader = new InputStreamReader(in, Charset.forName("UTF-8"));
        BufferedReader buffer = new BufferedReader(reader);
        StringBuffer body = new StringBuffer();
        String line = buffer.readLine();
        while(line != null){
            body.append(line);
            line = buffer.readLine();
        }
        buffer.close();
        reader.close();
        in.close();
        Map<String,Object> map = JSONUtil.parseObj(body.toString());
        Map<String,Object> result = new LinkedHashMap<>();
        for(String key:map.keySet()){
            Object val = map.get(key);
            if(val instanceof String){     //如果val是String类型，对它进行转义；否则不用转义
                if(!StrUtil.hasEmpty(val.toString())){
                    result.put(key, HtmlUtil.filter(val.toString()));
                }
            }
            else{
                result.put(key,val);
            }
        }
        //将result转成json字符串，再放入io流；因为需要返回InputStream类型
        String json = JSONUtil.toJsonStr(result);
        ByteArrayInputStream bain = new ByteArrayInputStream(json.getBytes());

        return new ServletInputStream() {   //ServletInputStream抽象类，需要实现方法
            @Override
            public boolean isFinished() {
                return false;
            }

            @Override
            public boolean isReady() {
                return false;
            }

            @Override
            public void setReadListener(ReadListener readListener) {

            }

            @Override
            public int read() throws IOException {
                return bain.read();
            }
        };
    }
}
