package com.example.chatapp.controllers;

import com.example.chatapp.daos.MessageDAO;
import com.example.chatapp.models.Message;
import com.example.chatapp.models.User;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;
import java.util.Arrays;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ClientHandler implements Runnable {

    private final Socket clientSocket;

    private DataInputStream input;
    private DataOutputStream output;

    private User user;

    public ClientHandler(Socket clientSocket, User user) throws IOException {
        this.clientSocket = clientSocket;
        this.input = new DataInputStream(clientSocket.getInputStream());
        this.output = new DataOutputStream(clientSocket.getOutputStream());
        this.user = user;
    }

    @Override
    public void run() {
        label:
        while(!clientSocket.isClosed()){
            try {

                String inputData = input.readUTF();
                System.out.println("ABC: " + inputData);
                String[] messages = inputData.split(",");

                System.out.println(Arrays.toString(messages));

                String messageType = messages[0]; // LOG_OUT, SEND_TEXT, SEND_FILE

                switch (messageType) {
                    case "LOG_OUT":
                        ServerController.logoutClient(ClientHandler.this);
                        clientSocket.close();
                        ServerController.updateOnlineUsers();
                        break;
                    case "SEND_TEXT": {
                        String senderUsername = messages[1];
                        String receiverUsername = messages[2];
                        String messageContent = messages[3];

                        for (ClientHandler client : ServerController.clients) {
                            if (client.getUsername().equals(receiverUsername)) {
                                if(MessageDAO.save(new Message(senderUsername, receiverUsername, messageContent))){
                                    System.out.println("Message saved");
                                    DataOutputStream receiverOutput = client.getOutputStream();
                                    String sendMessage = String.join(",", new String[]{"SEND_TEXT", senderUsername, receiverUsername, messageContent});
                                    System.out.println("sendMessage: "+ sendMessage);
                                    receiverOutput.writeUTF(sendMessage);
                                    receiverOutput.flush();
                                } else {
                                    System.out.println("Message not saved");
                                }

                            }
                        }
                        break;
                    }
                    case "SEND_FILE": {
//                        String receiver = messages[1];
//                        String fileName = messages[2];
//                        byte[] fileContent = new byte[1024];
//                        int bytesRead;
//
//                        DataOutputStream receiverOutput = null;
//
//                        for (ClientHandler client : ServerController.clients) {
//                            if (client.getUsername().equals(receiver)) {
//                                receiverOutput = client.getOutputStream();
//                                receiverOutput.writeUTF("RECEIVE_FILE," + user.getUsername() + "," + fileName);
//                                receiverOutput.flush();
//                                break;
//                            }
//                        }
//
//                        if (receiverOutput != null) {
//                            OutputStream fileOutputStream = clientSocket.getOutputStream();
//                            while ((bytesRead = input.read(fileContent)) != -1) {
//                                fileOutputStream.write(fileContent, 0, bytesRead);
//                            }
//                            fileOutputStream.flush();
//                        }
                        break;
                    }
                    case "GET_MESSAGES": {
                        System.out.println("HEREE");
                        String senderUsername = messages[1];
                        String receiverUsername = messages[2];
                        var result = MessageDAO.getMessages(senderUsername, receiverUsername);
                        StringBuilder prepareMessage = new StringBuilder();
                        for (Message message : result) {
                            prepareMessage.append(message.getContent()).append("|").append(message.getSender().equals(senderUsername)).append("||");
                        }

                        for (ClientHandler client : ServerController.clients) {
                            if (client.getUsername().equals(senderUsername)) {
                                client.getOutputStream().writeUTF("GET_MESSAGES," + prepareMessage.toString());
                                client.getOutputStream().flush();
                                break;

                            }
                        }
                        break;
                    }

                    default: {

                        System.out.println("Invalid messageType: ");
                        break label;
                    }
                }

            } catch (IOException e) {
                System.out.println("ERROR HEREEEE");
                e.printStackTrace();
                closeAll();
            } catch (Exception e) {
                e.printStackTrace();
                closeAll();
            }
        }
    }

    public String getUsername(){
        return user.getUsername();
    }

    public DataOutputStream getOutputStream()  {
        return output;
    }

    public void closeAll() {
        try {
            if (input != null) {
                input.close();
            }
            if (output != null) {
                output.close();
            }
            if (clientSocket != null) {
                clientSocket.close();
            }
        } catch (IOException e) {
            e.printStackTrace();

        }
    }
}
