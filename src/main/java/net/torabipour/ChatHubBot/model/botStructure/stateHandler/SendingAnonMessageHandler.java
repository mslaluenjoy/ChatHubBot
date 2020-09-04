/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.torabipour.ChatHubBot.model.botStructure.stateHandler;

import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.model.Message;
import com.pengrad.telegrambot.model.Update;
import net.torabipour.ChatHubBot.db.TransactionalDBAccess;
import net.torabipour.ChatHubBot.model.User;
import net.torabipour.ChatHubBot.model.UserStatus;
import net.torabipour.ChatHubBot.model.anonChat.PrivateMessage;
import net.torabipour.ChatHubBot.model.botStructure.AbstractStateHandler;
import net.torabipour.ChatHubBot.model.factory.PrivateMessageFactory;
import net.torabipour.ChatHubBot.model.utils.MediaManager;
import net.torabipour.ChatHubBot.model.utils.UserInterfaceException;
import org.hibernate.Session;

/**
 *
 * @author mohammad
 */
public class SendingAnonMessageHandler extends AbstractStateHandler {

    private PrivateMessage pm;

    public SendingAnonMessageHandler(Update update, TelegramBot bot) {
        super(update, bot);
    }

    @Override
    protected void validateInput(Message message, String messageText) throws UserInterfaceException {
    }

    @Override
    protected void onInvalidInput(User localUser, Message message, String messageText) {
    }

    @Override
    protected void onOperation(User localUser, Message message, String messageText) throws UserInterfaceException {

        pm = PrivateMessageFactory.create(message, localUser, mediaManager);
        new TransactionalDBAccess() {
            @Override
            protected void operation(Session session) {
                session.saveOrUpdate(pm);
            }
        }.execute();

    }

    @Override
    protected void onUnsuccessfullOperation(User localUser, Message message, String messageText) {
    }

    @Override
    protected void sendMessageOnSuccess(Long chatId, Boolean isEnglish, MediaManager mediaManager) {
        mediaManager.messageSend(!isEnglish ? "پیام شما ارسال شد. در صورت اتمام پیام ها فرمان /cancel را ارسال نمایید."
                : "Message sent. in order to stop sending messages, send /cancel command.", chatId);
        mediaManager.messageSend(isEnglish ? "You received an anonymous message. check your inbox to see your messages."
                : "شما یک پیام ناشناس دریافت کردید. جهت مشاهده پیام به صندوق پیام مراجعه نمایید.", pm.getReceiver().getChatId());
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
        mediaManager.messageSend(isEnglish ? "Sending anonymous chat done." : "ارسال پیام ناشناس انجام شد.", chatId);
        sendMainMenu(chatId, isEnglish);
    }

}
