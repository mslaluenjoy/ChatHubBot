/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.torabipour.ChatHubBot.controller;

import com.pengrad.telegrambot.BotUtils;
import com.pengrad.telegrambot.model.Update;
import net.torabipour.ChatHubBot.model.factory.GlobalChatRoomFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import spark.Request;

/**
 *
 * @author mohammad
 */
@RestController
public class BotController {
    @PostMapping("/")
    public void update(@RequestBody String request) {
        GlobalChatRoomFactory.initiate();
        Update update = BotUtils.parseUpdate(request);
        TelegramBotManager.getInstance().getHandler().handleUpdate(update);
    }
}
