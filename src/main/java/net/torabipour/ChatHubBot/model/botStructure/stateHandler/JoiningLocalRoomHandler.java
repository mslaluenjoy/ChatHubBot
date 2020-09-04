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
import net.torabipour.ChatHubBot.PropertiesFileManager;
import net.torabipour.ChatHubBot.db.TransactionalDBAccess;
import net.torabipour.ChatHubBot.model.Location;
import net.torabipour.ChatHubBot.model.User;
import net.torabipour.ChatHubBot.model.UserStatus;
import net.torabipour.ChatHubBot.model.botStructure.AbstractStateHandler;
import net.torabipour.ChatHubBot.model.globalChat.GlobalPost;
import net.torabipour.ChatHubBot.model.globalChat.LocalChatRoom;
import net.torabipour.ChatHubBot.model.utils.GlobalMediaManager;
import net.torabipour.ChatHubBot.model.utils.MediaManager;
import net.torabipour.ChatHubBot.model.utils.UserInterfaceException;
import net.torabipour.ChatHubBot.model.utils.location.NominatimReverseGeocodingJAPI;
import org.hibernate.Session;

/**
 *
 * @author mohammad
 */
public class JoiningLocalRoomHandler extends AbstractStateHandler {

    private LocalChatRoom lcr;

    public JoiningLocalRoomHandler(Update update, TelegramBot bot) {
        super(update, bot);
    }

    @Override
    protected List<String> getAbortPhrases() {
        List<String> aborts = super.getAbortPhrases();
        aborts.add("Nevermind");
        aborts.add("بی خیال");
        return aborts;
    }

    @Override
    protected void validateInput(Message message, String messageText) throws UserInterfaceException {
        if (message.location() == null) {
            throw new UserInterfaceException(null, null);
        }
    }

    @Override
    protected void onInvalidInput(User localUser, Message message, String messageText) {
        mediaManager.messageSendKeyboard(isEnglish ? "Please send a location." : "لطفا یک لوکیشن بفرستید.", message.chat().id(), "/cancel");
    }

    @Override
    protected void onOperation(User localUser, Message message, String messageText) throws UserInterfaceException {

        localUser.setLocationAndAddress(message.location(), new NominatimReverseGeocodingJAPI());
        new TransactionalDBAccess() {
            @Override
            protected void operation(Session session) {
                session.saveOrUpdate(localUser);
            }
        }.execute();

        Location location = localUser.getLocation();

        lcr = LocalChatRoom.loadByCityName(location.getCity());

        if (lcr == null) {
            lcr = new LocalChatRoom(null, location.getCity(), location.getCity(), "به چت روم شهر " + location.getCity() + " خوش آمدید!", "Welcome to" + location.getCity() + " local room!");
            lcr.setLocation(location);
            new TransactionalDBAccess() {
                @Override
                protected void operation(Session session) {
                    session.saveOrUpdate(lcr);
                }
            }.execute();
        }

        localUser.setStatus(UserStatus.InGlobalChat);
        localUser.setGcr(lcr);
        new TransactionalDBAccess() {
            @Override
            protected void operation(Session session) {
                session.update(localUser);
            }
        }.execute();

        List<GlobalPost> recentPosts = GlobalPost.loadRecentPosts(lcr, PropertiesFileManager.getInstance().getGlobalRecentNum());
        new GlobalMediaManager() {
            @Override
            public TelegramBot getBot() {
                return bot;
            }
        }.sendBatchGlobalPost(recentPosts, message.chat().id());

    }

    @Override
    protected void onUnsuccessfullOperation(User localUser, Message message, String messageText) {
        sendMainMenu(chatId, isEnglish);
    }

    @Override
    protected void sendMessageOnSuccess(Long chatId, Boolean isEnglish, MediaManager mediaManager) {
        mediaManager.messageSendKeyboard(isEnglish ? lcr.getEnglishPrompt() : lcr.getPersianPromt(), chatId, isEnglish ? "Leave room" : "خروج");
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
