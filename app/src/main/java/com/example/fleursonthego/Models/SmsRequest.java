package com.example.fleursonthego.Models;

public class SmsRequest {
    private String from;
    private String to;
    private String text;

    public SmsRequest(String from, String to, String text) {
        this.from = from;
        this.to = to;
        this.text = text;
    }
}
