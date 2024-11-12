package com.example.fleursonthego.views;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;

import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.fleursonthego.Adapters.MessageAdapter;
import com.example.fleursonthego.Models.ChatRoomModel;
import com.example.fleursonthego.Models.MessageModel;
import com.example.fleursonthego.Models.UserModel;
import com.example.fleursonthego.R;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import utils.FirebaseUtil;

public class ChatActivity extends AppCompatActivity {

    private TextView userNameTextView;
    private EditText messageInput;
    private ImageButton sendButton;
    private RecyclerView messagesRecyclerView;
    private MessageAdapter messageAdapter;

    private String chatRoomId;
    private String userId;
    private ChatRoomModel chatRoomModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        // Initialize UI components
        userNameTextView = findViewById(R.id.chatUserName);
        messageInput = findViewById(R.id.messageEditText);
        sendButton = findViewById(R.id.sendMessageBtn);
        messagesRecyclerView = findViewById(R.id.messagesRecyclerView);


        // Set up RecyclerView
        messagesRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        // Get user data from Intent
        userId = getIntent().getStringExtra("userId");
        String userName = getIntent().getStringExtra("userName");

        // Set chat room ID and display user name
        chatRoomId = FirebaseUtil.getChatRoomId(FirebaseAuth.getInstance().getCurrentUser().getUid(), userId);
        if (userName != null) {
            userNameTextView.setText("Chat with " + userName);
        } else {
            userNameTextView.setText("Chat with User");
            Toast.makeText(this, "No user name passed", Toast.LENGTH_SHORT).show();
        }

        // Set up or retrieve chat room model
        getOrCreateChatroomModel();

        // Set up messages RecyclerView and adapter
        setupMessagesRecyclerView();

        // Send a message when the button is clicked
        sendButton.setOnClickListener(v -> sendMessage());

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Enable back button in the toolbar
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);  // Show the back button
            getSupportActionBar().setDisplayShowHomeEnabled(true);  // Show home icon
        }
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle the toolbar item clicks
        switch (item.getItemId()) {
            case android.R.id.home:
                // Handle the back button press
                onBackPressed();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
    @Override
    public void onBackPressed() {
        super.onBackPressed();
        startActivity(new Intent(ChatActivity.this, ChatListActivity.class));
        finish();
    }

    private void getOrCreateChatroomModel() {
        String currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DatabaseReference chatRoomRef = FirebaseUtil.getChatRoomReference(chatRoomId);

        chatRoomRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                chatRoomModel = task.getResult().getValue(ChatRoomModel.class);
                if (chatRoomModel == null) {
                    chatRoomModel = new ChatRoomModel(chatRoomId, Arrays.asList(currentUserId, userId), null, null, null);
                    chatRoomRef.setValue(chatRoomModel);
                }
            }
        });
    }

    private void setupMessagesRecyclerView() {
        DatabaseReference messageRef = FirebaseUtil.getChatRoomReference(chatRoomId).child("messages");
        FirebaseRecyclerOptions<MessageModel> options = new FirebaseRecyclerOptions.Builder<MessageModel>()
                .setQuery(messageRef, MessageModel.class)
                .build();
        messageAdapter = new MessageAdapter(options);
        messagesRecyclerView.setAdapter(messageAdapter);
    }

    private void sendMessage() {
        String messageText = messageInput.getText().toString().trim();
        if (!messageText.isEmpty()) {
            String senderId = FirebaseUtil.currentUserID();
            Map<String, Object> message = new HashMap<>();
            message.put("senderId", senderId);
            message.put("message", messageText);
            message.put("timestamp", System.currentTimeMillis());

            FirebaseUtil.getChatRoomReference(chatRoomId).child("messages").push().setValue(message);
            messageInput.setText("");
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (messageAdapter != null) {
            messageAdapter.startListening();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (messageAdapter != null) {
            messageAdapter.stopListening();
        }
    }
}
