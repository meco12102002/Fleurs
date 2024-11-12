package com.example.fleursonthego.Models;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

import java.io.IOException;

import okhttp3.*;

public class SmsSender {
    private static final String SEMAPHORE_API_URL = "https://api.semaphore.co/api/v4/messages";
    private static final String API_KEY = "4c37a577938c9fb9ec623996a15c3e5b"; // Replace with your actual API key

    public void sendSms(String phoneNumber, String message, SmsSendCallback callback) {
        OkHttpClient client = new OkHttpClient();

        // Build the request with the necessary parameters
        RequestBody formBody = new FormBody.Builder()
                .add("apikey", API_KEY)
                .add("number", phoneNumber)
                .add("message", message)
                .build();

        Request request = new Request.Builder()
                .url(SEMAPHORE_API_URL)
                .post(formBody)
                .build();

        // Execute the request asynchronously
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                callback.onFailure(e);
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    callback.onSuccess(response.body().string());
                } else {
                    callback.onFailure(new Exception("Failed to send SMS: " + response.message()));
                }
            }
        });
    }

    // Callback interface for handling success/failure
    public interface SmsSendCallback {
        void onSuccess(String response);
        void onFailure(Exception e);
    }
}
