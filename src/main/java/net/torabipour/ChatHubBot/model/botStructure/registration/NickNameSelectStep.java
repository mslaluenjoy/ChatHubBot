/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.torabipour.ChatHubBot.model.botStructure.registration;

import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.model.Message;
import com.pengrad.telegrambot.model.Update;
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
public class NickNameSelectStep extends AbstractRegistrationStep {

    public NickNameSelectStep(Update update, TelegramBot bot) {
        super(update, bot);
    }

    @Override
    protected UserStatus getAbortUserStatus() {
        return UserStatus.NickNameSelect;
    }

    @Override
    protected UserStatus getNextUserStatus() {
        return UserStatus.ExposingSelect;
    }

    @Override
    protected void sendMessageOnAbort(Long chatId, Boolean isEnglish, MediaManager mediaManager) {
        mediaManager.messageSend(isEnglish
                ? "Choose yourself a user name which is shown to other users. \n"
                : "برای خود نام کاربری انتخاب کنید. \n توجه کنید که این نام قابل مشاهده توسط سایر کاربران این ربات خواهد بود.", chatId);
    }

    @Override
    protected void onOperation(User localUser, Message message, String messageText) throws UserInterfaceException {
        localUser.setNickName(messageText);
        saveLocalUser();
    }

    @Override
    protected void sendMessageOnSuccess(Long chatId, Boolean isEnglish, MediaManager mediaManager) {
        mediaManager.messageSendKeyboard(isEnglish ? ("Do you want your profile be visible to other users? if you tap yes then you will appear in"
                + " nearby people search result and also can search for other people around you.")
                : ("آیا مایل هستید که اطلاعات پروفایل شما با سایر کاربران به اشتراک گذاشته شود؟"
                + " درصورت موافقت شما می توانید از امکان جستجوی افراد نزدیک استفاده کنید."),
                chatId, new String[]{isEnglish ? "Yes" : "بله", isEnglish ? "No" : "نه"});
    }

    @Override
    protected void validateInput(Message message, String messageText) throws UserInterfaceException {
        if (messageText == null || messageText.contains("Male") || messageText.contains("Female") || messageText.contains("زن") || messageText.contains("مرد") || messageText.contains("/") || isUrl(messageText)) {
            throw new UserInterfaceException("مقدار وارد شده اشتباه است یا مجاز نیست.", "Invalid input for nickname.");
        }
        if (messageText.length() > 10) {
            throw new UserInterfaceException("طول مجاز برای نام مستعار حداکثر ۱۰ کاراکتر است.", "Invalid input for nickname. maximum allowed length is 10.");
        }
    }

    @Override
    protected void onAbort(User localUser, Message message, String messageText) {
    }

    @Override
    protected void onInvalidInput(User localUser, Message message, String messageText) {
        mediaManager.messageSend(isEnglish
                ? "Choose yourself a user name which is shown to other users. \n"
                : "برای خود نام کاربری انتخاب کنید. \n توجه کنید که این نام قابل مشاهده توسط سایر کاربران این ربات خواهد بود.", chatId);
    }

    @Override
    protected void onUnsuccessfullOperation(User localUser, Message message, String messageText) {
        onInvalidInput(localUser, message, messageText);
    }

}
