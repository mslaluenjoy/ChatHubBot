/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.torabipour.ChatHubBot.model.botStructure.stateHandler;

import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.model.Message;
import com.pengrad.telegrambot.model.Update;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import net.torabipour.ChatHubBot.db.TransactionalDBAccess;
import net.torabipour.ChatHubBot.model.Language;
import net.torabipour.ChatHubBot.model.User;
import net.torabipour.ChatHubBot.model.UserStatus;
import net.torabipour.ChatHubBot.model.anonChat.PrivateMessage;
import net.torabipour.ChatHubBot.model.anonChat.TargetedChatRequest;
import net.torabipour.ChatHubBot.model.botStructure.AbstractStateHandler;
import net.torabipour.ChatHubBot.model.utils.MediaManager;
import net.torabipour.ChatHubBot.model.utils.UserInterfaceException;
import net.torabipour.ChatHubBot.model.utils.UserMediaManager;
import org.hibernate.Session;
import org.slf4j.LoggerFactory;

/**
 *
 * @author mohammad
 */
public class ViewingProfileHandler extends AbstractStateHandler {

    public ViewingProfileHandler(Update update, TelegramBot bot) {
        super(update, bot);
    }

    @Override
    protected List<String> getAbortPhrases() {
        return new ArrayList<>(Arrays.asList("Main Menu", "منوی اصلی"));
    }

    @Override
    protected void validateInput(Message message, String messageText) throws UserInterfaceException {
    }

    @Override
    protected void onInvalidInput(User localUser, Message message, String messageText) {
    }

    @Override
    protected void onOperation(User localUser, Message message, String messageText) throws UserInterfaceException {
        if ("/back".equals(messageText) || "Back".equals(messageText) || "بازگشت".equals(messageText)) {
            List<User> usersList = User.loadAllExposingByCity(localUser.getLocation().getCity());
            usersList = usersList.stream().filter(x -> !x.getId().equals(localUser.getId())).collect(Collectors.toList());
            if (usersList.isEmpty()) {
                localUser.setStatus(UserStatus.Registered);
                new TransactionalDBAccess() {
                    @Override
                    protected void operation(Session session) {
                        session.saveOrUpdate(localUser);
                    }
                }.execute();
                mediaManager.messageSend(isEnglish ? "No result found." : "کسی پیدا نشد.", message.chat().id());
                sendMainMenu(message.chat().id(), isEnglish);
                return;
            }
            localUser.setStatus(UserStatus.FindingNearby);
            new TransactionalDBAccess() {
                @Override
                protected void operation(Session session) {
                    session.saveOrUpdate(localUser);
                }
            }.execute();
            bot.execute(new UserMediaManager() {
                @Override
                public TelegramBot getBot() {
                    return bot;
                }
            }.getUsersListView(usersList, message.chat().id(), isEnglish));
            return;
        }
        if (messageText != null && (messageText.contains("Chat request ") || messageText.contains("درخواست چت "))) {

            Long targetId = Long.valueOf(messageText.replaceAll("Chat request ", "").replaceAll("درخواست چت ", ""));
            User targetRequest = User.loadById(targetId);
            if (TargetedChatRequest.loadByRequesterTargeted(localUser, targetRequest) != null) {
                mediaManager.messageSend(isEnglish ? "Chat request successfully submitted." : "درخواست چت با موفقیت ثبت شد.", message.chat().id());
                return;
            }
            TargetedChatRequest cr = new TargetedChatRequest(null, localUser, targetRequest.getSex(), true, null);
            cr.setTargetRequest(targetRequest);
            cr.setIsAcceptedByTarget(false);
            new TransactionalDBAccess() {
                @Override
                protected void operation(Session session) {
                    session.saveOrUpdate(cr);
                }
            }.execute();
            Boolean isTargetEnglish = targetRequest.getLang().equals(Language.English);
            mediaManager.messageSend(isTargetEnglish ? (localUser.getNickName() + " hash requested to chat with you. to accept chat request press /acceptchatrequest" + localUser.getId() + " command.")
                    : (localUser.getNickName() + " درخواست چت با شما را داده است. چهت قبول درخواست جهت فرمان " + "/acceptchatrequest" + localUser.getId() + " را ارسال نمایید."), targetRequest.getChatId());
            mediaManager.messageSend(isEnglish ? "Chat request successfully submitted." : "درخواست چت با موفقیت ثبت شد.", message.chat().id());
            return;
        }

        if (messageText != null && messageText.contains("/view")) {
            try {
                Long targetId = Long.valueOf(messageText.replaceAll("/view", ""));
                User view = User.loadById(targetId);
                bot.execute(new UserMediaManager() {
                    @Override
                    public TelegramBot getBot() {
                        return bot;
                    }
                }.generateUserProfile(view, isEnglish, message.chat().id()));
                localUser.setStatus(UserStatus.ViewingProfile);
                new TransactionalDBAccess() {
                    @Override
                    protected void operation(Session session) {
                        session.saveOrUpdate(localUser);
                    }
                }.execute();
            } catch (UserInterfaceException ex) {
                mediaManager.messageSend(ex.getMessage(), message.chat().id());
            } catch (Exception ex) {
                LoggerFactory.getLogger(ViewingProfileHandler.class).error("Error", ex);
                mediaManager.messageSend(isEnglish ? "Unable to view profile" : "خطا در مشاهده پروفایل", message.chat().id());
            }
            return;
        }

        if (messageText != null && (messageText.contains("Send message ") || messageText.contains("ارسال پیام "))) {
            Long targetId = Long.valueOf(messageText.replaceAll("Send message ", "").replaceAll("ارسال پیام ", ""));
            User targetRequest = User.loadById(targetId);
            PrivateMessage pm = new PrivateMessage(null, localUser, targetRequest, null, null, null, null, null);
            new TransactionalDBAccess() {
                @Override
                protected void operation(Session session) {
                    session.saveOrUpdate(pm);
                }
            }.execute();
            localUser.setStatus(UserStatus.SendingAnonMessage);
            new TransactionalDBAccess() {
                @Override
                protected void operation(Session session) {
                    session.saveOrUpdate(localUser);
                }
            }.execute();
            mediaManager.messageSendKeyboard(isEnglish ? "You are now sending message to " + targetRequest.getNickName() : "شما هم اکنون درحال ارسال پیام به " + targetRequest.getNickName() + " هستید.", message.chat().id(), "/cancel");
        }
    }

    @Override
    protected void onUnsuccessfullOperation(User localUser, Message message, String messageText) {
    }

    @Override
    protected void sendMessageOnSuccess(Long chatId, Boolean isEnglish, MediaManager mediaManager) {
    }

    @Override
    protected UserStatus getAbortUserStatus() {
        return UserStatus.Registered;
    }

    @Override
    protected void onAbort(User localUser, Message message, String messageText) {
    }

    @Override
    protected void sendMessageOnAbort(Long chatId, Boolean isEnglish, MediaManager mediaManager) {
        sendMainMenu(chatId, isEnglish);
    }

}
