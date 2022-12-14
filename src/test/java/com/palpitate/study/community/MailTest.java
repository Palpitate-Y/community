package com.palpitate.study.community;


import com.palpitate.study.community.util.MailClient;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

/**
 * @author xzzz2020
 * @version 1.0
 * @date 2021/12/7 15:57
 */
@RunWith(SpringRunner.class)
@SpringBootTest
@ContextConfiguration(classes = CommunityApplication.class)
public class MailTest {

    @Autowired
    private MailClient mailClient;

    @Autowired
    private TemplateEngine templateEngine;//模版引擎

    @Test
    public void testMail(){
        mailClient.sendMail("yzr15045062426@163.com","Test1","hello");

    }

    @Test
    public void testHtmlMail(){
        //生成动态网页
        Context context=new Context();
        context.setVariable("username","haohao");

        String content = templateEngine.process("/mail/demo", context);
        System.out.println(content);

        //发送邮件
        mailClient.sendMail("yzr15045062426@163.com","Test2",content);
    }
}
