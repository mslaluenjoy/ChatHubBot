/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.torabipour.ChatHubBot.model.factory;

import net.torabipour.ChatHubBot.db.TransactionalDBAccess;
import net.torabipour.ChatHubBot.model.anonChat.Chat;
import net.torabipour.ChatHubBot.model.MessageType;
import net.torabipour.ChatHubBot.model.User;
import java.nio.charset.StandardCharsets;
import org.hibernate.Session;

/**
 *
 * @author mohammad
 */
public class MessageFactory {

    public static void createAndSave(String content, MessageType type, Integer senderId, Integer receiverId, Chat chat, User sender) {
        byte[] bytes = content.getBytes(StandardCharsets.UTF_8);
        String utf8EncodedString = new String(bytes, StandardCharsets.UTF_8);
        net.torabipour.ChatHubBot.model.anonChat.Message newMsg = new net.torabipour.ChatHubBot.model.anonChat.Message(null, sender, chat.getEndpoint1().getId().equals(sender.getId())
                ? chat.getEndpoint2() : chat.getEndpoint1(), chat, utf8EncodedString, senderId, receiverId, type);
        try {
            new TransactionalDBAccess() {
                @Override
                protected void operation(Session session) {
                    session.saveOrUpdate(newMsg);
                }
            }.execute();
        } catch (Exception ex) {

        }
    }
}
