package com.offcn.all;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication(exclude = DataSourceAutoConfiguration.class)//取消数据源自动配置
@ComponentScan({"com.offcn"})
@EnableDiscoveryClient
@EnableFeignClients(basePackages= {"com.offcn"})
public class WebAllApplication {

    public static void main(String[] args) {

        SpringApplication.run(WebAllApplication.class, args);
    }

}
