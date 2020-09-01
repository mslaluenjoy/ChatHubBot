/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.torabipour.ChatHubBot.model.utils;

public class UserInterfaceException extends Exception {

    private int errorCode;

    public UserInterfaceException(String message) {
        super(message);
    }

    public int getErrorCode() {
        return errorCode;
    }
}
