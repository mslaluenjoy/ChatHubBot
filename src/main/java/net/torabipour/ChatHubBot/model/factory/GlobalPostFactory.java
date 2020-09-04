/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.torabipour.ChatHubBot.model.factory;

import com.pengrad.telegrambot.model.Animation;
import com.pengrad.telegrambot.model.Audio;
import com.pengrad.telegrambot.model.Document;
import com.pengrad.telegrambot.model.Location;
import com.pengrad.telegrambot.model.PhotoSize;
import com.pengrad.telegrambot.model.Sticker;
import com.pengrad.telegrambot.model.Video;
import com.pengrad.telegrambot.model.Voice;
import com.pengrad.telegrambot.response.SendResponse;
import java.net.URL;
import net.torabipour.ChatHubBot.model.MessageType;
import net.torabipour.ChatHubBot.model.User;
import net.torabipour.ChatHubBot.model.globalChat.ChatMessage;
import net.torabipour.ChatHubBot.model.globalChat.GlobalChatRoom;
import net.torabipour.ChatHubBot.model.globalChat.GlobalPost;
import net.torabipour.ChatHubBot.model.utils.MediaManager;
import net.torabipour.ChatHubBot.model.utils.UserInterfaceException;
import java.util.Date;
import net.torabipour.ChatHubBot.PropertiesFileManager;
import net.torabipour.ChatHubBot.model.Language;

/**
 *
 * @author mohammad
 */
public class GlobalPostFactory {

    public static GlobalPost create(com.pengrad.telegrambot.model.Message message, User localUser, MediaManager mediaManager, GlobalChatRoom gcr) throws UserInterfaceException {
        GlobalPost gp = new GlobalPost();
        gp.setSender(localUser);
        gp.setCaption(message.caption());
        setContentAndType(message, mediaManager, gp, localUser.getLang() == null || localUser.getLang().equals(Language.English));
        gp.setDate(new Date());
        gp.setGcr(gcr);

        if (message.replyToMessage() != null) {
            ChatMessage cm = ChatMessage.loadByMessageId(message.replyToMessage().messageId());
            if (cm != null) {
                gp.setReplyTo(cm.getGp());
            }
        }

        gp.setTelegramChatId(message.chat().id());
        gp.setTelegramMessageId(message.messageId());
        return gp;
    }

    private static void setContentAndType(com.pengrad.telegrambot.model.Message message, MediaManager mediaManager, GlobalPost gp, Boolean isEnglish) throws UserInterfaceException {
        String messageText = message.text();
        Video vid = message.video();
        PhotoSize[] photo = message.photo();
        Animation anim = message.animation();
        Audio audio = message.audio();
        Document doc = message.document();
        Location location = message.location();
        Sticker sticker = message.sticker();
        Voice voice = message.voice();

        MessageType type = MessageType.Text;
        String content = "";
        if (messageText != null && !messageText.isEmpty()) {
            type = MessageType.Text;
            content = messageText;
            if (isValid(content)) {
                throw new UserInterfaceException("ارسال لینک در روم های گلوبال مجاز نیست." , "Links are not allowed in global rooms.");
            }
        } else if (vid != null) {
            type = MessageType.Video;
            content = mediaManager.getFullPath(vid.fileId());
        } else if (photo != null) {
            type = MessageType.Photo;
            content = mediaManager.getFullPath(photo[photo.length - 1].fileId());
        } else if (anim != null) {
            type = MessageType.Animation;
            content = mediaManager.getFullPath(anim.fileId());
        } else if (audio != null) {
            type = MessageType.Audio;
            content = mediaManager.getFullPath(audio.fileId());
        } else if (doc != null) {
            type = MessageType.Document;
            content = mediaManager.getFullPath(doc.fileId());
        } else if (location != null) {
            type = MessageType.Location;
            String lat = String.valueOf(location.latitude());
            String longi = String.valueOf(location.longitude());
            content = lat + "-" + longi;
        } else if (sticker != null) {
            type = MessageType.Sticker;
            content = mediaManager.getFullPath(sticker.fileId());
        } else if (voice != null) {
            type = MessageType.Voice;
            content = mediaManager.getFullPath(voice.fileId());
        }

        gp.setContent(content);
        gp.setType(type);
    }

    public static boolean isValid(String url) {
        
        if(url == null || url.contains(PropertiesFileManager.getInstance().getBotName())){
            return false;
        }
        
        /* Try creating a valid URL */
        try {
            new URL(url).toURI();
            return true;
        } // If there was an Exception 
        // while creating URL object 
        catch (Exception e) {
            return false;
        }
    }
}
