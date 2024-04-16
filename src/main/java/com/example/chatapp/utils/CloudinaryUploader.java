package com.example.chatapp.utils;
import com.cloudinary.*;
import com.cloudinary.utils.ObjectUtils;

import java.io.IOException;

public class CloudinaryUploader {

    public static String upload(String filePath) {
        Cloudinary cloudinary = new Cloudinary("cloudinary://355489418972132:4SXqhVRQSXsc5h6ofCS3r5H9hKM@dudsfr6aq");
        try {
            return cloudinary.uploader().upload(filePath, ObjectUtils.emptyMap()).get("url").toString();
        } catch (IOException e) {
            System.out.println("Error uploading image");
            return null;
        }
    }
}
