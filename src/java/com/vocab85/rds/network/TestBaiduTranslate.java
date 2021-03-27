package com.vocab85.rds.network;

import cn.hutool.core.util.RandomUtil;
import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import cn.hutool.http.HttpUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import cn.hutool.setting.Setting;

/*
 * Author: jianqing
 * Date: Mar 12, 2021
 * Description: This document is created for
 */
/**
 * Utilize baidu translate API.
 *
 * @author jianqing
 */
public class TestBaiduTranslate
{

    final static String SCHEME = "https://fanyi-api.baidu.com/api/trans/vip/translate";
    final static Setting SETTING = new Setting("baidut.setting");

    public static String getTranslationJSON(String words, String origin, String target)
    {
        HttpRequest request = HttpUtil.createPost(SCHEME);
        HttpResponse response;

        String salt = RandomUtil.randomNumbers(10);
        String appid = SETTING.get("id");
        String secret = SETTING.get("secret");
        String sign = BaiduSignature.computeForSignature(appid, words, salt, secret);
        //put parameters
        request.form("q", words);
        request.form("from", origin);
        request.form("to", target);
        request.form("appid", appid);
        request.form("salt", salt);
        request.form("sign", sign);
        response = request.execute();
        return response.body();

    }

    public static Translation getTranslationObj(String words, String origin, String target)
    {
        JSONObject responseJSON;
        responseJSON = JSONUtil.parseObj(getTranslationJSON(words, origin, target));
        //System.out.println(responseJSON);
        return responseJSON.toBean(Translation.class);
    }

    public static String getSourceLanguageJSONStr(String words)
    {
        HttpRequest request = HttpUtil.createPost("https://fanyi-api.baidu.com/api/trans/vip/language");
        String salt = RandomUtil.randomNumbers(10);
        String appid = SETTING.get("id");
        String secret = SETTING.get("secret");
        String sign = BaiduSignature.computeForSignature(appid, words, salt, secret);
        request.form("q", words);
        request.form("appid", appid);
        request.form("salt", salt);
        request.form("sign", sign);
        HttpResponse response = request.execute();
        return response.body();
    }
    
    public static void main(String[] args)
    {
        System.out.println(getSourceLanguageJSONStr("你好"));
    }
}
