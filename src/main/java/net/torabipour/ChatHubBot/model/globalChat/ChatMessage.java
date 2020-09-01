/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.torabipour.ChatHubBot.model.globalChat;

import net.torabipour.ChatHubBot.db.DBAccessWithResult;
import net.torabipour.ChatHubBot.model.User;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.persistence.Embeddable;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import org.hibernate.Session;

/**
 *
 * @author mohammad
 */
@Entity
@NamedQueries(value = {
    @NamedQuery(name = "loadByGlobalPost", query = "SELECT cm from ChatMessage cm where gp=:gp"),
    @NamedQuery(name = "loadByMessageId", query = "SELECT cm from ChatMessage cm where messageId=:messageId"),
    @NamedQuery(name = "loadByGlobalPostAndChatId", query = "SELECT cm from ChatMessage cm where gp=:gp and chatId=:chatId")
})
public class ChatMessage {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    private int messageId;
    private Long chatId;
    @ManyToOne(fetch = FetchType.LAZY)
    private GlobalPost gp;

    public ChatMessage(Long id, int messageId, Long chatId, GlobalPost gp) {
        this.id = id;
        this.messageId = messageId;
        this.chatId = chatId;
        this.gp = gp;
    }

    public ChatMessage() {
    }

    public GlobalPost getGp() {
        return gp;
    }

    public void setGp(GlobalPost gp) {
        this.gp = gp;
    }

    public int getMessageId() {
        return messageId;
    }

    public void setMessageId(int messageId) {
        this.messageId = messageId;
    }

    public Long getChatId() {
        return chatId;
    }

    public void setChatId(Long chatId) {
        this.chatId = chatId;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public static Set<ChatMessage> loadByGlobalPost(GlobalPost gp) {
        return new DBAccessWithResult<Set<ChatMessage>>() {
            @Override
            protected Set<ChatMessage> operation(Session session) {
                return loadByGlobalPost(gp, session);
            }
        }.execute();
    }

    public static Set<ChatMessage> loadByGlobalPost(GlobalPost gp, Session session) {
        List<ChatMessage> result = session.getNamedQuery("loadByGlobalPost").setParameter("gp", gp).list();
        if (result == null || result.isEmpty()) {
            return null;
        }
        return new HashSet<>(result);
    }
    
    public static Set<ChatMessage> loadByGlobalPostAndChatId(GlobalPost gp, Long chatId) {
        return new DBAccessWithResult<Set<ChatMessage>>() {
            @Override
            protected Set<ChatMessage> operation(Session session) {
                return loadByGlobalPost(gp, session);
            }
        }.execute();
    }

    public static Set<ChatMessage> loadByGlobalPostAndChatId(GlobalPost gp, Long chatId, Session session) {
        List<ChatMessage> result = session.getNamedQuery("loadByGlobalPost").setParameter("gp", gp).setParameter("chatId", chatId).list();
        if (result == null || result.isEmpty()) {
            return null;
        }
        return new HashSet<>(result);
    }
    
    public static ChatMessage loadByMessageId(int messageId) {
        return new DBAccessWithResult<ChatMessage>() {
            @Override
            protected ChatMessage operation(Session session) {
                return loadByMessageId(messageId, session);
            }
        }.execute();
    }

    public static ChatMessage loadByMessageId(int messageId, Session session) {
        List<ChatMessage> result = session.getNamedQuery("loadByMessageId").setParameter("messageId", messageId).list();
        if (result == null || result.isEmpty()) {
            return null;
        }
        return result.get(0);
    }
}
