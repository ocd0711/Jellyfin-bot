package com.ocd.controller;

import lombok.extern.slf4j.Slf4j;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@SpringBootApplication
@RestController
@EnableScheduling
@EnableAsync(proxyTargetClass = true)
@MapperScan(basePackages = "com.ocd.mapper")
@ComponentScan(
        basePackages = {"com.ocd.bean", "com.ocd.service", "com.ocd.controller", "com.ocd.util", "com.ocd.bean.mysql", "com.ocd.config"}
)
public class BotControllerApplication {

    public static void main(String[] args) {
        SpringApplication.run(BotControllerApplication.class, args);
//        new SpringApplicationBuilder(IsenControllerApplication.class)
//                .main(SpringVersion.class)
//                .bannerMode(Banner.Mode.CONSOLE).run(args);

//        try (TelegramBotsLongPollingApplication botsApplication = new TelegramBotsLongPollingApplication()) {
//            botsApplication.registerBot(BotConfig.getInstance().COMMANDS_TOKEN, new CommandsHandler(BotConfig.getInstance().COMMANDS_TOKEN, BotConfig.getInstance().COMMANDS_USER));
//            Thread.currentThread().join();
//        } catch (Exception e) {
//            log.error("Error registering bot", e);
//        }
    }
}
