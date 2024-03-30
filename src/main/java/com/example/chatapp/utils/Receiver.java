package com.example.chatapp.utils;

import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.util.Arrays;

public class Receiver implements Runnable {
    private DataInputStream input;

    public Receiver(DataInputStream dis) {
        this.input = dis;
    }

    @Override
    public void run() {
        try {
            while (true) {
                // Chờ thông điệp từ server
                String[] messageReceived = input.readUTF().split(",");

                if (messageReceived[0].equals("Text")) {
                    // Nhận một tin nhắn văn bản
                    String sender = messageReceived[1];
                    String receiver = messageReceived[2];
                    String message = messageReceived[3];
                    // In tin nhắn lên màn hình chat với người gửi
//                    newMessage(sender, receiver, message, false);
//                    autoScroll();

                } else if (messageReceived[0].equals("File")) {
                    // Nhận một file
                    String sender = messageReceived[1];
                    String receiver = messageReceived[2];
                    String filename = messageReceived[3];
//                    int size = Integer.parseInt(messageReceived[4]);
//                    int bufferSize = 2048;
//                    byte[] buffer = new byte[bufferSize];
//                    ByteArrayOutputStream file = new ByteArrayOutputStream();
//
//                    while (size > 0) {
//                        input.read(buffer, 0, Math.min(bufferSize, size));
//                        file.write(buffer, 0, Math.min(bufferSize, size));
//                        size -= bufferSize;
//                    }

                } else if (messageReceived[0].equals("Online users")) {
                    // Nhận yêu cầu cập nhật danh sách người dùng trực tuyến
                    String[] users = input.readUTF().split(",");
                    System.out.println("Online users: " + Arrays.toString(users));
//                    String chat = (String) cbOnlineUsers.getSelectedItem();
//                    cbOnlineUsers.removeAllItems();
//                    boolean isChattingOnline = false;
//
//                    for (String u : users) {
//                        if (u.equals(account.getUserName()) == false) {
//                            // Cập nhật danh sách các người dùng trực tuyến vào ComboBox onlineUsers (trừ bản thân)
//                            cbOnlineUsers.addItem(u);
//                            if (messageContent.get(u) == null) {
//                                messageContent.put(u, "");
//                            }
//                        }
//                        if ((chat != null) && chat.equals(u)) {
//                            isChattingOnline = true;
//                        }
//                    }
//                    if (isChattingOnline == true) {
//                        cbOnlineUsers.setSelectedItem(chat);
//                    } else if (cbOnlineUsers.getSelectedItem() != null) {
//                        cbOnlineUsers.setSelectedIndex(0);
//                    }
//                    cbOnlineUsers.validate();
                }

            }

        } catch (IOException ex) {
            System.err.println(ex);
        } finally {
            try {
                if (input != null) {
                    input.close();
                } else {
                    System.out.println("Have redundant data...");
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
