package com.example.chatapp.controllers;

import com.example.chatapp.ChatApplication;
import com.example.chatapp.daos.UserDAO;
import com.example.chatapp.models.GroupChat;
import com.example.chatapp.models.User;
import com.example.chatapp.utils.UserData;
import com.example.chatapp.utils.UserProps;
import javafx.application.Platform;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleMapProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.ObservableMap;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.*;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.DataInputStream;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.*;

public class DashboardController implements Initializable {

    UserProps userProps = UserProps.getInstance();

    Thread receiverThread;

    UserData user = new UserData();

    List<String> onlineUsers = new ArrayList<>();

    List<GroupChat> groups = new ArrayList<>();

    SimpleStringProperty userChattingWith = new SimpleStringProperty();

    SimpleIntegerProperty groupIdChattingWith = new SimpleIntegerProperty();

    @FXML
    private BorderPane dashboardContainer;

    @FXML
    private Label testLabel;

    @FXML
    VBox onlineUsersBox;

    @FXML
    VBox groupsBox;

    @FXML
    Label currentLoggedInUsername;

    @FXML
    VBox chatContainer;

    @FXML
    TextField chatInput;

    @FXML
    ImageView sendBtn;

    @FXML
    VBox messagesContainer;


    @FXML
    ScrollPane chatScrollPane;

    @FXML
    ImageView avatarImage;

    @FXML
    public void onChooseAvatar() {
        Stage stage = (Stage) dashboardContainer.getScene().getWindow();
        FileChooser fc = new FileChooser();
        fc.setTitle("Choose a image");
        FileChooser.ExtensionFilter imageFilter = new FileChooser.ExtensionFilter("Image Files", "*.jpg", "*.png");
        fc.getExtensionFilters().add(imageFilter);
        File file = fc.showOpenDialog(stage);
        if (file != null) {
            System.out.println(file.toURI().toString());
            try {
                user.getOutputStream().writeUTF("CHANGE_AVATAR," + user.getUsername() + "," + file.toURI());
                user.getOutputStream().flush();
                if (userChattingWith.getValue() != null) {
                    user.getOutputStream().writeUTF("GET_MESSAGES," + user.getUsername() + "," + userChattingWith.getValue());
                }

            } catch (IOException e) {
                e.printStackTrace();
                System.out.println("Error when changing avatar");
            }
            Image image = new Image(file.toURI().toString(), 40, 40, false, true);
            avatarImage.setImage(image);
        }
    }

    @FXML
    public void onSendBtnClick() {

        String message = chatInput.getText();
        System.out.println("Sending message: " + message);
        appendMessage(message, true, user.getAvatarUrl());
        if(userChattingWith.getValue() != null) {
            sendTextMessageToUser(message, userChattingWith.getValue());
        } else if(groupIdChattingWith.getValue() != -1) {
            sendTextMessageToGroup(message, groupIdChattingWith.getValue());
        }
        chatInput.clear();
    }

    private void sendTextMessageToUser(String message, String receiver) {
        String messageToServer = String.join(",", new String[]{"SEND_TEXT", user.getUsername(), receiver, message, user.getAvatarUrl().isEmpty() ? "NoAvatar" : user.getAvatarUrl()});
        try {
            user.getOutputStream().writeUTF(messageToServer);
            user.getOutputStream().flush();
        } catch (IOException e) {
            System.out.println("Error sending message to server");
        }

    }

    private void sendTextMessageToGroup(String message, int groupId) {
        String messageToServer = String.join(",", new String[]{"SEND_GROUP_TEXT", user.getUsername(), groupId + "", message, user.getAvatarUrl().isEmpty() ? "NoAvatar" : user.getAvatarUrl()});
        try {
            user.getOutputStream().writeUTF(messageToServer);
            user.getOutputStream().flush();
        } catch (IOException e) {
            System.out.println("Error sending message to server");
        }
    }

    @FXML
    public void onChatInputEnter(KeyEvent keyEvent) {
        if (keyEvent.getCode() == KeyCode.ENTER) {
            onSendBtnClick();
            keyEvent.consume();
        }
    }

