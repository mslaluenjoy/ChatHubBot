/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.torabipour.ChatHubBot.model.utils;

public class UserInterfaceException extends Exception {

    private String persian;
    private String english;

    public UserInterfaceException(String message) {
        super(message);
    }

    public UserInterfaceException(String persian, String english) {
        super("Exception");
        this.persian = persian;
        this.english = english;
    }

    public String getPersian() {
        return persian;
    }

    public void setPersian(String persian) {
        this.persian = persian;
    }

    public String getEnglish() {
        return english;
    }

    public void setEnglish(String english) {
        this.english = english;
    }
    
}
