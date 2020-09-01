/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.torabipour.ChatHubBot.controller;

import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.request.SetWebhook;
import net.torabipour.ChatHubBot.PropertiesFileManager;

/**
 *
 * @author mohammad
 */
public class TelegramBotManager {

    private static TelegramBotManager telegramManager = null;

    private TelegramBot bot;
    private BotHandler handler;

    private TelegramBotManager() {
        bot = new TelegramBot(PropertiesFileManager.getInstance().getBotToken());
        bot.execute(new SetWebhook().url(PropertiesFileManager.getInstance().getIpAddress() + ":" + PropertiesFileManager.getInstance().getPort()));
        handler = new BotHandler() {
            @Override
            protected TelegramBot getBot() {
                return bot;
            }
        };
    }

    public static TelegramBotManager getInstance() {
        if (telegramManager == null) {
            telegramManager = new TelegramBotManager();
        }
        return telegramManager;
    }

    public TelegramBot getBot() {
        return bot;
    }

    public BotHandler getHandler() {
        return handler;
    }

}
