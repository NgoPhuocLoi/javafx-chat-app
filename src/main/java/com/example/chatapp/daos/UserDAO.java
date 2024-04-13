package com.example.chatapp.daos;

import com.example.chatapp.dbs.MySQLDB;
import com.example.chatapp.models.User;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class UserDAO {
    public boolean saveUser(User user) {
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
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return false;
    }

    public User findUserByUsername(String username) {
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
        }catch (Exception e) {
            throw new RuntimeException(e);
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

    //get all user
    public List<User> getAll() {
        String sql = "select * from users ";
        List<User> users = new ArrayList<>();
        try (
            Connection con = MySQLDB.getConnection();
            PreparedStatement psmt = con.prepareStatement(sql))
        {
            ResultSet rs = psmt.executeQuery();
            while (rs.next()) {
                users.add(new User(rs.getString("username"), rs.getString("password"),
                        rs.getString("avatar")));
            }
        } catch (Exception e) {
           throw new RuntimeException(e);
        }
        return users;
    }
}
