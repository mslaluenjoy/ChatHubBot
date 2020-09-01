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

public enum UserStatus {
    LanguageSelect("درحال ثبت نام", "registering"),
    SexSelect("درحال ثبت نام", "registering"),
    NickNameSelect("درحال ثبت نام", "registering"),
    ExposingSelect("درحال ثبت نام", "registering"),
    LocationSelect("درحال ثبت نام", "registering"),
    ProfilePictureSelect("درحال ثبت نام", "registering"),
    AgeSelect("درحال ثبت نام", "registering"),
    Registered("در دسترس", "available"),
    WaitingForConnection("در انتظار اتصال به چت", "waiting for connection"),
    InChat("درحال چت", "in chat"),
    SelectingGlobalChat("درحال چت گلوبال", "selecting global chat"),
    InGlobalChat("درحال چت گلوبال", "in global chat"),
    SendingAnonMessage("درحال ارسال پیام", "sending anonymous message"),
    ReadingAnonMessage("درحال خواندن پیام", "checking inbox"),
    JoiningLocalRoom("درحال چت گلوبال", "joining local room"),
    ViewingProfile("درحال مشاهده پروفایل دیگران", "viewing profile"),
    FindingNearby("درحال جستجوی افراد نزدیک", "finding nearby");
    
    private String persian;
    private String english;

    private UserStatus(String persian, String english) {
        this.persian = persian;
        this.english = english;
    }

    public String getPersian() {
        return persian;
    }

    public String getEnglish() {
        return english;
    }
    
}
