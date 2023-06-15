package com.lulu.usercenter.model.request;

import lombok.Data;

import java.io.Serializable;


/**
 * 用户退出队伍请求体
 */
@Data
public class TeamQuitRequest  implements Serializable {
    private static final long serialVersionUID = -2269110047321806989L;
    private  Long teamId;

}
