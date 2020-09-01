/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.torabipour.ChatHubBot.model.utils;

import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.model.Animation;
import com.pengrad.telegrambot.model.Audio;
import com.pengrad.telegrambot.model.Document;
import com.pengrad.telegrambot.model.Location;
import com.pengrad.telegrambot.model.Message;
import com.pengrad.telegrambot.model.PhotoSize;
import com.pengrad.telegrambot.model.Sticker;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.model.Video;
import com.pengrad.telegrambot.model.Voice;
import com.pengrad.telegrambot.response.SendResponse;
import net.torabipour.ChatHubBot.model.anonChat.Chat;
import net.torabipour.ChatHubBot.model.MessageType;
import net.torabipour.ChatHubBot.model.User;
import net.torabipour.ChatHubBot.model.factory.MessageFactory;

/**
 *
 * @author mohammad
 */
public abstract class AnonymousChatHandler {

    private MediaManager mediaManager;

    public AnonymousChatHandler() {
        mediaManager = new MediaManager() {
            @Override
            public TelegramBot getBot() {
                return AnonymousChatHandler.this.getBot();
            }
        };
    }

     public void copyMessageAndForward(Long chatId) throws UserInterfaceException{
        com.pengrad.telegrambot.model.Message message = getUpdate().message();
        com.pengrad.telegrambot.model.User user = message.from();
        String messageText = message.text();
        Message replyToMessage = message.replyToMessage();

        String caption = message.caption();
        if (caption == null) {
            caption = "";
        }
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
        SendResponse response = null;
        if (replyToMessage == null) {
            if (messageText != null && !messageText.isEmpty()) {
                response = mediaManager.messageSend(messageText, chatId);
                type = MessageType.Text;
                content = messageText;
            } else if (vid != null) {
                response = mediaManager.sendVideo(chatId, vid, caption);
                type = MessageType.Video;
                content = mediaManager.getFullPath(vid.fileId());
            } else if (photo != null) {
                response = mediaManager.sendPhoto(chatId, photo, caption);
                type = MessageType.Photo;
                content = mediaManager.getFullPath(photo[photo.length - 1].fileId());
            } else if (anim != null) {
                response = mediaManager.sendAnim(chatId, anim);
                type = MessageType.Animation;
                content = mediaManager.getFullPath(anim.fileId());
            } else if (audio != null) {
                response = mediaManager.sendAudio(chatId, audio, caption);
                type = MessageType.Audio;
                content = mediaManager.getFullPath(audio.fileId());
            } else if (doc != null) {
                response = mediaManager.sendDoc(chatId, doc, caption);
                type = MessageType.Document;
                content = mediaManager.getFullPath(doc.fileId());
            } else if (location != null) {
                response = mediaManager.sendLoc(chatId, location);
                type = MessageType.Location;
                content = location.toString();
            } else if (sticker != null) {
                response = mediaManager.sendSticker(chatId, sticker);
                type = MessageType.Sticker;
                content = mediaManager.getFullPath(sticker.fileId());
            } else if (voice != null) {
                response = mediaManager.sendVoice(chatId, voice, caption);
                type = MessageType.Voice;
                content = mediaManager.getFullPath(voice.fileId());
            }
        } else {
            Integer originalReply = getReceiveIdBySendId(replyToMessage.messageId());
            if (messageText != null && !messageText.isEmpty()) {
                mediaManager.messageSendReply(messageText, chatId, originalReply);
                type = MessageType.Text;
                content = messageText;
            } else if (vid != null) {
                mediaManager.sendVideoReply(chatId, vid, originalReply, caption);
                type = MessageType.Video;
                content = mediaManager.getFullPath(vid.fileId());

            } else if (photo != null) {
                mediaManager.sendPhotoReply(chatId, photo, originalReply, caption);
                type = MessageType.Photo;
                content = mediaManager.getFullPath(photo[photo.length - 1].fileId());

            } else if (anim != null) {
                mediaManager.sendAnimReply(chatId, anim, originalReply);
                type = MessageType.Animation;
                content = mediaManager.getFullPath(anim.fileId());

            } else if (audio != null) {
                mediaManager.sendAudioReply(chatId, audio, originalReply, caption);
                type = MessageType.Audio;
                content = mediaManager.getFullPath(audio.fileId());

            } else if (doc != null) {
                mediaManager.sendDocReply(chatId, doc, originalReply, caption);
                type = MessageType.Document;
                content = mediaManager.getFullPath(doc.fileId());

            } else if (location != null) {
                mediaManager.sendLocReply(chatId, location, originalReply);
                type = MessageType.Location;
                content = location.toString();

            } else if (sticker != null) {
                mediaManager.sendStickerReply(chatId, sticker, originalReply);
                type = MessageType.Sticker;
                content = mediaManager.getFullPath(sticker.fileId());

            } else if (voice != null) {
                mediaManager.sendVoiceReply(chatId, voice, originalReply, caption);
                type = MessageType.Voice;
                content = mediaManager.getFullPath(voice.fileId());

            }
        }

        MessageFactory.createAndSave(content, type, response.message().messageId(), message.messageId(), getCurrentChat(), getLocalUser());

    }

    public int getReceiveIdBySendId(int sendId) {
        int receiveId;
        net.torabipour.ChatHubBot.model.anonChat.Message msg = net.torabipour.ChatHubBot.model.anonChat.Message.loadBySendId(sendId);
        if (msg == null) {
            msg = net.torabipour.ChatHubBot.model.anonChat.Message.loadByReceiveId(sendId);
            if (msg == null) {
                receiveId = sendId;
            } else {
                receiveId = msg.getSendId();
            }
        } else {
            receiveId = msg.getReceiveId();
        }
        return receiveId;
    }

    public abstract TelegramBot getBot();

    public abstract Update getUpdate();

    public abstract Chat getCurrentChat();

    public abstract User getLocalUser();
}
