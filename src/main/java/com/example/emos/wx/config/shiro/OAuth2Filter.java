package com.example.emos.wx.config.shiro;

import cn.hutool.core.util.StrUtil;
import com.auth0.jwt.exceptions.JWTDecodeException;
import com.auth0.jwt.exceptions.TokenExpiredException;
import org.apache.http.HttpStatus;
import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.web.filter.authc.AuthenticatingFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Scope;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

@Component
/*
    @Scope("prototype")表明这个OAuth2Filter类是多例对象，否则默认单例对象
 */
@Scope("prototype")
public class OAuth2Filter extends AuthenticatingFilter {
    @Autowired
    private ThreadLocalToken threadLocalToken;

    @Value("${emos.jwt.expire}")
    private int cacheExpire;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private RedisTemplate redisTemplate;

    /*
        拦截请求后，将令牌字符串封装为对象
     */
    @Override
    protected AuthenticationToken createToken(ServletRequest servletRequest, ServletResponse servletResponse) throws Exception {
        HttpServletRequest req =(HttpServletRequest) servletRequest;
        String token = getRequestToken(req);
        /*
            需要判断token是否为空，为空返回空值，否则封装为OAuth2Token(是AuthenticationToken的实现类)
         */
        if(StrUtil.isBlank(token)){
            return null;
        }
        return new OAuth2Token(token);
    }

    @Override
    protected boolean isAccessAllowed(ServletRequest request, ServletResponse response, Object mappedValue) {
        HttpServletRequest req =(HttpServletRequest) request;
        //req.getMethod()得到req的请求类型,然后判断是不是options类型
        //不是options类型请求，由shiro框架处理；是的话就不用
        if(req.getMethod().equals(RequestMethod.OPTIONS.name())){
            return true;
        }
        return false;
    }

    @Override
    protected boolean onAccessDenied(ServletRequest servletRequest, ServletResponse servletResponse) throws Exception {
        HttpServletRequest req = (HttpServletRequest) servletRequest;
        HttpServletResponse resp = (HttpServletResponse) servletResponse;
        resp.setContentType("text/html");
        resp.setCharacterEncoding("UTF-8");
        //允许跨域请求
        resp.setHeader("Access-Control-Allow-Credentials","true");
        resp.setHeader("Access-Control-Allow-Orign",req.getHeader("Origin"));
        threadLocalToken.clear();
        String token = getRequestToken(req);
        if(StrUtil.isBlank(token)){
            resp.setStatus(HttpStatus.SC_UNAUTHORIZED);
            resp.getWriter().print("无效的令牌");       //获得流并写入东西
            return false;
        }
        try{
            jwtUtil.verifierToken(token);
        }catch(TokenExpiredException e){               //令牌过期异常
            //客户端令牌过期，服务端令牌还没过期；需要进行令牌的刷新，生成一个新的令牌。新的令牌redis要存，threadLocal也要存
            if(redisTemplate.hasKey(token)){
                redisTemplate.delete(token);
                int userId = jwtUtil.getUserId(token); //从老令牌获得userId
                token = jwtUtil.createToken(userId);   //生成新令牌
                redisTemplate.opsForValue().set(token,userId+"",cacheExpire, TimeUnit.DAYS);//TimeUnit.DAYS设置单位为天数
                threadLocalToken.setToken(token);
            }
            else{
                //客户端令牌过期，服务器令牌也过期了，需要用户重新登录
                resp.setStatus(HttpStatus.SC_UNAUTHORIZED);
                resp.getWriter().print("令牌已过期");    //获得流并写入东西
                return false;
            }
        }catch (Exception e){                 //令牌内容不对
            resp.setStatus(HttpStatus.SC_UNAUTHORIZED);
            resp.getWriter().print("无效的令牌");
            return false;
        }
        //令牌一点问题没有,调用executeLogin方法让shiro去调用realm类，间接执行realm类
        boolean bool = executeLogin(servletRequest,servletResponse);
        return bool;  //返回false，认证与授权失败
    }

    //认证失败
    @Override
    protected boolean onLoginFailure(AuthenticationToken token, AuthenticationException e, ServletRequest request, ServletResponse response) {
        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse resp = (HttpServletResponse) response;
        resp.setStatus(HttpStatus.SC_UNAUTHORIZED);
        resp.setContentType("application/json;charset=utf-8");
        //允许跨域请求
        resp.setHeader("Access-Control-Allow-Credentials","true");
        resp.setHeader("Access-Control-Allow-Orign",req.getHeader("Origin"));
        try{
            resp.getWriter().print(e.getMessage());
        }catch(IOException exception){

        }
        return super.onLoginFailure(token, e, request, response);
    }

    @Override
    public void doFilterInternal(ServletRequest request, ServletResponse response, FilterChain chain) throws ServletException, IOException {
        super.doFilterInternal(request, response, chain);
    }

    private String getRequestToken(HttpServletRequest request){
        //从header获取token
        String token = request.getHeader("token");
        //如果header没有token，则从参数获取token
        if(StrUtil.isBlank(token)){
            token = request.getParameter("token");
        }
        return token;
    }


}
