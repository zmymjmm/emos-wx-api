package com.example.emos.wx.controller;

import com.example.emos.wx.common.util.R;
import com.example.emos.wx.form.TestSayHelloForm;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.apache.shiro.authz.annotation.Logical;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

/*
    @RestController是@ResponseBody和@Controller的组合注解。
    @Controller处理http请求
 */
/*
    @RequestMapping与@GetMapping的区别：
    @RequestMapping可以指定GET、POST请求方式。
    @GetMapping等价于@RequestMapping的GET请求方式，@GetMapping【组合注解】相当于@RequestMapping(method = RequestMethod.GET)的缩写。
    eg:
        @RequestMapping(value = "/get/{id}", method = RequestMethod.GET)
        可简化为：
        @GetMapping("/get/{id}")
 */
@RestController
@RequestMapping("/test")
@Api("测试web接口")                   //用在类上，说明该类的作用
public class TestController {

    @PostMapping("/sayHello")
    @ApiOperation("最简单的测试方法")  //注解来给API增加方法说明，可以将方法添加到Swagger页面
//    public R sayHello(){
//        return R.ok().put("message","HelloWorld");
//    }
    public R sayHello(@Valid @RequestBody TestSayHelloForm form){
        return R.ok().put("message","Hello,"+form.getName());
    }

    @PostMapping("/addUser")
    @ApiOperation("添加用户")
    @RequiresPermissions(value = {"ROOT","USER:ADD"},logical = Logical.OR) //或关系，"ROOT","USER:ADD"这两个权限满足一个即可
    public R addUser(){
        return R.ok("用户添加成功");
    }
}
