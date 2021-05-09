package com.example.emos.wx.config;

import io.swagger.annotations.ApiOperation;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.service.ApiKey;
import springfox.documentation.service.AuthorizationScope;
import springfox.documentation.service.SecurityReference;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spi.service.contexts.SecurityContext;
import springfox.documentation.spring.web.plugins.ApiSelectorBuilder;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

import java.util.ArrayList;
import java.util.List;

@Configuration
@EnableSwagger2
public class SwaggerConfig {
    @Bean
    //@Bean使用说明: 这个注解主要用在方法上，声明当前方法体中包含了最终产生bean实例的逻辑，方法的返回值是一个Bean，这个bean会被Spring加入到容器中进行管理。
    public Docket createRestApi(){
        Docket docket = new Docket(DocumentationType.SWAGGER_2);

        //ApiInfoBuilder用于在Swagger页面添加各种信息
        ApiInfoBuilder builder = new ApiInfoBuilder();
        builder.title("EMOS在线办公系统");
        ApiInfo apiInfo = builder.build();
        docket.apiInfo(apiInfo);

        //ApiSelectorBuilder设置哪些类的方法可以设置到REST API中
        ApiSelectorBuilder selectorBuilder = docket.select();
        selectorBuilder.paths(PathSelectors.any());         //所有包下的类

        //使用@ApiOperation的方法会被提取到REST API中
        selectorBuilder.apis(RequestHandlerSelectors.withMethodAnnotation(ApiOperation.class));
        docket = selectorBuilder.build();

        /*下面是开启对JWT的支持，当用户用Swagger调用受JWT认证保护的方法
           必须要先提交参数，例如令牌
        */
        List<ApiKey> apikey = new ArrayList();
        //规定用户需要输入什么参数
        apikey.add(new ApiKey("token","token","header"));
        docket.securitySchemes(apikey);

        //用户JWT认证通过，则在Swagger中全局有效
        AuthorizationScope scope = new AuthorizationScope("global","accessEverything");
        AuthorizationScope[] scopeArray = {scope};

        //存储令牌和作用域
        SecurityReference reference = new SecurityReference("token",scopeArray);
        List refList = new ArrayList();
        refList.add(reference);
        SecurityContext context = SecurityContext.builder().securityReferences(refList).build();
        List cxtList = new ArrayList();
        cxtList.add(context);
        docket.securityContexts(cxtList);

        return docket;
    }


}
