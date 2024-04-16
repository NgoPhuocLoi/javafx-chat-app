package com.example.chatapp;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class ChatApplication extends Application {
    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(ChatApplication.class.getResource("views/login.fxml"));
        Scene scene = new Scene(fxmlLoader.load());
        stage.setTitle("Chitchat 1");
        stage.setScene(scene);
        stage.show();

        Stage secondaryStage = new Stage();

        secondaryStage.setTitle("ChitChat 2");
        secondaryStage.setScene(new Scene(new FXMLLoader(ChatApplication.class.getResource("views/login.fxml")).load()));
        secondaryStage.show();

        Stage thirdStage = new Stage();

        thirdStage.setTitle("ChitChat 3");
        thirdStage.setScene(new Scene(new FXMLLoader(ChatApplication.class.getResource("views/login.fxml")).load()));
        thirdStage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}