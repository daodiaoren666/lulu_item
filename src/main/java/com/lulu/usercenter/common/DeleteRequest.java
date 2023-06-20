package com.lulu.usercenter.common;

import lombok.Data;

import java.io.Serializable;

/**
 *  删除请求统一类
 */
@Data
public class DeleteRequest  implements Serializable {

    private static final long serialVersionUID = 4510511593630253045L;
    private long id;
}
