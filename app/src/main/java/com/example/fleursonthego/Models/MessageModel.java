// MessageModel.java
package com.example.fleursonthego.Models;

public class MessageModel {
    private String senderId;
    private String message;
    private long timestamp;

    public MessageModel() {
        // Default constructor required for Firebase
    }

    public MessageModel(String senderId, String message, long timestamp) {
        this.senderId = senderId;
        this.message = message;
        this.timestamp = timestamp;
    }

    public String getSenderId() {
        return senderId;
    }

    public String getMessage() {
        return message;
    }

    public long getTimestamp() {
        return timestamp;
    }
}
