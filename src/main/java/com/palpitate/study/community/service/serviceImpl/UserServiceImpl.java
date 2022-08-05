package com.palpitate.study.community.service.serviceImpl;

import com.palpitate.study.community.entity.LoginTicket;
import com.palpitate.study.community.entity.User;
import com.palpitate.study.community.mapper.LoginTicketMapper;
import com.palpitate.study.community.mapper.UserMapper;
import com.palpitate.study.community.service.UserService;
import com.palpitate.study.community.util.CommunityConstant;
import com.palpitate.study.community.util.CommunityUtil;
import com.palpitate.study.community.util.MailClient;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

/**
 * @author Palpitate Yzr
 */
@Service
public class UserServiceImpl implements UserService , CommunityConstant {
    @Autowired
    private UserMapper userMapper;

    @Autowired
    private MailClient mailClient;

    @Autowired
    private TemplateEngine templateEngine;

    @Autowired
    private LoginTicketMapper loginTicketMapper;

    @Value("${community.path.domain}")
    private String domain;

    @Value("${server.servlet.context-path}")
    private String contextPath;

    public User findUserById(int id){
        return userMapper.selectById(id);
    }

    public Map<String,Object> register(User user){
        HashMap<String, Object> map = new HashMap<>();
        //空值判断
        if(user==null){
            throw new IllegalArgumentException("参数不能为空");
        }
        if(StringUtils.isBlank(user.getUsername())){
            map.put("usernameMsg","账号不能为空");
            return map;
        }
        if(StringUtils.isBlank(user.getPassword())){
            map.put("passwordMsg","账号不能为空");
            return map;
        }

        if(StringUtils.isBlank(user.getEmail())){
            map.put("emailMsg","邮箱不能为空");
            return map;
        }
        //验证账号
        User u = userMapper.selectByName(user.getUsername());
        if(u!=null){
            map.put("usernameMsg","该账号已存在");
            return map;
        }
        //验证邮箱
        u = userMapper.selectByName(user.getEmail());
        if(u!=null){
            map.put("emailMsg","该邮箱已被注册");
            return map;
        }
        /*注册用户*/
        //生成随机字符串对密码加密,要5位
        user.setSalt(CommunityUtil.generateUUID().substring(0,5));
        //随机字符+真密码=注册数据库里的密码
        user.setPassword(CommunityUtil.md5(user.getPassword()+user.getSalt()));
        //普通用户，状态为未激活
        user.setType(0);
        user.setStatus(0);
        //设置激活码,随机生成的字符
        user.setActivationCode(CommunityUtil.generateUUID());
        //设置随机头像0-1000  https://images.nowcoder.com/head/1t.png
        user.setHeaderUrl(String.format("https://images.nowcoder.com/head/%dt.png",new Random().nextInt(1000)));
        user.setCreateTime(new Date());
        userMapper.insertUser(user);

        /*
         * 发送激活邮件 html邮件  模版 带连接，
         * */

        //创建对象携带变量，带上email，
        Context context=new Context();
        context.setVariable("email",user.getEmail());
        //设置url域名+项目名+激活功能+id+激活码
        //https://localhost:8080/community/activation/101/code
        String url=domain+contextPath+"/activation/"+user.getId()+"/"+user.getActivationCode();
        context.setVariable("url",url);
        //利用模版引擎生成邮件的内容
        String content = templateEngine.process("/mail/activation", context);

        //邮件客户端发送邮件
        mailClient.sendMail(user.getEmail(),"激活帐号",content);
        return map;
    }


    //激活
     public int activation(int userId, String code){
        User user = userMapper.selectById(userId);
        if(user.getStatus()==1){
            return ACTIVATION_REPEAT;
        }else if(user.getActivationCode().equals(code)){
            userMapper.updateStatus(userId,1);
            return ACTIVATION_SUCCESS;
        }else {
            return ACTIVATION_FAILED;
        }
    }



    public Map<String,Object> login(String username,String password,int expiredSeconds){
        Map<String,Object> map=new HashMap<>();

        //空值判断
        if(StringUtils.isBlank(username)){
            map.put("usernameMsg","账号不能为空！");
            return map;
        }
        if(StringUtils.isBlank(password)){
            map.put("passwordMsg","密码不能为空！");
            return map;
        }
        //输入值合法性判断
        /*先根据帐号去查询，无就返回错误，再判断账号是否被激活，再判断密码是否正确，*/
        User user = userMapper.selectByName(username);
        if(user==null){
            map.put("usernameMsg","账号不存在！");
            return map;
        }
        if(user.getStatus()==0){
            map.put("usernameMsg","账号未激活！");
            return map;
        }
        password = CommunityUtil.md5(password + user.getSalt());
        if(!password.equals(user.getPassword())){
            map.put("passwordMsg","密码错误！");
            return map;
        }

        //验证通过，生成登录凭证，设置凭证信息，存入到数据库中
        LoginTicket loginTicket=new LoginTicket();
        loginTicket.setUserId(user.getId());
        loginTicket.setStatus(0); //0有效
        loginTicket.setTicket(CommunityUtil.generateUUID());
        loginTicket.setExpired(new Date(System.currentTimeMillis()+expiredSeconds* 1000L));
        loginTicketMapper.insertLoginTicket(loginTicket);
        //将凭证存入到redis里面
/*
        String ticketKey = RedisKeyUtil.getTicketKey(loginTicket.getTicket());
        redisTemplate.opsForValue().set(ticketKey,loginTicket);
*/


        //将生成的登录的凭证，存到map中
        map.put("ticket",loginTicket.getTicket());

        return map;
    }
}
