package com.example.chatapp.controllers;

import com.example.chatapp.ChatApplication;
import com.example.chatapp.daos.GroupChatDAO;
import com.example.chatapp.daos.UserDAO;
import com.example.chatapp.models.User;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.ResourceBundle;

public class AddGroupController implements Initializable {


    @FXML
    private ComboBox<User> cbx_user;

    @FXML
    private TableView<User> tbl_groupUsers;

    @FXML
    private TableColumn<User, String> tblCol_no;

    @FXML
    private TableColumn<User, String> tblCol_username;

    @FXML
    private TableColumn<User, Void> tblCol_action;

    @FXML
    private TextField tf_groupName;

    @FXML
    private Button btn_addNewGroup;

    private final UserDAO userDAO = new UserDAO();
    private List<User> groupUsers = new ArrayList<>();

    private User currentUser;


    private void showAlertBox(String header, String content) {
        // create a alert
        Alert a = new Alert(Alert.AlertType.INFORMATION);

        a.setHeaderText(header);
        a.setContentText(content);
        a.showAndWait();
    }

    private void showUserTable() {
        tblCol_no.setCellValueFactory(cellData ->
                new SimpleStringProperty(String.valueOf(groupUsers.indexOf(cellData.getValue()) + 1)));
        tblCol_username.setCellValueFactory(new PropertyValueFactory<>("username"));
        tbl_groupUsers.setItems(FXCollections.observableList(groupUsers));
        tblCol_action.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);

                } else {
                    Image deleteImg = new Image(Objects.requireNonNull(ChatApplication.class.getResourceAsStream("/images/ic_delete.png")));
                    ImageView deleteIcon = new ImageView(deleteImg);
                    deleteIcon.setFitWidth(25);
                    deleteIcon.setFitHeight(25);
                    deleteIcon.setPreserveRatio(true);
                    deleteIcon.setStyle("-fx-cursor: hand");
                    Tooltip.install(deleteIcon, new Tooltip("Xóa"));

                    deleteIcon.setOnMouseClicked(e -> {
                        User selectedUser = tbl_groupUsers.getSelectionModel().getSelectedItem();
                        groupUsers.remove(selectedUser);
                        cbx_user.getItems().add(selectedUser);
                        showUserTable();
                    });
                    HBox action = new HBox(deleteIcon);
                    action.setStyle("-fx-alignment: center");
                    action.setPrefHeight(deleteIcon.getFitHeight());
                    setGraphic(action);

                }


            }
        });
    }

    public void addUserToGroup() {
        User user = cbx_user.getValue();
        if (user == null) {
            showAlertBox("Error", "Please select a user to add to group");
            return;
        } else {
            groupUsers.add(user);
            cbx_user.getItems().remove(user);
            showUserTable();
        }
    }

    public void addNewGroup() {
        String groupName = tf_groupName.getText();
        if (groupName.isEmpty()) {
            showAlertBox("Error", "Please enter a group name");

        } else if (groupUsers.size() < 3) {
            showAlertBox("Error", "Please add at least three users to create a group");

        } else {
            GroupChatDAO.create(groupName);
            int newGroupId = GroupChatDAO.getNewGroupId();
            GroupChatDAO.addMembers(groupUsers, newGroupId);
            Stage stage = (Stage) btn_addNewGroup.getScene().getWindow();
            stage.close();
        }
    }

    @FXML
    public void reset() {
        tf_groupName.clear();
        groupUsers.clear();
        cbx_user.setValue(null);
        loadInitialData(currentUser.getUsername());
    }




    public void loadInitialData(String username){
        List<User> users = userDAO.getAll();
        this.currentUser = users.stream().filter(user -> user.getUsername().equals(username))
                .findFirst().orElse(null);
        users.remove(currentUser);
         cbx_user.setItems(FXCollections.observableList(users));
         groupUsers.add(currentUser);
            showUserTable();

    }


    @Override
    public void initialize(URL location, ResourceBundle resources) {



    }


}
