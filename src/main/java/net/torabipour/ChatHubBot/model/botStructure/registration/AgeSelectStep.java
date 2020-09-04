/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.torabipour.ChatHubBot.model.botStructure.registration;

import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.model.Message;
import com.pengrad.telegrambot.model.Update;
import net.torabipour.ChatHubBot.model.User;
import net.torabipour.ChatHubBot.model.UserStatus;
import net.torabipour.ChatHubBot.model.botStructure.AbstractRegistrationStep;
import net.torabipour.ChatHubBot.model.utils.MediaManager;
import net.torabipour.ChatHubBot.model.utils.UserInterfaceException;

/**
 *
 * @author mohammad
 */
public class AgeSelectStep extends AbstractRegistrationStep {

    public AgeSelectStep(Update update, TelegramBot bot) {
        super(update, bot);
    }

    @Override
    protected UserStatus getAbortUserStatus() {
        return UserStatus.Registered;
    }

    @Override
    protected UserStatus getNextUserStatus() {
        return UserStatus.ProfilePictureSelect;
    }

    @Override
    protected void sendMessageOnAbort(Long chatId, Boolean isEnglish, MediaManager mediaManager) {
        sendRegistrationSuccessfull(chatId, isEnglish);
        sendMainMenu(chatId, isEnglish);
    }

    @Override
    protected void onOperation(User localUser, Message message, String messageText) throws UserInterfaceException {
        localUser.setAge(Integer.parseInt(messageText));
    }

    @Override
    protected void onAbort(User localUser, Message message, String messageText) {

    }

    @Override
    protected void sendMessageOnSuccess(Long chatId, Boolean isEnglish, MediaManager mediaManager) {
        mediaManager.messageSendKeyboard(isEnglish ? "Send your profile picture." : " عکس پروفایل خود را ارسال نمایید.", chatId, "/cancel");
    }

    @Override
    protected void validateInput(Message message, String messageText) throws UserInterfaceException {
        if (messageText == null) {
            throw new UserInterfaceException("مقدار وارد شده برای سن نامعتبر است.", "Invalid input for age.");
        }
        try {
            int age = Integer.parseInt(messageText);
            if (age < 10 || age > 80) {
                throw new Exception();
            }
        } catch (NumberFormatException ex) {
            throw new UserInterfaceException("مقدار وارد شده برای سن نامعتبر است.", "Invalid input for age.");
        } catch (Exception ex) {
            throw new UserInterfaceException("مقدار وارد شده برای سن نامعتبر است.", "Invalid input for age.");
        }
    }

    @Override
    protected void onInvalidInput(User localUser, Message message, String messageText) {
        mediaManager.messageSendKeyboard(isEnglish ? "Send your age." : "سن خود را وارد کنید.", chatId, "/cancel");
    }

    @Override
    protected void onUnsuccessfullOperation(User localUser, Message message, String messageText) {
        onInvalidInput(localUser, message, messageText);
    }

}
