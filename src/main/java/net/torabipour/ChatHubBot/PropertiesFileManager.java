/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.torabipour.ChatHubBot;

import java.io.IOException;
import java.util.Properties;
import java.util.Random;

/**
 *
 * @author mohammad
 */
public class PropertiesFileManager {

    private static PropertiesFileManager propertyManager = null;
    private Properties pro;
    private Properties englishTips;

    public String getRandomTip() {
        readEnglishRelationshipFile();
        int size = englishTips.keySet().size();
        int item = new Random().nextInt(size); // In real life, the Random object should be rather more shared than this
        int i = 0;
        for (Object obj : englishTips.keySet()) {
            if (i == item) {
                StringBuilder sb = new StringBuilder();
                sb.append("Relationship advice ❤️👇🏻\n\n");
                sb.append("🔔");
                sb.append(String.valueOf(obj));
                sb.append("\n");
                sb.append("✅");
                sb.append(String.valueOf(englishTips.get(obj)));
                return sb.toString();
            }
            i++;
        }
        return "";
    }

    public String getBotToken() {
        return readConfigFile().getProperty("token", "");
    }
    
    public String getIpAddress() {
        return readConfigFile().getProperty("ipAddress", "https://server.chathubbot.com");
    }
    
    public Integer getPort() {
        return Integer.valueOf(readConfigFile().getProperty("port", "88"));
    }

    public String getBotName() {
        return readConfigFile().getProperty("name", "ChattHubBot");
    }

    public Integer getGlobalRecentNum() {
        return Integer.valueOf(readConfigFile().getProperty("globalRecentNum", "25"));
    }

    public Integer getUsersShowNum() {
        return Integer.valueOf(readConfigFile().getProperty("usersShowNum", "25"));
    }

    public Integer getMaxMediaSizeMB() {
        return Integer.valueOf(readConfigFile().getProperty("maxMediaSizeMB", "10"));
    }
    
    public Boolean checkForFileSize() {
        return Boolean.valueOf(readConfigFile().getProperty("checkForFileSize", "true"));
    }
    
    public Boolean isTest() {
        return Boolean.valueOf(readConfigFile().getProperty("isTest", "false"));
    }

    public Boolean showAdvice() {
        return Boolean.valueOf(readConfigFile().getProperty("showAdvice", "true"));
    }

    public Integer getMaxMediaSizeKb() {
        return getMaxMediaSizeMB() * 1000;
    }

    public Integer getMaxMediaSizeByte() {
        return getMaxMediaSizeKb() * 1000;
    }

    public Properties readConfigFile() {

        try {
            if (pro == null) {
                pro = new Properties();
                pro.load(this.getClass().getResourceAsStream("/Config.properties"));
            }
        } catch (IOException ex) {
        }
        return pro;
    }

    public Properties readEnglishRelationshipFile() {

        try {
            if (englishTips == null) {
                englishTips = new Properties();
                englishTips.load(this.getClass().getResourceAsStream("/EnglishRelationshipTips.properties"));
            }
        } catch (IOException ex) {
        }
        return englishTips;
    }

    private PropertiesFileManager() {
        readConfigFile();
    }

    public static PropertiesFileManager getInstance() {
        if (propertyManager == null) {
            propertyManager = new PropertiesFileManager();
        }
        return propertyManager;
    }
}
