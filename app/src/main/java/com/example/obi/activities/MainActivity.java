package com.example.obi.activities;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Base64;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.example.obi.adapters.RecentConversationsAdapter;
import com.example.obi.databinding.ActivityMainBinding;
import com.example.obi.listeners.ConversionListener;
import com.example.obi.models.ChatMessage;
import com.example.obi.models.User;
import com.example.obi.network.FriendTask;
import com.example.obi.network.TcpClient;
import com.example.obi.utilities.Constants;
import com.example.obi.utilities.PreferenceManager;
import com.example.obi.utilities.UserManager;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

public class MainActivity extends BaseActivity implements ConversionListener {

    private ActivityMainBinding binding;
    private PreferenceManager preferenceManager;
    private RecentConversationsAdapter conversationsAdapter;
    private UserManager userManager;
    private TcpClient tcpClient;
    private String userId;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        preferenceManager = new PreferenceManager(getApplicationContext());
        userId = preferenceManager.getString(Constants.KEY_USER_ID);
        init();
        loadUserDetails();
        setListeners();
    }

    private void init(){
        tcpClient = TcpClient.getInstance();
        tcpClient.setUserId(userId);
        tcpClient.setChatRoomHandler(new chatRoomHandler());
        userManager = UserManager.getInstance(userId);
        userManager.setActivityMainBinding(binding);
        conversationsAdapter = new RecentConversationsAdapter(userManager.getChatRooms(), this);
        userManager.setRecentConversationsAdapter(conversationsAdapter);
        binding.conversationsRecycleView.setAdapter(conversationsAdapter);
        new FriendTask(binding,userId,conversationsAdapter).execute();
    }

    private void setListeners(){
        binding.fabNewChat.setOnClickListener(view ->
                startActivity(new Intent(getApplicationContext(),UsersActivity.class)));
        binding.imageSignOut.setOnClickListener(view -> signOut());
    }
    private void loadUserDetails(){
        binding.textName.setText(preferenceManager.getString(Constants.KEY_NAME));
        byte[] bytes = Base64.decode(preferenceManager.getString(Constants.KEY_IMAGE),Base64.DEFAULT);
        Bitmap bitmap = BitmapFactory.decodeByteArray(bytes,0,bytes.length);
        binding.imageProfile.setImageBitmap(bitmap);
    }

    private void notifyToast(String s) {
        Toast.makeText(getApplicationContext(), s, Toast.LENGTH_SHORT).show();
    }

    private void signOut(){
        notifyToast("Signing out...");
        preferenceManager.clear();
        tcpClient.stopListening();
        userManager.clear();
        startActivity(new Intent(getApplicationContext(),SignInActivity.class));
        finish();
    }
    @Override
    public void onConversionClicked(User user) {
        Intent intent = new Intent(getApplicationContext(),ChatActivity.class);
        intent.putExtra(Constants.KEY_USER,user);
        startActivity(intent);
    }

    class chatRoomHandler extends Handler{
        @Override
        public void handleMessage(@NonNull Message msg) {
            super.handleMessage(msg);
            if(msg.arg1==0){
                // ask for a new chatroom with a new friend
                String id=(String) msg.obj;
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        JsonObject obj = new JsonObject();
                        obj.addProperty("aId",userId);
                        obj.addProperty("bId",id);
                        tcpClient.sendMessage("getChatRoom",obj);
                    }
                }).start();
            }else if(msg.arg1==1){
                // get a new chatroom with a new friend
                JsonObject reply = (JsonObject) msg.obj;
                if(!reply.get("success").getAsBoolean()){
                    return;
                }
                String A = reply.get("A").getAsString();
                String B = reply.get("B").getAsString();
                String friend = userId.equals(A) ? B : A;
                JsonArray messages = reply.get("messages").getAsJsonArray();
                ArrayList<ChatMessage> chatMessages=new ArrayList<>();
                String friendImage = userId.equals(A) ? reply.get("bImage").getAsString() : reply.get("aImage").getAsString();
                String friendName = userId.equals(A) ? reply.get("bName").getAsString() : reply.get("aName").getAsString();
                for(JsonElement m:messages){
                    JsonObject message = m.getAsJsonObject();
                    ChatMessage chatMessage = new ChatMessage();
                    String senderReference = message.get("sender").getAsString();
                    chatMessage.senderId = senderReference.equals("A") ? A : B;
                    chatMessage.message = message.get("message").getAsString();
                    Date date = new Date(message.get("timestamp").getAsJsonObject().get("seconds").getAsLong()*1000);
                    chatMessage.dateObj = date;
                    chatMessage.dateTime = getReadableDateTime(date);
                    chatMessages.add(chatMessage);
                }
                userManager.addChatRoom(friend,chatMessages,friendName,friendImage);
                conversationsAdapter.notifyItemInserted(userManager.getRoomNum()-1);
                binding.conversationsRecycleView.smoothScrollToPosition(userManager.getRoomNum()-1);
                binding.conversationsRecycleView.setVisibility(View.VISIBLE);
            }
            else if(msg.arg1==2){
                // refresh last message
                String friendId=(String)msg.obj;
                conversationsAdapter.notifyItemChanged(userManager.getIndex(friendId));
            }
        }
    }

    private String getReadableDateTime(Date date) {
        return new SimpleDateFormat("MMMM dd, yyyy - hh:mm a", Locale.getDefault()).format(date);
    }
}
