package com.palpitate.study.community.controller;

import com.palpitate.study.community.entity.DiscussPost;
import com.palpitate.study.community.entity.Page;
import com.palpitate.study.community.entity.User;
import com.palpitate.study.community.service.DiscussPostService;
import com.palpitate.study.community.service.serviceImpl.UserServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Palpitate Yzr
 */
@Controller
public class HomeController{
    @Autowired
    private DiscussPostService discussPostService;
    @Autowired
    private UserServiceImpl userService;

    @RequestMapping("/homePage")
    public String getIndexPage(Model model, Page page){
        //方法调用前 springmvc 会自动实例化model 和page 并将page注入model
        //所以在thymeleaf中不需要再model.addA() 可以直接访问page
        page.setRows(discussPostService.findDiscussPostRows(0));
        page.setPath("/homePage");

        List<DiscussPost> list = discussPostService.findDiscussPosts(0, page.getOffset(), page.getLimit());
        List<Map<String,Object>> discussPosts=new ArrayList<>();
        if (list!=null){
            for (DiscussPost post : list) {
                Map<String,Object> map=new HashMap<>();
                map.put("post",post);
                User user = userService.findUserById(post.getUserId());
                map.put("user",user);
                discussPosts.add(map);
            }
        }
        model.addAttribute("discussPosts",discussPosts);
        return "homePage";
    }
}
