/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.torabipour.ChatHubBot.model.globalChat;

import net.torabipour.ChatHubBot.db.DBAccessWithResult;
import net.torabipour.ChatHubBot.model.User;
import java.util.List;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import org.hibernate.Session;

/**
 *
 * @author mohammad
 */

@Entity
@Inheritance(strategy = InheritanceType.TABLE_PER_CLASS)
@NamedQueries(value = {
    @NamedQuery(name = "loadAll", query = "SELECT gc from GlobalChatRoom gc"),
    @NamedQuery(name = "loadByName", query = "SELECT gc from GlobalChatRoom gc where persianName=:name OR englishName=:name")
})
public class GlobalChatRoom {
 
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
    
    private String persianName;
    
    private String englishName;
    
    private String persianPromt;
    
    private String englishPrompt;

    public GlobalChatRoom() {
    }

    public GlobalChatRoom(Long id, String persianName, String englishName, String persianPromt, String englishPrompt) {
        this.id = id;
        this.persianName = persianName;
        this.englishName = englishName;
        this.persianPromt = persianPromt;
        this.englishPrompt = englishPrompt;
    }

    public String getPersianPromt() {
        return persianPromt;
    }

    public void setPersianPromt(String persianPromt) {
        this.persianPromt = persianPromt;
    }

    public String getEnglishPrompt() {
        return englishPrompt;
    }

    public void setEnglishPrompt(String englishPrompt) {
        this.englishPrompt = englishPrompt;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getPersianName() {
        return persianName;
    }

    public void setPersianName(String persianName) {
        this.persianName = persianName;
    }

    public String getEnglishName() {
        return englishName;
    }

    public void setEnglishName(String englishName) {
        this.englishName = englishName;
    }
    
    public static List<GlobalChatRoom> loadAll() {
        return new DBAccessWithResult<List<GlobalChatRoom>>() {
            @Override
            protected List<GlobalChatRoom> operation(Session session) {
                return loadAll(session);
            }
        }.execute();
    }

    public static List<GlobalChatRoom> loadAll(Session session) {
        List<GlobalChatRoom> result = session.getNamedQuery("loadAll").list();
        if (result == null || result.isEmpty()) {
            return null;
        }
        return result;
    }
    
    public static GlobalChatRoom loadByName(String name) {
        return new DBAccessWithResult<GlobalChatRoom>() {
            @Override
            protected GlobalChatRoom operation(Session session) {
                return loadByName(name, session);
            }
        }.execute();
    }

    public static GlobalChatRoom loadByName(String name, Session session) {
        List<GlobalChatRoom> result = session.getNamedQuery("loadByName").setParameter("name", name).list();
        if (result == null || result.isEmpty()) {
            return null;
        }
        return result.get(0);
    }
}
