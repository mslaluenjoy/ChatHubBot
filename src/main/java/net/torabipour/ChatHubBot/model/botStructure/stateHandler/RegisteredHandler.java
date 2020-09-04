/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.torabipour.ChatHubBot.model.botStructure.stateHandler;

import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.model.Message;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.request.SendMessage;
import java.util.List;
import java.util.stream.Collectors;
import net.torabipour.ChatHubBot.PropertiesFileManager;
import net.torabipour.ChatHubBot.db.TransactionalDBAccess;
import net.torabipour.ChatHubBot.model.User;
import net.torabipour.ChatHubBot.model.UserStatus;
import net.torabipour.ChatHubBot.model.anonChat.PrivateMessage;
import net.torabipour.ChatHubBot.model.botStructure.AbstractStateHandler;
import net.torabipour.ChatHubBot.model.globalChat.GlobalChatRoom;
import net.torabipour.ChatHubBot.model.globalChat.GlobalPost;
import net.torabipour.ChatHubBot.model.globalChat.LocalChatRoom;
import net.torabipour.ChatHubBot.model.utils.GlobalMediaManager;
import net.torabipour.ChatHubBot.model.utils.MediaManager;
import net.torabipour.ChatHubBot.model.utils.PrivateMediaManager;
import net.torabipour.ChatHubBot.model.utils.UserInterfaceException;
import org.hibernate.Session;

/**
 *
 * @author mohammad
 */
public class RegisteredHandler extends AbstractStateHandler {

    private LocalChatRoom lcr;

    public RegisteredHandler(Update update, TelegramBot bot) {
        super(update, bot);
    }

    @Override
    protected void validateInput(Message message, String messageText) throws UserInterfaceException {
        if (messageText == null) {
            throw new UserInterfaceException("مقدار وارد شده نامعتبر است.", "Invalid input for main menu.");
        }
    }

    @Override
    protected void onInvalidInput(User localUser, Message message, String messageText) {
        sendMainMenu(chatId, isEnglish);
    }

