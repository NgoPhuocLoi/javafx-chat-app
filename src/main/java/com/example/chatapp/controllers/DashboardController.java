package com.example.chatapp.controllers;

import com.example.chatapp.models.User;
import com.example.chatapp.utils.UserProps;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;

import java.io.DataInputStream;
import java.io.IOException;
import java.net.URL;
import java.util.*;

public class DashboardController implements Initializable {

    UserProps userProps = UserProps.getInstance();

    Thread receiverThread;

    User user = new User();

    List<String> onlineUsers = new ArrayList<>();

   SimpleStringProperty userChattingWith = new SimpleStringProperty();

    @FXML
    private BorderPane dashboardContainer;

    @FXML
    private Label testLabel;

    @FXML
     VBox onlineUsersBox;

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
    public void onSendBtnClick()  {

        String message = chatInput.getText();
        System.out.println("Sending message: " + message);
        appendMessage(message, true);
        String messageToServer = String.join(",", new String[]{"SEND_TEXT", user.getUsername(), userChattingWith.getValue(), message});
        try {
            userProps.getDataOutputStream().writeUTF(messageToServer);
        } catch (IOException e) {
            System.out.println("Error sending message to server");
        }
        System.out.println(messagesContainer.getHeight());
        chatInput.clear();
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        currentLoggedInUsername.setText(userProps.getUsername());
        user.setUsername(userProps.getUsername());
        if(userChattingWith.getValue() == null){
            chatContainer.setVisible(false);
        }

        userChattingWith.addListener((observable, oldValue, newValue) -> {
            if(newValue != null){
                chatContainer.setVisible(true);
                testLabel.setText(newValue);
            }
            System.out.println("User chatting with: " + newValue);
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

                    if (messageReceived[0].equals("SEND_TEXT")) {
                        // Nhận một tin nhắn văn bản
                        String sender = messageReceived[1];
                        String receiver = messageReceived[2];
                        String message = messageReceived[3];
                        // In tin nhắn lên màn hình chat với người gửi
//                    newMessage(sender, receiver, message, false);
//                    autoScroll();
                        if(Objects.equals(receiver, user.getUsername())){
                            if(Objects.equals(sender, userChattingWith.getValue())){
                                Platform.runLater(() -> {
                                    appendMessage(message, false);
                                });  }

                            else {
                                onlineUsersBox.getChildren().forEach(child -> {
                                    if(child.getId().equals(sender)){
                                        child.setStyle("-fx-background-color: red; -fx-padding: 10px;");
                                    }
                                });
                            }


                        }


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
                        Platform.runLater(() -> {
                            onlineUsersBox.getChildren().clear();
                            for (String u : users) {
                                if (!u.equals(user.getUsername())) {
                                    appendOnlineUser(u, "", "");
                                }
                            }
                        });
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

    private void appendOnlineUser(String username, String avatarUrl, String lastMessage){
        HBox onlineUserContainer = new HBox();
        onlineUserContainer.setAlignment(Pos.CENTER_LEFT);
        onlineUserContainer.setPadding(new javafx.geometry.Insets(0, 0, 0, 10));
        HBox.setMargin(onlineUserContainer, new javafx.geometry.Insets(10, 0, 0, 0));
        onlineUserContainer.setId(username);
        onlineUserContainer.setOnMouseClicked( e  -> {

            userChattingWith.set(username);

            onlineUsersBox.getChildren().forEach(child -> {
                if(child.getId().equals(username)){
                    child.getStyleClass().add("dark-gray-bg");
                }else{
                    child.getStyleClass().remove("dark-gray-bg");
                }
            });
        });

        Image image = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/images/icons8-avatar-48.png")));
        ImageView userAvatar = new ImageView();
        userAvatar.setImage(image);

        Label usernameLabel = new Label();
        usernameLabel.setText(username);
        usernameLabel.setFont(new javafx.scene.text.Font(16));
        usernameLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 16px; -fx-text-fill: white;");


        Label lastMessageLabel = new Label();
        lastMessageLabel.setText("Last message");
        lastMessageLabel.setStyle(" -fx-font-size: 12px; -fx-text-fill: #9da7a7;");


        VBox userInfoContainer = new VBox();
        userInfoContainer.setPadding(new javafx.geometry.Insets(4, 0, 0, 0));
        userInfoContainer.getChildren().addAll(usernameLabel, lastMessageLabel);

        onlineUserContainer.getChildren().setAll(userAvatar, userInfoContainer);
        if(userChattingWith.getValue() != null && userChattingWith.getValue().equals(username)){
            onlineUserContainer.getStyleClass().add("dark-gray-bg");
        }
        onlineUsersBox.getChildren().add(onlineUserContainer);
    }

    private void appendMessage(String message, boolean isSender){
        HBox messageContainer = new HBox();
        Pos alignment = isSender ? Pos.CENTER_RIGHT : Pos.CENTER_LEFT;
        messageContainer.setAlignment(alignment);

        Image image = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/images/icons8-avatar-48.png")));
        ImageView userAvatar = new ImageView();
        userAvatar.setImage(image);

        Label messageLabel = new Label();
        messageLabel.setText(message);
        messageLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #a19c9c;");
        messageLabel.getStyleClass().add("incoming-bubble");

        if(isSender){
            messageContainer.getChildren().addAll(messageLabel, userAvatar);
        }else{
            messageContainer.getChildren().addAll(userAvatar, messageLabel);
        }




        this.messagesContainer.getChildren().add(messageContainer);
    }
}
