/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.torabipour.ChatHubBot.model;

/**
 *
 * @author mohammad
 */
public enum Language {
    
    English("🇬🇧"),
    Persian("🇮🇷");
    
    private String name;

    private Language(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
    
    public String getButtonCaption(){
        return this.toString() + " (" + this.getName() + ") ";
    }
    
}
