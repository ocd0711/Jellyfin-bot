package com.ocd.controller;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

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

    /**
     * 读取本地文本文件的内容并返回
     *
     * @return
     */
    private String getLocalFileContent() {
        String content = null;

        try {
            InputStream is = new FileInputStream("/tmp/hello.txt");
            List<String> lines = IOUtils.readLines(is, "UTF-8");

            if (null != lines && lines.size() > 0) {
                content = lines.get(0);
            }
        } catch (FileNotFoundException e) {
//            log.error("local file not found", e);
        } catch (IOException e) {
//            log.error("io exception", e);
        }

        return content;
    }

    /**
     * 该http服务返回当前应用是否正常，
     * 如果能从本地txt文件成功读取内容，当前应用就算正常，返回码为200，
     * 如果无法从本地txt文件成功读取内容，当前应用就算异常，返回码为403
     *
     * @return
     */
    @RequestMapping(value = "/getstate", method = RequestMethod.GET)
    public ResponseEntity<String> getstate() {
        String localFileContent = getLocalFileContent();

        if (StringUtils.isEmpty(localFileContent)) {
//            log.error("service is unhealthy");
            return ResponseEntity.status(403).build();
        } else {
//            log.info("service is healthy");
            return ResponseEntity.status(200).build();
        }
    }
}
