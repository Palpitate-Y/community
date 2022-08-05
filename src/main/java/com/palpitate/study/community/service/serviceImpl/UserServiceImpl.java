package com.palpitate.study.community.service.serviceImpl;

import com.palpitate.study.community.entity.User;
import com.palpitate.study.community.mapper.UserMapper;
import com.palpitate.study.community.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.security.PrivateKey;

/**
 * @author Palpitate Yzr
 */
@Service
public class UserServiceImpl implements UserService {
    @Autowired
    private UserMapper userMapper;

    public User findUserById(int id){
        return userMapper.selectById(id);
    }
}
