/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.torabipour.ChatHubBot.model.botStructure;

import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.model.Update;
import net.torabipour.ChatHubBot.db.TransactionalDBAccess;
import net.torabipour.ChatHubBot.model.User;
import net.torabipour.ChatHubBot.model.UserStatus;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.hibernate.Session;

/**
 *
 * @author mohammad
 */
public abstract class AbstractBotOperation {

    protected TelegramBot bot;
    protected Update update;
    protected Long chatId;
    protected com.pengrad.telegrambot.model.User telegramUser;
    protected User localUser;
    protected String messageText;

    protected abstract UserStatus getAllowedUserStatus();

    protected abstract UserStatus getAbortUserStatus();
    
    protected abstract UserStatus getNextUserStatus();

    protected List<String> getAbortPhrases() {
        return new ArrayList<>(Arrays.asList("/cancel", "/start", "/restart", "/abort", "/terminate"));
    }

    protected abstract void onAbort();

    public void execute() {
        if(messageText != null && getAbortPhrases().contains(messageText)){
            localUser.setStatus(getAbortUserStatus());
            new TransactionalDBAccess() {
                @Override
                protected void operation(Session session) {
                    session.saveOrUpdate(localUser);
                }
            }.execute();
            onAbort();
            return;
        }
    }
}
