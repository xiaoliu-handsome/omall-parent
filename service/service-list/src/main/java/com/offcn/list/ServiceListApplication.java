package com.offcn.list;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication(exclude = DataSourceAutoConfiguration.class)
@ComponentScan({"com.offcn"})
@EnableDiscoveryClient
@EnableFeignClients(basePackages= {"com.offcn"})
public class ServiceListApplication {
    public static void main(String[] args) {
        SpringApplication.run(ServiceListApplication.class,args);
    }

}
