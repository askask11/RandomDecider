/*
 * Author: jianqing
 * Date: Dec 16, 2020
 * Description: This document is created for
 */
package com.vocab85.rds.network;

import cn.hutool.core.exceptions.ExceptionUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.lang.UUID;
import cn.hutool.core.util.StrUtil;
import cn.hutool.crypto.digest.HMac;
import cn.hutool.crypto.digest.HmacAlgorithm;
import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import cn.hutool.http.HttpUtil;
import cn.hutool.http.Method;
import cn.hutool.setting.Setting;
import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.Base64;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author jianqing
 */
public class AliOSS
{
   
    public static String getDateGMT()
    {
        return getDateGMT(-8);
    }
    
    public static String getDateGMT(int adjust)
    {
        return (LocalDateTime.now().plusHours(adjust).atOffset(ZoneOffset.UTC).format(DateTimeFormatter.RFC_1123_DATE_TIME));
    }
    
    
    public static String generateOSSSignature(String secret, String method, String resource, String date)
    {
        String content = method + "\n"//HTTP METHOD
                + "\n"//Content-MD5
                + "\n"//Content-Type
                + date + "\n"//Date in GMT format
                + resource;//resource to access
        return generateOSSSignature(secret, content);
    }
    
    public static String generateOSSSignature(String secret, String content)
    {
        HMac encrypter = new HMac(HmacAlgorithm.HmacSHA1, secret.getBytes());
        return java.util.Base64.getEncoder().encodeToString(encrypter.digest(content));
    }
    
    public static String generateOSSAuthHeader(String accessKeyId, String secret,  String method, String resource, String date)
    {
        return "OSS " + accessKeyId + ":" + generateOSSSignature(secret, method, resource, date);
    }
    public static String generateOSSAuthHeader(String accessKeyId, String secret,  String method, String resource)
    {
        return generateOSSAuthHeader(accessKeyId, secret, method, resource, getDateGMT());
    }
    
    
    /**
     * Try to send aliyun OSS a request.
     */
    public static void trySendReqOSS()
    {
        Auth auth = new Auth();
        String authorization = generateOSSAuthHeader(auth.getId(), auth.getSecret(), "GET", "/xeduo/index.html");
        String date = getDateGMT();
        HttpRequest request = HttpUtil.createRequest(Method.GET, "http://xeduo.oss-cn-hongkong.aliyuncs.com/index.html");
        request.header("Authorization", authorization);
        request.header("Date",date);
        HttpResponse response = request.execute();
        System.out.println(request);
        System.out.println(response);
    }
    
    /**
     * Try to send aliyun OSS a reques
     * @param file.
     * @param aliyunAddress
     * @param pathtofile
     * @param contentType
     * @return 
     */
    public static HttpResponse uploadFileAliyun(File file, String aliyunAddress, String pathtofile, String contentType)
    {
       // String authorization = generateOSSAuthHeader(ACCESSKEY_ID, accessKeySecret, "POST", "/xeduo?uploads");
        String date = getDateGMT();
        Auth auth = new Auth();
        HttpRequest request = HttpUtil.createPost(aliyunAddress);
       // String policy;
        String policy = "{\"expiration\": \"2120-01-01T12:00:00.000Z\","
                + "\"conditions\": [{\"bucket\":\"xeduo\"}]}";
        String encodePolicy = new String(Base64.getEncoder().encode(policy.getBytes()));
        request.header("Date",date);
        //add form
        request.form("key", pathtofile+file.getName());
        request.form("OSSAccessKeyId", auth.getId());
        request.form("Policy", encodePolicy);//needs to be encoded as well.
        request.form("Signature", generateOSSSignature(auth.getSecret(), encodePolicy));
        request.form("Content-Disposition","attachment; filename="+file.getName());
        request.form("file", file);
        request.form("x-oss-meta-uuid",UUID.fastUUID());
        request.form("submit","Upload to OSS");
        if(!StrUtil.isBlank(contentType))
        {
            request.form("Content-Type",contentType);
        }
        HttpResponse response = request.execute();
        System.out.println(request);
        System.out.println(response);
        return response;
    }
    
    
    public static void logError(Throwable t)
    {
        String error = ExceptionUtil.stacktraceToString(t);
        File up;
        try
        {
            up = File.createTempFile("exception."+t.getStackTrace().length+".", ".txt");
            up = FileUtil.writeUtf8String(error, up);
            up.deleteOnExit();
            uploadFileAliyun(up, "http://xeduo.oss-cn-hongkong.aliyuncs.com/", "logs/tomcat/RandomDecider/","text/plain");
        } catch (IOException ex)
    {
            Logger.getLogger(AliOSS.class.getName()).log(Level.SEVERE, null, ex);
        }
        
    }
    
    public static void main(String[] args) throws UnsupportedEncodingException
    {
        try
        {
            Integer.parseInt("shabi");
        } catch (NumberFormatException e)
        {
            logError(e);
        }

    }
            
    
    
}

class Auth
{ 
   private final String id, secret;

    public Auth()
    {
        Setting setting = new Setting("oss.setting");
        this.id = setting.getStr("keyid");
        this.secret = setting.getStr("keypassword");
    }

    public String getId()
    {
        return id;
    }

    public String getSecret()
    {
        return secret;
    }
    
    
    
}
