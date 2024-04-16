package com.example.chatapp.controllers;

import com.example.chatapp.daos.GroupChatDAO;
import com.example.chatapp.daos.UserDAO;
import com.example.chatapp.models.GroupChat;
import com.example.chatapp.models.User;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

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
                System.out.println("New client connected: " + clientSocket);
                DataInputStream inputStream = new DataInputStream(clientSocket.getInputStream());
                DataOutputStream outputStream = new DataOutputStream(clientSocket.getOutputStream());

                String request = inputStream.readUTF();
                System.out.println("request: " + request);
                if (request.equals("SIGN_UP")) {
                    String username = inputStream.readUTF();
                    String password = inputStream.readUTF();
                    File defaultAvatar = new File("src/main/resources/images/icons8-avatar-48.png");
                    User user = new User(username, password, defaultAvatar.toURI().toString());
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
                            String userAvatarUrl = foundUser.getAvatarUrl() == null ? "" : foundUser.getAvatarUrl();
                            outputStream.writeUTF(userAvatarUrl);
                            outputStream.flush();
                            ClientHandler clientHandler = new ClientHandler(clientSocket, foundUser);
                            clients.add(clientHandler);

                            Thread thread = new Thread(clientHandler);

                            thread.start();

                            updateOnlineUsers();
                            updateGroupsOfUser(username);
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
            exception.printStackTrace();
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
                message.append("|");
                message.append(client.getUsername());
                message.append(",");
                String userAvatar = client.getUserAvatarUrl();
                message.append(userAvatar.isEmpty() ? "NoAvatar" : userAvatar);
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

    public static void updateGroupsOfUser(String username){
        var result = GroupChatDAO.getGroupsByUsername(username);
        if(result.isEmpty()){
            return;
        }
        System.out.println("Groups of user: " + result.stream().map(GroupChat::getName).collect(Collectors.joining(", ")));
        StringBuilder prepareMessage = new StringBuilder(" ");
        for (var group : result) {
            prepareMessage.append("|").append(group.getId()).append(",").append(group.getName());
        }
        for (ClientHandler client : ServerController.clients) {
            if (client.getUsername().equals(username)) {
                try {
                    System.out.println("GET_GROUPS: " + prepareMessage.substring(2));
                    client.getOutputStream().writeUTF("GET_GROUPS");
                    client.getOutputStream().writeUTF(prepareMessage.substring(2));
                    client.getOutputStream().flush();
                } catch (IOException e) {
                    System.out.println("Error when getting groups");
                    throw new RuntimeException(e);
                }
                break;
            }
        }
    }

    public static void logoutClient(ClientHandler clientHandler) {

        for(int i = 0; i < clients.size(); i++){
            if(clientHandler.getUsername().equals(clients.get(i).getUsername())){
                clients.remove(i);
            }
        }
        System.out.println(clients.stream().map(ClientHandler::getUsername).collect(Collectors.joining(", ")));
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
