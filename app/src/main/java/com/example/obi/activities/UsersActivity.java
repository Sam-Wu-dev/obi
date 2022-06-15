package com.example.obi.activities;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.example.obi.adapters.UsersAdapter;
import com.example.obi.databinding.ActivityUsersBinding;
import com.example.obi.listeners.UserListener;
import com.example.obi.models.User;
import com.example.obi.network.TcpClient;
import com.example.obi.network.UserTask;
import com.example.obi.utilities.Constants;
import com.example.obi.utilities.PreferenceManager;
import com.example.obi.utilities.UserManager;
import com.google.gson.JsonArray;
import java.util.ArrayList;
import java.util.List;

public class UsersActivity extends BaseActivity implements UserListener {
    private ActivityUsersBinding binding;
    private PreferenceManager preferenceManager;
    private TcpClient tcpClient;
    private UserManager userManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityUsersBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        preferenceManager = new PreferenceManager(getApplicationContext());
        userManager = UserManager.getInstance();
        setListeners();
        tcpClient = TcpClient.getInstance();
        tcpClient.setUsersHandler(new usersHandler());
        getUsers();
    }

    private void setListeners(){
        binding.imageBack.setOnClickListener(view -> onBackPressed());
    }

    private void getUsers(){
        UserTask userTask = new UserTask(binding,this);
        userTask.execute(preferenceManager.getString(Constants.KEY_EMAIL));
    }

    @Override
    public void onUserClicked(User user) {
        Intent intent = new Intent(getApplicationContext(),ChatActivity.class);
        intent.putExtra(Constants.KEY_USER,user);
        startActivity(intent);
        finish();
    }

    class usersHandler extends Handler{
        @Override
        public void handleMessage(@NonNull Message msg) {
            super.handleMessage(msg);
            JsonArray jsonArray = (JsonArray) msg.obj;
            List<User> myUsers = new ArrayList<>();
            jsonArray.forEach(user->{
                User myUser= new User();
                myUser.name=user.getAsJsonObject().get(Constants.KEY_NAME).getAsString();
                myUser.email=user.getAsJsonObject().get(Constants.KEY_EMAIL).getAsString();
                myUser.image=user.getAsJsonObject().get(Constants.KEY_IMAGE).getAsString();
                myUser.id = user.getAsJsonObject().get(Constants.KEY_USER_ID).getAsString();
                myUsers.add(myUser);
            });
            UsersAdapter usersAdapter = new UsersAdapter(myUsers, new UserListener() {
                @Override
                public void onUserClicked(User user) {
                    if(userManager.isNewFriend(user.id)){
                        Message msg = new Message();
                        msg.arg1=0;
                        msg.obj = user.id;
                        tcpClient.getChatRoomHandler().sendMessage(msg);
                        Log.d("test","Is new friend");
                        finish();
                    }else{
                        Log.d("test","Is old friend");
                    }
                }
            });
            binding.usersRecycleView.setAdapter(usersAdapter);
            binding.usersRecycleView.setVisibility(View.VISIBLE);
        }
    }
    private void notifyToast(String s) {
        Toast.makeText(getApplicationContext(), s, Toast.LENGTH_SHORT).show();
    }
}