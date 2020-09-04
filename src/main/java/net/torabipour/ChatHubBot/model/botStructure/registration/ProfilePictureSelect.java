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
public class ProfilePictureSelect extends AbstractRegistrationStep {

    public ProfilePictureSelect(Update update, TelegramBot bot) {
        super(update, bot);
    }

    @Override
    protected UserStatus getAbortUserStatus() {
        return UserStatus.Registered;
    }

    @Override
    protected UserStatus getNextUserStatus() {
        return UserStatus.Registered;
    }

    @Override
    protected void sendMessageOnAbort(Long chatId, Boolean isEnglish, MediaManager mediaManager) {
        sendRegistrationSuccessfull(chatId, isEnglish);
        sendMainMenu(chatId, isEnglish);
    }

    @Override
    protected void onOperation(User localUser, Message message, String messageText) throws UserInterfaceException {
        localUser.setProfilePicture(mediaManager.getFullPath(message.photo()[message.photo().length - 1].fileId()));
        localUser.setExposing(true);
        saveLocalUser();
    }

    @Override
    protected void onAbort(User localUser, Message message, String messageText) {
    }

    @Override
    protected void sendMessageOnSuccess(Long chatId, Boolean isEnglish, MediaManager mediaManager) {
        sendRegistrationSuccessfull(chatId, isEnglish);
        sendMainMenu(chatId, isEnglish);
    }

    @Override
    protected void validateInput(Message message, String messageText) throws UserInterfaceException {
        if (message.photo() == null) {
            throw new UserInterfaceException("مقدار وارد شده برای عکس پروفایل نامعتبر است.", "Invalid input for profile picture.");
        }
    }

    @Override
    protected void onInvalidInput(User localUser, Message message, String messageText) {
        mediaManager.messageSendKeyboard(isEnglish ? "Send your profile picture." : " عکس پروفایل خود را ارسال نمایید.", chatId, "/cancel");
    }

    @Override
    protected void onUnsuccessfullOperation(User localUser, Message message, String messageText) {
        onInvalidInput(localUser, message, messageText);
    }

}
