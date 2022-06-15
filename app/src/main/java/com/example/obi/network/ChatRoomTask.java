package com.example.obi.network;

import android.os.AsyncTask;
import android.util.Log;
import android.view.View;

import com.example.obi.adapters.RecentConversationsAdapter;
import com.example.obi.databinding.ActivityMainBinding;
import com.example.obi.models.ChatMessage;
import com.example.obi.utilities.UserManager;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

public class ChatRoomTask extends AsyncTask<String,Boolean,JsonArray> {
    private TcpClient tcpClient;
    private UserManager userManager;
    private String userId;
    private ActivityMainBinding binding;
    private RecentConversationsAdapter recentConversationsAdapter;
    public ChatRoomTask(String userId, ActivityMainBinding binding, RecentConversationsAdapter recentConversationsAdapter){
        this.tcpClient = TcpClient.getInstance();
        this.userId=userId;
        this.binding=binding;
        this.recentConversationsAdapter=recentConversationsAdapter;
        this.userManager = UserManager.getInstance(userId);
    }
    private void getChatRoom(String aId, String bId){
        Log.d("test", "Getting list of users");
        JsonObject obj = new JsonObject();
        obj.addProperty("aId",aId);
        obj.addProperty("bId",bId);
        tcpClient.sendMessage("getChatRoom",obj);
    }

    @Override
    protected JsonArray doInBackground(String ... strings) {
        for (String user:strings){
            getChatRoom(userId,user);
        }
        int counter = 0;
        JsonArray replies = new JsonArray();
        String s;
        while(counter<strings.length){
            try {
                s = tcpClient.mBufferIn.readLine();
                if (s != null) {
                    try{
                        replies.add(tcpClient.stringToJson(s));
                        counter++;
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    break;
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return replies;
    }

    private String getReadableDateTime(Date date) {
        return new SimpleDateFormat("MMMM dd, yyyy - hh:mm a", Locale.getDefault()).format(date);
    }

    @Override
    protected void onPostExecute(JsonArray replies) {
        if (replies.size()<1){
            tcpClient.startListening();
            return;
        }
        super.onPostExecute(replies);
        for(JsonElement r:replies){
            JsonObject reply = r.getAsJsonObject();
            if(!reply.get("success").getAsBoolean()){
                continue;
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
        }
        recentConversationsAdapter.notifyDataSetChanged();
        binding.conversationsRecycleView.smoothScrollToPosition(replies.size()-1);
        binding.conversationsRecycleView.setVisibility(View.VISIBLE);
        tcpClient.startListening();
    }
}
