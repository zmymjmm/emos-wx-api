package com.example.emos.wx.form;

import com.example.emos.wx.common.util.R;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;

/*
    字段校验
    @NotNull:不能为null，但可以为empty
    @NotEmpty:不能为null，而且长度必须大于0
    @NotBlank:只能作用在String上，不能为null，而且调用trim()后，长度必须大于0  ***在使用@NotBlank等注解时，一定要和@valid一起使用，不然@NotBlank不起作用
    eg:
        https://zhuanlan.zhihu.com/p/54001828
 */
@ApiModel   //用在返回对象类上
@Data
public class TestSayHelloForm {
    //@NotBlank
    //@Pattern(regexp = "^[\\u4e00-\\u9fa5]{2,15}$") //使用正则表达式过滤，前面是汉字的Unicode范围，后面是汉字长度2-15
    @ApiModelProperty("姓名")   //用在出入参数对象的字段上
    private String name;
}
