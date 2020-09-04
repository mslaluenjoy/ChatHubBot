/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.torabipour.ChatHubBot.model.botStructure.stateHandler;

import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.model.Message;
import com.pengrad.telegrambot.model.Update;
import java.util.List;
import net.torabipour.ChatHubBot.db.TransactionalDBAccess;
import net.torabipour.ChatHubBot.model.User;
import net.torabipour.ChatHubBot.model.UserStatus;
import net.torabipour.ChatHubBot.model.botStructure.AbstractStateHandler;
import net.torabipour.ChatHubBot.model.factory.GlobalPostFactory;
import net.torabipour.ChatHubBot.model.globalChat.ChatMessage;
import net.torabipour.ChatHubBot.model.globalChat.GlobalPost;
import net.torabipour.ChatHubBot.model.utils.GlobalMediaManager;
import net.torabipour.ChatHubBot.model.utils.MediaManager;
import net.torabipour.ChatHubBot.model.utils.UserInterfaceException;
import org.hibernate.Session;

/**
 *
 * @author mohammad
 */
public class InGlobalChatHandler extends AbstractStateHandler {

    public InGlobalChatHandler(Update update, TelegramBot bot) {
        super(update, bot);
    }

    @Override
    protected List<String> getAbortPhrases() {
        List<String> aborts = super.getAbortPhrases();
        aborts.add("Leave room");
        aborts.add("خروج");
        aborts.add("/leaveglobal");
        return aborts; 
    }

    @Override
    protected void validateInput(Message message, String messageText) throws UserInterfaceException {
    }

    @Override
    protected void onInvalidInput(User localUser, Message message, String messageText) {
    }

    @Override
    protected void onOperation(User localUser, Message message, String messageText) throws UserInterfaceException {
        GlobalPost globalPost = GlobalPostFactory.create(message, localUser, mediaManager, localUser.getGcr());
        new TransactionalDBAccess() {
            @Override
            protected void operation(Session session) {
                session.saveOrUpdate(globalPost);
            }
        }.execute();
        ChatMessage cm = new ChatMessage(null, message.messageId(), message.chat().id(), globalPost);
        new TransactionalDBAccess() {
            @Override
            protected void operation(Session session) {
                session.saveOrUpdate(cm);
            }
        }.execute();
        new GlobalMediaManager() {
            @Override
            public TelegramBot getBot() {
                return bot;
            }
        }.emmitPost(globalPost, localUser);
    }

    @Override
    protected void onUnsuccessfullOperation(User localUser, Message message, String messageText) {
    }

    @Override
    protected void sendMessageOnSuccess(Long chatId, Boolean isEnglish, MediaManager mediaManager) {
    }

    @Override
    protected UserStatus getAbortUserStatus() {
        return UserStatus.Registered;
    }

    @Override
    protected void onAbort(User localUser, Message message, String messageText) {
        new GlobalMediaManager() {
            @Override
            public TelegramBot getBot() {
                return bot;
            }
        }.notifyUserLeft(localUser.getGcr(), localUser);
        localUser.setGcr(null);
        new TransactionalDBAccess() {
            @Override
            protected void operation(Session session) {
                session.update(localUser);
            }
        }.execute();
        return;
    }

    @Override
    protected void sendMessageOnAbort(Long chatId, Boolean isEnglish, MediaManager mediaManager) {
        sendMainMenu(chatId, isEnglish);
    }

}
