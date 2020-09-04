/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.torabipour.ChatHubBot.model.botStructure.stateHandler;

import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.model.Message;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.model.request.ReplyKeyboardMarkup;
import com.pengrad.telegrambot.request.SendMessage;
import net.torabipour.ChatHubBot.controller.BotHandler;
import net.torabipour.ChatHubBot.db.TransactionalDBAccess;
import net.torabipour.ChatHubBot.model.Language;
import net.torabipour.ChatHubBot.model.User;
import net.torabipour.ChatHubBot.model.UserStatus;
import net.torabipour.ChatHubBot.model.anonChat.Chat;
import net.torabipour.ChatHubBot.model.anonChat.TargetedChatRequest;
import net.torabipour.ChatHubBot.model.botStructure.AbstractStateHandler;
import net.torabipour.ChatHubBot.model.utils.MediaManager;
import net.torabipour.ChatHubBot.model.utils.UserInterfaceException;
import org.hibernate.Session;

/**
 *
 * @author mohammad
 */
public class AcceptingChatRequestHandler extends AbstractStateHandler {
    
    private Chat chat;

    public AcceptingChatRequestHandler(Update update, TelegramBot bot) {
        super(update, bot);
    }

    @Override
    protected void validateInput(Message message, String messageText) throws UserInterfaceException {
        if (!messageText.contains("/acceptchatrequest")) {
            throw new UserInterfaceException("فرمت ورودی اشتباه جهت قبول کردن درخواست چت.", "Malformatted input for accepting chat request operation.");
        }
    }

    @Override
    protected void onInvalidInput(User localUser, Message message, String messageText) {
        localUser.setStatus(UserStatus.Registered);
        saveLocalUser();
        sendMainMenu(chatId, isEnglish);
    }

    @Override
    protected void onOperation(User localUser, Message message, String messageText) throws UserInterfaceException {
        if (messageText != null && messageText.contains("/acceptchatrequest")) {
            Long requesterId = Long.valueOf(messageText.replaceAll("/acceptchatrequest", ""));
            User requester = User.loadById(requesterId);
            if (requester.getStatus().equals(UserStatus.InChat) || requester.getStatus().equals(UserStatus.InGlobalChat)) {
                mediaManager.messageSend(isEnglish ? "User is currently in chat. please wait for them to become available." : "کاربر درحال چت است. لطفا تا زمانی که دوباره در دسترس بشود صبر کنید.", message.chat().id());
                return;
            }
            if (localUser.getStatus().equals(UserStatus.InChat) || localUser.getStatus().equals(UserStatus.InGlobalChat)) {
                mediaManager.messageSend(isEnglish ? "You are currently in chat. please leave your current chat." : "شما هم اکنون درحال چت هستید. اول از چت خارج شوید.", message.chat().id());
                return;
            }
            TargetedChatRequest tcr = TargetedChatRequest.loadByRequesterTargeted(requester, localUser);
            if (tcr == null) {
                return;
            }
            chat = new Chat(null, localUser, requester, true);

            new TransactionalDBAccess() {
                @Override
                protected void operation(Session session) {
                    session.saveOrUpdate(chat);
                    tcr.setChat(chat);
                    tcr.setActive(false);
                    session.merge(tcr);

                    localUser.setStatus(UserStatus.InChat);
                    requester.setStatus(UserStatus.InChat);
                    session.merge(localUser);
                    session.merge(requester);
                }
            }.execute();
        }
    }

    @Override
    protected void onUnsuccessfullOperation(User localUser, Message message, String messageText) {
        localUser.setStatus(UserStatus.Registered);
        saveLocalUser();
        sendMainMenu(chatId, isEnglish);
    }

    @Override
    protected void sendMessageOnSuccess(Long chatId, Boolean isEnglish, MediaManager mediaManager) {
        bot.execute(new SendMessage(chat.getEndpoint1().getChatId(), chat.getEndpoint1().getLang().equals(Language.English)
                ? "Your chat with " + chat.getEndpoint2().getNickName() + " has started! say hello!"
                : "چت شما با " + chat.getEndpoint2().getNickName() + " شروع شد! بهش سلام کن!").replyMarkup(new ReplyKeyboardMarkup(
                new String[]{"/terminate"}
        )
                .oneTimeKeyboard(true)
                .resizeKeyboard(true)
                .selective(true)));

        bot.execute(new SendMessage(chat.getEndpoint2().getChatId(), chat.getEndpoint2().getLang().equals(Language.English)
                ? "Your chat with " + chat.getEndpoint1().getNickName() + " has started! say hello!"
                : "چت شما با " + chat.getEndpoint1().getNickName() + " شروع شد! بهش سلام کن!").replyMarkup(new ReplyKeyboardMarkup(
                new String[]{"/terminate"}
        )
                .oneTimeKeyboard(true)
                .resizeKeyboard(true)
                .selective(true)));
    }

    @Override
    protected UserStatus getAbortUserStatus() {
        return UserStatus.Registered;
    }

    @Override
    protected void onAbort(User localUser, Message message, String messageText) {
    }

    @Override
    protected void sendMessageOnAbort(Long chatId, Boolean isEnglish, MediaManager mediaManager) {
        sendMainMenu(chatId, isEnglish);
    }

}
