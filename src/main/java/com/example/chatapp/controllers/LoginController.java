package com.example.chatapp.controllers;

import com.example.chatapp.ChatApplication;
import com.example.chatapp.utils.UserProps;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.TilePane;
import javafx.stage.Stage;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.URL;
import java.util.ResourceBundle;

public class LoginController implements Initializable {

    private Socket clientSocket;

    private DataInputStream input;

    private DataOutputStream output;

    UserProps userProps = UserProps.getInstance();

    @FXML
    private TextField username;

    @FXML
    private PasswordField password;

    @FXML
    private AnchorPane loginFormContainer;

    @FXML
    public void onLoginButtonClick()  {
        try {
            connectToServer();
            output.writeUTF("SIGN_IN");
            output.writeUTF(username.getText());
            output.writeUTF(password.getText());
            output.flush();

            String response = input.readUTF();

            if(response.equals("Log in successful")){
                showAlertBox("Success", "Log in successful");

                userProps.setUsername(username.getText());
                userProps.setAvatarUrl(input.readUTF());
                userProps.setDataInputStream(input);
                userProps.setDataOutputStream(output);

                var stage = (Stage)loginFormContainer.getScene().getWindow();
                stage.close();
                Scene dashboardScene = new Scene(new FXMLLoader(ChatApplication.class.getResource("views/dashboard.fxml")).load());

                stage.setScene(dashboardScene);
                stage.show();
            }
            else {
                showAlertBox("Error", response);
            }

        }catch (Exception e) {
            System.out.println("ERROR when login " + e.getMessage());
        }
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {

    }

    public void showAlertBox(String header, String content){
        TilePane r = new TilePane();

        // create a alert
        Alert a = new Alert(Alert.AlertType.INFORMATION);

        a.setHeaderText(header);
        a.setContentText(content);
        a.showAndWait();
    }

    public void connectToServer() {
        try {
            if (clientSocket != null) {
                clientSocket.close();
            }
            clientSocket = new Socket("127.0.0.1", 9999);
            this.input = new DataInputStream(clientSocket.getInputStream());
            this.output = new DataOutputStream(clientSocket.getOutputStream());
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }
}