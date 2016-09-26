package com.feige.notepad;

import cn.bmob.v3.BmobObject;

/**
 * Created by Administrator on 2016/9/22 0022.
 * 用户登录注册表
 */
public class User extends BmobObject {
    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getUserpassword() {
        return userpassword;
    }

    public void setUserpassword(String userpassword) {
        this.userpassword = userpassword;
    }


    private String username;
    private String userpassword;
}
