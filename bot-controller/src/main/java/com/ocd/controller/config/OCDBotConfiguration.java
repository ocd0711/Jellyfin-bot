package com.ocd.controller.config;

import com.ocd.controller.updateshandlers.CommandsHandler;
import com.ocd.controller.util.AuthorityUtil;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.telegram.telegrambots.longpolling.BotSession;
import org.telegram.telegrambots.longpolling.TelegramBotsLongPollingApplication;
import org.telegram.telegrambots.longpolling.interfaces.LongPollingUpdateConsumer;
import org.telegram.telegrambots.longpolling.util.DefaultGetUpdatesGenerator;
import org.telegram.telegrambots.meta.TelegramUrl;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.Arrays;
import java.util.List;

/**
 * @author ch.hu
 * @date 2024/12/02 10:31
 * Description:
 */
@Configuration
public class OCDBotConfiguration {

    @Bean
    public TelegramBotsLongPollingApplication telegramBotsLongPollingApplication() {
        return new TelegramBotsLongPollingApplication() {
            @Override
            public BotSession registerBot(String botToken, LongPollingUpdateConsumer updatesConsumer) throws TelegramApiException {
                return registerBot(botToken,
                        () -> TelegramUrl.DEFAULT_URL,
                        new DefaultGetUpdatesGenerator(getAllowedUpdates()),
                        updatesConsumer);
            }
        };
    }

    private List<String> getAllowedUpdates() {
        return Arrays.asList("message", "edited_message", "channel_post",
                "edited_channel_post", "message_reaction", "message_reaction_count",
                "inline_query", "chosen_inline_result", "chosen_inline_result",
                "callback_query", "shipping_query", "pre_checkout_query",
                "poll", "poll_answer", "my_chat_member", "chat_member",
                "chat_join_request", "chat_boost", "removed_chat_boost");
    }

    @Bean
    public BotSession botSession(TelegramBotsLongPollingApplication botsApplication, CommandsHandler commandsHandler) throws TelegramApiException {
        return botsApplication.registerBot(AuthorityUtil.botConfig.token, commandsHandler);
    }
}
