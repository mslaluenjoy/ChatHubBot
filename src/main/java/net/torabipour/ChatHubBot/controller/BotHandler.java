/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.torabipour.ChatHubBot.controller;

import com.pengrad.telegrambot.BotUtils;
import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.model.request.ReplyKeyboardMarkup;
import com.pengrad.telegrambot.request.EditMessageText;
import com.pengrad.telegrambot.request.SendMessage;
import net.torabipour.ChatHubBot.db.TransactionalDBAccess;
import net.torabipour.ChatHubBot.model.anonChat.Chat;
import net.torabipour.ChatHubBot.model.anonChat.ChatRequest;
import net.torabipour.ChatHubBot.model.Language;
import net.torabipour.ChatHubBot.model.Location;
import net.torabipour.ChatHubBot.model.MessageType;
import net.torabipour.ChatHubBot.model.Sex;
import net.torabipour.ChatHubBot.model.User;
import net.torabipour.ChatHubBot.model.UserStatus;
import net.torabipour.ChatHubBot.model.anonChat.PrivateMessage;
import net.torabipour.ChatHubBot.model.anonChat.TargetedChatRequest;
import net.torabipour.ChatHubBot.model.factory.GlobalPostFactory;
import net.torabipour.ChatHubBot.model.factory.PrivateMessageFactory;
import net.torabipour.ChatHubBot.model.globalChat.ChatMessage;
import net.torabipour.ChatHubBot.model.globalChat.GlobalChatRoom;
import net.torabipour.ChatHubBot.model.globalChat.GlobalPost;
import net.torabipour.ChatHubBot.model.globalChat.LocalChatRoom;
import net.torabipour.ChatHubBot.model.utils.AnonymousChatHandler;
import net.torabipour.ChatHubBot.model.utils.GlobalMediaManager;
import net.torabipour.ChatHubBot.model.utils.MediaManager;
import net.torabipour.ChatHubBot.model.utils.PrivateMediaManager;
import net.torabipour.ChatHubBot.model.utils.UserInterfaceException;
import net.torabipour.ChatHubBot.model.utils.UserMediaManager;
import net.torabipour.ChatHubBot.model.utils.location.Address;
import net.torabipour.ChatHubBot.model.utils.location.NominatimReverseGeocodingJAPI;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;
import net.torabipour.ChatHubBot.PropertiesFileManager;
import org.apache.log4j.Logger;
import org.hibernate.Session;

/**
 *
 * @author mohammad
 */
public abstract class BotHandler {

    protected abstract TelegramBot getBot();

    private MediaManager mediaManager;
    private GlobalMediaManager globalMediaManager;
    private PrivateMediaManager privateMediaManager;
    private User localUser;
    private NominatimReverseGeocodingJAPI geo;
    private LocalChatRoom lcr;
    private UserMediaManager userMediaManager;
    private Logger logger;

    public BotHandler() {
        mediaManager = new MediaManager() {
            @Override
            public TelegramBot getBot() {
                return BotHandler.this.getBot();
            }
        };

        globalMediaManager = new GlobalMediaManager() {
            @Override
            public TelegramBot getBot() {
                return BotHandler.this.getBot();
            }
        };

        privateMediaManager = new PrivateMediaManager() {
            @Override
            public TelegramBot getBot() {
                return BotHandler.this.getBot();
            }
        };

        userMediaManager = new UserMediaManager() {
            @Override
            public TelegramBot getBot() {
                return BotHandler.this.getBot();
            }
        };

        geo = new NominatimReverseGeocodingJAPI();

        this.logger = Logger.getLogger(this.getClass());
    }

