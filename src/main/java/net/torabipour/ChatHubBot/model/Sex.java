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
public enum Sex {
    
    Male("مرد"),
    Female("زن");
    
    
    private String name;

    private Sex(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
    
}
