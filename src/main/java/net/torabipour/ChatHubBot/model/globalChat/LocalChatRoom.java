/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.torabipour.ChatHubBot.model.globalChat;

import net.torabipour.ChatHubBot.db.DBAccessWithResult;
import net.torabipour.ChatHubBot.model.Location;
import java.util.List;
import javax.persistence.Entity;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.OneToOne;
import org.hibernate.Session;
import org.hibernate.annotations.Polymorphism;
import org.hibernate.annotations.PolymorphismType;

/**
 *
 * @author mohammad
 */
@Entity
@Polymorphism(type = PolymorphismType.EXPLICIT)
@NamedQueries(value = {
    @NamedQuery(name = "loadByCityName", query = "SELECT gc from LocalChatRoom gc where gc.location.city =: cityName")
})
public class LocalChatRoom extends GlobalChatRoom{
    
    @OneToOne
    private Location location;

    public LocalChatRoom() {
    }

    public LocalChatRoom(Long id, String persianName, String englishName, String persianPromt, String englishPrompt) {
        super(id, persianName, englishName, persianPromt, englishPrompt);
    }

    public Location getLocation() {
        return location;
    }

    public void setLocation(Location location) {
        this.location = location;
    }
    
    public static LocalChatRoom loadByCityName(String cityName) {
        return new DBAccessWithResult<LocalChatRoom>() {
            @Override
            protected LocalChatRoom operation(Session session) {
                return loadByCityName(cityName, session);
            }
        }.execute();
    }

    public static LocalChatRoom loadByCityName(String cityName, Session session) {
        List<LocalChatRoom> result = session.getNamedQuery("loadByCityName").setParameter("cityName", cityName).list();
        if (result == null || result.isEmpty()) {
            return null;
        }
        return result.get(0);
    }
}
