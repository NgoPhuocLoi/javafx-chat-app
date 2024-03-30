package com.example.chatapp.controllers;

import com.example.chatapp.daos.UserDAO;
import com.example.chatapp.models.User;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ServerController {
    private ServerSocket server;
    static ArrayList<ClientHandler> clients = new ArrayList<>();
    User account;


    public ServerController() {
        try {
            this.server = new ServerSocket(9999);
            Socket clientSocket;
            while (true) {
                clientSocket = server.accept();

                DataInputStream inputStream = new DataInputStream(clientSocket.getInputStream());
                DataOutputStream outputStream = new DataOutputStream(clientSocket.getOutputStream());

                String request = inputStream.readUTF();
                System.out.println("request: " + request);
                if (request.equals("SIGN_UP")) {
                    String username = inputStream.readUTF();
                    String password = inputStream.readUTF();
                    User user = new User(username, password, "");
                    System.out.println(username + ": " + password);
                    if (!isExistedUserWithUsername(username)) {
                        this.saveUser(user);
                        outputStream.writeUTF("Sign up successful");
                        outputStream.flush();
                    } else {
                        outputStream.writeUTF("This username is being used");
                        outputStream.flush();
                    }
                } else if (request.equals("SIGN_IN")) {
                    String username = inputStream.readUTF();
                    String password = inputStream.readUTF();

                    User foundUser = getUserByUsername(username);

                    if (foundUser != null) {
                        if (foundUser.getPassword().equals(password)) {
                            var foundClient = clients.stream().anyMatch(client -> client.getUsername().equals(username));
                            if (foundClient) {
                                outputStream.writeUTF("This user is already logged in");
                                outputStream.flush();
                                continue;
                            }
                            outputStream.writeUTF("Log in successful");
//                        outputStream.writeUTF(foundUser.getAvatarUrl());
                            outputStream.flush();
                            ClientHandler clientHandler = new ClientHandler(clientSocket, foundUser);
                            clients.add(clientHandler);

                            Thread thread = new Thread(clientHandler);

                            thread.start();

                            updateOnlineUsers();
                        } else {
                            outputStream.writeUTF("Wrong password");
                            outputStream.flush();
                        }
                    } else {
                        outputStream.writeUTF("This username is not existed");
                        outputStream.flush();
                    }
                }
            }
        }catch(Exception exception) {
            System.out.println("ERRORRRR .");
            closeAll();
        }
        }

    private String saveUser(User user) {
        var userDao = new UserDAO();
        try {
            if (userDao.saveUser(user)) {
                return "Register successful";
            }
        } catch (Exception ex) {
            Logger.getLogger(ServerController.class.getName()).log(Level.SEVERE, null, ex);
        }
        return "ERROR";
    }

    private String updateUser(User user) {
        var userDao = new UserDAO();
        try {
            if (userDao.updateUser(user)) {
                return "Update successful";
            }
        } catch (Exception ex) {
            Logger.getLogger(ServerController.class.getName()).log(Level.SEVERE, null, ex);
        }
        return "Not Updated";
    }

    private User getUserByUsername(String username) throws Exception {
        var userDao = new UserDAO();
        return userDao.findUserByUsername(username);
    }

    private boolean isExistedUserWithUsername(String username) throws Exception {
        var userDao = new UserDAO();
        return userDao.findUserByUsername(username) != null;
    }

    public static void updateOnlineUsers() {
        StringBuilder message = new StringBuilder(" ");
        if (!clients.isEmpty()) {
            for (ClientHandler client : clients) {
                message.append(",");
                message.append(client.getUsername());
            }
            if (!message.toString().equals(" ")) {
                for (ClientHandler client : clients) {
                    try {
                        if (client.getOutputStream() != null) {
                            client.getOutputStream().writeUTF("Online users");
                            client.getOutputStream().writeUTF(message.substring(2));
                            client.getOutputStream().flush();

                            System.out.println("Online users: " + message.substring(2));
                        }
                    } catch (IOException e) {
                        System.out.println("Error");
                        //e.printStackTrace();
                    }
                }
            }
        }

    }

    public static void logoutClient(ClientHandler clientHandler) {
        clients.remove(clientHandler);
        System.out.println("User with username: " + clientHandler.getUsername() + " has logged out");
    }

    public static void main(String[] args) {
        try {
            new ServerController();
            System.out.println("Server is running on PORT 9999");
        } catch (Exception e) {

            e.printStackTrace();
        }
    }

   private void closeAll(){
        try {
            if(server != null){
                server.close();
            }
        } catch (IOException e){
            e.printStackTrace();
   }}
}
