package com.example.obi.network;

import android.os.Handler;
import android.os.Message;
import android.util.Log;
import com.example.obi.models.ChatMessage;
import com.example.obi.utilities.UserManager;
import com.google.gson.Gson;
import com.google.gson.JsonObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class TcpClient {
    private static TcpClient instance = null;
    public static final String SERVER_IP = "192.168.1.112"; //server IP address
    public static final int SERVER_PORT = 1337;
    private boolean isListening = false;
    public PrintWriter mBufferOut;
    public BufferedReader mBufferIn;
    private String userId;
    private Handler usersHandler;
    private Handler chatRoomHandler;
    private Handler messageHandler;
    private UserManager userManager;

    public Handler getChatRoomHandler() {
        return chatRoomHandler;
    }

    public void setUsersHandler(Handler usersHandler) {
        this.usersHandler = usersHandler;
    }

    public void setChatRoomHandler(Handler chatRoomHandler) {
        this.chatRoomHandler = chatRoomHandler;
    }

    public void setMessageHandler(Handler messageHandler) {
        this.messageHandler = messageHandler;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }


    public static TcpClient getInstance() {
        if (instance == null) {
            instance = new TcpClient();
        }
        return instance;
    }

    public JsonObject getReplyOnce(){
        JsonObject reply=new JsonObject();
        String s;
        while(true){
            try {
                s = mBufferIn.readLine();
                if (s != null) {
                    try{
                        Log.d("test", "receive from server: "+s);
                        reply=stringToJson(s);
                        Log.d("test","json to string: "+reply.toString());
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    break;
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return reply;
    }

    public void sendMessage(String type,final JsonObject message) {
        JsonObject obj=new JsonObject();
        obj.addProperty("type",type);
        obj.add("message",message);
        String s = obj.toString();
        Runnable runnable = () -> {
            if (mBufferOut != null) {
                Log.d("test", "Sending: " + s +" to server");
                Log.d("test", "Size of sending message: "+s.length());
                mBufferOut.println(s);
                mBufferOut.flush();
            }
        };
        Thread thread = new Thread(runnable);
        thread.start();
    }

    /**
     * Close the connection and release the members
     */
    public void stopListening() {
        Log.d("test", "Stop listening");
        isListening = false;
    }

    public void run() throws IOException {
        Socket socket = new Socket(SERVER_IP, SERVER_PORT);
        Log.d("test", "Connect to server: " + SERVER_IP);
        mBufferOut = new PrintWriter(new BufferedWriter(new OutputStreamWriter(socket.getOutputStream())), true);
        mBufferIn = new BufferedReader(new InputStreamReader(socket.getInputStream()));
    }

    private String getReadableDateTime(Date date) {
        return new SimpleDateFormat("MMMM dd, yyyy - hh:mm a", Locale.getDefault()).format(date);
    }

    public void startListening(){
        Log.d("test","start listening");
        new Thread(new Runnable() {
            @Override
            public void run() {
                isListening = true;
                do{
                    JsonObject reply = getReplyOnce();
                    Log.d("test","getting reply");
                    if(reply.get("type").getAsString().equals("newMessage")){
                        Log.d("test","get new message");
                        Log.d("test","friendId: "+reply.get("receiver").getAsString());
                        String sender = reply.get("sender").getAsString();
                        ChatMessage chatMessage = new ChatMessage();
                        chatMessage.senderId = reply.get("sender").getAsString();
                        chatMessage.message = reply.get("message").getAsString();
                        chatMessage.dateObj = new Date(reply.get("timestamp").getAsString());
                        chatMessage.dateTime = getReadableDateTime(chatMessage.dateObj);
                        Log.d("test",chatMessage.message);
                        if(sender.equals(userId)){
                            Message msg=new Message();
                            msg.obj=reply;
                            Log.d("test","friendId: "+reply.get("receiver").getAsString());
                            userManager = UserManager.getInstance();
                            userManager.getChatRoom(reply.get("receiver").getAsString()).getChatMessages().add(chatMessage);
                            messageHandler.sendMessage(msg);
                        }else{
                            // sender is friend
                            String friendId = reply.get("sender").getAsString();
                            Log.d("test",friendId);
                            userManager = UserManager.getInstance();
                            if(userManager.isNewFriend(friendId)){
                                Message msg = new Message();
                                msg.arg1=0;
                                msg.obj = friendId;
                                chatRoomHandler.sendMessage(msg);
                            }else{
                                Message msgg=new Message();
                                //
                                userManager = UserManager.getInstance();
                                userManager.getChatRoom(friendId).getChatMessages().add(chatMessage);
                                if(messageHandler!=null){
                                    messageHandler.sendMessage(msgg);
                                }
                                Message msg=new Message();
                                msg.arg1=2;
                                msg.obj=friendId;
                                chatRoomHandler.sendMessage(msg);
                            }
                        }
                    }else if(reply.get("type").getAsString().equals("getUsers")){
                        Log.d("test","get users");
                        Message msg=new Message();
                        msg.obj = reply.get("users").getAsJsonArray();
                        usersHandler.sendMessage(msg);
                    }else if(reply.get("type").getAsString().equals("getChatRoom")){
                        Log.d("test","get chat room");
                        Message msg=new Message();
                        msg.arg1=1;
                        msg.obj=reply;
                        chatRoomHandler.sendMessage(msg);
                    }
                }while(isListening);
            }
        }).start();
    }

    public JsonObject stringToJson(String s) {
        Gson gson = new Gson();
        return gson.fromJson(s, JsonObject.class);
    }

}