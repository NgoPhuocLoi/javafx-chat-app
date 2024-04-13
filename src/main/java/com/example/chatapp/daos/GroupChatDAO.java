package com.example.chatapp.daos;

import com.example.chatapp.dbs.MySQLDB;
import com.example.chatapp.models.GroupChat;
import com.example.chatapp.models.User;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

public class GroupChatDAO {
    public static boolean create(String groupName){
        String sql = "insert into chatgroups (group_name) values(?)";
        try (
                Connection con = MySQLDB.getConnection();
                PreparedStatement psmt = con.prepareStatement(sql);) {
            psmt.setString(1, groupName);
            if (psmt.executeUpdate() > 0) {
                return true;
            }
        }catch (Exception e) {
            throw new RuntimeException(e);
        }
        return false;
    }

    public static boolean addMembers(List<User> users, int groupId){
        String sql = "insert into groupmembers (username, group_id) values(?, ?)";
        try (
                Connection con = MySQLDB.getConnection();
                PreparedStatement psmt = con.prepareStatement(sql);) {
            for (User user : users) {
                psmt.setString(1, user.getUsername());
                psmt.setInt(2, groupId);
                psmt.executeUpdate();
            }

        }catch (Exception e) {
            throw new RuntimeException(e);
        }
        return false;
    }

    public static int getNewGroupId(){
        String sql = "SELECT MAX(group_id) FROM chatgroups";
        try (
                Connection con = MySQLDB.getConnection();
                PreparedStatement psmt = con.prepareStatement(sql);) {

            ResultSet rs = psmt.executeQuery();
            if(rs.next()){
                return rs.getInt(1);
            }

        }catch (Exception e) {
            throw new RuntimeException(e);
        }
        return 0;
    }

    public static List<GroupChat> getGroupsByUsername(String username) {
        String sql = "select * from chatgroups where group_id in (select group_id from groupmembers where username=?)";
        List<GroupChat> groups = new ArrayList<>();
        try (
            Connection con = MySQLDB.getConnection();
            PreparedStatement psmt = con.prepareStatement(sql)) {
            psmt.setString(1, username);
            System.out.println("AAA");
            ResultSet rs = psmt.executeQuery();
            while(rs.next()){
                System.out.println("BBB");
                GroupChat group = new GroupChat(rs.getInt(1), rs.getString(2));
                groups.add(group);

            }

        } catch (Exception e) {
            System.out.println("Error when getting groups in DAO");
            throw new RuntimeException(e);
        }
        return groups;
    }

    public static List<String> getMembersInGroup(int groupId) {
        String sql = "select username from groupmembers where group_id=?";
        List<String> members = new ArrayList<>();
        try (
            Connection con = MySQLDB.getConnection();
            PreparedStatement psmt = con.prepareStatement(sql)) {
            psmt.setInt(1, groupId);
            ResultSet rs = psmt.executeQuery();
            while(rs.next()){
                members.add(rs.getString(1));
            }

        } catch (Exception e) {
            System.out.println("Error when getting members in group in DAO");
            throw new RuntimeException(e);
        }
        return members;
    }
}
