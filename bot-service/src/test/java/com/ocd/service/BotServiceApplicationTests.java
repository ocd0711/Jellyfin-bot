package com.ocd.service;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Locale;

@SpringBootTest(classes = {BotServiceApplicationTests.class})
class BotServiceApplicationTests {

    @Test
    public void dateTest() throws ParseException {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSSSSSS", Locale.getDefault());
        System.out.println(dateFormat.parse("2025-01-05 16:23:45.2650642").toString());
    }
}
