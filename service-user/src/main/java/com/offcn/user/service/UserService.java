package com.offcn.user.service;

import com.offcn.model.user.UserInfo;

public interface UserService {
    /**
     * 登录方法
     * @param userInfo
     * @return
     */
    UserInfo login(UserInfo userInfo);
}
