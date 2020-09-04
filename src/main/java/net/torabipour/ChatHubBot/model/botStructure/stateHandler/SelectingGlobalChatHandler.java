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
import java.util.stream.Collectors;
import net.torabipour.ChatHubBot.PropertiesFileManager;
import net.torabipour.ChatHubBot.db.TransactionalDBAccess;
import net.torabipour.ChatHubBot.model.User;
import net.torabipour.ChatHubBot.model.UserStatus;
import net.torabipour.ChatHubBot.model.botStructure.AbstractStateHandler;
import net.torabipour.ChatHubBot.model.globalChat.GlobalChatRoom;
import net.torabipour.ChatHubBot.model.globalChat.GlobalPost;
import net.torabipour.ChatHubBot.model.utils.GlobalMediaManager;
import net.torabipour.ChatHubBot.model.utils.MediaManager;
import net.torabipour.ChatHubBot.model.utils.UserInterfaceException;
import org.hibernate.Session;

/**
 *
 * @author mohammad
 */
public class SelectingGlobalChatHandler extends AbstractStateHandler {

    private GlobalChatRoom gcr;

    public SelectingGlobalChatHandler(Update update, TelegramBot bot) {
        super(update, bot);
    }

    @Override
    protected void validateInput(Message message, String messageText) throws UserInterfaceException {
        if (message == null || GlobalChatRoom.loadByName(messageText) == null) {
            throw new UserInterfaceException("نام گلوبال روم وارد شده معتبر نیست.", "Invalid global room name.");
        }
    }

    @Override
    protected void onInvalidInput(User localUser, Message message, String messageText) {
        sendGlobalRoomSelect(chatId, isEnglish);
    }

    @Override
    protected void onOperation(User localUser, Message message, String messageText) throws UserInterfaceException {
        gcr = GlobalChatRoom.loadByName(messageText);
        if (gcr != null) {
            localUser.setStatus(UserStatus.InGlobalChat);
            localUser.setGcr(gcr);
            new TransactionalDBAccess() {
                @Override
                protected void operation(Session session) {
                    session.update(localUser);
                }
            }.execute();

            List<GlobalPost> recentGlobalPosts = GlobalPost.loadRecentPosts(gcr, PropertiesFileManager.getInstance().getGlobalRecentNum());
            new GlobalMediaManager() {
                @Override
                public TelegramBot getBot() {
                    return bot;
                }
            }.sendBatchGlobalPost(recentGlobalPosts, message.chat().id());
            new GlobalMediaManager() {
                @Override
                public TelegramBot getBot() {
                    return bot;
                }
            }.notifyUserJoined(gcr, localUser);
        }
    }

    @Override
    protected void onUnsuccessfullOperation(User localUser, Message message, String messageText) {
    }

    @Override
    protected void sendMessageOnSuccess(Long chatId, Boolean isEnglish, MediaManager mediaManager) {
        mediaManager.messageSendKeyboard(isEnglish ? gcr.getEnglishPrompt() : gcr.getPersianPromt(), chatId, isEnglish ? "Leave room" : "خروج");
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

    private void sendGlobalRoomSelect(Long chatId, Boolean isEnglish) {
        List<GlobalChatRoom> allRooms = GlobalChatRoom.loadAll().stream().filter(x -> !x.getEnglishName().equals(x.getPersianName())).collect(Collectors.toList());
        List<String> roomOptions = allRooms.stream().map(x -> isEnglish ? x.getEnglishName() : x.getPersianName()).collect(Collectors.toList());
        roomOptions.add(isEnglish ? "Back" : "بازگشت");
        String[] roomOptionsArr = new String[roomOptions.size()];
        roomOptions.toArray(roomOptionsArr);
        mediaManager.messageSendKeyboard(isEnglish ? "Select room." : "انتخاب کنید.", chatId, 3, roomOptionsArr);
    }

}
