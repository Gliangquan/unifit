package com.jcen.unifit;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.jcen.unifit.mapper")
public class UniFitMainApplication {

    public static void main(String[] args) {
        SpringApplication.run(UniFitMainApplication.class, args);
    }
}
