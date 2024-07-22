package com.tikchat;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@EnableAsync
@org.springframework.boot.autoconfigure.SpringBootApplication(scanBasePackages = {"com.tikchat"})
@MapperScan("com.tikchat.mapper")
@EnableTransactionManagement//事务处理的注解
@EnableScheduling
public class TikChatApplication {

    public static void main(String[] args) {
        SpringApplication.run(TikChatApplication.class, args);
    }

}
