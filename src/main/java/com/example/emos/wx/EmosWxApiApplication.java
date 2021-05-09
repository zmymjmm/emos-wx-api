package com.example.emos.wx;

import cn.hutool.core.util.StrUtil;
import com.example.emos.wx.config.SystemConstants;
import com.example.emos.wx.db.dao.SysConfigDao;
import com.example.emos.wx.db.pojo.SysConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.web.servlet.ServletComponentScan;
import org.springframework.scheduling.annotation.EnableAsync;

import javax.annotation.PostConstruct;
import java.io.File;
import java.lang.reflect.Field;
import java.util.List;

@SpringBootApplication(exclude = {DataSourceAutoConfiguration.class})
/*
在SpringBootApplication上使用@ServletComponentScan注解后，
Servlet（控制器）、Filter（过滤器）、Listener（监听器）可以直接通过@WebServlet、@WebFilter、@WebListener注解自动注册到Spring容器中，无需其他代码。
 */
@ServletComponentScan
//因为要通过日志输出异常消息
@Slf4j
//加上@EnableAsync就可以使用多线程，@Async加在线程任务的方法上（需要异步执行的任务）
@EnableAsync
public class EmosWxApiApplication {

    @Autowired
    private SysConfigDao sysConfigDao;

    @Autowired
    private SystemConstants constants;

    @Value("${emos.image-folder}")
    private String imageFolder;

    public static void main(String[] args) {
        SpringApplication.run(EmosWxApiApplication.class, args);
    }

    /*
    @PostConstruct该注解被用来修饰一个非静态的void（）方法。被@PostConstruct修饰的方法会在服务器加载Servlet的时候运行，
    并且只会被服务器执行一次。PostConstruct在构造函数之后执行，init（）方法之前执行
    ----------------------------------------------------------------------------------------------------
    在spring项目中，在一个bean的初始化过程中，方法执行先后顺序为
    Constructor > @Autowired > @PostConstruct
    先执行完构造方法，再注入依赖，最后执行初始化操作，所以这个注解就避免了一些需要在构造方法里使用依赖组件的尴尬。
     */
    @PostConstruct
    public void init(){
        List<SysConfig> list = sysConfigDao.selectAllParam();
        list.forEach(one->{
            String key = one.getParamKey();
            //使用驼峰命名法，原始的key:attendance_start_time -> attendanceStartTime,跟封装类的属性名对应上
            key = StrUtil.toCamelCase(key);
            String value = one.getParamValue();
            /*
                java反射机制，参考：http://www.voidcn.com/article/p-hlwhhhcj-bqq.html
                field相当于constants对象的某个属性
             */
            try {
                Field field = constants.getClass().getDeclaredField(key);
                //往constants对象的某个属性放值
                field.set(constants,value);
            }catch(Exception e){
                log.error("执行异常", e);
            }
        });
        //有该文件夹就不动，没有就创建；imageFolder为存放签到照片临时文件夹
        new File(imageFolder).mkdirs();
    }
}
