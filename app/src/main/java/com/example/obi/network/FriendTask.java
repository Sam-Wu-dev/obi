package com.example.obi.network;

import android.os.AsyncTask;
import android.util.Log;
import android.view.View;

import com.example.obi.adapters.RecentConversationsAdapter;
import com.example.obi.databinding.ActivityMainBinding;
import com.example.obi.utilities.PreferenceManager;
import com.example.obi.utilities.UserManager;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import java.io.IOException;
import java.util.ArrayList;

public class FriendTask extends AsyncTask<Void,Boolean, ArrayList<String>> {
    private String userId;
    private TcpClient tcpClient;
    private ActivityMainBinding binding;
    private RecentConversationsAdapter recentConversationsAdapter;
    private UserManager userManager;
    public FriendTask(ActivityMainBinding binding,String userId,RecentConversationsAdapter recentConversationsAdapter){
        this.userId=userId;
        this.binding=binding;
        this.tcpClient=TcpClient.getInstance();
        this.recentConversationsAdapter=recentConversationsAdapter;
        this.userManager = UserManager.getInstance();
    }
    void getFriend(){
        JsonObject obj = new JsonObject();
        obj.addProperty("userId",userId);
        tcpClient.sendMessage("getFriend",obj);
    }
    @Override
    protected ArrayList<String> doInBackground(Void... voids) {
        publishProgress(true);
        getFriend();
        JsonObject reply=tcpClient.getReplyOnce();
        JsonArray arr = reply.get("friends").getAsJsonArray();
        ArrayList<String> friends = new ArrayList<>();
        arr.forEach(str->{
            friends.add(str.getAsString());
        });
        publishProgress(false);
        return friends;
    }

    @Override
    protected void onProgressUpdate(Boolean... values) {
        super.onProgressUpdate(values);
        if (values[0]) {
            binding.progressBar.setVisibility(View.VISIBLE);
        } else {
            binding.progressBar.setVisibility(View.INVISIBLE);
        }
    }

    @Override
    protected void onPostExecute(ArrayList<String> strings) {
        super.onPostExecute(strings);
        Log.d("test", String.valueOf(strings.size()));
        new ChatRoomTask(userId,binding,recentConversationsAdapter).execute(strings.toArray(new String[strings.size()]));
    }
}
