package com.lan;

import lombok.SneakyThrows;
import org.mybatis.spring.annotation.MapperScan;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.env.Environment;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.net.InetAddress;

/**
 * @author lan
 */
@SpringBootApplication
@EnableScheduling
// @MapperScan("com.lan.mapper")
public class FriendApplication {
    private static Logger logger = LoggerFactory.getLogger(FriendApplication.class);

    @SneakyThrows
    public static void main(String[] args) {
        // SpringApplication.run(MainApplication.class, args);
        SpringApplication app = new SpringApplication(FriendApplication.class);
        ConfigurableApplicationContext application = app.run(args);
        Environment env = application.getEnvironment();
        logger.info("\n----------------------------------------------------------\n\t" +
                        "项目名称 Application '{}' is running! Access URLs:\n\t" +
                        "本地路径 Local: \t\thttp://localhost:{}{}\n\t" +
                        "External: \t\t\thttp://{}:{}\n\t" +
                        "上下文路径 context-path: \t{}\n\t" +
                        "接口文档地址 Doc: \thttp://{}:{}{}/doc.html\n" +
                        "----------------------------------------------------------",
                env.getProperty("spring.application.name"),
                env.getProperty("server.port"),
                env.getProperty("server.servlet.context-path"),
                InetAddress.getLocalHost().getHostAddress(),
                env.getProperty("server.port"),
                env.getProperty("server.servlet.context-path"),
                InetAddress.getLocalHost().getHostAddress(),
                env.getProperty("server.port"),
                env.getProperty("server.servlet.context-path"));
    }
}
