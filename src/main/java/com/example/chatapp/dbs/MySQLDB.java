package com.example.chatapp.dbs;

import java.sql.Connection;
import java.sql.DriverManager;

public class MySQLDB {
    public static Connection getConnection() throws Exception{
        Class.forName("com.mysql.cj.jdbc.Driver");
        String connectionUrl = "jdbc:mysql://localhost/chatapp";
        String username = "root";
        String password = "<<ROOT_PASSWORD_HERE>>";
        return DriverManager.getConnection(connectionUrl, username, password);
    }
}
