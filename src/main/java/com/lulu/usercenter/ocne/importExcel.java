package com.lulu.usercenter.ocne;

import com.alibaba.excel.EasyExcel;

import java.io.File;
import java.util.List;
import java.util.Map;

/**
 * 导入excel
 */
public class importExcel {
    public static void main(String[] args) {
        // 写法1：JDK8+ ,不用额外写一个DemoDataListener
        // since: 3.0.0-beta1
        String fileName ="D:\\java\\idea\\项目\\user-center-backend-master\\src\\main\\resources\\user.xlsx";
        // 这里默认每次会读取100条数据 然后返回过来 直接调用使用数据就行
        // 具体需要返回多少
        monitorRead(fileName);
        synchronousRead(fileName);
    }

    public  static void monitorRead(String fileName){
        // 写法1：JDK8+ ,不用额外写一个DemoDataListener
        // since: 3.0.0-beta1
        // 这里默认每次会读取100条数据 然后返回过来 直接调用使用数据就行
        // 具体需要返回多少
        EasyExcel.read(fileName, ZhiHuiTableUserInfo.class,new TableListener()).sheet().doRead();
    }

    /**
     * 同步读
     * @param fileName
     */
    public static  void synchronousRead(String fileName){
        // 这里 需要指定读用哪个class去读，然后读取第一个sheet 同步读取会自动finish
        List<ZhiHuiTableUserInfo> list = EasyExcel.read(fileName).head(ZhiHuiTableUserInfo.class).sheet().doReadSync();
        for (ZhiHuiTableUserInfo zhiHuiTableUserInfo : list) {
            System.out.println(zhiHuiTableUserInfo);
        }

    }





}
