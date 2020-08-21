package pers.lyl232.jakgd;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.multipart.MultipartResolver;
import org.springframework.web.multipart.commons.CommonsMultipartResolver;

import java.util.regex.Pattern;

@SuppressWarnings("SpringBootApplicationSetup")
@SpringBootApplication(scanBasePackages = {"pers.lyl232"})
@EnableAutoConfiguration(exclude = {org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration.class})
public class JakgdApplication {

    public static void main(String[] args) {
        SpringApplication.run(JakgdApplication.class, args);
    }

    @Bean
    public BCryptPasswordEncoder bCryptPasswordEncoder() {
        // 密码加密Bean
        return new BCryptPasswordEncoder();
    }

    @Bean
    public Pattern validLabelPattern() {
        // 合法label的模式
        // 只含有汉字、数字、字母 长度为2至20
        return Pattern.compile("[a-zA-Z\\u4e00-\\u9fa5]([a-zA-Z\\u4e00-\\u9fa50-9]){1,20}");
    }

    @Bean(name = "multipartResolver")
    public MultipartResolver multipartResolver(){
        CommonsMultipartResolver resolver = new CommonsMultipartResolver();
        resolver.setDefaultEncoding("UTF-8");
        //resolveLazily属性启用是为了推迟文件解析，以在在UploadAction中捕获文件大小异常
        resolver.setResolveLazily(true);
        resolver.setMaxInMemorySize(40960);
        //上传文件大小 50M 50*1024*1024
        resolver.setMaxUploadSize(50*1024*1024);
        return resolver;
    }

}
