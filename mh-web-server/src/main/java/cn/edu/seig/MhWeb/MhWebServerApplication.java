package cn.edu.seig.MhWeb;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.scheduling.annotation.EnableAsync;

@EnableCaching   // 开启Spring Boot基于注解的缓存管理支持
@SpringBootApplication
public class MhWebServerApplication {
    public static void main(String[] args) {
        SpringApplication.run(MhWebServerApplication.class, args);
    }

}
