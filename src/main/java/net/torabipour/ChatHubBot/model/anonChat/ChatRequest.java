/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.torabipour.ChatHubBot.model.anonChat;

import net.torabipour.ChatHubBot.db.DBAccessWithResult;
import net.torabipour.ChatHubBot.model.Sex;
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
@Inheritance(strategy = InheritanceType.TABLE_PER_CLASS)
@NamedQueries({
    @NamedQuery(name = "loadProperMatch", query = "SELECT cr from ChatRequest cr WHERE active = TRUE AND chat = NULL AND (requester.sex = :targetSex OR :targetSex = NULL) and (targetSex = :requesterSex OR targetSex = NULL)")
    ,
    @NamedQuery(name = "loadProperMatchNoSex", query = "SELECT cr from ChatRequest cr WHERE active = TRUE AND chat = NULL")
    ,
    @NamedQuery(name = "loadByRequester", query = "SELECT cr from ChatRequest cr WHERE active = TRUE AND requester = :requester")
})
public class ChatRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    private User requester;

    @Enumerated(EnumType.ORDINAL)
    private Sex targetSex;

    private Boolean active;

    @ManyToOne(fetch = FetchType.LAZY)
    private Chat chat;
    
    @Temporal(javax.persistence.TemporalType.TIMESTAMP)
    private Date date;

    public ChatRequest() {
    }

    public ChatRequest(Long id, User requester, Sex targetSex, Boolean active, Chat chat) {
        this.id = id;
        this.requester = requester;
        this.targetSex = targetSex;
        this.active = active;
        this.chat = chat;
        this.date = new Date();
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public User getRequester() {
        return requester;
    }

    public void setRequester(User requester) {
        this.requester = requester;
    }

    public Sex getTargetSex() {
        return targetSex;
    }

    public void setTargetSex(Sex targetSex) {
        this.targetSex = targetSex;
    }

    public Boolean getActive() {
        return active;
    }

    public void setActive(Boolean active) {
        this.active = active;
    }

    public Chat getChat() {
        return chat;
    }

    public void setChat(Chat chat) {
        this.chat = chat;
    }

    public static List<ChatRequest> loadProperMatch(Sex targetSex, Sex requesterSex) {
        return new DBAccessWithResult<List<ChatRequest>>() {
            @Override
            protected List<ChatRequest> operation(Session session) {
                return loadProperMatch(targetSex, requesterSex, session);
            }
        }.execute();
    }

    public static List<ChatRequest> loadProperMatch(Sex targetSex, Sex requesterSex, Session session) {
        List<ChatRequest> result = session.getNamedQuery("loadProperMatch").setParameter("targetSex", targetSex).setParameter("requesterSex", requesterSex).list();
        
        return result;
    }

    public static List<ChatRequest> loadProperMatchNoSex() {
        return new DBAccessWithResult<List<ChatRequest>>() {
            @Override
            protected List<ChatRequest> operation(Session session) {
                return loadProperMatchNoSex(session);
            }
        }.execute();
    }

    public static List<ChatRequest> loadProperMatchNoSex(Session session) {
        List<ChatRequest> result = session.getNamedQuery("loadProperMatchNoSex").list();
        
        return result;
    }

    public static List<ChatRequest> loadByRequester(User user) {
        return new DBAccessWithResult<List<ChatRequest>>() {
            @Override
            protected List<ChatRequest> operation(Session session) {
                return loadByRequester(user, session);
            }
        }.execute();
    }

    public static List<ChatRequest> loadByRequester(User requester, Session session) {
        List<ChatRequest> result = session.getNamedQuery("loadByRequester").setParameter("requester", requester).list();
        
        return result;
    }

}
