/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.torabipour.ChatHubBot.model;

import net.torabipour.ChatHubBot.PropertiesFileManager;
import net.torabipour.ChatHubBot.db.DBAccessWithResult;
import net.torabipour.ChatHubBot.db.TransactionalDBAccess;
import net.torabipour.ChatHubBot.model.globalChat.GlobalChatRoom;
import net.torabipour.ChatHubBot.model.utils.UserInterfaceException;
import net.torabipour.ChatHubBot.model.utils.location.Address;
import net.torabipour.ChatHubBot.model.utils.location.NominatimReverseGeocodingJAPI;
import java.io.Serializable;
import java.util.Date;
import java.util.List;

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
import javax.persistence.OneToOne;
import org.hibernate.Session;

@Entity

@NamedQueries(value = {
    @NamedQuery(name = "loadByTelegramId", query = "SELECT u from User u where telegramId=:telegramId")
    ,
    @NamedQuery(name = "loadById", query = "SELECT u from User u where id=:id")
    ,
    @NamedQuery(name = "loadByGcr", query = "SELECT u from User u where gcr=:gcr")
    ,
    @NamedQuery(name = "loadAllExposingByCity", query = "SELECT u from User u where location.city=:city and exposing is true ORDER BY lastLogin DESC")
    ,
    @NamedQuery(name = "loadAllExposingByCityAndSex", query = "SELECT u from User u where location.city=:city and sex=:sex and exposing is true ORDER BY lastLogin DESC")
})
public class User implements Serializable {

    private static final long serialVersionUID = -1798070786993154676L;

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Enumerated(EnumType.ORDINAL)
    private Sex sex;

    @Enumerated(EnumType.ORDINAL)
    private Language lang;

    @Enumerated(EnumType.ORDINAL)
    private UserStatus status;

    @ManyToOne(fetch = FetchType.EAGER)
    private GlobalChatRoom gcr;

    @OneToOne
    private Location location;

    @OneToOne(fetch = FetchType.LAZY)
    private Address address;

    private Integer age;

    private String profilePicture;

    private Boolean exposing;

    private String telegramId;
    
    private Date lastLogin;

    private String telegramUserName;

    private Long chatId;

    private String firstName;

    private String lastName;

    private String nickName;

    public User() {
    }

    public User(Sex sex, Language lang, String telegramId, String telegramUserName, String firstName, String lastName, UserStatus status, Long chatId) {
        this.sex = sex;
        this.lang = lang;
        this.telegramId = telegramId;
        this.telegramUserName = telegramUserName;
        this.firstName = firstName;
        this.lastName = lastName;
        this.status = status;
        this.chatId = chatId;
    }

    public Date getLastLogin() {
        return lastLogin;
    }

    public void setLastLogin(Date lastLogin) {
        this.lastLogin = lastLogin;
    }

    public Integer getAge() {
        return age;
    }

    public void setAge(Integer age) {
        this.age = age;
    }

    public String getProfilePicture() {
        return profilePicture;
    }

    public void setProfilePicture(String profilePicture) {
        this.profilePicture = profilePicture;
    }

    public Boolean getExposing() {
        return exposing;
    }

    public void setExposing(Boolean exposing) {
        this.exposing = exposing;
    }

    public Address getAddress() {
        return address;
    }

    public void setAddress(Address address) {
        this.address = address;
    }

    public Location getLocation() {
        return location;
    }

    public void setLocation(Location location) {
        this.location = location;
    }

    public GlobalChatRoom getGcr() {
        return gcr;
    }

    public void setGcr(GlobalChatRoom gcr) {
        this.gcr = gcr;
    }

    public Long getChatId() {
        return chatId;
    }

    public void setChatId(Long chatId) {
        this.chatId = chatId;
    }

    public UserStatus getStatus() {
        return status;
    }

    public void setStatus(UserStatus status) {
        this.status = status;
    }

    public String getNickName() {
        return nickName;
    }