    @Override
    protected void onOperation(User localUser, Message message, String messageText) throws UserInterfaceException {
        if (getAbortPhrases().contains(messageText)) {
            sendMainMenu(message.chat().id(), isEnglish);
        } else if (messageText.equals("/newchat") || messageText.contains("Start chat") || messageText.contains("شروع چت")) {
            localUser.setStatus(UserStatus.WaitingForConnection);
            new TransactionalDBAccess() {
                @Override
                protected void operation(Session session) {
                    session.update(localUser);
                }
            }.execute();
            sendTargetSexSelect(message.chat().id(), isEnglish);
        } else if (messageText.equals("/joinglobalroom") || messageText.contains("Join global rooms") || messageText.contains("چت روم گلوبال")) {
            localUser.setStatus(UserStatus.SelectingGlobalChat);
            new TransactionalDBAccess() {
                @Override
                protected void operation(Session session) {
                    session.update(localUser);
                }
            }.execute();
            sendGlobalRoomSelect(message.chat().id(), isEnglish);
        } else if (messageText.equals("/getanonlink") || messageText.contains("Get anonymous link") || messageText.contains("دریافت لینک چت ناشناس")) {
            bot.execute(new SendMessage(message.chat().id(), "https://t.me/" + PropertiesFileManager.getInstance().getBotName() + "?start=" + String.valueOf(localUser.getId())));
        } else if (messageText.contains("/start") && messageText.split(" ").length == 2) {
            Long receiver = Long.valueOf(messageText.split(" ")[1]);
            if (localUser.getId().equals(receiver)) {
                mediaManager.messageSend(isEnglish ? "You can not leave anonymous message for yourself." : "شما نمیتوانید برای خودتان پیام ناشناس بگذارید.", message.chat().id());
                return;
            }
            localUser.setStatus(UserStatus.SendingAnonMessage);
            PrivateMessage pm = new PrivateMessage(null, localUser, User.loadById(receiver), null, null, null, null, null);
            new TransactionalDBAccess() {
                @Override
                protected void operation(Session session) {
                    session.saveOrUpdate(pm);
                    session.saveOrUpdate(localUser);
                }
            }.execute();
            mediaManager.messageSendKeyboard(isEnglish ? "Leave your message." : "پیام خود را بگذارید.", message.chat().id(), "/cancel");
        } else if (messageText.contains("Inbox") || messageText.contains("صندوق پیام") || messageText.equals("/inbox")) {
            List<PrivateMessage> unreadMessages = PrivateMessage.loadByReceiver(localUser).stream().filter(x -> x.getType() != null).collect(Collectors.toList());
            if (unreadMessages == null || unreadMessages.isEmpty()) {
                mediaManager.messageSend(isEnglish ? "No new messages." : "هیچ پیامی یافت نشد.", message.chat().id());
                sendMainMenu(message.chat().id(), isEnglish);
                return;
            }
            for (PrivateMessage pm : unreadMessages) {
                try {
                    pm.setIsRed(true);
                    bot.execute(new PrivateMediaManager() {
                        @Override
                        public TelegramBot getBot() {
                            return bot;
                        }
                    }.sendPrivateMessage(pm, message.chat().id(), null));
                    new TransactionalDBAccess() {
                        @Override
                        protected void operation(Session session) {
                            session.saveOrUpdate(pm);
                        }
                    }.execute();
                } catch (UserInterfaceException ex) {
                    mediaManager.messageSend(ex.getMessage(), message.chat().id());
                }
            }
        } else if (messageText.contains("Edit Profile") || messageText.contains("ویرایش پروفایل") || messageText.equals("/editprofile")) {
            localUser.setStatus(UserStatus.LanguageSelect);
            new TransactionalDBAccess() {
                @Override
                protected void operation(Session session) {
                    session.saveOrUpdate(localUser);
                }
            }.execute();
            sendLanguageSelect(message.chat().id());
        } else if (messageText.contains("Join local room") || messageText.contains("چت روم محلی") || messageText.equals("/joinlocalroom")) {
            if (localUser.getLocation() == null) {
                localUser.setStatus(UserStatus.JoiningLocalRoom);
                new TransactionalDBAccess() {
                    @Override
                    protected void operation(Session session) {
                        session.saveOrUpdate(localUser);
                    }
                }.execute();
                mediaManager.locationRequestSend(isEnglish ? "Please send your location." : "لطفا لوکیشن خود را ارسال نمایید.", message.chat().id(), isEnglish ? "Send location" : "ارسال موقعیت", isEnglish);
            } else {
                lcr = LocalChatRoom.loadByCityName(localUser.getLocation().getCity());
                if (lcr == null) {
                    lcr = new LocalChatRoom(null, localUser.getLocation().getCity(), localUser.getLocation().getCity(), " به چت روم شهر " + localUser.getLocation().getCity() + " خوش آمدید! ", " Welcome to " + localUser.getLocation().getCity() + " local room! ");
                    lcr.setLocation(localUser.getLocation());
                    new TransactionalDBAccess() {
                        @Override
                        protected void operation(Session session) {
                            session.saveOrUpdate(lcr);
                        }
                    }.execute();
                }
                localUser.setStatus(UserStatus.InGlobalChat);
                localUser.setGcr(lcr);
                new TransactionalDBAccess() {
                    @Override
                    protected void operation(Session session) {
                        session.update(localUser);
                    }
                }.execute();

                List<GlobalPost> recentPosts = GlobalPost.loadRecentPosts(lcr, PropertiesFileManager.getInstance().getGlobalRecentNum());
                new GlobalMediaManager() {
                    @Override
                    public TelegramBot getBot() {
                        return bot;
                    }
                }.sendBatchGlobalPost(recentPosts, message.chat().id());
                mediaManager.messageSendKeyboard(isEnglish ? lcr.getEnglishPrompt() : lcr.getPersianPromt(), message.chat().id(), isEnglish ? "Leave room" : "خروج");

            }
        } else if (messageText.contains("Find people nearby") || messageText.contains("جستجوی افراد نزدیک شما") || messageText.contains("/findpeople")) {
            if (localUser.getExposing() != null && localUser.getExposing()) {
                localUser.setStatus(UserStatus.FindingNearby);
                new TransactionalDBAccess() {
                    @Override
                    protected void operation(Session session) {
                        session.saveOrUpdate(localUser);
                    }
                }.execute();
                sendTargetSexSelect(message.chat().id(), isEnglish ? "Search for " : "جنسیت افراد مورد نظر را انتخاب کنید ", isEnglish);
            } else {
                mediaManager.messageSend(isEnglish ? "You can not search nearby people unless you complete your profile information."
                        : "جهت جستجوی افراد نزدیک شما باید ابتدا اطلاعات پروفایلتان را کامل کنید.", message.chat().id());
            }
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

    private void sendTargetSexSelect(Long chatId, String message, Boolean isEnglish) {
        mediaManager.messageSendKeyboard(message, chatId,
                new String[]{isEnglish ? "Male 👨‍🦱" : "مرد 👨‍🦱", isEnglish ? "Female 👩" : "زن 👩"},
                new String[]{isEnglish ? "No difference 👨‍👩‍👦" : "فرقی نمی کند 👨‍👩‍👦"});
    }

    private void sendTargetSexSelect(Long chatId, Boolean isEnglish) {
        mediaManager.messageSendKeyboard(isEnglish ? "I'm intrested to talk to a " : "جنسیت شخص مورد نظر جهت چت را انتخاب کنید", chatId,
                new String[]{isEnglish ? "Male 👨‍🦱" : "مرد 👨‍🦱", isEnglish ? "Female 👩" : "زن 👩"},
                new String[]{isEnglish ? "No difference 👨‍👩‍👦" : "فرقی نمی کند 👨‍👩‍👦"});
    }

    private void sendLanguageSelect(Long chatId) {
        mediaManager.messageSendKeyboard("زبان خود را انتخاب کنید. \n Choose your language.", chatId, "English 🇬🇧", "Persian 🇮🇷");
    }

    private void sendGlobalRoomSelect(Long chatId, Boolean isEnglish) {
        List<GlobalChatRoom> allRooms = GlobalChatRoom.loadAll().stream().filter(x -> !x.getEnglishName().equals(x.getPersianName())).collect(Collectors.toList());
        List<String> roomOptions = allRooms.stream().map(x -> isEnglish ? x.getEnglishName() : x.getPersianName()).collect(Collectors.toList());
        roomOptions.add(isEnglish ? "Back" : "بازگشت");
        String[] roomOptionsArr = new String[roomOptions.size()];
        roomOptions.toArray(roomOptionsArr);
        mediaManager.messageSendKeyboard(isEnglish ? "Select room." : "انتخاب کنید.", chatId, 3, roomOptionsArr);
    }
}
