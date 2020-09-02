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
import com.pengrad.telegrambot.model.PhotoSize;
import com.pengrad.telegrambot.model.Sticker;
import com.pengrad.telegrambot.model.Video;
import com.pengrad.telegrambot.model.Voice;
import com.pengrad.telegrambot.model.request.KeyboardButton;
import com.pengrad.telegrambot.model.request.ReplyKeyboardMarkup;
import com.pengrad.telegrambot.request.GetFile;
import com.pengrad.telegrambot.request.SendAnimation;
import com.pengrad.telegrambot.request.SendAudio;
import com.pengrad.telegrambot.request.SendDocument;
import com.pengrad.telegrambot.request.SendLocation;
import com.pengrad.telegrambot.request.SendMessage;
import com.pengrad.telegrambot.request.SendPhoto;
import com.pengrad.telegrambot.request.SendSticker;
import com.pengrad.telegrambot.request.SendVideo;
import com.pengrad.telegrambot.request.SendVoice;
import com.pengrad.telegrambot.response.SendResponse;
import net.torabipour.ChatHubBot.PropertiesFileManager;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author mohammad
 */
public abstract class MediaManager {

    public SendResponse messageSendKeyboardReply(String messageText, Long chatId, Integer replyMessageId, String... buttonValues) {
        return getBot().execute(new SendMessage(chatId, messageText).replyMarkup(new ReplyKeyboardMarkup(
                buttonValues
        )
                .oneTimeKeyboard(true)
                .resizeKeyboard(true)
                .selective(true)).replyToMessageId(replyMessageId));
    }

    public SendResponse locationRequestSend(String messageText, Long chatId, String buttonValue, Boolean isEnglish) {
        return getBot().execute(new SendMessage(chatId, messageText).replyMarkup(new ReplyKeyboardMarkup(
                new KeyboardButton[]{getRequestLocation(buttonValue), new KeyboardButton(isEnglish ? "Nevermind" : "بی خیال")}
        )
                .oneTimeKeyboard(true)
                .resizeKeyboard(true)
                .selective(true)));
    }

    public KeyboardButton getRequestLocation(String buttonValue) {
        return new KeyboardButton(buttonValue).requestLocation(true);
    }

    public SendResponse messageSendReply(String messageText, Long chatId, int reply) {
        try {
            SendResponse response = getBot().execute(new SendMessage(chatId, messageText).replyToMessageId(reply));
            return response;
        } catch (Exception ex) {
            return messageSend(messageText, chatId);
        }
    }

    public SendResponse messageSend(String messageText, Long chatId) {
        SendResponse response = getBot().execute(new SendMessage(chatId, messageText));
        return response;

    }

    public SendResponse messageSendKeyboard(String messageText, Long chatId, String... buttonValues) {
        return getBot().execute(new SendMessage(chatId, messageText).replyMarkup(new ReplyKeyboardMarkup(
                buttonValues
        )
                .oneTimeKeyboard(true)
                .resizeKeyboard(true)
                .selective(true)));
    }

    public SendResponse messageSendKeyboard(String messageText, Long chatId, int perRow, String... buttonValues) {

        List<String> list = Arrays.asList(buttonValues);
        List<ArrayList<String>> result = new ArrayList<>();

        for (int i = 0; i < list.size(); i++) {
            try {
                result.get(i / perRow).add(list.get(i));
            } catch (Exception ex) {
                result.add(new ArrayList<>(Arrays.asList(list.get(i))));
            }
        }

        String[][] array = new String[result.size()][];
        for (int i = 0; i < result.size(); i++) {
            List<String> row = result.get(i);
            array[i] = row.toArray(new String[row.size()]);
        }

        return getBot().execute(new SendMessage(chatId, messageText).replyMarkup(new ReplyKeyboardMarkup(
                array
        )
                .oneTimeKeyboard(true)
                .resizeKeyboard(true)
                .selective(true)));
    }

    public SendResponse messageSendKeyboard(String messageText, Long chatId, String[]... buttonValues) {
        return getBot().execute(new SendMessage(chatId, messageText).replyMarkup(new ReplyKeyboardMarkup(
                buttonValues
        )
                .oneTimeKeyboard(true)
                .resizeKeyboard(true)
                .selective(true)));
    }

    public SendResponse sendVideoReply(Long chatId, Video vid, int reply, String caption) throws UserInterfaceException {
        SendResponse response = getBot().execute(new SendVideo(chatId, download(vid.fileId())).replyToMessageId(reply).caption(caption));
        return response;
    }

    public SendResponse sendPhotoReply(Long chatId, PhotoSize[] photo, int reply, String caption) throws UserInterfaceException {
        SendResponse response = getBot().execute(new SendPhoto(chatId, download(photo[photo.length - 1].fileId())).replyToMessageId(reply).caption(caption));
        return response;
    }

    public SendResponse sendAnimReply(Long chatId, Animation anim, int reply) throws UserInterfaceException {
        SendResponse response = getBot().execute(new SendAnimation(chatId, download(anim.fileId())).replyToMessageId(reply));
        return response;
    }