    public void setNickName(String nickName) {
        this.nickName = nickName;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public Language getLang() {
        return lang;
    }

    public void setLang(Language lang) {
        this.lang = lang;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Sex getSex() {
        return sex;
    }

    public void setSex(Sex sex) {
        this.sex = sex;
    }

    public String getTelegramId() {
        return telegramId;
    }

    public void setTelegramId(String telegramId) {
        this.telegramId = telegramId;
    }

    public String getTelegramUserName() {
        return telegramUserName;
    }

    public void setTelegramUserName(String telegramUserName) {
        this.telegramUserName = telegramUserName;
    }

    public static User loadByTelegramId(String telegramId) {
        return new DBAccessWithResult<User>() {
            @Override
            protected User operation(Session session) {
                return loadByTelegramId(telegramId, session);
            }
        }.execute();
    }

    public static User loadByTelegramId(String telegramId, Session session) {
        List<User> result = session.getNamedQuery("loadByTelegramId").setParameter("telegramId", telegramId).list();
        if (result == null || result.isEmpty()) {
            return null;
        }
        return result.get(0);
    }

    public static User loadById(Long id) {
        return new DBAccessWithResult<User>() {
            @Override
            protected User operation(Session session) {
                return loadById(id, session);
            }
        }.execute();
    }

    public static User loadById(Long id, Session session) {
        List<User> result = session.getNamedQuery("loadById").setParameter("id", id).list();
        if (result == null || result.isEmpty()) {
            return null;
        }
        return result.get(0);
    }

    public static List<User> loadAllExposingByCityAndSex(String city, Sex sex) {
        return new DBAccessWithResult<List<User>>() {
            @Override
            protected List<User> operation(Session session) {
                return loadAllExposingByCityAndSex(city, sex, session);
            }
        }.execute();
    }

    public static List<User> loadAllExposingByCityAndSex(String city, Sex sex, Session session) {
        List<User> result = session.getNamedQuery("loadAllExposingByCityAndSex").setParameter("city", city).setParameter("sex", sex).setMaxResults(PropertiesFileManager.getInstance().getUsersShowNum()).list();
       
        return result;
    }

    public static List<User> loadAllExposingByCity(String city) {
        return new DBAccessWithResult<List<User>>() {
            @Override
            protected List<User> operation(Session session) {
                return loadAllExposingByCity(city, session);
            }
        }.execute();
    }

    public static List<User> loadAllExposingByCity(String city, Session session) {
        List<User> result = session.getNamedQuery("loadAllExposingByCity").setParameter("city", city).setMaxResults(PropertiesFileManager.getInstance().getUsersShowNum()).list();
        
        return result;
    }

    public static List<User> loadByGcr(GlobalChatRoom gcr) {
        return new DBAccessWithResult<List<User>>() {
            @Override
            protected List<User> operation(Session session) {
                return loadByGcr(gcr, session);
            }
        }.execute();
    }

    public static List<User> loadByGcr(GlobalChatRoom gcr, Session session) {
        List<User> result = session.getNamedQuery("loadByGcr").setParameter("gcr", gcr).list();
        
        return result;
    }

    public void save() {
        new TransactionalDBAccess() {
            @Override
            protected void operation(Session session) {
                session.saveOrUpdate(this);
            }
        }.execute();
    }

    public void setLocationAndAddress(com.pengrad.telegrambot.model.Location location, NominatimReverseGeocodingJAPI geo) throws UserInterfaceException {

        Location loc = new Location(location.latitude(), location.longitude());
        Address address = geo.getAdress(Double.valueOf(loc.getLatitude()), Double.valueOf(loc.getLongitude()));
        if (address.getCity() == null || address.getCity() == null) {
            Boolean isEnglish = Language.English.equals(lang);
            throw new UserInterfaceException(isEnglish ? "Please send a valid location." : "لطفا یک موقعیت جغرافیایی معتبر بفرستید.");
        }
        loc.setCity(address.getCity());

        new TransactionalDBAccess() {
            @Override
            protected void operation(Session session) {
                session.saveOrUpdate(address);
                session.saveOrUpdate(loc);
            }
        }.execute();

        setLocation(loc);
        setAddress(address);
    }

}
