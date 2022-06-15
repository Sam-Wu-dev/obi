package com.example.obi.network;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.activity.ComponentActivity;

import com.example.obi.activities.MainActivity;
import com.example.obi.databinding.ActivitySignUpBinding;
import com.example.obi.utilities.Constants;
import com.example.obi.utilities.PreferenceManager;
import com.google.gson.JsonObject;

import java.io.IOException;

public class SignUpTask extends AsyncTask<Void,Boolean,Boolean> {
    final private ActivitySignUpBinding binding;
    final private JsonObject newUser;
    final private TcpClient tcpClient;
    final private ComponentActivity context;
    final private PreferenceManager preferenceManager;
    public SignUpTask(ComponentActivity context,ActivitySignUpBinding binding,String name, String email, String password, String image){
        super();
        this.context=context;
        this.binding=binding;
        this.tcpClient=TcpClient.getInstance();
        this.preferenceManager=new PreferenceManager(context);
        this.newUser = new JsonObject();
        this.newUser.addProperty(Constants.KEY_NAME,name);
        this.newUser.addProperty(Constants.KEY_EMAIL,email);
        this.newUser.addProperty(Constants.KEY_PASSWORD,password);
        this.newUser.addProperty(Constants.KEY_IMAGE,image);
    }
    private void signUp(){
        Log.d("test", "Signing Up");
        tcpClient.sendMessage("signUp",newUser);
    }
    @Override
    protected Boolean doInBackground(Void... voids) {
        publishProgress(true);
        signUp();
        JsonObject reply=tcpClient.getReplyOnce();
        publishProgress(false);
        if(reply.has(Constants.KEY_USER_ID)){
            newUser.addProperty(Constants.KEY_USER_ID,reply.get(Constants.KEY_USER_ID).getAsString());
        }
        return !reply.isJsonNull() &&reply.get("success").getAsBoolean();
    }

    @Override
    protected void onProgressUpdate(Boolean... values) {
        super.onProgressUpdate(values);
        loading(values[0]);
    }

    @Override
    protected void onPostExecute(Boolean aBoolean) {
        super.onPostExecute(aBoolean);
        if(aBoolean){
            preferenceManager.putString(Constants.KEY_USER_ID, newUser.get(Constants.KEY_USER_ID).getAsString());
            preferenceManager.putString(Constants.KEY_NAME, newUser.get(Constants.KEY_NAME).getAsString());
            preferenceManager.putString(Constants.KEY_IMAGE, newUser.get(Constants.KEY_IMAGE).getAsString());
            context.onBackPressed();
            Toast.makeText(context, "Success to sign up!", Toast.LENGTH_SHORT).show();
        }else{
            Toast.makeText(context, "Sign up fail!", Toast.LENGTH_SHORT).show();
            Log.d("test", "Sign up fail!");
        }
    }

    private void loading(Boolean isLoading) {
        if (isLoading) {
            binding.buttonSignUp.setVisibility(View.INVISIBLE);
            binding.signUpProgressBar.setVisibility(View.VISIBLE);
        } else {
            binding.buttonSignUp.setVisibility(View.VISIBLE);
            binding.signUpProgressBar.setVisibility(View.INVISIBLE);
        }
    }
}
