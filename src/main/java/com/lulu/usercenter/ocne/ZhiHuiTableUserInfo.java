package com.lulu.usercenter.ocne;

import com.alibaba.excel.annotation.ExcelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
@Data
public class ZhiHuiTableUserInfo {

    /**
     * 星球编号
     */
    @ExcelProperty("成员编号")
    private String planetCode;
    /**
     * 用户昵称
     */
    @ExcelProperty("成员昵称")
    private String username;


}
