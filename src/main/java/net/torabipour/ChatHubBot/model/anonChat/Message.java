/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.torabipour.ChatHubBot.model.anonChat;

import net.torabipour.ChatHubBot.model.anonChat.Chat;
import net.torabipour.ChatHubBot.db.DBAccessWithResult;
import net.torabipour.ChatHubBot.model.MessageType;
import net.torabipour.ChatHubBot.model.User;
import java.util.Date;
import java.util.List;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Temporal;
import org.hibernate.Session;

/**
 *
 * @author mohammad
 */

@Entity
@NamedQueries(value = {
    @NamedQuery(name = "loadBySendId", query = "SELECT m from Message m where sendId=:sendId"),
    @NamedQuery(name = "loadByReceiveId", query = "SELECT m from Message m where receiveId=:receiveId"),
    @NamedQuery(name = "loadByChat", query = "SELECT m from Message m where chat=:chat")
})
@Inheritance(strategy = InheritanceType.TABLE_PER_CLASS)  
public class Message {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    protected Long id;
    
    @ManyToOne(fetch = FetchType.EAGER)
    protected User sender;
    @ManyToOne(fetch = FetchType.EAGER)
    protected User receiver;
    @ManyToOne(fetch = FetchType.EAGER)
    protected Chat chat;
    
    protected String content;
    
    protected Integer sendId;
    
    protected Integer receiveId;
    
    protected String caption;
    
    @Enumerated(EnumType.ORDINAL)
    protected MessageType type;
    
    @Temporal(javax.persistence.TemporalType.TIMESTAMP)
    protected Date date;

    public Message() {
    }

    public Message(Long id, User sender, User receiver, Chat chat, String content, Integer sendId, Integer receiveId, MessageType type) {
        this.id = id;
        this.sender = sender;
        this.receiver = receiver;
        this.chat = chat;
        this.content = content;
        this.sendId = sendId;
        this.receiveId = receiveId;
        this.type = type;
        this.date = new Date();
    }

    public String getCaption() {
        return caption;
    }

    public void setCaption(String caption) {
        this.caption = caption;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public Chat getChat() {
        return chat;
    }

    public void setChat(Chat chat) {
        this.chat = chat;
    }

    public Integer getSendId() {
        return sendId;
    }

    public void setSendId(Integer sendId) {
        this.sendId = sendId;
    }

    public Integer getReceiveId() {
        return receiveId;
    }

    public void setReceiveId(Integer receiveId) {
        this.receiveId = receiveId;
    }

    public MessageType getType() {
        return type;
    }

    public void setType(MessageType type) {
        this.type = type;
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

    public User getReceiver() {
        return receiver;
    }

    public void setReceiver(User receiver) {
        this.receiver = receiver;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }
    
    public static Message loadBySendId(Integer sendId) {
        return new DBAccessWithResult<Message>() {
            @Override
            protected Message operation(Session session) {
                return loadBySendId(sendId, session);
            }
        }.execute();
    }

    public static Message loadBySendId(Integer sendId, Session session) {
        List<Message> result = session.getNamedQuery("loadBySendId").setParameter("sendId", sendId).list();
        if (result == null || result.isEmpty()) {
            return null;
        }
        return result.get(0);
    }
    
    public static Message loadByReceiveId(Integer receiveId) {
        return new DBAccessWithResult<Message>() {
            @Override
            protected Message operation(Session session) {
                return loadByReceiveId(receiveId, session);
            }
        }.execute();
    }

    public static Message loadByReceiveId(Integer receiveId, Session session) {
        List<Message> result = session.getNamedQuery("loadByReceiveId").setParameter("receiveId", receiveId).list();
        if (result == null || result.isEmpty()) {
            return null;
        }
        return result.get(0);
    }
    
    public static List<Message> loadByChat(Chat chat) {
        return new DBAccessWithResult<List<Message>>() {
            @Override
            protected List<Message> operation(Session session) {
                return loadByChat(chat, session);
            }
        }.execute();
    }

    public static List<Message> loadByChat(Chat chat, Session session) {
        List<Message> result = session.getNamedQuery("loadByChat").setParameter("chat", chat).list();
        
        return result;
    }
    
}
