package com.example.emos.wx.config.shiro;

import com.example.emos.wx.db.pojo.TbUser;
import com.example.emos.wx.service.UserService;
import org.apache.shiro.authc.*;
import org.apache.shiro.authz.AuthorizationInfo;
import org.apache.shiro.authz.SimpleAuthorizationInfo;
import org.apache.shiro.realm.AuthorizingRealm;
import org.apache.shiro.subject.PrincipalCollection;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Set;

/*
    @Component注解：把普通pojo就是普通JavaBeans实例化到spring容器中，相当于配置文件中的<bean id="" class=""/>
    一旦使用关于Spring的注解出现在类里，例如我在实现类中用到了@Autowired注解，被注解的这个类是从Spring容器中取出来的，那调用的实现类也需要被Spring容器管理，加上@Component
 */
@Component
public class OAuth2Realm extends AuthorizingRealm {

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private UserService userService;

    @Override
    public boolean supports(AuthenticationToken token) {
        return token instanceof OAuth2Token;
    }

    @Override//授权方法(验证权限时调用）Note:已经登录成功
    protected AuthorizationInfo doGetAuthorizationInfo(PrincipalCollection collection) {
        TbUser user =  (TbUser) collection.getPrimaryPrincipal();
        int userId = user.getId();
        //查询用户的权限列表
        Set<String> permsSet = userService.searchUserPermissions(userId);
        //把权限列表添加到info授权对象中
        SimpleAuthorizationInfo info = new SimpleAuthorizationInfo();
        info.setStringPermissions(permsSet);
        return info;
    }

    @Override//认证方法（验证登录时调用）
    protected AuthenticationInfo doGetAuthenticationInfo(AuthenticationToken token) throws AuthenticationException {
        //从token对象获得token字符串
        String accessToken = (String) token.getPrincipal();
        //从令牌中获取userId
        int userId = jwtUtil.getUserId(accessToken);
        TbUser user = userService.searchById(userId);
        //用户离职，status=0，查询user为null
        if(user==null){
            throw new LockedAccountException("账号已被锁定，请联系管理员");
        }
        //往info认证对象中添加用户信息、Token字符串;getName得到当前realm的名称
        SimpleAuthenticationInfo info = new SimpleAuthenticationInfo(user,accessToken,getName());
        return info;
    }
}

