package com.example.chatapp.daos;

import com.example.chatapp.dbs.MySQLDB;
import com.example.chatapp.models.User;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class UserDAO {
    public boolean saveUser(User user) throws Exception {
        String sql = "insert into users values(?,?,?)";
        try (
                Connection con = MySQLDB.getConnection();
                PreparedStatement psmt = con.prepareStatement(sql);) {
            psmt.setString(1, user.getUsername());
            psmt.setString(2, user.getPassword());
            psmt.setString(3, user.getAvatarUrl());

            if (psmt.executeUpdate() > 0) {
                return true;
            }
        }
        return false;
    }

    public User findUserByUsername(String username) throws Exception{
        String sql = "select * from users where username=?";
        try (
            Connection con = MySQLDB.getConnection();
            PreparedStatement psmt = con.prepareStatement(sql))
        {
            psmt.setString(1, username);

            ResultSet rs = psmt.executeQuery();
            if (rs.next()) {
                return new User(rs.getString(1), rs.getString(2), rs.getString(3));
            }
        }
        return null;
    }

    public boolean updateUser(User user) throws Exception {
        String sql = "update users set password=?, avatar=? where username=?";
        try (
                Connection con = MySQLDB.getConnection();
                PreparedStatement psmt = con.prepareStatement(sql);) {
            psmt.setString(1, user.getPassword());
            psmt.setString(2, user.getAvatarUrl());
            psmt.setString(3, user.getUsername());
            if (psmt.executeUpdate() > 0) {
                return true;
            }
        }
        return false;
    }
}
