package com.lulu.usercenter.model.request;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;

@Data
public class UserJoinTeamRequest implements Serializable {

    private static final long serialVersionUID = 7005544703500689702L;
    private  Integer id;

    /**
     * 密码
     */
    private String password;

}
