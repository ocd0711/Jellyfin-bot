package com.ocd.controller;

import com.ocd.bean.dto.jellby.PlaybackRecord;
import com.ocd.controller.util.EmbyUtil;
import com.ocd.controller.util.ImageGenerator;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

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
}
