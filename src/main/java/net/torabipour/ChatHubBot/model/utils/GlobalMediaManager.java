/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.torabipour.ChatHubBot.model.utils;

import com.pengrad.telegrambot.request.AbstractSendRequest;
import com.pengrad.telegrambot.request.BaseRequest;
import com.pengrad.telegrambot.request.SendAnimation;
import com.pengrad.telegrambot.request.SendAudio;
import com.pengrad.telegrambot.request.SendDocument;
import com.pengrad.telegrambot.request.SendLocation;
import com.pengrad.telegrambot.request.SendMessage;
import com.pengrad.telegrambot.request.SendPhoto;
import com.pengrad.telegrambot.request.SendSticker;
import com.pengrad.telegrambot.request.SendVideo;
import com.pengrad.telegrambot.request.SendVoice;
import com.pengrad.telegrambot.response.BaseResponse;
import com.pengrad.telegrambot.response.SendResponse;
import net.torabipour.ChatHubBot.db.TransactionalDBAccess;
import net.torabipour.ChatHubBot.model.Language;
import net.torabipour.ChatHubBot.model.MessageType;
import net.torabipour.ChatHubBot.model.User;
import net.torabipour.ChatHubBot.model.globalChat.ChatMessage;
import net.torabipour.ChatHubBot.model.globalChat.GlobalPost;
import java.io.File;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import net.torabipour.ChatHubBot.model.globalChat.GlobalChatRoom;
import org.hibernate.Session;
import org.slf4j.LoggerFactory;

/**
 *
 * @author mohammad
 */
public abstract class GlobalMediaManager extends MediaManager {

    public void emmitPost(GlobalPost globalPost, User localUser) throws UserInterfaceException {
        File gFile = downloadGlobalFile(globalPost);
        List<Long> chatIds = User.loadByGcr(globalPost.getGcr()).stream().map(x -> x.getChatId()).filter(x -> !x.equals(localUser.getChatId())).collect(Collectors.toList());
        for (Long chatId : chatIds) {
            GlobalPost replyTo = globalPost.getReplyTo();
            SendResponse response;
            if (replyTo == null) {
                response = (SendResponse) getBot().execute(sendGlobalPost(globalPost, chatId, null, gFile));
            } else {
                response = (SendResponse) getBot().execute(sendGlobalPost(globalPost, chatId, ChatMessage.loadByGlobalPostAndChatId(replyTo, chatId).iterator().next().getMessageId(), gFile));
            }

            if (response != null && response.message() != null) {
                ChatMessage cm = new ChatMessage(null, response.message().messageId(), chatId, globalPost);
                new TransactionalDBAccess() {
                    @Override
                    protected void operation(Session session) {
                        session.saveOrUpdate(cm);
                    }
                }.execute();
            }

        }
    }

    public void notifyUserJoined(GlobalChatRoom gcr, User localUser) {
        if(gcr == null){
            return;
        }
        List<User> users = User.loadByGcr(gcr).stream().filter(x -> !x.getChatId().equals(localUser.getChatId())).collect(Collectors.toList());
        for (User user : users) {
            Boolean isEnglish = user.getLang() == null || user.getLang().equals(Language.English);
            getBot().execute(new SendMessage(user.getChatId(), isEnglish ? ("admin message: " + user.getNickName() + " joined global room") : ("پیام ادمین: ") + user.getNickName() + " وارد گلوبال روم شد."));
        }
    }

    public void notifyUserLeft(GlobalChatRoom gcr, User localUser) {
        if(gcr == null){
            return;
        }
        List<User> users = User.loadByGcr(gcr).stream().filter(x -> !x.getChatId().equals(localUser.getChatId())).collect(Collectors.toList());
        for (User user : users) {
            Boolean isEnglish = user.getLang() == null || user.getLang().equals(Language.English);
            getBot().execute(new SendMessage(user.getChatId(), isEnglish ? ("admin message: " + user.getNickName() + " left global room") : ("پیام ادمین: ") + user.getNickName() + " گلوبال روم را ترک کرد."));
        }
    }

