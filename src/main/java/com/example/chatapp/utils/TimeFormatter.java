package com.example.chatapp.utils;

import java.time.format.DateTimeFormatter;

public class TimeFormatter {

    public static String now(){
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
        return formatter.format(java.time.LocalDateTime.now());
    }

//    public static String formattedTimestampFromNow(String timestamp) {
//        String[] time = timestamp.split(" ");
//        String[] date = time[0].split("-");
//        String[] timeOfDay = time[1].split(":");
//        String[] currentDate = getCurrentDate().split("-");
//        String[] currentTime = getCurrentTime().split(":");
//        if (date[0].equals(currentDate[0]) && date[1].equals(currentDate[1]) && date[2].equals(currentDate[2])) {
//            if (timeOfDay[0].equals(currentTime[0])) {
//                return formatTime(time[1]);
//            } else {
//                return "Today at " + formatTime(time[1]);
//            }
//        } else {
//            return date[2] + "/" + date[1] + "/" + date[0] + " at " + formatTime(time[1]);
//        }
//    }
//
//    public static void main(String[] args) {
//        System.out.println(formatTime("13:30"));
//    }
}
