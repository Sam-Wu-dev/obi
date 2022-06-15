package com.example.obi.network;

import android.os.AsyncTask;
import android.util.Log;

import com.example.obi.databinding.ActivityChatBinding;
import com.example.obi.models.User;
import com.example.obi.utilities.Constants;
import com.example.obi.utilities.PreferenceManager;
import com.google.gson.JsonObject;
import java.util.Date;

public class MessagingTask extends AsyncTask<String,Void,Void> {
    private TcpClient tcpClient;
    private ActivityChatBinding binding;
    private User receiver;
    private PreferenceManager preferenceManager;
    private ChatRoomTask chatTask;
    public MessagingTask(ActivityChatBinding binding, PreferenceManager preferenceManager, User receiver){
        this.binding = binding;
        this.tcpClient = TcpClient.getInstance();
        this.preferenceManager = preferenceManager;
        this.receiver = receiver;
    }
    private void sendMessage(String aId, String bId, String message, Date timestamp){
        Log.d("test", "Getting list of users");
        JsonObject obj = new JsonObject();
        obj.addProperty("aId",aId);
        obj.addProperty("bId",bId);
        obj.addProperty("message",message);
        obj.addProperty("timestamp",timestamp.toString());
        tcpClient.sendMessage("messaging",obj);
    }

    @Override
    protected Void doInBackground(String... strings) {
        sendMessage(preferenceManager.getString(Constants.KEY_USER_ID),receiver.id,strings[0],new Date());
        return null;
    }

    @Override
    protected void onPostExecute(Void unused) {
        binding.inputMessage.setText(null);
    }
}
