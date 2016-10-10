package com.rmpi.telegram.calculator;

import org.telegram.telegrambots.api.methods.send.SendMessage;
import org.telegram.telegrambots.api.objects.Message;
import org.telegram.telegrambots.api.objects.Update;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.exceptions.TelegramApiException;

import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.stream.Collectors;

public class CalculatorBot extends TelegramLongPollingBot {
    private final String TOKEN;
    private String reply;

    public CalculatorBot(String token) {
        TOKEN = token;
    }

    @Override
    public String getBotToken() {
        return TOKEN;
    }

    @Override
    public void onUpdateReceived(Update update) {
        if (!update.hasMessage())
            return;
        Message request = update.getMessage();
        if (!request.hasText() || !request.isCommand())
            return;
        String message = request.getText();
        assert message.startsWith("/");
        ArrayList<String> tokens = new ArrayList<>(Arrays.asList(message.split(" ")));
        String command = tokens.remove(0).substring(1);

        if (command.contains("@")) {
            String[] commandSplit = command.split("@");
            if (commandSplit.length > 2) return;
            if (!commandSplit[1].equals(getBotUsername())) return;
            command = commandSplit[0];
        }

        switch (command) {
            case "calculate":

                reply = "INVALID VALUE";
                Thread parseThread = new Thread(() -> {
                    try {
                        reply = new ExpressionParser(
                                new ByteArrayInputStream(
                                        tokens.parallelStream()
                                                .collect(Collectors.joining(" "))
                                                .getBytes()
                                )
                        ).expr().toPlainString();
                    } catch (ParseException e) {
                        reply = "INVALID VALUE";
                    }
                });
                parseThread.start();

                try {
                    parseThread.join(5000);
                } catch (InterruptedException e) {

                }

                if (parseThread.isAlive()) {
                    parseThread.stop();
                    reply = "TIMEOUT";
                }

                new Thread(() -> {
                    for (String chunk : reply.split("(?<=\\G.{100})")) {
                        SendMessage response = new SendMessage();
                        response.setChatId(String.valueOf(request.getChatId()));
                        response.setReplyToMessageId(request.getMessageId());
                        response.setText(chunk);
                        response.disableWebPagePreview();

                        try {
                            sendMessage(response);
                        } catch (TelegramApiException e) {
                            e.printStackTrace();
                        }

                        try {
                            Thread.sleep(1500);
                        } catch (InterruptedException e) {

                        }
                    }
                }) {
                    public String parserIdentifier;
                }.start();
                break;
            case "break":
                Thread.getAllStackTraces().keySet().forEach(t -> {
                    try {
                        t.getClass().getDeclaredField("parserIdentifier");
                        t.stop();
                    } catch (NoSuchFieldException e) {

                    }
                });
                SendMessage response = new SendMessage();
                response.setChatId(String.valueOf(request.getChatId()));
                response.setReplyToMessageId(request.getMessageId());
                response.setText("All child execution is broken");
                response.disableWebPagePreview();

                try {
                    sendMessage(response);
                } catch (TelegramApiException e) {
                    e.printStackTrace();
                }

                break;
        }
    }

    @Override
    public String getBotUsername() {
        return "calculatorRbot";
    }
}
