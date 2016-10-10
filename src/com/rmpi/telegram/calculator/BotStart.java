package com.rmpi.telegram.calculator;

import org.telegram.telegrambots.TelegramBotsApi;
import org.telegram.telegrambots.exceptions.TelegramApiRequestException;

public class BotStart {
    public static void main(String[] args) {
        if (args.length < 1) {
            System.out.println("Requires bot token for first argument");
            return;
        }

        TelegramBotsApi api = new TelegramBotsApi();

        try {
            api.registerBot(new CalculatorBot(args[0]));
        } catch (TelegramApiRequestException e) {
            e.printStackTrace();
        }
    }
}
