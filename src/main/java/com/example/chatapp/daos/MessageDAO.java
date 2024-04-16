package com.example.chatapp.daos;

import com.example.chatapp.dbs.MySQLDB;
import com.example.chatapp.models.Message;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mysql.cj.xdevapi.JsonArray;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class MessageDAO {
    public static boolean save(Message newMessage) throws Exception{
        String sql = "insert into messages (sender, receiver, content, group_id) values(?,?,?, ?)";
        try (
                Connection con = MySQLDB.getConnection();
                PreparedStatement psmt = con.prepareStatement(sql);)
        {
            psmt.setString(1, newMessage.getSender());
            psmt.setString(2, newMessage.getReceiver());
            psmt.setString(3, newMessage.getContent());
            if(newMessage.getGroupId() == null) {
                psmt.setNull(4, java.sql.Types.INTEGER);
            }else {
                psmt.setInt(4, newMessage.getGroupId());
            }
            if (psmt.executeUpdate() > 0) {
                return true;
            }
        }
        return false;
    }

    public static List<Message> getMessages(String sender, String receiver) throws Exception{
        String sql = "select * from messages where (sender=? and receiver=?) or (sender=? and receiver=?) ORDER BY id";
        List<Message> messages = new ArrayList<>();
        try (
                Connection con = MySQLDB.getConnection();
                PreparedStatement psmt = con.prepareStatement(sql);)
        {
            psmt.setString(1, sender);
            psmt.setString(2, receiver);
            psmt.setString(3, receiver);
            psmt.setString(4, sender);

            ResultSet rs = psmt.executeQuery();
            while(rs.next()){
                Message message = new Message(rs.getString(2), rs.getString(3), rs.getString(4));
                String timestamp = rs.getString(6);
                System.out.println(timestamp);
                message.setTimestamp(timestamp);
                messages.add(message);

            }
        }

        return messages;
    }

    public static List<Message> getMessagesInGroup(int groupId) throws Exception{
        String sql = "select * from messages where group_id = ? ORDER BY id";
        List<Message> messages = new ArrayList<>();
        try (
                Connection con = MySQLDB.getConnection();
                PreparedStatement psmt = con.prepareStatement(sql);)
        {
            psmt.setInt(1, groupId);

            ResultSet rs = psmt.executeQuery();
            while(rs.next()){
                Message message = new Message(rs.getString(2), rs.getString(3), rs.getString(4));
                message.setGroupId(rs.getInt(5));
                String timestamp = rs.getString(6);
                System.out.println(timestamp);
                message.setTimestamp(timestamp);
                messages.add(message);

            }
        }

        return messages;
    }
}
