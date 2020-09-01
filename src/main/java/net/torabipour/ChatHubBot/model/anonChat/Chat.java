/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.torabipour.ChatHubBot.model.anonChat;

import net.torabipour.ChatHubBot.db.DBAccessWithResult;
import net.torabipour.ChatHubBot.model.User;
import java.util.Date;
import java.util.List;
import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.OneToMany;
import javax.persistence.Temporal;
import javax.persistence.Transient;
import org.hibernate.Session;

/**
 *
 * @author mohammad
 */
@Entity
@NamedQueries({
    @NamedQuery(name = "loadByEndpoint", query = "SELECT chat FROM Chat chat where (chat.endpoint1=:user OR chat.endpoint2=:user) and chat.active is true")
})
public class Chat {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    private User endpoint1;
    @ManyToOne(fetch = FetchType.EAGER)
    private User endpoint2;

    private Boolean active;
    
    @Temporal(javax.persistence.TemporalType.TIMESTAMP)
    private Date date;

    public Chat() {
    }

    public Chat(Long id, User endpoint1, User endpoint2, Boolean active) {
        this.id = id;
        this.endpoint1 = endpoint1;
        this.endpoint2 = endpoint2;
        this.active = active;
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

    public User getEndpoint1() {
        return endpoint1;
    }

    public void setEndpoint1(User endpoint1) {
        this.endpoint1 = endpoint1;
    }

    public User getEndpoint2() {
        return endpoint2;
    }

    public void setEndpoint2(User endpoint2) {
        this.endpoint2 = endpoint2;
    }

    @Transient
    public List<Message> getmessages() {
        return Message.loadByChat(this);
    }


    public Boolean getActive() {
        return active;
    }

    public void setActive(Boolean active) {
        this.active = active;
    }

    public static Chat loadByEndpoint(User user) {
        return new DBAccessWithResult<Chat>() {
            @Override
            protected Chat operation(Session session) {
                return loadByEndpoint(user, session);
            }
        }.execute();
    }

    public static Chat loadByEndpoint(User user, Session session) {
        List<Chat> result = session.getNamedQuery("loadByEndpoint").setParameter("user", user).list();
        if (result == null || result.isEmpty()) {
            return null;
        }
        return result.get(0);
    }

}