    public void sendBatchGlobalPost(List<GlobalPost> gps, Long chatId) {
        if (gps == null) {
            return;
        }
        gps = gps.stream().sorted((a1, a2) -> a1.getDate().compareTo(a2.getDate())).collect(Collectors.toList());
        Map<Long, Integer> messageMap = new HashMap<>();
        for (GlobalPost gp : gps) {
            SendResponse response;
            if (gp.getReplyTo() != null) {
                Long replyTo = gp.getReplyTo().getId();
                try {
                    response = (SendResponse) getBot().execute(sendGlobalPost(gp, chatId, messageMap.get(replyTo)));
                } catch (UserInterfaceException ex) {
                    continue;
                }
            } else {
                try {
                    response = (SendResponse) getBot().execute(sendGlobalPost(gp, chatId, null));
                } catch (UserInterfaceException ex) {
                    continue;
                }
            }

            if (response == null || response.message() == null) {
                LoggerFactory.getLogger(this.getClass()).error("Send batch fault reply = " + String.valueOf(gp.getReplyTo() != null));
                continue;
            }

            ChatMessage cm = new ChatMessage(null, response.message().messageId(), chatId, gp);
            new TransactionalDBAccess() {
                @Override
                protected void operation(Session session) {
                    session.saveOrUpdate(cm);
                }
            }.execute();
            messageMap.put(gp.getId(), response.message().messageId());
        }
    }

    public AbstractSendRequest sendGlobalPost(GlobalPost gp, Long chatId, Integer replyTo, File file) {
        AbstractSendRequest request = null;
        MessageType type = gp.getType();
        String caption = getCaption(gp);
        switch (type) {
            case Animation:
                request = new SendAnimation(chatId, file).caption(caption);
                break;
            case Audio:
                request = new SendAudio(chatId, file).caption(caption);
                break;
            case Document:
                request = new SendDocument(chatId, file).caption(caption);
                break;
            case Location:
                String content = gp.getContent();
                String latStr = content.split("-")[0];
                String longStr = content.split("-")[1];
                request = new SendLocation(chatId, Float.valueOf(latStr), Float.valueOf(longStr));
                break;
            case Photo:
                request = new SendPhoto(chatId, file).caption(caption);
                break;
            case Sticker:
                request = new SendSticker(chatId, file);
                break;
            case Text:
                request = new SendMessage(chatId, getHeader(gp) + "\n\n" + gp.getContent());
                break;
            case Video:
                request = new SendVideo(chatId, file).caption(caption);
                break;
            case Voice:
                request = new SendVoice(chatId, file).caption(caption);
                break;
        }

        if (replyTo != null) {
            request = request.replyToMessageId(replyTo);
        }

        return request;
    }

    public AbstractSendRequest sendGlobalPost(GlobalPost gp, Long chatId, Integer replyTo) throws UserInterfaceException {
        return sendGlobalPost(gp, chatId, replyTo, downloadGlobalFile(gp));
    }

    public File downloadGlobalFile(GlobalPost gp) throws UserInterfaceException {
        MessageType type = gp.getType();
        if (type.equals(MessageType.Text) || type.equals(MessageType.Location)) {
            return null;
        }
        if (type.equals(MessageType.Animation)) {
            return downloadUrl(gp.getContent(), ".gif");
        }
        return downloadUrl(gp.getContent());
    }

    private String getHeader(GlobalPost gp) {
        String pattern = "MM/dd/yyyy HH:mm";
        DateFormat df = new SimpleDateFormat(pattern);

        StringBuilder sb = new StringBuilder();
        sb.append(gp.getSender().getNickName());
        sb.append(" - ");
        sb.append(df.format(gp.getDate()));
        return sb.toString();
    }

    private String getCaption(GlobalPost gp) {
        StringBuilder sb = new StringBuilder();
        sb.append(getHeader(gp));
        if (gp.getCaption() != null && !gp.getCaption().isEmpty()) {
            sb.append("\n\n");
            sb.append(gp.getCaption());
        }
        return sb.toString();
    }

}
