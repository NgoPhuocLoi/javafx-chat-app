package com.example.chatapp;

import com.example.chatapp.controllers.ServerController;

public class Main {
    public static void main(String[] args) {
        Thread serverThread = new Thread(() -> {
            ServerController.main(null);
        });

        Thread clientThread = new Thread(() -> {
            ChatApplication.main(null);
        });

        serverThread.start();
        clientThread.start();
    }
}
