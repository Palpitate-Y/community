package com.palpitate.study.community.controller;

import com.google.code.kaptcha.Producer;
import com.palpitate.study.community.entity.User;
import com.palpitate.study.community.service.UserService;
import com.palpitate.study.community.util.CommunityConstant;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.imageio.ImageIO;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Map;

/**
 * @author Palpitate Yzr
 */
@Controller

public class LoginController implements CommunityConstant {

    private static final Logger logger= LoggerFactory.getLogger(LoginController.class);

    @Autowired
    private UserService userService;

    @Autowired
    private Producer kaptchaProducer;

    @Value("${server.servlet.context-path}")
    private String contextPath;

    @RequestMapping("/register")
    public String getRegisterPage(){
        return "/site/register";
    }

    @PostMapping("/register")
    public String register(Model model, User user){
        Map<String,Object> map= userService.register(user);
        //map为空表示注册成功
        if(map==null || map.isEmpty()){
            //注册成功后跳转到中间页面去，而不是登录页面，因为还要激活
            //设置提示信息，并跳转到中间页面
            model.addAttribute("msg","您的账号已经注册成功，我们已发送激活邮件，请及时激活！");
            model.addAttribute("target","/index");
            return "/site/operate-result";
        }else{
            //注册失败，回到注册页面，携带提示信息
            model.addAttribute("usernameMsg",map.get("usernameMsg"));
            model.addAttribute("passwordMsg",map.get("passwordMsg"));
            model.addAttribute("emailMsg",map.get("emailMsg"));
            return "/site/register";
        }
    }
    //激活
    //https://localhost:8080/community/activation/101/code
    @RequestMapping(path = "/activation/{userId}/{code}",method = RequestMethod.GET)
    public String Toactivation(Model model,
                               @PathVariable("userId")int userId,
                               @PathVariable("code")String code){
        //调用业务层去激活
        int res = userService.activation(userId, code);
        //查看返回结果与接口中的常量对比
        if(res==ACTIVATION_SUCCESS){
            //激活成功到中间页面，再到登录页面
            model.addAttribute("msg","您的账号已经成功激活！");
            model.addAttribute("target","/login");
        }else if(res==ACTIVATION_REPEAT){
            //重复激活，去首页
            model.addAttribute("msg","不可重复激活！");
            model.addAttribute("target","/index");
        }else{
            //激活失败也去首页
            model.addAttribute("msg","激活失败！");
            model.addAttribute("target","/index");
        }

        return "/site/operate-result";
    }

    @RequestMapping("/login")
    public String getLoginPage(){
        return "/site/login";
    }
    /*登录功能：之前的验证码在session中，登陆成功需要客户端cookie保存ticket，所以有response*/
    @RequestMapping(path = "/login",method = RequestMethod.POST)
    public String login(Model model,String username,String password,
                        String code,boolean remember,
            HttpSession session,HttpServletResponse response){
        //先判断验证码是否正确
        //先取出验证码，取出的验证码、传入的验证码为空，验证码是否相等，不区分大小写
        //从redis里面去验证码需要key，key需要随机凭证，所以加上注解从cookie中取出凭证@CookieValue
        String kaptcha= (String) session.getAttribute("kaptcha");
        //判断key是否有效（不为空没有过期）
/*        if(!StringUtils.isBlank(kaptchaOwner)){
            String redisKey=RedisKeyUtil.getKaptchaKey(kaptchaOwner);
            kaptcha = (String) redisTemplate.opsForValue().get(redisKey);
        }*/

        if(StringUtils.isBlank(code)|| StringUtils.isBlank(kaptcha) || !kaptcha.equalsIgnoreCase(code)){
            model.addAttribute("codeMsg","验证码不正确！");
            return "/site/login";//错误回到登录页面，提示信息
        }

        // 验证账号密码
        /*选择超时时间：勾选记住我，存储的时间长，不勾选时间短，定义常量时间*/
        int time=remember ? REMEMBER_EXPIRED_SECONDS : DEFAULT_EXPIRED_SECONDS;
        //调用业务层登录功能
        Map<String, Object> map = userService.login(username, password, time);

        //当map里面有ticket时，说明登陆成功，重定向到首页。
        if(map.containsKey("ticket")){
            // 客户端cookie存ticket
            Cookie cookie=new Cookie("ticket",map.get("ticket").toString());
            //设置cookie的生效路径，（在配置文件中写了，注入进来使用）
            cookie.setPath(contextPath);
            // 设置cookie的有效时间
            cookie.setMaxAge(time);
            //将cookie发送给浏览器
            response.addCookie(cookie);

            return "redirect:/index";
        }else {
            // 错误返回登录页面，将错误的消息带给登录页面，向model里面存信息
            model.addAttribute("usernameMsg",map.get("usernameMsg"));
            model.addAttribute("passwordMsg",map.get("passwordMsg"));

            return "/site/login";
        }

    }

    @RequestMapping("/kaptcha")
    public void getKaptcha(HttpServletResponse response, HttpSession session){
        //生成验证码
        String text = kaptchaProducer.createText();
        BufferedImage image = kaptchaProducer.createImage(text);

        //存入session
        session.setAttribute("kaptcha",text);

        //图片直接输出给浏览器（人工输出）
        //先声明返回的是什么格式
        response.setContentType("image/png");
        //获得输出流
        try {
            //使用工具输出，不用去手动关闭输出流，spring会管理
            OutputStream os = response.getOutputStream();
            ImageIO.write(image,"png",os);
        } catch (IOException e) {
            // 错误时输出日志
            logger.error("响应验证码失败："+e.getMessage());
        }
    }
}
