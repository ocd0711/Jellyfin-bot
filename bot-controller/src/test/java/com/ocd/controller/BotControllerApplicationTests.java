package com.ocd.controller;

import cn.hutool.captcha.CaptchaUtil;
import cn.hutool.captcha.GifCaptcha;
import cn.hutool.captcha.LineCaptcha;
import cn.hutool.captcha.ShearCaptcha;
import com.ocd.bean.dto.jellby.PlaybackRecord;
import com.ocd.controller.util.EmbyUtil;
import com.ocd.controller.util.ImageGenerator;
import com.ocd.controller.util.MessageUtil;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.awt.*;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.List;

@SpringBootTest(classes = {BotControllerApplicationTests.class})
class BotControllerApplicationTests {

    @Test
    public void imageTestDemo() throws IOException {
        List<PlaybackRecord> movie = EmbyUtil.getInstance().getPlaybackInfo(true, new Date());
        List<PlaybackRecord> tv = EmbyUtil.getInstance().getPlaybackInfo(false, new Date());
        ImageGenerator.generateRankingImage(false, movie, tv.subList(0, 5), 1280);
    }

    @Test
    public void randomPass() throws IOException {
        System.out.println(EmbyUtil.getInstance().generatePassword());
    }

    @Test
    public void captchaTest() throws IOException {
        // ------ captcha old -----
        GifCaptcha gifService = CaptchaUtil.createGifCaptcha(200, 100, 5);
        System.out.println(gifService.getCode());
        for (int i = 0; i < 4; i++) {
            String randomCode = MessageUtil.INSTANCE.generateSimilarCode(gifService.getCode());
            System.out.println(randomCode);
        }
//        lineService.setFont(new Font("Comic Sans MS", Font.BOLD, 200));
//        lineService.setBackground(Color.LIGHT_GRAY);
//        gifService.write(new File("/Users/ch.hu/Downloads/line_captcha.gif"));
        // ------ captcha old -----
//        ShearCaptcha shearCaptcha = CaptchaUtil.createShearCaptcha(1280, 720, 5, 5);
//        shearCaptcha.write(new File("/Users/ch.hu/Downloads/shear_captcha.png"));
    }
}
