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
import net.torabipour.ChatHubBot.model.Language;
import net.torabipour.ChatHubBot.model.Sex;
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
public class SexSelectStep extends AbstractRegistrationStep {

    public SexSelectStep(Update update, TelegramBot bot) {
        super(update, bot);
    }

    @Override
    protected UserStatus getAbortUserStatus() {
        return UserStatus.SexSelect;
    }

    @Override
    protected UserStatus getNextUserStatus() {
        return UserStatus.NickNameSelect;
    }

    @Override
    protected void sendMessageOnAbort(Long chatId, Boolean isEnglish, MediaManager mediaManager) {
        mediaManager.messageSendKeyboard(isEnglish ? "Select your sex." : "جنسیت خود را انتخاب کنید.", chatId,
                new String[]{isEnglish ? "Male 👨‍🦱" : "مرد 👨‍🦱", isEnglish ? "Female 👩" : "زن 👩"});
    }

    @Override
    protected void onOperation(User localUser, Message message, String messageText) throws UserInterfaceException {
        String val = messageText.split(" ")[0];
        Sex sex = ("مرد".equals(val) || "Male".equals(val)) ? Sex.Male : Sex.Female;
        localUser.setSex(sex);
        saveLocalUser();
    }

    @Override
    protected void sendMessageOnSuccess(Long chatId, Boolean isEnglish, MediaManager mediaManager) {
        mediaManager.messageSend(isEnglish
                ? "Choose yourself a user name which is shown to other users. \n"
                : "برای خود نام کاربری انتخاب کنید. \n توجه کنید که این نام قابل مشاهده توسط سایر کاربران این ربات خواهد بود.", chatId);
    }

    @Override
    protected void validateInput(Message message, String messageText) throws UserInterfaceException {
        if (messageText == null || messageText.split(" ").length != 2) {
            throw new UserInterfaceException("مقدار وارد شده اشتباه است.", "Invalid input for sex.");
        }
        String sexPharase = messageText.split(" ")[0];
        if (!"مرد".equals(sexPharase) && !"زن".equals(sexPharase) && !"Male".equals(sexPharase) && !"Female".equals(sexPharase)) {
            throw new UserInterfaceException("مقدار وارد شده اشتباه است.", "Invalid input for sex.");
        }
    }

    @Override
    protected void onAbort(User localUser, Message message, String messageText) {
    }

    @Override
    protected void onInvalidInput(User localUser, Message message, String messageText) {
        mediaManager.messageSendKeyboard(isEnglish ? "Select your sex." : "جنسیت خود را انتخاب کنید.", chatId,
                new String[]{isEnglish ? "Male 👨‍🦱" : "مرد 👨‍🦱", isEnglish ? "Female 👩" : "زن 👩"});
    }

    @Override
    protected void onUnsuccessfullOperation(User localUser, Message message, String messageText) {
        onInvalidInput(localUser, message, messageText);
    }

}
