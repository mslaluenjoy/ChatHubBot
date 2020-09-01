/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.torabipour.ChatHubBot.model.utils;

import com.pengrad.telegrambot.model.request.ReplyKeyboardMarkup;
import com.pengrad.telegrambot.request.SendMessage;
import com.pengrad.telegrambot.request.SendPhoto;
import net.torabipour.ChatHubBot.model.Sex;
import net.torabipour.ChatHubBot.model.User;
import java.util.List;

/**
 *
 * @author mohammad
 */
public abstract class UserMediaManager extends MediaManager {

    public SendMessage getUsersListView(List<User> toBeShown, Long chatId, Boolean isEnglish) {
        return new SendMessage(chatId, generateUserList(toBeShown, isEnglish)).replyMarkup(new ReplyKeyboardMarkup(new String[]{isEnglish ? "Back" : "بازگشت"}));
    }

    protected String generateUserList(List<User> users, Boolean isEnglish) {
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < users.size(); i++) {
            result.append(generateUserHeader(users.get(i), i, isEnglish));
            result.append("\n\n");
        }
        return result.toString();
    }

    protected StringBuilder generateUserHeader(User user, int index, Boolean isEnglish) {
        StringBuilder sb = new StringBuilder();
        sb.append(index + 1);
        sb.append(" - /view");
        sb.append(user.getId());
        sb.append(isEnglish ? " Name: " : " نام: ");
        sb.append(user.getNickName());
        sb.append("  ");
        sb.append(user.getSex().equals(Sex.Male) ? "🙎🏻‍♂️" : "🙍🏻‍♀️");
        sb.append("  ");
        sb.append(isEnglish ? " status: " : " وضعیت: ");
        sb.append(isEnglish ? user.getStatus().getEnglish() : user.getStatus().getPersian());
        return sb;
    }

    protected String generateUserCaption(User user, Boolean isEnglish) {
        StringBuilder sb = new StringBuilder();
        sb.append(user.getSex().equals(Sex.Male) ? "🙎🏻‍♂️" : "🙍🏻‍♀️");
        sb.append("\n");

        sb.append(isEnglish ? " Name: " : " نام: ");
        sb.append(user.getNickName());
        sb.append("\n");

        sb.append(isEnglish ? " Age: " : " سن: ");
        sb.append(user.getAge());
        sb.append("\n");

        sb.append(isEnglish ? " status: " : " وضعیت: ");
        sb.append(isEnglish ? user.getStatus().getEnglish() : user.getStatus().getPersian());
        sb.append("\n");

        return sb.toString();
    }

    public SendPhoto generateUserProfile(User user, Boolean isEnglish, Long chatId) throws UserInterfaceException {
        return new SendPhoto(chatId, downloadUrl(user.getProfilePicture())).caption(generateUserCaption(user, isEnglish))
                .replyMarkup(new ReplyKeyboardMarkup(new String[]{isEnglish ? "Back" : "بازگشت", isEnglish ? "Main Menu" : "منوی اصلی"},
                new String[]{isEnglish ? ("Send message " + user.getId()) : ("ارسال پیام " + user.getId())},
                new String[]{isEnglish ? ("Chat request " + user.getId()) : ("درخواست چت " + user.getId())
                }));
    }
}
