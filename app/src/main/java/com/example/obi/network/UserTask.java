package com.example.obi.network;

import android.os.AsyncTask;
import android.util.Log;
import android.view.View;
import com.example.obi.databinding.ActivityUsersBinding;
import com.example.obi.listeners.UserListener;
import com.google.gson.JsonObject;


public class UserTask extends AsyncTask<String, Boolean, Void> {
    private TcpClient tcpClient;
    private ActivityUsersBinding binding;
    public UserListener userListener;
    public UserTask(ActivityUsersBinding binding, UserListener userListener){
        this.binding=binding;
        this.userListener=userListener;
        this.tcpClient=TcpClient.getInstance();
    }
    private void getUser(String email){
        Log.d("test", "Getting list of users");
        JsonObject obj = new JsonObject();
        obj.addProperty("email",email);
        tcpClient.sendMessage("getUsers",obj);
    }

    @Override
    protected Void doInBackground(String... email) {
        publishProgress(true);
        Log.d("test",email[0]);
        getUser(email[0]);
        publishProgress(false);
        return null;
    }

    @Override
    protected void onProgressUpdate(Boolean... values) {
        super.onProgressUpdate(values);
        if (values[0]) {
            binding.progressBar.setVisibility(View.VISIBLE);
        } else {
            binding.progressBar.setVisibility(View.GONE);
        }
    }

}
