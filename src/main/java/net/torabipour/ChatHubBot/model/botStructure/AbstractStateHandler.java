/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.torabipour.ChatHubBot.model.botStructure;

import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.model.Message;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.request.SendMessage;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import net.torabipour.ChatHubBot.PropertiesFileManager;
import net.torabipour.ChatHubBot.db.TransactionalDBAccess;
import net.torabipour.ChatHubBot.model.Language;
import net.torabipour.ChatHubBot.model.User;
import net.torabipour.ChatHubBot.model.UserStatus;
import net.torabipour.ChatHubBot.model.utils.MediaManager;
import net.torabipour.ChatHubBot.model.utils.UserInterfaceException;
import org.hibernate.Session;

/**
 *
 * @author mohammad
 */
public abstract class AbstractStateHandler {

    protected TelegramBot bot;
    protected Update update;
    protected Long chatId;
    protected com.pengrad.telegrambot.model.User telegramUser;
    protected User localUser;
    protected String messageText;
    protected Boolean isEnglish;
    protected MediaManager mediaManager;
    protected Message message;

    public AbstractStateHandler(Update update, TelegramBot bot) {
        this.update = update;
        this.message = update.message();
        if (message == null) {
            message = update.editedMessage();
        }
        this.chatId = message.chat().id();
        this.telegramUser = message.from();
        this.messageText = message.text();
        this.localUser = User.loadByTelegramId(String.valueOf(telegramUser.id()));
        this.bot = bot;
        this.isEnglish = (localUser.getLang() == null) || localUser.getLang().equals(Language.English);
        this.mediaManager = new MediaManager() {
            @Override
            public TelegramBot getBot() {
                return AbstractStateHandler.this.bot;
            }
        };
    }

    public Boolean execute() {
        if (messageText != null && getAbortPhrases().contains(messageText)) {
            onAbort(localUser, message, messageText);
            localUser.setStatus(getAbortUserStatus());
            new TransactionalDBAccess() {
                @Override
                protected void operation(Session session) {
                    session.saveOrUpdate(localUser);
                }
            }.execute();
            sendMessageOnAbort(message.chat().id(), isEnglish, mediaManager);
            return false;
        }
        try {
            validateInput(message, messageText);
        } catch (UserInterfaceException ex) {
            if (ex.getEnglish() != null && ex.getPersian() != null && !ex.getEnglish().isEmpty() && !ex.getPersian().isEmpty()) {
                bot.execute(new SendMessage(chatId, isEnglish ? ex.getEnglish() : ex.getPersian()));
            }
            onInvalidInput(localUser, message, messageText);
            return false;
        }
        try {
            onOperation(localUser, message, messageText);
            sendMessageOnSuccess(message.chat().id(), isEnglish, mediaManager);
            return true;
        } catch (UserInterfaceException ex) {
            if (ex.getEnglish() != null && ex.getPersian() != null && !ex.getEnglish().isEmpty() && !ex.getPersian().isEmpty()) {
                bot.execute(new SendMessage(chatId, isEnglish ? ex.getEnglish() : ex.getPersian()));
            }
            onUnsuccessfullOperation(localUser, message, messageText);
            return false;
        }
    }

    public static boolean isUrl(String str) {
        try {
            new URL(str).toURI();
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    protected void saveLocalUser() {
        new TransactionalDBAccess() {
            @Override
            protected void operation(Session session) {
                session.saveOrUpdate(localUser);
            }
        }.execute();
    }

    protected void sendMainMenu(Long chatId, Boolean isEnglish) {
        mediaManager.messageSendKeyboard(isEnglish ? "Main menu" : "منوی اصلی", chatId,
                new String[]{isEnglish ? "Start chat 🗯" : "شروع چت 🗯", isEnglish ? "Join global rooms 🌍" : "چت روم گلوبال 🌍"},
                new String[]{isEnglish ? "Inbox 📥" : "صندوق پیام 📥", isEnglish ? "Get anonymous link 🔗" : "دریافت لینک چت ناشناس 🔗"},
                new String[]{isEnglish ? "Edit Profile ✏️" : "ویرایش پروفایل ✏️", isEnglish ? "Join local room 🌆" : "چت روم محلی 🌆"},
                new String[]{isEnglish ? "Find people nearby 📍" : "جستجوی افراد نزدیک شما 📍"});
    }

    protected void sendRelationshipAdvise(Long chatId) {
        if (PropertiesFileManager.getInstance().showAdvice()) {
            mediaManager.messageSend(PropertiesFileManager.getInstance().getRandomTip(), chatId);
        }
    }

    protected List<String> getAbortPhrases() {
        return new ArrayList<>(Arrays.asList("/cancel", "/start", "/restart", "/abort", "/terminate", "/back"));
    }

    protected abstract void validateInput(Message message, String messageText) throws UserInterfaceException;

    protected abstract void onInvalidInput(User localUser, Message message, String messageText);

    protected abstract void onOperation(User localUser, Message message, String messageText) throws UserInterfaceException;

    protected abstract void onUnsuccessfullOperation(User localUser, Message message, String messageText);

    protected abstract void sendMessageOnSuccess(Long chatId, Boolean isEnglish, MediaManager mediaManager);

    protected abstract UserStatus getAbortUserStatus();

    protected abstract void onAbort(User localUser, Message message, String messageText);

    protected abstract void sendMessageOnAbort(Long chatId, Boolean isEnglish, MediaManager mediaManager);

}
