module com.example.chatapp {
    requires javafx.controls;
    requires javafx.fxml;
    requires mysql.connector.j;
    requires java.sql;
    requires com.fasterxml.jackson.databind;
    requires cloudinary.http45;
    requires cloudinary.core;


    opens com.example.chatapp to javafx.fxml;
    opens com.example.chatapp.models to javafx.base;
    exports com.example.chatapp;
    exports com.example.chatapp.controllers;
    opens com.example.chatapp.controllers to javafx.fxml;
}