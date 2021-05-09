package com.example.emos.wx.task;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Scope;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Component
@Scope("prototype")
public class EmailTask {
    @Autowired
    private JavaMailSender javaMailSender;

    @Value("${emos.email.system}")
    private String mailbox;

    //主类加上@EnableAsync就可以使用多线程，@Async加在线程任务的方法上（需要异步执行的任务）
    @Async
    public void sendAsync(SimpleMailMessage message){
        message.setFrom(mailbox);
        //将邮件抄送给自己（发件人），因为邮件可能被SMTP服务器认定为垃圾邮件
        message.setCc(mailbox);
        javaMailSender.send(message);
    }
}
