/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.torabipour.ChatHubBot.model.botStructure;

import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.model.Update;
import net.torabipour.ChatHubBot.model.UserStatus;

/**
 *
 * @author mohammad
 */
public abstract class AbstractRegistrationStep extends AbstractStateHandler {

    protected abstract UserStatus getNextUserStatus();

    @Override
    public Boolean execute() {
        if (super.execute()) {
            localUser.setStatus(getNextUserStatus());
            saveLocalUser();
            return true;
        } else {
            return false;
        }
    }

    public AbstractRegistrationStep(Update update, TelegramBot bot) {
        super(update, bot);
    }

    protected void sendRegistrationSuccessfull(Long chatId, Boolean isEnglish) {
        mediaManager.messageSend(isEnglish
                ? "Registration completed. Enjoy!\n"
                : "فرایند ثبت نام به پایان رسید. از بات لذت ببرید!", chatId);
    }

}
