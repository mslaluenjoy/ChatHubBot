/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.torabipour.ChatHubBot.model.botStructure;

import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.model.Update;
import net.torabipour.ChatHubBot.model.UserStatus;
import net.torabipour.ChatHubBot.model.botStructure.registration.AgeSelectStep;
import net.torabipour.ChatHubBot.model.botStructure.registration.ExposingSelectStep;
import net.torabipour.ChatHubBot.model.botStructure.registration.LanguageSelectStep;
import net.torabipour.ChatHubBot.model.botStructure.registration.LocationSelectStep;
import net.torabipour.ChatHubBot.model.botStructure.registration.NickNameSelectStep;
import net.torabipour.ChatHubBot.model.botStructure.registration.ProfilePictureSelect;
import net.torabipour.ChatHubBot.model.botStructure.registration.SexSelectStep;
import net.torabipour.ChatHubBot.model.botStructure.stateHandler.AcceptingChatRequestHandler;
import net.torabipour.ChatHubBot.model.botStructure.stateHandler.FindingNearbyHandler;
import net.torabipour.ChatHubBot.model.botStructure.stateHandler.InChatHandler;
import net.torabipour.ChatHubBot.model.botStructure.stateHandler.InGlobalChatHandler;
import net.torabipour.ChatHubBot.model.botStructure.stateHandler.JoiningLocalRoomHandler;
import net.torabipour.ChatHubBot.model.botStructure.stateHandler.RegisteredHandler;
import net.torabipour.ChatHubBot.model.botStructure.stateHandler.SelectingGlobalChatHandler;
import net.torabipour.ChatHubBot.model.botStructure.stateHandler.SendingAnonMessageHandler;
import net.torabipour.ChatHubBot.model.botStructure.stateHandler.ViewingProfileHandler;
import net.torabipour.ChatHubBot.model.botStructure.stateHandler.WaitingForConnectionHandler;

/**
 *
 * @author mohammad
 */
public class StateHandlerFactory {

    public static AbstractStateHandler getHandler(UserStatus status, TelegramBot bot, Update update) {
        switch (status) {
            case LanguageSelect:
                return new LanguageSelectStep(update, bot);
            case SexSelect:
                return new SexSelectStep(update, bot);
            case NickNameSelect:
                return new NickNameSelectStep(update, bot);
            case ExposingSelect:
                return new ExposingSelectStep(update, bot);
            case LocationSelect:
                return new LocationSelectStep(update, bot);
            case AgeSelect:
                return new AgeSelectStep(update, bot);
            case ProfilePictureSelect:
                return new ProfilePictureSelect(update, bot);
            case WaitingForConnection:
                return new WaitingForConnectionHandler(update, bot);
            case InChat:
                return new InChatHandler(update, bot);
            case Registered:
                return new RegisteredHandler(update, bot);
            case ViewingProfile:
                return new ViewingProfileHandler(update, bot);
            case FindingNearby:
                return new FindingNearbyHandler(update, bot);
            case JoiningLocalRoom:
                return new JoiningLocalRoomHandler(update, bot);
            case SendingAnonMessage:
                return new SendingAnonMessageHandler(update, bot);
            case SelectingGlobalChat:
                return new SelectingGlobalChatHandler(update, bot);
            case InGlobalChat:
                return new InGlobalChatHandler(update, bot);
            case AcceptingChatRequest:
                return new AcceptingChatRequestHandler(update, bot);
            default:
                return null;
        }
    }
}
