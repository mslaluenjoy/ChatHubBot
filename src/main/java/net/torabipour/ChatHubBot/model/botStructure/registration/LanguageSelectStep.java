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
import net.torabipour.ChatHubBot.model.utils.UserInterfaceException;

/**
 *
 * @author mohammad
 */
public abstract class LanguageSelectStep extends AbstractRegistrationStep{

    public LanguageSelectStep(Update update) {
        super(update);
    }

    @Override
    protected UserStatus getAbortUserStatus() {
        return UserStatus.LanguageSelect;
    }

    @Override
    protected UserStatus getNextUserStatus() {
        return UserStatus.SexSelect;
    }

    @Override
    protected void onAbort(User localUser, Message message) {
        
    }

    @Override
    protected void onOperation(User localUser, Message message) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    protected void validateInput(Message message) throws UserInterfaceException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    
}
