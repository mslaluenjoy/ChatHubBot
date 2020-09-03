package net.torabipour.ChatHubBot;

import net.torabipour.ChatHubBot.controller.TelegramBotManager;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class ChatHubBotApplication {

    public static void main(String[] args) {
        TelegramBotManager.getInstance();
        SpringApplication.run(ChatHubBotApplication.class, args);
    }

}
