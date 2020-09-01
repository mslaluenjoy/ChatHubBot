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
public enum MessageType {
    Text("تکست"),
    Video("ویدیو"),
    Photo("عکس"),
    Audio("صدا"),
    Animation("انیمیشن"),
    Document("سند"),
    Location("موقعیت جغرافیایی"),
    Sticker("استیکر"),
    Voice("ویس");
    
    private String name;

    private MessageType(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
}