    @FXML
    public void onLogout() {
        try {
            user.getOutputStream().writeUTF("LOG_OUT");
            user.getOutputStream().flush();
            var stage = (Stage) dashboardContainer.getScene().getWindow();
            stage.close();
            Scene dashboardScene = new Scene(new FXMLLoader(ChatApplication.class.getResource("views/login.fxml")).load());

            stage.setScene(dashboardScene);
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Error logging out");
        }
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        currentLoggedInUsername.setText(userProps.getUsername());
        if (!userProps.getAvatarUrl().isEmpty()) {
            Image image = new Image(userProps.getAvatarUrl(), 50, 50, false, true);
            avatarImage.setImage(image);
        }
        user.setUsername(userProps.getUsername());
        user.setAvatarUrl(userProps.getAvatarUrl());
        user.setInputStream(userProps.getDataInputStream());
        user.setOutputStream(userProps.getDataOutputStream());
        if (userChattingWith.getValue() == null) {
            chatContainer.setVisible(false);
        }

        userChattingWith.addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                chatContainer.setVisible(true);
                groupIdChattingWith.set(-1);
                testLabel.setText(newValue);
            }
            onlineUsersBox.getChildren().forEach(child -> {
                if (child.getId().equals(newValue)) {
                    child.getStyleClass().remove("red-bg");
                    child.getStyleClass().add("dark-gray-bg");
                } else {
                    child.getStyleClass().remove("dark-gray-bg");
                }
            });
            System.out.println("User chatting with: " + newValue);
        });

        groupIdChattingWith.addListener((observable, oldValue, newValue) -> {
            if (newValue.intValue() != -1) {
                chatContainer.setVisible(true);
                userChattingWith.set(null);
                GroupChat g = groups.stream().filter(group -> group.getId() == newValue.intValue()).findFirst().get();
                testLabel.setText(g.getName());
            }
            groupsBox.getChildren().forEach(child -> {
                if (child.getId().equals(newValue + "")) {
                    child.getStyleClass().remove("red-bg");
                    child.getStyleClass().add("dark-gray-bg");
                } else {
                    child.getStyleClass().remove("dark-gray-bg");
                }
            });
            System.out.println("Group chatting with: " + newValue);
        });

        messagesContainer.heightProperty().addListener((observable, oldValue, newValue) -> {
            System.out.println("Height: " + newValue);
            chatScrollPane.setVvalue((Double) newValue);
        });
        receiverThread = new Thread(new Receiver(userProps.getDataInputStream()));

        receiverThread.start();

    }

    class Receiver implements Runnable {
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

                    switch (messageReceived[0]) {
                        case "SEND_TEXT": {
                            // Nhận một tin nhắn văn bản
                            String sender = messageReceived[1];
                            String receiver = messageReceived[2];
                            String message = messageReceived[3];
                            String senderAvatar = messageReceived[4].equals("NoAvatar") ? "" : messageReceived[4];
                            // In tin nhắn lên màn hình chat với người gửi
//                    newMessage(sender, receiver, message, false);
//                    autoScroll();
                            if (Objects.equals(receiver, user.getUsername())) {
                                if (Objects.equals(sender, userChattingWith.getValue())) {
                                    Platform.runLater(() -> {
                                        appendMessage(message, false, senderAvatar);
                                    });
                                } else {
                                    onlineUsersBox.getChildren().forEach(child -> {
                                        if (child.getId().equals(sender)) {
                                            child.getStyleClass().add("red-bg");
                                        }
                                    });
                                }
                            }


                            break;
                        }
                        case "SEND_GROUP_TEXT": {
                            // Nhận một tin nhắn văn bản
                            String sender = messageReceived[1];
                            int groupId = Integer.parseInt(messageReceived[2]);

                            String message = messageReceived[3];
                            String senderAvatar = messageReceived[4].equals("NoAvatar") ? "" : messageReceived[4];


                                if (groupId == groupIdChattingWith.getValue()) {
                                    Platform.runLater(() -> {
                                        appendMessage(message, false, senderAvatar);
                                    });
                                } else {
                                    onlineUsersBox.getChildren().forEach(child -> {
                                        if (child.getId().equals(groupId + "")) {
                                            child.getStyleClass().add("red-bg");
                                        }
                                    });
                                }

                            // In tin nhắn lên màn hình chat với người gửi
                        }
                        case "File": {
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

                            break;
                        }
                        case "Online users": {
                            String[] users = input.readUTF().split("\\|");

                            System.out.println("Online users: " + Arrays.toString(users));
                            Platform.runLater(() -> {
                                onlineUsersBox.getChildren().clear();
                                boolean isUserChatWithOnline = false;
                                for (String u : users) {
                                    String[] userParts = u.split(",");
                                    String username = userParts[0];
                                    String avatarUrl = userParts[1];
                                    if (userChattingWith.getValue() != null && userChattingWith.getValue().equals(username)) {
                                        isUserChatWithOnline = true;
                                    }
                                    if (!username.equals(user.getUsername())) {
                                        appendOnlineUser(username, avatarUrl.equals("NoAvatar") ? "" : avatarUrl, "");
                                    }
                                }
                                if (!isUserChatWithOnline && groupIdChattingWith.getValue() == -1){
                                    userChattingWith.set(null);
                                    chatContainer.setVisible(false);
                                }
                            });
                            if (userChattingWith.getValue() != null) {
                                user.getOutputStream().writeUTF("GET_MESSAGES," + user.getUsername() + "," + userChattingWith.getValue());
                                user.getOutputStream().flush();
                            }
                            break;
                        }
                        case "GET_MESSAGES":
                        case "GET_GROUP_MESSAGES": {
                            try {
                                String prepareMessage = messageReceived[1];
                                String[] messages = prepareMessage.split("\\|\\|");
                                Platform.runLater(() -> {
                                    messagesContainer.getChildren().clear();
                                });
                                for (String message : messages) {
                                    String[] messageParts = message.split("\\|");
                                    String content = messageParts[0];
                                    String senderUsername = messageParts[1];
                                    String userAvatar = messageParts[2].equals("NoAvatar") ? "" : messageParts[2];
                                    Platform.runLater(() -> {
                                        appendMessage(content, senderUsername.equals(user.getUsername()), userAvatar);
                                    });
                                }
                            } catch (Exception e) {
                                Platform.runLater(() -> {
                                    messagesContainer.getChildren().clear();
                                });
                                System.out.println("No messages found...");
                            }
                            break;
                        }
                        case "GET_GROUPS": {
                            String[] groups = input.readUTF().split("\\|");

                            System.out.println("Groups of users: " + Arrays.toString(groups));
                            Platform.runLater(() -> {
                                groupsBox.getChildren().clear();
//                                boolean isUserChatWithOnline = false;
                                for (String group : groups) {
                                    String[] groupPart = group.split(",");
                                    String groupId = groupPart[0];
                                    String groupName = groupPart[1];
//                                    if (userChattingWith.getValue() != null && userChattingWith.getValue().equals(username)) {
//                                        isUserChatWithOnline = true;
//                                    }

                                    appendGroupOfUser(user.getUsername(), Integer.parseInt(groupId), groupName);

                                }
//                                if (!isUserChatWithOnline) {
//                                    userChattingWith.set(null);
//                                    chatContainer.setVisible(false);
//                                }
                            });
                        }


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

    private void appendOnlineUser(String username, String avatarUrl, String lastMessage) {
        HBox onlineUserContainer = new HBox();

        onlineUserContainer.setAlignment(Pos.CENTER_LEFT);
        onlineUserContainer.setPadding(new javafx.geometry.Insets(4, 0, 4, 10));
        HBox.setMargin(onlineUserContainer, new javafx.geometry.Insets(10, 0, 0, 0));
        onlineUserContainer.setId(username);
        onlineUserContainer.setOnMouseClicked(e -> {
            try {
                user.getOutputStream().writeUTF("GET_MESSAGES," + user.getUsername() + "," + username);
                user.getOutputStream().flush();
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            }
            userChattingWith.set(username);


        });

        Image avatar = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/images/icons8-avatar-48.png")));
        if (!avatarUrl.isEmpty()) {
            avatar = new Image(avatarUrl, 40, 40, false, true);
        }
        ImageView userAvatar = new ImageView();
        userAvatar.setImage(avatar);

        HBox.setMargin(userAvatar, new javafx.geometry.Insets(0, 10, 0, 0));

        Label usernameLabel = new Label();
        usernameLabel.setText(username);
        usernameLabel.setFont(new javafx.scene.text.Font(16));
        usernameLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 16px; -fx-text-fill: white;");


        Label lastMessageLabel = new Label();
        lastMessageLabel.setText("Last message");
        lastMessageLabel.setStyle(" -fx-font-size: 12px; -fx-text-fill: #9da7a7;");


        VBox userInfoContainer = new VBox();
        userInfoContainer.setPadding(new javafx.geometry.Insets(4, 0, 0, 0));

        VBox.setMargin(userInfoContainer, new javafx.geometry.Insets(0, 0, 0, 10));
        userInfoContainer.getChildren().addAll(usernameLabel, lastMessageLabel);

        onlineUserContainer.getChildren().setAll(userAvatar, userInfoContainer);
        if (userChattingWith.getValue() != null && userChattingWith.getValue().equals(username)) {
            onlineUserContainer.getStyleClass().add("dark-gray-bg");
        }
        onlineUsersBox.getChildren().add(onlineUserContainer);
    }

    private void appendGroupOfUser(String username, int groupId, String groupName) {
        groups.add(new GroupChat(groupId, groupName));
        HBox groupContainer = new HBox();

        groupContainer.setAlignment(Pos.CENTER_LEFT);
        groupContainer.setPadding(new javafx.geometry.Insets(4, 0, 4, 10));
        HBox.setMargin(groupContainer, new javafx.geometry.Insets(10, 0, 0, 0));
        groupContainer.setId(groupId + "");
        groupContainer.setOnMouseClicked(e -> {
            try {
                user.getOutputStream().writeUTF("GET_GROUP_MESSAGES," + groupId);
                user.getOutputStream().flush();
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            }


            groupIdChattingWith.set(groupId);


        });


        Image groupAvatarImage = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/images/group.png")), 40, 40, false, true);

        ImageView groupAvatar = new ImageView();
        groupAvatar.setImage(groupAvatarImage);

        HBox.setMargin(groupAvatar, new javafx.geometry.Insets(0, 10, 0, 0));

        Label groupNameLabel = new Label();
        groupNameLabel.setText(groupName);
        groupNameLabel.setFont(new javafx.scene.text.Font(16));
        groupNameLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 16px; -fx-text-fill: white;");






        VBox.setMargin(groupNameLabel, new javafx.geometry.Insets(0, 0, 0, 10));


        groupContainer.getChildren().setAll(groupAvatar, groupNameLabel);
//        if (userChattingWith.getValue() != null && userChattingWith.getValue().equals(username)) {
//            onlineUserContainer.getStyleClass().add("dark-gray-bg");
//        }
        groupsBox.getChildren().add(groupContainer);
    }
    private void appendMessage(String message, boolean isSender, String userAvatarUrl) {
        HBox messageContainer = new HBox();
        messageContainer.setPadding(new javafx.geometry.Insets(5, 10, 5, 10));

        Pos alignment = isSender ? Pos.CENTER_RIGHT : Pos.CENTER_LEFT;
        messageContainer.setAlignment(alignment);

        Image image = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/images/icons8-avatar-48.png")));
        if (!userAvatarUrl.isEmpty()) {
            image = new Image(userAvatarUrl, 40, 40, false, true);
        }
        ImageView userAvatar = new ImageView();
        userAvatar.setImage(image);

        int avatarMarginLeftValue = isSender ? 10 : 0;
        int avatarMarginRightValue = isSender ? 0 : 10;

        HBox.setMargin(userAvatar, new javafx.geometry.Insets(0, avatarMarginRightValue, 0, avatarMarginLeftValue));

        Label messageLabel = new Label();
        messageLabel.setText(message);
        messageLabel.setStyle("-fx-font-size: 12px;");


        if (isSender) {
            messageLabel.getStyleClass().add("outgoing-bubble");
            messageContainer.getChildren().addAll(messageLabel, userAvatar);
        } else {
            messageLabel.getStyleClass().add("incoming-bubble");
            messageContainer.getChildren().addAll(userAvatar, messageLabel);
        }


        this.messagesContainer.getChildren().add(messageContainer);
    }
}
