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
import net.torabipour.ChatHubBot.db.TransactionalDBAccess;
import net.torabipour.ChatHubBot.model.User;
import net.torabipour.ChatHubBot.model.UserStatus;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import net.torabipour.ChatHubBot.model.Language;
import net.torabipour.ChatHubBot.model.utils.UserInterfaceException;
import org.hibernate.Session;

/**
 *
 * @author mohammad
 */
public abstract class AbstractRegistrationStep {

    protected TelegramBot bot;
    protected Update update;
    protected Long chatId;
    protected com.pengrad.telegrambot.model.User telegramUser;
    protected User localUser;
    protected String messageText;
    protected Boolean isEnglish;

    protected abstract UserStatus getAbortUserStatus();

    protected abstract UserStatus getNextUserStatus();

    protected List<String> getAbortPhrases() {
        return new ArrayList<>(Arrays.asList("/cancel", "/start", "/restart", "/abort", "/terminate"));
    }

    protected abstract void onAbort(User localUser, Message message);

    protected abstract void onOperation(User localUser, Message message);

    public void execute() {
        if (messageText != null && getAbortPhrases().contains(messageText)) {
            localUser.setStatus(getAbortUserStatus());
            new TransactionalDBAccess() {
                @Override
                protected void operation(Session session) {
                    session.saveOrUpdate(localUser);
                }
            }.execute();
            onAbort(localUser, update.message());
            return;
        }
        try {
            validateInput(update.message());
        } catch (UserInterfaceException ex) {
            bot.execute(new SendMessage(chatId, isEnglish ? ex.getEnglish() : ex.getPersian()));
            return;
        }
        onOperation(localUser, update.message());
    }

    protected abstract void validateInput(Message message) throws UserInterfaceException;

    protected abstract TelegramBot getBot();

    public AbstractRegistrationStep(Update update) {
        this.update = update;
        this.chatId = update.message().chat().id();
        this.telegramUser = update.message().from();
        this.messageText = update.message().text();
        this.localUser = User.loadByTelegramId(String.valueOf(telegramUser.id()));
        this.bot = getBot();
        this.isEnglish = localUser.getLang() == null || localUser.getLang().equals(Language.English);
    }

}
