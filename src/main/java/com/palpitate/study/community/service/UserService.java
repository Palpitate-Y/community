package com.palpitate.study.community.service;

import com.palpitate.study.community.entity.User;

import java.util.Map;

public interface UserService {
    User findUserById(int id);
    Map<String,Object> register(User user);
    int activation(int userId, String code);
    Map<String,Object> login(String username,String password,int expiredSeconds);
}
