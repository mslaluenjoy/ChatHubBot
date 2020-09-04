/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.torabipour.ChatHubBot.model.botStructure.registration;

import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.model.Message;
import com.pengrad.telegrambot.model.Update;
import java.util.List;
import net.torabipour.ChatHubBot.db.TransactionalDBAccess;
import net.torabipour.ChatHubBot.model.User;
import net.torabipour.ChatHubBot.model.UserStatus;
import net.torabipour.ChatHubBot.model.botStructure.AbstractRegistrationStep;
import net.torabipour.ChatHubBot.model.utils.MediaManager;
import net.torabipour.ChatHubBot.model.utils.UserInterfaceException;
import org.hibernate.Session;

/**
 *
 * @author mohammad
 */
public class ExposingSelectStep extends AbstractRegistrationStep {

    public ExposingSelectStep(Update update, TelegramBot bot) {
        super(update, bot);
    }

    @Override
    protected List<String> getAbortPhrases() {
        List<String> abort = super.getAbortPhrases();
        abort.add("No");
        abort.add("نه");
        return abort;
    }

    @Override
    protected UserStatus getAbortUserStatus() {
        return UserStatus.Registered;
    }

    @Override
    protected UserStatus getNextUserStatus() {
        return UserStatus.LocationSelect;
    }

    @Override
    protected void sendMessageOnAbort(Long chatId, Boolean isEnglish, MediaManager mediaManager) {
        sendRegistrationSuccessfull(chatId, isEnglish);
        sendMainMenu(chatId, isEnglish);
    }

    @Override
    protected void onOperation(User localUser, Message message, String messageText) throws UserInterfaceException {
        localUser.setExposing(true);
        saveLocalUser();
    }

    @Override
    protected void sendMessageOnSuccess(Long chatId, Boolean isEnglish, MediaManager mediaManager) {
        mediaManager.locationRequestSend(isEnglish ? "Send your current location." : "موقعیت جغرافیایی خود را ارسال نمایید.",
                chatId, isEnglish ? "Send Location" : "ارسال موقعیت", isEnglish);
    }

    @Override
    protected void validateInput(Message message, String messageText) throws UserInterfaceException {
        if (!"Yes".equals(messageText) && !"بله".equals(messageText)) {
            throw new UserInterfaceException(null, null);
        }
    }

    @Override
    protected void onAbort(User localUser, Message message, String messageText) {
    }

    @Override
    protected void onInvalidInput(User localUser, Message message, String messageText) {
        mediaManager.messageSendKeyboard(isEnglish ? ("Do you want your profile be visible to other users? if you tap yes then you will appear in"
                + " nearby people search result and also can search for other people around you.")
                : ("آیا مایل هستید که اطلاعات پروفایل شما با سایر کاربران به اشتراک گذاشته شود؟"
                + " درصورت موافقت شما می توانید از امکان جستجوی افراد نزدیک استفاده کنید."),
                chatId, new String[]{isEnglish ? "Yes" : "بله", isEnglish ? "No" : "نه"});
    }

    @Override
    protected void onUnsuccessfullOperation(User localUser, Message message, String messageText) {
        onInvalidInput(localUser, message, messageText);
    }

}
