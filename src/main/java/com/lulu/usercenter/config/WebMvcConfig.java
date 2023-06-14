package com.lulu.usercenter.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;

@Configuration
public class WebMvcConfig {
    public void addCorsMapping(CorsRegistry  registry){
        //设置允许跨域的路径
        registry.addMapping("/**")
                //设置允许跨域的域名
                .allowedOrigins("http://localhost:5173","http://127.0.0.1:8080","http://127.0.0.1:8081","http://127.0.0.1:8082")
               //是否允许证书 默认不开启
                .allowCredentials(true)
                //设置允许方法
                .allowedMethods("*")
                //跨域允许时间
                .maxAge(3600);
    }
}
