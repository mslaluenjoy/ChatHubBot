/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.torabipour.ChatHubBot.model.botStructure.stateHandler;

import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.model.Message;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.request.EditMessageText;
import java.util.List;
import net.torabipour.ChatHubBot.controller.BotHandler;
import net.torabipour.ChatHubBot.db.TransactionalDBAccess;
import net.torabipour.ChatHubBot.model.Language;
import net.torabipour.ChatHubBot.model.User;
import net.torabipour.ChatHubBot.model.UserStatus;
import net.torabipour.ChatHubBot.model.anonChat.Chat;
import net.torabipour.ChatHubBot.model.anonChat.ChatRequest;
import net.torabipour.ChatHubBot.model.botStructure.AbstractStateHandler;
import net.torabipour.ChatHubBot.model.utils.AnonymousChatHandler;
import net.torabipour.ChatHubBot.model.utils.MediaManager;
import net.torabipour.ChatHubBot.model.utils.UserInterfaceException;
import org.hibernate.Session;

/**
 *
 * @author mohammad
 */
public class InChatHandler extends AbstractStateHandler {

    private Chat currentChat;
    private User endpoint1;
    private User endpoint2;

    public InChatHandler(Update update, TelegramBot bot) {
        super(update, bot);
        currentChat = Chat.loadByEndpoint(localUser);
        if (currentChat != null) {
            endpoint1 = currentChat.getEndpoint1();
            endpoint2 = currentChat.getEndpoint2();
        }

    }

    @Override
    protected void validateInput(Message message, String messageText) throws UserInterfaceException {
        if (currentChat == null) {
            throw new UserInterfaceException(null,null);
        }
    }

    @Override
    protected void onInvalidInput(User localUser, Message message, String messageText) {
        localUser.setStatus(UserStatus.Registered);
        new TransactionalDBAccess() {
            @Override
            protected void operation(Session session) {
                session.saveOrUpdate(localUser);
            }
        }.execute();
        sendChatTerminated(message.chat().id(), isEnglish);
        sendMainMenu(message.chat().id(), isEnglish);
    }

    @Override
    protected void onOperation(User localUser, Message message, String messageText) throws UserInterfaceException {

        if (update.editedMessage() != null) {
            Integer originalReply;
            net.torabipour.ChatHubBot.model.anonChat.Message msg = net.torabipour.ChatHubBot.model.anonChat.Message.loadBySendId(message.messageId());
            if (msg == null) {
                msg = net.torabipour.ChatHubBot.model.anonChat.Message.loadByReceiveId(message.messageId());
                if (msg == null) {
                    originalReply = message.messageId();
                } else {
                    originalReply = msg.getSendId();
                }
            } else {
                originalReply = msg.getReceiveId();
            }

            Long chatId = currentChat.getEndpoint1().getId().equals(localUser.getId())
                    ? currentChat.getEndpoint2().getChatId() : currentChat.getEndpoint1().getChatId();
            if (message.text() != null) {
                bot.execute(new EditMessageText(chatId, originalReply, message.text()));
            }
            return;
        }

        new AnonymousChatHandler() {
            @Override
            public TelegramBot getBot() {
                return bot;
            }

            @Override
            public Update getUpdate() {
                return update;
            }

            @Override
            public Chat getCurrentChat() {
                return currentChat;
            }

            @Override
            public User getLocalUser() {
                return localUser;
            }
        }.copyMessageAndForward(endpoint1.getChatId().equals(message.chat().id()) ? endpoint2.getChatId() : endpoint1.getChatId(), isEnglish);
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
        currentChat.setActive(false);
        endpoint1.setStatus(UserStatus.Registered);
        endpoint2.setStatus(UserStatus.Registered);
        List<ChatRequest> prevEnd1 = ChatRequest.loadByRequester(endpoint1);
        List<ChatRequest> prevEnd2 = ChatRequest.loadByRequester(endpoint2);
        new TransactionalDBAccess() {
            @Override
            protected void operation(Session session) {
                session.update(currentChat);
                session.merge(endpoint1);
                session.merge(endpoint2);
                if (prevEnd1 != null) {
                    prevEnd1.forEach(x -> {
                        x.setActive(false);
                        session.merge(x);
                    });
                }
                if (prevEnd2 != null) {
                    prevEnd2.forEach(x -> {
                        x.setActive(false);
                        session.merge(x);
                    });
                }
            }
        }.execute();
    }

    @Override
    protected void sendMessageOnAbort(Long chatId, Boolean isEnglish, MediaManager mediaManager) {
        sendChatTerminated(endpoint1.getChatId(), endpoint1.getLang().equals(Language.English));
        sendChatTerminated(endpoint2.getChatId(), endpoint2.getLang().equals(Language.English));
        sendMainMenu(endpoint1.getChatId(), endpoint1.getLang().equals(Language.English));
        sendMainMenu(endpoint2.getChatId(), endpoint2.getLang().equals(Language.English));
    }

    private void sendChatTerminated(Long chatId, Boolean isEnglish) {
        mediaManager.messageSend(isEnglish
                ? "Chat terminated.\n"
                : "چت اتمام یافت.", chatId);
    }

}
