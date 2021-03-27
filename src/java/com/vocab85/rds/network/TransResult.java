/*
 * Author: jianqing
 * Date: Mar 12, 2021
 * Description: This document is created for
 */
package com.vocab85.rds.network;

/**
 * Bean class represent one "trans_result" Object.
 * @author jianqing
 */
public class TransResult
{
    private String dst;
    private String src;

    public String getDst()
    {
        return dst;
    }

    public void setDst(String dst)
    {
        this.dst = dst;
    }

    public String getSrc()
    {
        return src;
    }

    public void setSrc(String src)
    {
        this.src = src;
    }

    public TransResult()
    {
        this.dst = null;
        this.src = null;
    }

    public TransResult(String det, String src)
    {
        this.dst = det;
        this.src = src;
    }

    @Override
    public String toString()
    {
        return "TransResult{" + "det=" + dst + ", src=" + src + '}';
    }
    
    
}
