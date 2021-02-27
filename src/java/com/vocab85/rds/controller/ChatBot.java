/*
 * Author: jianqing
 * Date: Jan 29, 2021
 * Description: This document is created for
 */
package com.vocab85.rds.controller;

import cn.hutool.core.util.RandomUtil;
import cn.hutool.crypto.digest.DigestAlgorithm;
import cn.hutool.crypto.digest.Digester;
import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import cn.hutool.http.HttpUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import cn.hutool.setting.Setting;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;
import java.util.SortedSet;
import java.util.TreeSet;

/**
 *
 * @author jianqing
 */
public class ChatBot
{

    
    private String sessionId;

    public ChatBot()
    {
        sessionId = RandomUtil.randomNumbers(4);
    }

    public void setSessionId(String sessionId)
    {
        this.sessionId = sessionId;
    }

    public String getSessionId()
    {
        return sessionId;
    }

    public static final long getTimeStampSec()
    {
        return System.currentTimeMillis() / 1000;
    }

    /**
     * Generate the parameter string for the request URL.
     *
     * @param params The URL parameter
     * @return The parameter part of the request URL.
     */
    public static String getParameterString(Map<? extends String, ? extends Object> params,String apikey) throws UnsupportedEncodingException
    {
        //Setting setting = new Setting("chatbot.setting");
        //create keys instance
        SortedSet<String> keys = getSortedKey(params);
        String paramsString = "";
        for (String key : keys)
        {
            //System.out.println(key);
            paramsString += key + "=" + URLEncoder.encode((String) params.get(key), "UTF-8") + "&";
        }
        //append the last parameter
        paramsString += "app_key=" + apikey;
        return paramsString;
    }

    /**
     * Compute for a MD5 Signature.
     *
     * @param toSign The string to sign
     * @return The signature result.
     */
    public static String sign(String toSign)
    {
        Digester md5 = new Digester(DigestAlgorithm.MD5);
        String digestHex = md5.digestHex(toSign);
        return digestHex.toUpperCase();
    }

    /**
     * Send a message to the chatbot, wait for response.
     *
     * @param message
     * @param sessionId
     * @return
     * @throws java.io.UnsupportedEncodingException
     */
    public static JSONObject chat(String message, String sessionId) throws UnsupportedEncodingException
    {
        return getChatJSON(message,sessionId);
        
    }
    
    public static JSONObject getChatJSON(String message, String sessionId) throws UnsupportedEncodingException
    {
        
        Setting setting = new Setting("chatbot.setting");
        String key = setting.getStr("key");
        HashMap<String, Object> params = new HashMap<>();
        params.put("app_id", setting.get("id"));
        params.put("time_stamp", Long.toString(getTimeStampSec()));
        params.put("nonce_str", RandomUtil.randomString(10));
        params.put("question", message);
        //params.put("sign", "");
        params.put("session", sessionId);

        params.put("sign", sign(getParameterString(params,key)));

        HttpRequest req = HttpUtil.createGet("https://api.ai.qq.com/fcgi-bin/nlp/nlp_textchat?" + getParameterString(params,key).replace("app_key=" + key, ""));
        //System.out.println(req);
        HttpResponse response = req.execute();

        return JSONUtil.parseObj(response.body());
    }

//    public String chat(String message) throws UnsupportedEncodingException
//    {
//        return chat(message, this.sessionId);
//    }

    /**
     *
     * @param map
     * @return
     */
    public static TreeSet getSortedKey(Map<? extends String, ?> map)
    {
        return new TreeSet<>(map.keySet());
    }

    public static void main(String[] args) throws UnsupportedEncodingException
    {
        ChatBot cb = new ChatBot();
        Scanner keyboard = new Scanner(System.in);
        boolean flag = true;
        System.out.println("欢迎来到森森聊天机器人！请给我起个名吧！");
        String name = keyboard.nextLine();
        System.out.println("好的，我就叫" + name);
        System.out.println("现在开始和我聊天吧，直接按回车可以退出哦！");
        String input;
        while (flag)
        {
            System.out.print(">>");
            input = keyboard.nextLine();
            if (input.isEmpty())
            {
                flag = false;
                input = "再见";
            }
            JSONObject jsonin = chat(input, cb.getSessionId());//JSONUtil.parseObj(cb.chat(input));
            if (jsonin.getInt("ret") == 0)
            {
                System.out.println(name + ":" + jsonin.get("data", JSONObject.class).getStr("answer"));
            } else
            {
                System.err.println("Oh no 出错了呜呜呜┭┮﹏┭┮");
                System.out.println(jsonin.toStringPretty());
            }
            //结束程序
        }
    }
}
