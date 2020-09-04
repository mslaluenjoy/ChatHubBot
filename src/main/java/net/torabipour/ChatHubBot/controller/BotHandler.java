/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.torabipour.ChatHubBot.controller;

import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.model.Update;
import net.torabipour.ChatHubBot.model.User;
import net.torabipour.ChatHubBot.model.UserStatus;
import net.torabipour.ChatHubBot.model.botStructure.AbstractStateHandler;
import net.torabipour.ChatHubBot.model.botStructure.StateHandlerFactory;
import net.torabipour.ChatHubBot.model.utils.MediaManager;
import net.torabipour.ChatHubBot.model.factory.UsreFactory;

/**
 *
 * @author mohammad
 */
public class BotHandler {

    private TelegramBot bot;

    public BotHandler(TelegramBot bot) {
        this.bot = bot;
    }

    public void handleUpdate(Update update) {

        User localUser = new UsreFactory().createOrFetchUser(update);
        if (localUser.getStatus().equals(UserStatus.LanguageSelect) && localUser.getLang() == null) {
            sendLanguageSelect(update.message().chat().id());
            return;
        }

        UserStatus status = localUser.getStatus();

        AbstractStateHandler handler = StateHandlerFactory.getHandler(status, bot, update);
        handler.execute();
    }

    private void sendLanguageSelect(Long chatId) {
        new MediaManager() {
            @Override
            public TelegramBot getBot() {
                return bot;
            }
        }.messageSendKeyboard("زبان خود را انتخاب کنید. \n Choose your language.", chatId, "English 🇬🇧", "Persian 🇮🇷");
    }

}
