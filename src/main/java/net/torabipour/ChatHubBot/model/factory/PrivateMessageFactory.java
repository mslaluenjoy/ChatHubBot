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
import net.torabipour.ChatHubBot.model.MessageType;
import net.torabipour.ChatHubBot.model.User;
import net.torabipour.ChatHubBot.model.anonChat.Message;
import net.torabipour.ChatHubBot.model.anonChat.PrivateMessage;
import net.torabipour.ChatHubBot.model.globalChat.ChatMessage;
import net.torabipour.ChatHubBot.model.globalChat.GlobalChatRoom;
import net.torabipour.ChatHubBot.model.globalChat.GlobalPost;
import net.torabipour.ChatHubBot.model.utils.MediaManager;
import net.torabipour.ChatHubBot.model.utils.UserInterfaceException;
import java.util.Date;

/**
 *
 * @author mohammad
 */
public class PrivateMessageFactory {
    
    
    public static PrivateMessage create(com.pengrad.telegrambot.model.Message message, User localUser, MediaManager mediaManager) throws UserInterfaceException{
        Message lastPm = PrivateMessage.loadBySender(localUser);
        PrivateMessage gp = new PrivateMessage(null, localUser, lastPm.getReceiver(), null, null, null, null, null);
        gp.setCaption(message.caption());
        setContentAndType(message, mediaManager, gp);
        return gp;
    }
    
    private static void setContentAndType(com.pengrad.telegrambot.model.Message message, MediaManager mediaManager, PrivateMessage gp) throws UserInterfaceException{
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
}
