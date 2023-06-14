package com.lulu.usercenter.model.request;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.google.gson.annotations.SerializedName;
import lombok.Data;

import java.io.Serializable;

@Data
public class PageRequest {



    /**
     * 页面大小
     */
    private int  pageSize=10;
    /**
     * 当前页面
     */
    private long pageNum=1;
}
