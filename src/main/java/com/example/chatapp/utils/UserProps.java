package com.example.chatapp.utils;

import java.io.DataInputStream;
import java.io.DataOutputStream;

public class UserProps {

    private static final UserProps userProps = new UserProps();

    public static UserProps getInstance(){
        return userProps;
    }
    private String username;

    private String avatarUrl;

    private DataInputStream dataInputStream;

    private DataOutputStream dataOutputStream;


    private UserProps() {
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getAvatarUrl() {
        return avatarUrl;
    }

    public void setAvatarUrl(String avatarUrl) {
        this.avatarUrl = avatarUrl;
    }

    public DataInputStream getDataInputStream() {
        return dataInputStream;
    }

    public void setDataInputStream(DataInputStream dataInputStream) {
        this.dataInputStream = dataInputStream;
    }

    public DataOutputStream getDataOutputStream() {
        return dataOutputStream;
    }

    public void setDataOutputStream(DataOutputStream dataOutputStream) {
        this.dataOutputStream = dataOutputStream;
    }
}
