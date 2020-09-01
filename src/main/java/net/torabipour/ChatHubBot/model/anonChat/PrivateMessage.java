/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.torabipour.ChatHubBot.model.anonChat;

import net.torabipour.ChatHubBot.db.DBAccessWithResult;
import net.torabipour.ChatHubBot.model.MessageType;
import net.torabipour.ChatHubBot.model.User;
import static net.torabipour.ChatHubBot.model.anonChat.Message.loadByReceiveId;
import java.util.List;
import javax.persistence.Entity;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import org.hibernate.Session;

/**
 *
 * @author mohammad
 */
@Entity
@NamedQueries(value = {
    @NamedQuery(name = "loadBySender", query = "SELECT m from Message m where sender=:sender ORDER BY date DESC"),
    @NamedQuery(name = "loadByReceiver", query = "SELECT m from Message m where receiver=:receiver and isRed is false ORDER BY date ASC"),
})
public class PrivateMessage extends Message {

    private Boolean isRed;

    public PrivateMessage(Long id, User sender, User receiver, Chat chat, String content, Integer sendId, Integer receiveId, MessageType type) {
        super(id, sender, receiver, chat, content, sendId, receiveId, type);
        this.isRed = false;
    }

    public PrivateMessage() {
    }
    
    public Boolean getIsRed() {
        return isRed;
    }

    public void setIsRed(Boolean isRed) {
        this.isRed = isRed;
    }

    public static PrivateMessage loadBySender(User sender) {
        return new DBAccessWithResult<PrivateMessage>() {
            @Override
            protected PrivateMessage operation(Session session) {
                return loadBySender(sender, session);
            }
        }.execute();
    }

    public static PrivateMessage loadBySender(User sender, Session session) {
        List<PrivateMessage> result = session.getNamedQuery("loadBySender").setParameter("sender", sender).list();
        if (result == null || result.isEmpty()) {
            return null;
        }
        return result.get(0);
    }
    
    public static List<PrivateMessage> loadByReceiver(User receiver) {
        return new DBAccessWithResult<List<PrivateMessage>>() {
            @Override
            protected List<PrivateMessage> operation(Session session) {
                return loadByReceiver(receiver, session);
            }
        }.execute();
    }

    public static List<PrivateMessage> loadByReceiver(User receiver, Session session) {
        List<PrivateMessage> result = session.getNamedQuery("loadByReceiver").setParameter("receiver", receiver).list();
        return result;
    }

}
