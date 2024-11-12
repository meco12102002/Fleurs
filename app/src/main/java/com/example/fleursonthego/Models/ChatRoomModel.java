package com.example.fleursonthego.Models;

import java.sql.Timestamp;
import java.util.List;

public class ChatRoomModel {
    public String getChatRoomID() {
        return chatRoomID;
    }

    public void setChatRoomID(String chatRoomID) {
        this.chatRoomID = chatRoomID;
    }

    public List<String> getUserId() {
        return userIds;
    }

    public void setUserId(List<String> userId) {
        this.userIds = userId;
    }

    public Timestamp getLastMessageTimestamp() {
        return lastMessageTimestamp;
    }

    public void setLastMessageTimestamp(Timestamp lastMessageTimestamp) {
        this.lastMessageTimestamp = lastMessageTimestamp;
    }

    public String getLastMessage() {
        return lastMessage;
    }

    public void setLastMessage(String lastMessage) {
        this.lastMessage = lastMessage;
    }

    public String getLastMessageSenderId() {
        return lastMessageSenderId;
    }

    public void setLastMessageSenderId(String lastMessageSenderId) {
        this.lastMessageSenderId = lastMessageSenderId;
    }

    String chatRoomID;
    List<String> userIds;
    Timestamp lastMessageTimestamp;
    String lastMessage;
    String lastMessageSenderId;

    public ChatRoomModel() {

    }

    public ChatRoomModel(String chatRoomID, List<String> userId, Timestamp lastMessageTimestamp, String lastMessage, String lastMessageSenderId) {
        this.chatRoomID = chatRoomID;
        this.userIds = userId;
        this.lastMessageTimestamp = lastMessageTimestamp;
        this.lastMessage = lastMessage;
        this.lastMessageSenderId = lastMessageSenderId;
    }


}
