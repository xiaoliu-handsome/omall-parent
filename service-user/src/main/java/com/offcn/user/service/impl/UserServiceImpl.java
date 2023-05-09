package com.offcn.user.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.offcn.model.user.UserInfo;
import com.offcn.user.Mapper.UserInfoMapper;
import com.offcn.user.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;

@Service
public class UserServiceImpl implements UserService {
    // 调用mapper 层
    @Autowired
    private UserInfoMapper userInfoMapper;


    @Override
    public UserInfo login(UserInfo userInfo) {
        // select * from userInfo where userName = ? and passwd = ?
        // 注意密码是加密：
        String passwd = userInfo.getPasswd(); //123
        // 将passwd 进行加密
        String newPasswd = DigestUtils.md5DigestAsHex(passwd.getBytes());
        //获取mysql数据库
        QueryWrapper<UserInfo> queryWrapper = new QueryWrapper<>();
        //用前端传来的账号密码与数据库进行比较
        queryWrapper.eq("login_name", userInfo.getLoginName());
        queryWrapper.eq("passwd", newPasswd);
        UserInfo info = userInfoMapper.selectOne(queryWrapper);
        if (info != null) {

            return info;
        }
        return null;
    }

}
