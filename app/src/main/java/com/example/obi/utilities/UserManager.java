package com.example.obi.utilities;

import android.view.View;
import com.example.obi.adapters.ChatAdapter;
import com.example.obi.adapters.RecentConversationsAdapter;
import com.example.obi.databinding.ActivityChatBinding;
import com.example.obi.databinding.ActivityMainBinding;
import com.example.obi.models.ChatMessage;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class UserManager {
    private static UserManager userManager;
    private String userId;
    private HashMap<String,ChatRoom> friends;
    private List<ChatRoom> chatRooms;
    private ChatAdapter chatAdapter;
    private RecentConversationsAdapter recentConversationsAdapter;
    private ActivityMainBinding activityMainBinding;
    private ActivityChatBinding activityChatBinding;

    public void setChatAdapter(ChatAdapter chatAdapter) {
        this.chatAdapter = chatAdapter;
    }

    public void setRecentConversationsAdapter(RecentConversationsAdapter recentConversationsAdapter) {
        this.recentConversationsAdapter = recentConversationsAdapter;
    }

    public void setActivityMainBinding(ActivityMainBinding activityMainBinding) {
        this.activityMainBinding = activityMainBinding;
    }

    public void setActivityChatBinding(ActivityChatBinding activityChatBinding) {
        this.activityChatBinding = activityChatBinding;
    }

    public void clear(){
        userManager=null;
    }

    public class ChatRoom{
        private String me;
        private String friendId;
        private ArrayList<ChatMessage> chatMessages;
        private String friendName;
        private String friendImage;
        public ChatRoom(String myId,String friendId,ArrayList<ChatMessage> chatMessages,String friendName,String friendImage){
            this.me=myId;
            this.friendId=friendId;
            this.chatMessages=chatMessages;
            this.friendName=friendName;
            this.friendImage=friendImage;
        }

        public String getMe() {
            return me;
        }

        public String getFriendId() {
            return friendId;
        }

        public ArrayList<ChatMessage> getChatMessages() {
            return chatMessages;
        }

        public String getFriendName() {
            return friendName;
        }


        public String getFriendImage() {
            return friendImage;
        }

        public String getLastMessage(){
            if (chatMessages.size()-1>=0){
                return chatMessages.get(chatMessages.size()-1).message;
            }
            return null;
        }

    }
    private UserManager(String userId){
        this.userId=userId;
        this.friends = new HashMap<>();
        this.chatRooms = new ArrayList<>();
    }
    public static UserManager getInstance(String userId){
        if(userManager ==null){
            userManager = new UserManager(userId);
        }
        return userManager;
    }

    public static UserManager getInstance(){
        if(userManager ==null){
            return null;
        }
        return userManager;
    }

    public void addChatRoom(String friendId,ArrayList<ChatMessage> chatMessages,String friendName,String friendImage){
        ChatRoom chatRoom = new ChatRoom(userId,friendId,chatMessages,friendName,friendImage);
        friends.put(friendId,chatRoom);
        chatRooms.add(chatRoom);
    }

    public List<ChatRoom> getChatRooms() {
        return chatRooms;
    }

    public ChatRoom getChatRoom(String id) {
        return friends.get(id);
    }

    public void addMessage(String id,ChatMessage chatMessage){
        ChatRoom chatRoom = getChatRoom(id);
        chatRoom.getChatMessages().add(chatMessage);
        if (chatAdapter!=null){
            chatAdapter.notifyItemInserted(chatRoom.getChatMessages().size()-1);
            activityChatBinding.chatRecycleView.smoothScrollToPosition(chatRoom.getChatMessages().size()-1);
            activityChatBinding.chatRecycleView.setVisibility(View.VISIBLE);
        }
    }
    public boolean isNewFriend(String id){
        return !friends.containsKey(id);
    }

    public int getIndex(String id){
        for(int i=0;i<chatRooms.size();i++){
            if(chatRooms.get(i).equals(getChatRoom(id))){
                return i;
            }
        }
        return -1;
    }

    public int getRoomNum(){
        return friends.size();
    }
}
