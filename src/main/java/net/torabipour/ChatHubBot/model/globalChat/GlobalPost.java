/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.torabipour.ChatHubBot.model.globalChat;

import net.torabipour.ChatHubBot.db.DBAccessWithResult;
import net.torabipour.ChatHubBot.model.MessageType;
import net.torabipour.ChatHubBot.model.User;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Transient;
import org.hibernate.Session;

/**
 *
 * @author mohammad
 */

@Entity
@NamedQueries(value = {
    @NamedQuery(name = "loadRecentPosts", query = "SELECT gp from GlobalPost gp where gcr=:gcr ORDER BY date DESC")
})
public class GlobalPost {
    
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
    
    @ManyToOne(fetch = FetchType.EAGER)
    private User sender;
    
    @Temporal(TemporalType.TIMESTAMP)
    private Date date;
    
    @ManyToOne(fetch = FetchType.LAZY)
    private GlobalChatRoom gcr;
    
    private String content;
    
    private String caption;
    
    @Enumerated(EnumType.ORDINAL)
    private MessageType type;
    
    @ManyToOne(fetch = FetchType.EAGER)
    private GlobalPost replyTo;
    
    private int telegramMessageId;
    
    private Long telegramChatId;

    public GlobalPost(Long id, User sender, Date date, GlobalChatRoom gcr, String content, String caption, MessageType type, GlobalPost replyTo, int telegramMessageId, Long telegramChatId) {
        this.id = id;
        this.sender = sender;
        this.date = date;
        this.gcr = gcr;
        this.content = content;
        this.caption = caption;
        this.type = type;
        this.replyTo = replyTo;
        this.telegramMessageId = telegramMessageId;
        this.telegramChatId = telegramChatId;
    }
    
    public GlobalPost() {
    }

    public GlobalPost getReplyTo() {
        return replyTo;
    }

    public void setReplyTo(GlobalPost replyTo) {
        this.replyTo = replyTo;
    }
    
    public int getTelegramMessageId() {
        return telegramMessageId;
    }

    public void setTelegramMessageId(int telegramMessageId) {
        this.telegramMessageId = telegramMessageId;
    }

    public Long getTelegramChatId() {
        return telegramChatId;
    }

    public void setTelegramChatId(Long telegramChatId) {
        this.telegramChatId = telegramChatId;
    }

    @Transient
    public Set<ChatMessage> getEmmited() {
        return ChatMessage.loadByGlobalPost(this);
    }
    
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public User getSender() {
        return sender;
    }

    public void setSender(User sender) {
        this.sender = sender;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public GlobalChatRoom getGcr() {
        return gcr;
    }

    public void setGcr(GlobalChatRoom gcr) {
        this.gcr = gcr;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getCaption() {
        return caption;
    }

    public void setCaption(String caption) {
        this.caption = caption;
    }

    public MessageType getType() {
        return type;
    }

    public void setType(MessageType type) {
        this.type = type;
    }
    
    public static List<GlobalPost> loadRecentPosts(GlobalChatRoom gcr, int maxSize) {
        return new DBAccessWithResult<List<GlobalPost>>() {
            @Override
            protected List<GlobalPost> operation(Session session) {
                return loadRecentPosts(gcr,maxSize, session);
            }
        }.execute();
    }

    public static List<GlobalPost> loadRecentPosts(GlobalChatRoom gcr,int maxSize, Session session) {
        List<GlobalPost> result = session.getNamedQuery("loadRecentPosts").setMaxResults(maxSize).setParameter("gcr", gcr).list();
        if (result == null || result.isEmpty()) {
            return null;
        }
        return result;
    }
}
