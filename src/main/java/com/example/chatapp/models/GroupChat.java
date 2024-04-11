package com.example.chatapp.models;

import java.util.ArrayList;
import java.util.List;

public class GroupChat {
    private int id;

    private String name;

    private List<User> members = new ArrayList<>();

    public GroupChat() {
    }

    public GroupChat(int id, String name) {
        this.id = id;
        this.name = name;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<User> getMembers() {
        return members;
    }

    public void setMembers(List<User> members) {
        this.members = members;
    }

    public void addMember(User user) {
        members.add(user);
    }
}
