/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.torabipour.ChatHubBot.model.anonChat;

import net.torabipour.ChatHubBot.db.DBAccessWithResult;
import net.torabipour.ChatHubBot.model.Sex;
import net.torabipour.ChatHubBot.model.User;
import java.util.List;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import org.hibernate.Session;
import org.hibernate.annotations.Polymorphism;
import org.hibernate.annotations.PolymorphismType;

/**
 *
 * @author mohammad
 */
@Entity
@Inheritance(strategy = InheritanceType.TABLE_PER_CLASS)
@NamedQueries({
    @NamedQuery(name = "loadByRequesterTargeted", query = "SELECT cr from TargetedChatRequest cr WHERE active = TRUE AND requester = :requester and targetRequest=:targetRequest")
})
@Polymorphism(type = PolymorphismType.EXPLICIT)
public class TargetedChatRequest extends ChatRequest {

    private Boolean isAcceptedByTarget;
    @ManyToOne(fetch = FetchType.EAGER)
    private User targetRequest;

    public TargetedChatRequest() {
    }

    public TargetedChatRequest(Long id, User requester, Sex targetSex, Boolean active, Chat chat) {
        super(id, requester, targetSex, active, chat);
    }

    public Boolean getIsAcceptedByTarget() {
        return isAcceptedByTarget;
    }

    public void setIsAcceptedByTarget(Boolean isAcceptedByTarget) {
        this.isAcceptedByTarget = isAcceptedByTarget;
    }

    public User getTargetRequest() {
        return targetRequest;
    }

    public void setTargetRequest(User targetRequest) {
        this.targetRequest = targetRequest;
    }

    public static TargetedChatRequest loadByRequesterTargeted(User requester, User targetRequest) {
        return new DBAccessWithResult<TargetedChatRequest>() {
            @Override
            protected TargetedChatRequest operation(Session session) {
                return loadByRequesterTargeted(requester, targetRequest, session);
            }
        }.execute();
    }

    public static TargetedChatRequest loadByRequesterTargeted(User requester, User targetRequest, Session session) {
        List<TargetedChatRequest> result = session.getNamedQuery("loadByRequesterTargeted").setParameter("requester", requester).setParameter("targetRequest", targetRequest).list();
        if (result == null || result.isEmpty()) {
            return null;
        }
        return result.get(0);
    }
}