    public SendResponse sendAudioReply(Long chatId, Audio audio, int reply, String caption) throws UserInterfaceException {
        SendResponse response = getBot().execute(new SendAudio(chatId, download(audio.fileId())).replyToMessageId(reply).caption(caption));
        return response;
    }

    public SendResponse sendDocReply(Long chatId, Document doc, int reply, String caption) throws UserInterfaceException {
        SendResponse response = getBot().execute(new SendDocument(chatId, download(doc.fileId())).replyToMessageId(reply).caption(caption));
        return response;
    }

    public SendResponse sendLocReply(Long chatId, Location loc, int reply) {
        return getBot().execute(new SendLocation(chatId, loc.latitude(), loc.latitude()));
    }

    public SendResponse sendStickerReply(Long chatId, Sticker sticker, int reply) throws UserInterfaceException {
        SendResponse response = getBot().execute(new SendSticker(chatId, download(sticker.fileId())).replyToMessageId(reply));
        return response;
    }

    public SendResponse sendVoiceReply(Long chatId, Voice voice, int reply, String caption) throws UserInterfaceException {
        SendResponse response = getBot().execute(new SendVoice(chatId, download(voice.fileId())).replyToMessageId(reply).caption(caption));
        return response;
    }

    public SendResponse sendVideo(Long chatId, Video vid, String caption) throws UserInterfaceException {
        SendResponse response = getBot().execute(new SendVideo(chatId, download(vid.fileId())).caption(caption));
        return response;
    }

    public SendResponse sendPhoto(Long chatId, PhotoSize[] photo, String caption) throws UserInterfaceException {
        SendResponse response = getBot().execute(new SendPhoto(chatId, download(photo[photo.length - 1].fileId())).caption(caption));
        return response;
    }

    public SendResponse sendAnim(Long chatId, Animation anim) throws UserInterfaceException {
        SendResponse response = getBot().execute(new SendAnimation(chatId, download(anim.fileId(), ".gif")));
        return response;
    }

    public SendResponse sendAudio(Long chatId, Audio audio, String caption) throws UserInterfaceException {
        SendResponse response = getBot().execute(new SendAudio(chatId, download(audio.fileId())).caption(caption));
        return response;
    }

    public SendResponse sendDoc(Long chatId, Document doc, String caption) throws UserInterfaceException {
        SendResponse response = getBot().execute(new SendDocument(chatId, download(doc.fileId())));
        return response;
    }

    public SendResponse sendLoc(Long chatId, Location loc) {
        return getBot().execute(new SendLocation(chatId, loc.latitude(), loc.latitude()));
    }

    public SendResponse sendSticker(Long chatId, Sticker sticker) throws UserInterfaceException {
        SendResponse response = getBot().execute(new SendSticker(chatId, download(sticker.fileId())));
        return response;
    }

    public SendResponse sendVoice(Long chatId, Voice voice, String caption) throws UserInterfaceException {
        SendResponse response = getBot().execute(new SendVoice(chatId, download(voice.fileId())).caption(caption));
        return response;
    }

    public String getFullPath(String telegramFileId) throws UserInterfaceException {
        GetFile getFile = new GetFile(telegramFileId);
        com.pengrad.telegrambot.model.File file = getBot().execute(getFile).file();
        if (file == null) {
            LoggerFactory.getLogger(this.getClass()).error("telegramFileId : " + telegramFileId);
            throw new UserInterfaceException("Unable to send file. file not found.");
        }
        if (PropertiesFileManager.getInstance().checkForFileSize()) {
            if (file.fileSize() > PropertiesFileManager.getInstance().getMaxMediaSizeByte()) {
                throw new UserInterfaceException("Media size too large. maximum allowed in mega bytes : " + PropertiesFileManager.getInstance().getMaxMediaSizeMB());
            }
        }
        String fullPath = getBot().getFullFilePath(file);
        return fullPath;
    }

    protected File download(String telegramFileId) throws UserInterfaceException {
        String fullPath = getFullPath(telegramFileId);
        return downloadUrl(fullPath);
    }

    protected File downloadUrl(String url) throws UserInterfaceException {
        try {
            File toSend = File.createTempFile("temp", null);
            FileUtils.copyURLToFile(new URL(url), toSend);
            return toSend;
        } catch (Exception ex) {
            throw new UserInterfaceException("Unable to send file");
        }
    }

    protected File downloadUrl(String url, String format) throws UserInterfaceException {
        try {
            File toSend = File.createTempFile("temp", format);
            FileUtils.copyURLToFile(new URL(url), toSend);
            return toSend;
        } catch (Exception ex) {
            throw new UserInterfaceException("Unable to send file");
        }
    }

    protected File download(String telegramFileId, String format) throws UserInterfaceException {
        String fullPath = getFullPath(telegramFileId);
        try {
            File toSend = File.createTempFile("temp", format);
            FileUtils.copyURLToFile(new URL(fullPath), toSend);
            return toSend;
        } catch (IOException ex) {
        }
        return null;
    }

    public abstract TelegramBot getBot();
}
