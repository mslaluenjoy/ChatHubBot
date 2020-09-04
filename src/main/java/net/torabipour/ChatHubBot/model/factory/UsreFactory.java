/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.torabipour.ChatHubBot.model.factory;

import com.pengrad.telegrambot.model.Update;
import java.util.Date;
import net.torabipour.ChatHubBot.db.TransactionalDBAccess;
import net.torabipour.ChatHubBot.model.Language;
import net.torabipour.ChatHubBot.model.User;
import net.torabipour.ChatHubBot.model.UserStatus;
import org.hibernate.Session;

/**
 *
 * @author mohammad
 */
public class UsreFactory {

    private User localUser;

    public User createOrFetchUser(Update update) {
        
        String messageText;
        String telegramId;
        com.pengrad.telegrambot.model.User user;
        com.pengrad.telegrambot.model.Message message;
        if (update.editedMessage() != null) {
            message = update.editedMessage();
        } else {
            message = update.message();
        }
        user = message.from();
        telegramId = String.valueOf(user.id());
        messageText = message.text();
        
        localUser = User.loadByTelegramId(telegramId);

        if (messageText != null && messageText.contains("/start") && messageText.split(" ").length == 2 && localUser == null) {
            localUser = new User(null, Language.English, telegramId, user.username(), user.firstName(), user.lastName(), UserStatus.Registered, message.chat().id());
            new TransactionalDBAccess() {
                @Override
                protected void operation(Session session) {
                    session.save(localUser);
                }
            }.execute();
            localUser.setNickName(String.valueOf(localUser.getId()));
            new TransactionalDBAccess() {
                @Override
                protected void operation(Session session) {
                    session.saveOrUpdate(localUser);
                }
            }.execute();
        }

        if (localUser == null) {
            User newLocalUser = new User(null, null, telegramId, user.username(), user.firstName(), user.lastName(), UserStatus.LanguageSelect, message.chat().id());
            new TransactionalDBAccess() {
                @Override
                protected void operation(Session session) {
                    session.save(newLocalUser);
                }
            }.execute();
            return newLocalUser;
        }

        localUser.setTelegramUserName(user.username());
        localUser.setChatId(message.chat().id());
        localUser.setLastLogin(new Date());
        
        if (messageText != null && messageText.contains("/acceptchatrequest")) {
            localUser.setStatus(UserStatus.AcceptingChatRequest);
        }

        new TransactionalDBAccess() {
            @Override
            protected void operation(Session session) {
                session.saveOrUpdate(localUser);
            }
        }.execute();
        
        return localUser;
    }
}
