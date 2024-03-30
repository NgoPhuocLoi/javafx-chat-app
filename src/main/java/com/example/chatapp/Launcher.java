package com.example.chatapp;

public class Launcher {

    public static void main(String[] args) {
        Thread clientThread1 = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    ChatApplication.main(args);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

        Thread clientThread2 = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(1000);
                    System.out.println("RUN HERER");

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

        clientThread1.start();
        clientThread2.start();
    }
}
