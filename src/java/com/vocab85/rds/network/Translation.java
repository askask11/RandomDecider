/*
 * Author: jianqing
 * Date: Mar 12, 2021
 * Description: This document is created for
 */
package com.vocab85.rds.network;

import java.util.Arrays;

/**
 *
 * @author jianqing
 */
public class Translation
{

    private String from;
    private String to;
    private String error_msg;
    private String error_code;
    private TransResult[] trans_result;

    public Translation(String from, String to, String error_msg, String error_code, TransResult[] trans_result)
    {
        this.from = from;
        this.to = to;
        this.error_msg = error_msg;
        this.error_code = error_code;
        this.trans_result = trans_result;
    }

    public String getFrom()
    {
        return from;
    }

    public String getTo()
    {
        return to;
    }

    public void setFrom(String from)
    {
        this.from = from;
    }

    public void setTo(String to)
    {
        this.to = to;
    }

    public void setTrans_result(TransResult[] trans_result)
    {
        this.trans_result = trans_result;
    }

    public TransResult[] getTrans_result()
    {
        return trans_result;
    }

    public String getError_code()
    {
        return error_code;
    }

    public String getError_msg()
    {
        return error_msg;
    }

    public void setError_code(String error_code)
    {
        this.error_code = error_code;
    }

    public void setError_msg(String error_msg)
    {
        this.error_msg = error_msg;
    }

    public boolean isOk()
    {
        return error_code == null;
    }

    public TransResult getTheResult()
    {

        return trans_result[0];

    }

    @Override
    public String toString()
    {
        return "Translation{" + "from=" + from + ", to=" + to + ", error_msg=" + error_msg + ", error_code=" + error_code + ", trans_result=" + Arrays.toString(trans_result) + '}';
    }

}
