/*
 * Author: jianqing
 * Date: Feb 1, 2021
 * Description: This document is created for
 */
package com.vocab85.rds.controller;

import cn.hutool.core.util.StrUtil;

/**
 *
 * @author jianqing
 */
public class User
{
    private String username;
    private String password;
    private int userId;
    private String token;
    private String email;
    private boolean active;

    public User()
    {
    }

    public boolean isActive()
    {
        return active;
    }

    public void setActive(boolean active)
    {
        this.active = active;
    }

    public String getEmail()
    {
        return email;
    }

    public void setEmail(String email)
    {
        this.email = email;
    }

    public String getUsername()
    {
        return username;
    }

    public void setUsername(String username)
    {
        this.username = username;
    }

    public String getPassword()
    {
        return password;
    }

    public void setPassword(String password)
    {
        this.password = password;
    }

    public int getUserId()
    {
        return userId;
    }

    public void setUserId(int userId)
    {
        this.userId = userId;
    }

    public String getToken()
    {
        return token;
    }

    public void setToken(String token)
    {
        this.token = token;
    }
    
    public boolean isReady()
    {
        return StrUtil.isAllNotEmpty(username,password,email);
    }

    @Override
    public String toString()
    {
        return "User{" + "username=" + username + ", password=" + password + ", userId=" + userId + ", token=" + token + ", email=" + email + ", active=" + active + '}';
    }
    
}
