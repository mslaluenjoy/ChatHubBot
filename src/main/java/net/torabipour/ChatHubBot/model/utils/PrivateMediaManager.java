/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.torabipour.ChatHubBot.model.utils;

import com.pengrad.telegrambot.request.AbstractSendRequest;
import com.pengrad.telegrambot.request.SendAnimation;
import com.pengrad.telegrambot.request.SendAudio;
import com.pengrad.telegrambot.request.SendDocument;
import com.pengrad.telegrambot.request.SendLocation;
import com.pengrad.telegrambot.request.SendMessage;
import com.pengrad.telegrambot.request.SendPhoto;
import com.pengrad.telegrambot.request.SendSticker;
import com.pengrad.telegrambot.request.SendVideo;
import com.pengrad.telegrambot.request.SendVoice;
import net.torabipour.ChatHubBot.model.MessageType;
import net.torabipour.ChatHubBot.model.anonChat.PrivateMessage;
import net.torabipour.ChatHubBot.model.globalChat.GlobalPost;

/**
 *
 * @author mohammad
 */
public abstract class PrivateMediaManager extends MediaManager{
    public AbstractSendRequest sendPrivateMessage(PrivateMessage gp, Long chatId, Integer replyTo) throws UserInterfaceException {
        AbstractSendRequest request = null;
        MessageType type = gp.getType();
        String caption = gp.getCaption();
        switch (type) {
            case Animation:
                request = new SendAnimation(chatId, downloadUrl(gp.getContent())).caption(caption);
                break;
            case Audio:
                request = new SendAudio(chatId, downloadUrl(gp.getContent())).caption(caption);
                break;
            case Document:
                request = new SendDocument(chatId, downloadUrl(gp.getContent())).caption(caption);
                break;
            case Location:
                String content = gp.getContent();
                String latStr = content.split("-")[0];
                String longStr = content.split("-")[1];
                request = new SendLocation(chatId, Float.valueOf(latStr), Float.valueOf(longStr));
                break;
            case Photo:
                request = new SendPhoto(chatId, downloadUrl(gp.getContent())).caption(caption);
                break;
            case Sticker:
                request = new SendSticker(chatId, downloadUrl(gp.getContent()));
                break;
            case Text:
                request = new SendMessage(chatId, gp.getContent());
                break;
            case Video:
                request = new SendVideo(chatId, downloadUrl(gp.getContent())).caption(caption);
                break;
            case Voice:
                request = new SendVoice(chatId, downloadUrl(gp.getContent())).caption(caption);
                break;
        }

        if (replyTo != null) {
            request = request.replyToMessageId(replyTo);
        }

        return request;
    }
}
