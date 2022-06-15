package com.example.obi.activities;


import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Base64;
import android.view.View;
import androidx.annotation.NonNull;
import com.example.obi.adapters.ChatAdapter;
import com.example.obi.databinding.ActivityChatBinding;
import com.example.obi.models.ChatMessage;
import com.example.obi.models.User;
import com.example.obi.network.MessagingTask;
import com.example.obi.network.TcpClient;
import com.example.obi.utilities.Constants;
import com.example.obi.utilities.PreferenceManager;
import com.example.obi.utilities.UserManager;
import com.google.gson.JsonObject;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

public class ChatActivity extends BaseActivity {
    private ActivityChatBinding binding;
    private User receiverUser;
    private ChatAdapter chatAdapter;
    private PreferenceManager preferenceManager;
    private TcpClient tcpClient;
    private UserManager userManager;
    private ArrayList<ChatMessage> chatMessages;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityChatBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        loadReceiverDetails();
        setListeners();
        init();
    }

    private void init() {
        userManager = UserManager.getInstance();
        chatMessages = userManager.getChatRoom(receiverUser.id).getChatMessages();
        tcpClient = TcpClient.getInstance();
        tcpClient.setMessageHandler(new MessageHandler());
        preferenceManager = new PreferenceManager(getApplicationContext());
        chatAdapter = new ChatAdapter(
                chatMessages,
                getBitmapFromEncodedString(receiverUser.image),
                preferenceManager.getString(Constants.KEY_USER_ID)
        );
        binding.chatRecycleView.setAdapter(chatAdapter);
        chatAdapter.notifyDataSetChanged();
        binding.chatRecycleView.setVisibility(View.VISIBLE);
        binding.progressBar.setVisibility(View.GONE);
        userManager.setActivityChatBinding(binding);
        userManager.setChatAdapter(chatAdapter);
    }

    class MessageHandler extends Handler {
        @Override
        public void handleMessage(@NonNull Message msg) {
            super.handleMessage(msg);
            chatAdapter.notifyItemInserted(chatMessages.size());
            binding.chatRecycleView.smoothScrollToPosition(chatMessages.size() - 1);
            binding.chatRecycleView.setVisibility(View.VISIBLE);
        }
    }

    private String getReadableDateTime(Date date) {
        return new SimpleDateFormat("MMMM dd, yyyy - hh:mm a", Locale.getDefault()).format(date);
    }

    private void sendMessage() {
        new MessagingTask(binding,preferenceManager,receiverUser)
                .execute(binding.inputMessage.getText().toString());
    }

    private Bitmap getBitmapFromEncodedString(String encodedImage) {
        if (encodedImage != null) {
            byte[] bytes = Base64.decode(encodedImage, Base64.DEFAULT);
            return BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
        } else {
            return null;
        }
    }

    private void loadReceiverDetails() {
        receiverUser = (User) getIntent().getSerializableExtra(Constants.KEY_USER);
        binding.textName.setText(receiverUser.name);
    }

    private void setListeners() {
        binding.layoutSend.setOnClickListener(view -> sendMessage());
        binding.imageBack.setOnClickListener(view -> onBackPressed());
    }

}