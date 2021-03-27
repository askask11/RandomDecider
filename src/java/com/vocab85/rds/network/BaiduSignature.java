package com.vocab85.rds.network;


import cn.hutool.crypto.SecureUtil;
import java.io.UnsupportedEncodingException;
import java.security.NoSuchAlgorithmException;

/*
 * Author: jianqing
 * Date: Mar 12, 2021
 * Description: This document is created for
 */

/**
 *
 * @author jianqing
 */
public class BaiduSignature
{
    
    public static String computeForSignature(String appid, String query, String salt, String key)
    {
        String concatenatedString = appid+query+salt+key;
        return SecureUtil.md5(concatenatedString);
    }
    
    public static void main(String[] args) throws NoSuchAlgorithmException, UnsupportedEncodingException
    {
        System.out.println(computeForSignature("2015063000000001", "apple", "1435660288", "12345678"));
    }
}
