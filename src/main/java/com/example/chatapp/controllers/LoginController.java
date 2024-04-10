package com.example.chatapp.controllers;

import com.example.chatapp.ChatApplication;
import com.example.chatapp.utils.UserProps;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.TilePane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
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

    SimpleBooleanProperty isSigningUp = new SimpleBooleanProperty(false);

    @FXML
    private TextField username;

    @FXML
    private PasswordField password;

    @FXML
    private PasswordField confirmPassword;

    @FXML
    private AnchorPane loginFormContainer;

    @FXML
    private Button signUpBtn;

    @FXML
    private Button loginBtn;

    @FXML
    private VBox confirmPasswordContainer;

    @FXML
    public void onLoginButtonClick()  {
//        System.out.println("LOGIN");
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
                stage.setOnCloseRequest(e -> {
                    try {
                        output.writeUTF("LOG_OUT");
                        output.flush();
                    } catch (IOException ioException) {
                        ioException.printStackTrace();
                    }
                });

            }
            else {
                showAlertBox("Error", response);
            }

        }catch (Exception e) {
            System.out.println("ERROR when login " + e.getMessage());
        }
    }

    @FXML
    public void onLoginEnter(KeyEvent event){
        if(event.getCode().toString().equals("ENTER")){
            onLoginButtonClick();
        }
    }

    @FXML
    public void onSignUpButtonClick(){

        if(!password.getText().equals(confirmPassword.getText())){
            showAlertBox("Error", "Password and confirm password do not match");
            return;
        }

        try {
            connectToServer();
            output.writeUTF("SIGN_UP");
            output.writeUTF(username.getText());
            output.writeUTF(password.getText());
            output.flush();
            String response = input.readUTF();

            if(response.equals("Sign up successful")){
                showAlertBox("Success", "Sign up successful");
                toggleSignUpAndSignInView();
            }else{
                showAlertBox("Error", response);
            }
        } catch (IOException e) {
            System.out.println("Network error: " + e.getMessage());;
        }
    }
    @FXML
    public Text signUpAndSignInToggle;

    @FXML
    public Text signUpAndSignInText;

    @FXML
    public void toggleSignUpAndSignInView(){
        if(isSigningUp.get()){
            signUpAndSignInText.setText("New user?");
            signUpAndSignInToggle.setText("Sign up now");
            confirmPasswordContainer.setVisible(false);
            loginBtn.setVisible(true);
            signUpBtn.setVisible(false);
        }else {
            signUpAndSignInText.setText("Already has an account?");
            signUpAndSignInToggle.setText("Sign In");
            confirmPasswordContainer.setVisible(true);
            loginBtn.setVisible(false);
            signUpBtn.setVisible(true);
        }
        isSigningUp.set(!isSigningUp.get());
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        isSigningUp.set(false);
        confirmPasswordContainer.setVisible(false);
        confirmPasswordContainer.managedProperty().bind(confirmPasswordContainer.visibleProperty());
        loginBtn.managedProperty().bind(loginBtn.visibleProperty());
        signUpBtn.managedProperty().bind(signUpBtn.visibleProperty());
        signUpBtn.setVisible(false);
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