    public void handleUpdate(Update update) {

        String messageText;
        String telegramId;
        com.pengrad.telegrambot.model.User user;
        com.pengrad.telegrambot.model.Message message;
        if (update.editedMessage() != null) {
            message = update.editedMessage();
        } else {
            message = update.message();
        }
        user = message.from();
        telegramId = String.valueOf(user.id());
        localUser = User.loadByTelegramId(telegramId);
        messageText = message.text();

        if (messageText != null && messageText.contains("/start") && messageText.split(" ").length == 2 && localUser == null) {
            localUser = new User(null, Language.English, telegramId, user.username(), user.firstName(), user.lastName(), UserStatus.Registered, message.chat().id());
            new TransactionalDBAccess() {
                @Override
                protected void operation(Session session) {
                    session.save(localUser);
                }
            }.execute();
            localUser.setNickName(String.valueOf(localUser.getId()));
            new TransactionalDBAccess() {
                @Override
                protected void operation(Session session) {
                    session.saveOrUpdate(localUser);
                }
            }.execute();
        }

        if (localUser == null) {
            User newLocalUser = new User(null, null, telegramId, user.username(), user.firstName(), user.lastName(), UserStatus.LanguageSelect, message.chat().id());
            new TransactionalDBAccess() {
                @Override
                protected void operation(Session session) {
                    session.save(newLocalUser);
                }
            }.execute();
            sendLanguageSelect(message.chat().id());
            return;
        }

        localUser.setTelegramUserName(user.username());
        localUser.setChatId(message.chat().id());
        localUser.setLastLogin(new Date());

        new TransactionalDBAccess() {
            @Override
            protected void operation(Session session) {
                session.saveOrUpdate(localUser);
            }
        }.execute();

        UserStatus status = localUser.getStatus();

        Boolean isEnglish = localUser.getLang() == null ? true : localUser.getLang().equals(Language.English);

        if (messageText != null && messageText.contains("/acceptchatrequest")) {
            Long requesterId = Long.valueOf(messageText.replaceAll("/acceptchatrequest", ""));
            User requester = User.loadById(requesterId);
            if (requester.getStatus().equals(UserStatus.InChat) || requester.getStatus().equals(UserStatus.InGlobalChat)) {
                mediaManager.messageSend(isEnglish ? "User is currently in chat. please wait for them to become available." : "کاربر درحال چت است. لطفا تا زمانی که دوباره در دسترس بشود صبر کنید.", message.chat().id());
                return;
            }
            if (localUser.getStatus().equals(UserStatus.InChat) || localUser.getStatus().equals(UserStatus.InGlobalChat)) {
                mediaManager.messageSend(isEnglish ? "You are currently in chat. please leave your current chat." : "شما هم اکنون درحال چت هستید. اول از چت خارج شوید.", message.chat().id());
                return;
            }
            TargetedChatRequest tcr = TargetedChatRequest.loadByRequesterTargeted(requester, localUser);
            if (tcr == null) {
                return;
            }
            Chat chat = new Chat(null, localUser, requester, true);

            new TransactionalDBAccess() {
                @Override
                protected void operation(Session session) {
                    session.saveOrUpdate(chat);
                    tcr.setChat(chat);
                    tcr.setActive(false);
                    session.merge(tcr);

                    localUser.setStatus(UserStatus.InChat);
                    requester.setStatus(UserStatus.InChat);
                    session.merge(localUser);
                    session.merge(requester);
                }
            }.execute();

            BotHandler.this.getBot().execute(new SendMessage(chat.getEndpoint1().getChatId(), chat.getEndpoint1().getLang().equals(Language.English)
                    ? "Your chat with " + chat.getEndpoint2().getNickName() + " has started! say hello!"
                    : "چت شما با " + chat.getEndpoint2().getNickName() + " شروع شد! بهش سلام کن!").replyMarkup(new ReplyKeyboardMarkup(
                    new String[]{"/terminate"}
            )
                    .oneTimeKeyboard(true)
                    .resizeKeyboard(true)
                    .selective(true)));

            BotHandler.this.getBot().execute(new SendMessage(chat.getEndpoint2().getChatId(), chat.getEndpoint2().getLang().equals(Language.English)
                    ? "Your chat with " + chat.getEndpoint1().getNickName() + " has started! say hello!"
                    : "چت شما با " + chat.getEndpoint1().getNickName() + " شروع شد! بهش سلام کن!").replyMarkup(new ReplyKeyboardMarkup(
                    new String[]{"/terminate"}
            )
                    .oneTimeKeyboard(true)
                    .resizeKeyboard(true)
                    .selective(true)));
        }

        switch (status) {
            case LanguageSelect:

                try {
                    localUser.setLang(Language.valueOf(messageText.split(" ")[0]));
                } catch (Exception ex) {
                    sendLanguageSelect(message.chat().id());
                    return;
                }

                localUser.setStatus(UserStatus.SexSelect);
                new TransactionalDBAccess() {
                    @Override
                    protected void operation(Session session) {
                        session.update(localUser);
                    }
                }.execute();
                isEnglish = localUser.getLang() == null ? true : localUser.getLang().equals(Language.English);
                sendSexSelect(message.chat().id(), isEnglish);
                return;

            case SexSelect:
                try {
                    localUser.setSex(localUser.getLang().equals(Language.English) ? Sex.valueOf(messageText.split(" ")[0]) : (messageText.contains("مرد") ? Sex.Male : Sex.Female));

                } catch (Exception ex) {
                    sendSexSelect(message.chat().id(), isEnglish);
                    return;
                }

                localUser.setStatus(UserStatus.NickNameSelect);
                new TransactionalDBAccess() {
                    @Override
                    protected void operation(Session session) {
                        session.update(localUser);
                    }
                }.execute();

                sendUserNameSelect(message.chat().id(), isEnglish);
                return;
            case NickNameSelect:
                localUser.setNickName(messageText);
                localUser.setStatus(UserStatus.ExposingSelect);
                new TransactionalDBAccess() {
                    @Override
                    protected void operation(Session session) {
                        session.update(localUser);
                    }
                }.execute();
                sendExposing(message.chat().id(), isEnglish);

                return;
            case ExposingSelect:

                if ("Yes".equals(messageText) || "بله".equals(messageText)) {
                    localUser.setExposing(false);
                    localUser.setStatus(UserStatus.LocationSelect);
                    new TransactionalDBAccess() {
                        @Override
                        protected void operation(Session session) {
                            session.saveOrUpdate(localUser);
                        }
                    }.execute();
                    mediaManager.locationRequestSend(isEnglish ? "Send your current location." : "موقعیت جغرافیایی خود را ارسال نمایید.",
                            message.chat().id(), isEnglish ? "Send Location" : "ارسال موقعیت", isEnglish);
                    return;
                } else if ("No".equals(messageText) || "نه".equals(messageText)) {
                    localUser.setExposing(false);
                    localUser.setStatus(UserStatus.Registered);
                    new TransactionalDBAccess() {
                        @Override
                        protected void operation(Session session) {
                            session.saveOrUpdate(localUser);
                        }
                    }.execute();
                    sendRegistrationSuccessfull(message.chat().id(), isEnglish);
                    sendMainMenu(message.chat().id(), isEnglish);
                    return;
                } else {
                    sendExposing(message.chat().id(), isEnglish);
                }
                break;
            case LocationSelect:
                if ("/cancel".equals(messageText) || "Nevermind".equals(messageText) || "بی خیال".equals(messageText)) {
                    localUser.setStatus(UserStatus.Registered);
                    localUser.setExposing(false);
                    new TransactionalDBAccess() {
                        @Override
                        protected void operation(Session session) {
                            session.saveOrUpdate(localUser);
                        }
                    }.execute();
                    sendRegistrationSuccessfull(message.chat().id(), isEnglish);
                    sendMainMenu(message.chat().id(), isEnglish);
                    return;
                }
                if (message.location() == null) {
                    mediaManager.locationRequestSend(isEnglish ? "Send your current location." : "موقعیت جغرافیایی خود را ارسال نمایید.",
                            message.chat().id(), isEnglish ? "Send Location" : "ارسال موقعیت", isEnglish);
                    return;
                }

                try {
                    localUser.setLocationAndAddress(message.location(), geo);
                    localUser.setStatus(UserStatus.AgeSelect);
                    new TransactionalDBAccess() {
                        @Override
                        protected void operation(Session session) {
                            session.saveOrUpdate(localUser);
                        }
                    }.execute();
                    mediaManager.messageSendKeyboard(isEnglish ? "Send your age." : "سن خود را وارد کنید.", message.chat().id(), "/cancel");
                } catch (UserInterfaceException ex) {
                    mediaManager.messageSend(ex.getMessage(), message.chat().id());
                }

                break;
            case AgeSelect:
                if ("/cancel".equals(messageText)) {
                    localUser.setStatus(UserStatus.Registered);
                    localUser.setExposing(false);
                    new TransactionalDBAccess() {
                        @Override
                        protected void operation(Session session) {
                            session.saveOrUpdate(localUser);
                        }
                    }.execute();
                    sendRegistrationSuccessfull(message.chat().id(), isEnglish);
                    sendMainMenu(message.chat().id(), isEnglish);
                    return;
                }
                Integer age = 0;
                try {
                    age = Integer.valueOf(messageText);
                    if (age < 10 || age > 80) {
                        throw new Exception();
                    }
                } catch (Exception ex) {
                    mediaManager.messageSendKeyboard(isEnglish ? "Send your age." : "سن خود را وارد کنید.", message.chat().id(), "/cancel");
                    return;
                }
                localUser.setAge(age);
                localUser.setStatus(UserStatus.ProfilePictureSelect);
                new TransactionalDBAccess() {
                    @Override
                    protected void operation(Session session) {
                        session.saveOrUpdate(localUser);
                    }
                }.execute();
                mediaManager.messageSendKeyboard(isEnglish ? "Send your profile picture." : " عکس پروفایل خود را ارسال نمایید.", message.chat().id(), "/cancel");
                break;
            case ProfilePictureSelect:
                if ("/cancel".equals(messageText)) {
                    localUser.setStatus(UserStatus.Registered);
                    localUser.setExposing(false);
                    new TransactionalDBAccess() {
                        @Override
                        protected void operation(Session session) {
                            session.saveOrUpdate(localUser);
                        }
                    }.execute();
                    sendRegistrationSuccessfull(message.chat().id(), isEnglish);
                    sendMainMenu(message.chat().id(), isEnglish);
                    return;
                }
                if (message.photo() == null) {
                    mediaManager.messageSendKeyboard(isEnglish ? "Send your profile picture." : " عکس پروفایل خود را ارسال نمایید.", message.chat().id(), "/cancel");
                    return;
                }
                try {
                    localUser.setProfilePicture(mediaManager.getFullPath(message.photo()[message.photo().length - 1].fileId()));
                    localUser.setStatus(UserStatus.Registered);
                    localUser.setExposing(true);
                    new TransactionalDBAccess() {
                        @Override
                        protected void operation(Session session) {
                            session.saveOrUpdate(localUser);
                        }
                    }.execute();
                    sendRegistrationSuccessfull(message.chat().id(), isEnglish);
                    sendMainMenu(message.chat().id(), isEnglish);
                } catch (UserInterfaceException ex) {
                    mediaManager.messageSend(ex.getMessage(), message.chat().id());
                }

                break;
            case WaitingForConnection:

                if (messageText == null) {
                    sendTargetSexSelect(message.chat().id(), isEnglish);
                    return;
                }

                List<ChatRequest> prevRequests = ChatRequest.loadByRequester(localUser);

                if (prevRequests != null && !prevRequests.isEmpty()) {
                    if ("/cancel".equals(messageText)) {
                        prevRequests.forEach(r -> {
                            r.setActive(false);
                            new TransactionalDBAccess() {
                                @Override
                                protected void operation(Session session) {
                                    localUser.setStatus(UserStatus.Registered);
                                    session.merge(r);
                                    session.merge(localUser);
                                }
                            }.execute();
                        });
                        sendCancelChatRequest(message.chat().id(), isEnglish);
                        sendMainMenu(message.chat().id(), isEnglish);
                        return;
                    } else {
                        sendStillTryingChat(message.chat().id(), isEnglish);
                        return;
                    }

                }

                Sex targetSex;

                if (messageText.contains("مرد") || messageText.contains("Male")) {
                    targetSex = Sex.Male;
                } else if (messageText.contains("زن") || messageText.contains("Female")) {
                    targetSex = Sex.Female;
                } else {
                    targetSex = null;
                }

                sendRelationshipAdvise(message.chat().id());

                ChatRequest req = new ChatRequest(null, localUser, targetSex, true, null);

                new TransactionalDBAccess() {
                    @Override
                    protected void operation(Session session) {
                        session.saveOrUpdate(req);
                    }
                }.execute();

                List<ChatRequest> requests = ChatRequest.loadProperMatch(targetSex, localUser.getSex(), localUser.getLang());

                if (requests == null || requests.isEmpty()) {
                    sendCanelConnection(message.chat().id(), isEnglish);
                    return;
                }

                requests = requests.stream().filter(r -> !r.getRequester().getId().equals(localUser.getId())).collect(Collectors.toList());

                if (requests.isEmpty()) {
                    sendCanelConnection(message.chat().id(), isEnglish);
                    return;
                }

                ChatRequest secondReq = requests.get(0);

                Chat chat = new Chat(null, localUser, secondReq.getRequester(), true);

                new TransactionalDBAccess() {
                    @Override
                    protected void operation(Session session) {
                        session.saveOrUpdate(chat);
                        req.setChat(chat);
                        secondReq.setChat(chat);
                        req.setActive(false);
                        secondReq.setActive(false);
                        session.merge(req);
                        session.merge(secondReq);

                        localUser.setStatus(UserStatus.InChat);
                        secondReq.getRequester().setStatus(UserStatus.InChat);
                        session.merge(localUser);
                        session.merge(secondReq.getRequester());
                    }
                }.execute();

                BotHandler.this.getBot().execute(new SendMessage(chat.getEndpoint1().getChatId(), chat.getEndpoint1().getLang().equals(Language.English)
                        ? "Your chat with " + chat.getEndpoint2().getNickName() + " has started! say hello!"
                        : "چت شما با " + chat.getEndpoint2().getNickName() + " شروع شد! بهش سلام کن!").replyMarkup(new ReplyKeyboardMarkup(
                        new String[]{"/terminate"}
                )
                        .oneTimeKeyboard(true)
                        .resizeKeyboard(true)
                        .selective(true)));

                BotHandler.this.getBot().execute(new SendMessage(chat.getEndpoint2().getChatId(), chat.getEndpoint2().getLang().equals(Language.English)
                        ? "Your chat with " + chat.getEndpoint1().getNickName() + " has started! say hello!"
                        : "چت شما با " + chat.getEndpoint1().getNickName() + " شروع شد! بهش سلام کن!").replyMarkup(new ReplyKeyboardMarkup(
                        new String[]{"/terminate"}
                )
                        .oneTimeKeyboard(true)
                        .resizeKeyboard(true)
                        .selective(true)));

                return;

            case InChat:
                Chat currentChat = Chat.loadByEndpoint(localUser);

                if (update.editedMessage() != null) {
                    Integer originalReply;
                    net.torabipour.ChatHubBot.model.anonChat.Message msg = net.torabipour.ChatHubBot.model.anonChat.Message.loadBySendId(message.messageId());
                    if (msg == null) {
                        msg = net.torabipour.ChatHubBot.model.anonChat.Message.loadByReceiveId(message.messageId());
                        if (msg == null) {
                            originalReply = message.messageId();
                        } else {
                            originalReply = msg.getSendId();
                        }
                    } else {
                        originalReply = msg.getReceiveId();
                    }

                    Long chatId = currentChat.getEndpoint1().getId().equals(localUser.getId())
                            ? currentChat.getEndpoint2().getChatId() : currentChat.getEndpoint1().getChatId();
                    if (message.text() != null) {
                        BotHandler.this.getBot().execute(new EditMessageText(chatId, originalReply, message.text()));
                    }
                    return;
                }

                if (currentChat == null) {
                    localUser.setStatus(UserStatus.Registered);
                    new TransactionalDBAccess() {
                        @Override
                        protected void operation(Session session) {
                            session.saveOrUpdate(localUser);
                        }
                    }.execute();
                    sendChatTerminated(message.chat().id(), isEnglish);
                    sendMainMenu(message.chat().id(), isEnglish);
                    return;
                }

                User endpoint1 = currentChat.getEndpoint1();
                User endpoint2 = currentChat.getEndpoint2();

                if ("/terminate".equals(messageText) || "/back".equals(messageText) || "/restart".equals(messageText) || "/leave".equals(messageText)|| "/cancel".equals(messageText)) {

                    currentChat.setActive(false);
                    endpoint1.setStatus(UserStatus.Registered);
                    endpoint2.setStatus(UserStatus.Registered);
                    List<ChatRequest> prevEnd1 = ChatRequest.loadByRequester(endpoint1);
                    List<ChatRequest> prevEnd2 = ChatRequest.loadByRequester(endpoint2);
                    new TransactionalDBAccess() {
                        @Override
                        protected void operation(Session session) {
                            session.update(currentChat);
                            session.merge(endpoint1);
                            session.merge(endpoint2);
                            if (prevEnd1 != null) {
                                prevEnd1.forEach(x -> {
                                    x.setActive(false);
                                    session.merge(x);
                                });
                            }
                            if (prevEnd2 != null) {
                                prevEnd2.forEach(x -> {
                                    x.setActive(false);
                                    session.merge(x);
                                });
                            }
                        }
                    }.execute();
                    sendChatTerminated(endpoint1.getChatId(), endpoint1.getLang().equals(Language.English));
                    sendChatTerminated(endpoint2.getChatId(), endpoint2.getLang().equals(Language.English));
                    sendMainMenu(endpoint1.getChatId(), endpoint1.getLang().equals(Language.English));
                    sendMainMenu(endpoint2.getChatId(), endpoint2.getLang().equals(Language.English));
                    return;
                }

                 {
                    try {
                        new AnonymousChatHandler() {
                            @Override
                            public TelegramBot getBot() {
                                return BotHandler.this.getBot();
                            }

                            @Override
                            public Update getUpdate() {
                                return update;
                            }

                            @Override
                            public Chat getCurrentChat() {
                                return currentChat;
                            }

                            @Override
                            public User getLocalUser() {
                                return localUser;
                            }
                        }.copyMessageAndForward(endpoint1.getChatId().equals(message.chat().id()) ? endpoint2.getChatId() : endpoint1.getChatId(), isEnglish);
                    } catch (UserInterfaceException ex) {
                        mediaManager.messageSend(ex.getMessage(), message.chat().id());
                    }
                }

                return;
            case Registered:
                if (messageText == null) {
                    break;
                }
                if (messageText.equals("/start")) {
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
                    getBot().execute(new SendMessage(message.chat().id(), "https://t.me/" + PropertiesFileManager.getInstance().getBotName() + "?start=" + String.valueOf(localUser.getId())));
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
                            getBot().execute(privateMediaManager.sendPrivateMessage(pm, message.chat().id(), null));
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
                        globalMediaManager.sendBatchGlobalPost(recentPosts, message.chat().id());
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
                break;
            case ViewingProfile:
                if ("Main Menu".equals(messageText) || "منوی اصلی".equals(messageText)) {
                    localUser.setStatus(UserStatus.Registered);
                    new TransactionalDBAccess() {
                        @Override
                        protected void operation(Session session) {
                            session.saveOrUpdate(localUser);
                        }
                    }.execute();
                    sendMainMenu(message.chat().id(), isEnglish);
                    return;
                }
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
                    getBot().execute(userMediaManager.getUsersListView(usersList, message.chat().id(), isEnglish));
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
                        getBot().execute(userMediaManager.generateUserProfile(view, isEnglish, message.chat().id()));
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
                        logger.error(ex, ex.getCause().fillInStackTrace());
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
                break;
            case FindingNearby:

                if ("/restart".equals(messageText) || "/start".equals(message) || "/cancel".equals(messageText) || "/back".equals(messageText) || "Back".equals(messageText) || "بازگشت".equals(messageText)) {
                    localUser.setStatus(UserStatus.Registered);
                    new TransactionalDBAccess() {
                        @Override
                        protected void operation(Session session) {
                            session.saveOrUpdate(localUser);
                        }
                    }.execute();
                    sendMainMenu(message.chat().id(), isEnglish);
                    return;
                }

                if (messageText != null && messageText.contains("/view")) {
                    try {
                        Long targetId = Long.valueOf(messageText.replaceAll("/view", ""));
                        User view = User.loadById(targetId);
                        getBot().execute(userMediaManager.generateUserProfile(view, isEnglish, message.chat().id()));
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
                        logger.error(ex, ex.getCause().fillInStackTrace());
                        mediaManager.messageSend(isEnglish ? "Unable to view profile" : "خطا در مشاهده پروفایل", message.chat().id());
                    }
                    return;
                }

                List<User> usersList;
                if (messageText.contains("Male") || messageText.contains("مرد")) {
                    usersList = User.loadAllExposingByCityAndSex(localUser.getLocation().getCity(), Sex.Male);
                } else if (messageText.contains("Female") || messageText.contains("زن")) {
                    usersList = User.loadAllExposingByCityAndSex(localUser.getLocation().getCity(), Sex.Female);
                } else {
                    usersList = User.loadAllExposingByCity(localUser.getLocation().getCity());
                }
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
                getBot().execute(userMediaManager.getUsersListView(usersList, message.chat().id(), isEnglish));

                break;
            case JoiningLocalRoom:
                if ("/cancel".equals(messageText) || "/start".equals(messageText) || "/restart".equals(messageText) || "Nevermind".equals(messageText) || "بی خیال".equals(messageText)) {
                    localUser.setStatus(UserStatus.Registered);
                    new TransactionalDBAccess() {
                        @Override
                        protected void operation(Session session) {
                            session.saveOrUpdate(localUser);
                        }
                    }.execute();
                    sendMainMenu(message.chat().id(), isEnglish);
                    return;
                }

                if (message.location() == null) {
                    mediaManager.messageSendKeyboard(isEnglish ? "Please send a location." : "لطفا یک لوکیشن بفرستید.", message.chat().id(), "/cancel");
                    return;
                }

                try {
                    localUser.setLocationAndAddress(message.location(), geo);
                    new TransactionalDBAccess() {
                        @Override
                        protected void operation(Session session) {
                            session.saveOrUpdate(localUser);
                        }
                    }.execute();
                } catch (UserInterfaceException ex) {
                    mediaManager.messageSend(ex.getMessage(), message.chat().id());
                }

                Location location = localUser.getLocation();

                lcr = LocalChatRoom.loadByCityName(location.getCity());

                if (lcr == null) {
                    lcr = new LocalChatRoom(null, location.getCity(), location.getCity(), "به چت روم شهر " + location.getCity() + " خوش آمدید!", "Welcome to" + location.getCity() + " local room!");
                    lcr.setLocation(location);
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
                globalMediaManager.sendBatchGlobalPost(recentPosts, message.chat().id());
                mediaManager.messageSendKeyboard(isEnglish ? lcr.getEnglishPrompt() : lcr.getPersianPromt(), message.chat().id(), isEnglish ? "Leave room" : "خروج");

                break;

            case SendingAnonMessage:
                if ("/cancel".equals(messageText) || "/start".equals(messageText) || "/restart".equals(messageText)) {
                    localUser.setStatus(UserStatus.Registered);
                    new TransactionalDBAccess() {
                        @Override
                        protected void operation(Session session) {
                            session.saveOrUpdate(localUser);
                        }
                    }.execute();
                    mediaManager.messageSend(isEnglish ? "Sending anonymous chat done." : "ارسال پیام ناشناس انجام شد.", message.chat().id());
                    sendMainMenu(message.chat().id(), isEnglish);
                } else {
                    PrivateMessage pm;
                    try {
                        pm = PrivateMessageFactory.create(message, localUser, mediaManager);
                        new TransactionalDBAccess() {
                            @Override
                            protected void operation(Session session) {
                                session.saveOrUpdate(pm);
                            }
                        }.execute();
                        mediaManager.messageSend(!isEnglish ? "پیام شما ارسال شد. در صورت اتمام پیام ها فرمان /cancel را ارسال نمایید."
                                : "Message sent. in order to stop sending messages, send /cancel command.", message.chat().id());
                        mediaManager.messageSend(isEnglish ? "You received an anonymous message. check your inbox to see your messages."
                                : "شما یک پیام ناشناس دریافت کردید. جهت مشاهده پیام به صندوق پیام مراجعه نمایید.", pm.getReceiver().getChatId());
                    } catch (UserInterfaceException ex) {
                        mediaManager.messageSend(ex.getMessage(), message.chat().id());
                    }

                }
                break;
            case SelectingGlobalChat:
                if (messageText == null) {
                    sendGlobalRoomSelect(message.chat().id(), isEnglish);
                    return;
                }
                if ("Back".equals(messageText) || "بازگشت".equals(messageText) || "/restart".equals(messageText) || "/start".equals(messageText)) {
                    localUser.setStatus(UserStatus.Registered);
                    new TransactionalDBAccess() {
                        @Override
                        protected void operation(Session session) {
                            session.update(localUser);
                        }
                    }.execute();
                    sendMainMenu(message.chat().id(), isEnglish);
                    return;
                }

                GlobalChatRoom gcr = GlobalChatRoom.loadByName(messageText);
                if (gcr != null) {
                    localUser.setStatus(UserStatus.InGlobalChat);
                    localUser.setGcr(gcr);
                    new TransactionalDBAccess() {
                        @Override
                        protected void operation(Session session) {
                            session.update(localUser);
                        }
                    }.execute();

                    List<GlobalPost> recentGlobalPosts = GlobalPost.loadRecentPosts(gcr, PropertiesFileManager.getInstance().getGlobalRecentNum());
                    globalMediaManager.sendBatchGlobalPost(recentGlobalPosts, message.chat().id());
                    globalMediaManager.notifyUserJoined(gcr, localUser);
                    mediaManager.messageSendKeyboard(isEnglish ? gcr.getEnglishPrompt() : gcr.getPersianPromt(), message.chat().id(), isEnglish ? "Leave room" : "خروج");
                }
                break;
            case InGlobalChat:
                if (messageText != null && (messageText.equals("Leave room") || messageText.equals("خروج") || messageText.equals("/restart") || messageText.equals("/start") || messageText.equals("/leaveglobal") || messageText.equals("/back"))) {
                    localUser.setStatus(UserStatus.Registered);
                    globalMediaManager.notifyUserLeft(localUser.getGcr(), localUser);
                    localUser.setGcr(null);
                    new TransactionalDBAccess() {
                        @Override
                        protected void operation(Session session) {
                            session.update(localUser);
                        }
                    }.execute();
                    sendMainMenu(message.chat().id(), isEnglish);
                    return;
                }

                try {
                    GlobalPost globalPost = GlobalPostFactory.create(message, localUser, mediaManager, localUser.getGcr());
                    new TransactionalDBAccess() {
                        @Override
                        protected void operation(Session session) {
                            session.saveOrUpdate(globalPost);
                        }
                    }.execute();
                    ChatMessage cm = new ChatMessage(null, message.messageId(), message.chat().id(), globalPost);
                    new TransactionalDBAccess() {
                        @Override
                        protected void operation(Session session) {
                            session.saveOrUpdate(cm);
                        }
                    }.execute();
                    globalMediaManager.emmitPost(globalPost, localUser);
                } catch (UserInterfaceException ex) {
                    mediaManager.messageSend(ex.getMessage(), message.chat().id());
                }

                break;
            default:
                break;
        }

    }

    private void sendGlobalRoomSelect(Long chatId, Boolean isEnglish) {
        List<GlobalChatRoom> allRooms = GlobalChatRoom.loadAll().stream().filter(x -> !x.getEnglishName().equals(x.getPersianName())).collect(Collectors.toList());
        List<String> roomOptions = allRooms.stream().map(x -> isEnglish ? x.getEnglishName() : x.getPersianName()).collect(Collectors.toList());
        roomOptions.add(isEnglish ? "Back" : "بازگشت");
        String[] roomOptionsArr = new String[roomOptions.size()];
        roomOptions.toArray(roomOptionsArr);
        mediaManager.messageSendKeyboard(isEnglish ? "Select room." : "انتخاب کنید.", chatId, 3, roomOptionsArr);
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

    private void sendCancelChatRequest(Long chatId, Boolean isEnglish) {
        mediaManager.messageSend(isEnglish ? "Chat request successfully canceled." : "درخواست چت با موفقیت لغو شد.", chatId);
    }

    private void sendMainMenu(Long chatId, Boolean isEnglish) {
        mediaManager.messageSendKeyboard(isEnglish ? "Main menu" : "منوی اصلی", chatId,
                new String[]{isEnglish ? "Start chat 🗯" : "شروع چت 🗯", isEnglish ? "Join global rooms 🌍" : "چت روم گلوبال 🌍"},
                new String[]{isEnglish ? "Inbox 📥" : "صندوق پیام 📥", isEnglish ? "Get anonymous link 🔗" : "دریافت لینک چت ناشناس 🔗"},
                new String[]{isEnglish ? "Edit Profile ✏️" : "ویرایش پروفایل ✏️", isEnglish ? "Join local room 🌆" : "چت روم محلی 🌆"},
                new String[]{isEnglish ? "Find people nearby 📍" : "جستجوی افراد نزدیک شما 📍"});
    }

    private void sendRegistrationSuccessfull(Long chatId, Boolean isEnglish) {
        mediaManager.messageSend(isEnglish
                ? "Registration completed. Enjoy!\n"
                : "فرایند ثبت نام به پایان رسید. از بات لذت ببرید!", chatId);
    }

    private void sendChatTerminated(Long chatId, Boolean isEnglish) {
        mediaManager.messageSend(isEnglish
                ? "Chat terminated.\n"
                : "چت اتمام یافت.", chatId);
    }

    private void sendStillTryingChat(Long chatId, Boolean isEnglish) {
        mediaManager.messageSendKeyboard(isEnglish ? "Please wait we're still trying to find a match for you as soon as possible."
                : "لظفا صبر کنید. ربات هنوز در تلاش است تا شما را به کاربر دیگری متصل کند.", chatId, "/cancel");
    }

    private void sendLanguageSelect(Long chatId) {
        mediaManager.messageSendKeyboard("زبان خود را انتخاب کنید. \n Choose your language.", chatId, "English 🇬🇧", "Persian 🇮🇷");
    }

    private void sendSexSelect(Long chatId, Boolean isEnglish) {
        mediaManager.messageSendKeyboard(isEnglish ? "Select your sex." : "جنسیت خود را انتخاب کنید.", chatId,
                new String[]{isEnglish ? "Male 👨‍🦱" : "مرد 👨‍🦱", isEnglish ? "Female 👩" : "زن 👩"});
    }

    private void sendUserNameSelect(Long chatId, Boolean isEnglish) {
        mediaManager.messageSend(isEnglish
                ? "Choose yourself a user name which is shown to other users. \n"
                : "برای خود نام کاربری انتخاب کنید. \n توجه کنید که این نام قابل مشاهده توسط سایر کاربران این ربات خواهد بود.", chatId);
    }

    private void sendCanelConnection(Long chatId, Boolean isEnglish) {
        mediaManager.messageSendKeyboard(isEnglish ? "Chat request submitted successfully. please wait."
                : "درخواست شما برای شروع چت ثبت شد. لطفا منتظر بمانید.", chatId, "/cancel");
    }

    private void sendRelationshipAdvise(Long chatId) {
        if (PropertiesFileManager.getInstance().showAdvice()) {
            mediaManager.messageSend(PropertiesFileManager.getInstance().getRandomTip(), chatId);
        }
    }

    private void sendExposing(Long chatId, Boolean isEnglish) {
        mediaManager.messageSendKeyboard(isEnglish ? ("Do you want your profile be visible to other users? if you tap yes then you will appear in"
                + " nearby people search result and also can search for other people around you.")
                : ("آیا مایل هستید که اطلاعات پروفایل شما با سایر کاربران به اشتراک گذاشته شود؟"
                + " درصورت موافقت شما می توانید از امکان جستجوی افراد نزدیک استفاده کنید."),
                chatId, new String[]{isEnglish ? "Yes" : "بله", isEnglish ? "No" : "نه"});
    }

}
