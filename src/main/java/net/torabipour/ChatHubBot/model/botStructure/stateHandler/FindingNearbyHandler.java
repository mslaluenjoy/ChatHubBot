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
import net.torabipour.ChatHubBot.db.TransactionalDBAccess;
import net.torabipour.ChatHubBot.model.Sex;
import net.torabipour.ChatHubBot.model.User;
import net.torabipour.ChatHubBot.model.UserStatus;
import net.torabipour.ChatHubBot.model.botStructure.AbstractStateHandler;
import net.torabipour.ChatHubBot.model.utils.MediaManager;
import net.torabipour.ChatHubBot.model.utils.UserInterfaceException;
import net.torabipour.ChatHubBot.model.utils.UserMediaManager;
import org.hibernate.Session;
import org.slf4j.LoggerFactory;

/**
 *
 * @author mohammad
 */
public class FindingNearbyHandler extends AbstractStateHandler {

    public FindingNearbyHandler(Update update, TelegramBot bot) {
        super(update, bot);
    }

    @Override
    protected List<String> getAbortPhrases() {
        List<String> aborts = super.getAbortPhrases();
        aborts.add("Back");
        aborts.add("بازگشت");
        return aborts;
    }

    @Override
    protected void validateInput(Message message, String messageText) throws UserInterfaceException {
        if(localUser.getLocation() == null || localUser.getLocation().getCity() == null){
            throw new UserInterfaceException("جهت استفاده از این ویژگی باید موقعیت جغرافیایی خود را از طریق پنل ویرایش پروفایل ست کنید.", "To use this feature you have to specify your location in your profile via Edit Profile button.");
        }
    }

    @Override
    protected void onInvalidInput(User localUser, Message message, String messageText) {
    }

    @Override
    protected void onOperation(User localUser, Message message, String messageText) throws UserInterfaceException {
        if (messageText != null && messageText.contains("/view")) {
            try {
                Long targetId = Long.valueOf(messageText.replaceAll("/view", ""));
                User view = User.loadById(targetId);
                bot.execute(new UserMediaManager() {
                    @Override
                    public TelegramBot getBot() {
                        return bot;
                    }
                }.generateUserProfile(view, isEnglish, message.chat().id()));
                localUser.setStatus(UserStatus.ViewingProfile);
                new TransactionalDBAccess() {
                    @Override
                    protected void operation(Session session) {
                        session.saveOrUpdate(localUser);
                    }
                }.execute();
            } catch (Exception ex) {
                LoggerFactory.getLogger(FindingNearbyHandler.class).error("Error", ex);
                throw new UserInterfaceException("خطا در مشاهده پروفایل", "Unable to view profile");
            }
            return;
        }

        List<User> usersList;
        if (messageText.contains("Male") || messageText.contains("مرد")) {
            usersList = User.loadAllExposingByCityAndSex(localUser.getLocation().getCity(), Sex.Male);
        } else if (messageText.contains("Female") || messageText.contains("زن")) {
            usersList = User.loadAllExposingByCityAndSex(localUser.getLocation().getCity(), Sex.Female);
        } else {
            usersList = User.loadAllExposingByCity(localUser.getLocation().getCity());
        }
        usersList = usersList.stream().filter(x -> !x.getId().equals(localUser.getId())).collect(Collectors.toList());
        if (usersList.isEmpty()) {
            localUser.setStatus(UserStatus.Registered);
            new TransactionalDBAccess() {
                @Override
                protected void operation(Session session) {
                    session.saveOrUpdate(localUser);
                }
            }.execute();
            mediaManager.messageSend(isEnglish ? "No result found." : "کسی پیدا نشد.", message.chat().id());
            sendMainMenu(message.chat().id(), isEnglish);
            return;
        }
        bot.execute(new UserMediaManager() {
            @Override
            public TelegramBot getBot() {
                return bot;
            }
        }.getUsersListView(usersList, message.chat().id(), isEnglish));
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

    }

    @Override
    protected void sendMessageOnAbort(Long chatId, Boolean isEnglish, MediaManager mediaManager) {
        sendMainMenu(chatId, isEnglish);
    }

}
