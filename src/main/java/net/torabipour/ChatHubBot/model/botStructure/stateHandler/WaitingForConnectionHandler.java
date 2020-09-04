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
import java.util.List;
import java.util.stream.Collectors;
import net.torabipour.ChatHubBot.controller.BotHandler;
import net.torabipour.ChatHubBot.db.TransactionalDBAccess;
import net.torabipour.ChatHubBot.model.Language;
import net.torabipour.ChatHubBot.model.Sex;
import net.torabipour.ChatHubBot.model.User;
import net.torabipour.ChatHubBot.model.UserStatus;
import net.torabipour.ChatHubBot.model.anonChat.Chat;
import net.torabipour.ChatHubBot.model.anonChat.ChatRequest;
import net.torabipour.ChatHubBot.model.botStructure.AbstractStateHandler;
import net.torabipour.ChatHubBot.model.utils.MediaManager;
import net.torabipour.ChatHubBot.model.utils.UserInterfaceException;
import org.hibernate.Session;

/**
 *
 * @author mohammad
 */
public class WaitingForConnectionHandler extends AbstractStateHandler {

    private Chat chat;

    public WaitingForConnectionHandler(Update update, TelegramBot bot) {
        super(update, bot);
    }

    @Override
    protected void validateInput(Message message, String messageText) throws UserInterfaceException {
        if (messageText == null) {
            throw new UserInterfaceException("مقدار ورودی نامعتبر است.", "Invalid input for target sex select.");
        }
    }

    @Override
    protected void onOperation(User localUser, Message message, String messageText) throws UserInterfaceException {
        Sex targetSex;

        if (messageText.contains("مرد") || messageText.contains("Male")) {
            targetSex = Sex.Male;
        } else if (messageText.contains("زن") || messageText.contains("Female")) {
            targetSex = Sex.Female;
        } else {
            targetSex = null;
        }

        sendRelationshipAdvise(message.chat().id());

        ChatRequest req = new ChatRequest(null, localUser, targetSex, true, null);

        new TransactionalDBAccess() {
            @Override
            protected void operation(Session session) {
                session.saveOrUpdate(req);
            }
        }.execute();

        List<ChatRequest> requests = ChatRequest.loadProperMatch(targetSex, localUser.getSex(), localUser.getLang());

        if (requests == null || requests.isEmpty()) {
            sendCanelConnection(message.chat().id(), isEnglish);
            return;
        }

        requests = requests.stream().filter(r -> !r.getRequester().getId().equals(localUser.getId())).collect(Collectors.toList());

        if (requests.isEmpty()) {
            sendCanelConnection(message.chat().id(), isEnglish);
            return;
        }

        ChatRequest secondReq = requests.get(0);

        chat = new Chat(null, localUser, secondReq.getRequester(), true);

        new TransactionalDBAccess() {
            @Override
            protected void operation(Session session) {
                session.saveOrUpdate(chat);
                req.setChat(chat);
                secondReq.setChat(chat);
                req.setActive(false);
                secondReq.setActive(false);
                session.merge(req);
                session.merge(secondReq);

                localUser.setStatus(UserStatus.InChat);
                secondReq.getRequester().setStatus(UserStatus.InChat);
                session.merge(localUser);
                session.merge(secondReq.getRequester());
            }
        }.execute();
    }

    @Override
    protected void sendMessageOnSuccess(Long chatId, Boolean isEnglish, MediaManager mediaManager) {
        
        if(chat != null){
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
        
    }

    @Override
    protected UserStatus getAbortUserStatus() {
        return UserStatus.Registered;
    }

    @Override
    protected void onAbort(User localUser, Message message, String messageText) {
        List<ChatRequest> prevRequests = ChatRequest.loadByRequester(localUser);

        if (prevRequests != null && !prevRequests.isEmpty()) {

            prevRequests.forEach(r -> {
                r.setActive(false);
                new TransactionalDBAccess() {
                    @Override
                    protected void operation(Session session) {
                        localUser.setStatus(UserStatus.Registered);
                        session.merge(r);
                        session.merge(localUser);
                    }
                }.execute();
            });
        }
    }

    @Override
    protected void sendMessageOnAbort(Long chatId, Boolean isEnglish, MediaManager mediaManager) {
        mediaManager.messageSend(isEnglish ? "Chat request successfully canceled." : "درخواست چت با موفقیت لغو شد.", chatId);
        sendMainMenu(chatId, isEnglish);
    }
    
    private void sendCanelConnection(Long chatId, Boolean isEnglish) {
        mediaManager.messageSendKeyboard(isEnglish ? "Chat request submitted successfully. please wait."
                : "درخواست شما برای شروع چت ثبت شد. لطفا منتظر بمانید.", chatId, "/cancel");
    }
    
    private void sendTargetSexSelect(Long chatId, Boolean isEnglish) {
        mediaManager.messageSendKeyboard(isEnglish ? "I'm intrested to talk to a " : "جنسیت شخص مورد نظر جهت چت را انتخاب کنید", chatId,
                new String[]{isEnglish ? "Male 👨‍🦱" : "مرد 👨‍🦱", isEnglish ? "Female 👩" : "زن 👩"},
                new String[]{isEnglish ? "No difference 👨‍👩‍👦" : "فرقی نمی کند 👨‍👩‍👦"});
    }

    @Override
    protected void onInvalidInput(User localUser, Message message, String messageText) {
        sendTargetSexSelect(chatId, isEnglish);
    }

    @Override
    protected void onUnsuccessfullOperation(User localUser, Message message, String messageText) {
        localUser.setStatus(UserStatus.Registered);
        sendMainMenu(chatId, isEnglish);
    }

}
