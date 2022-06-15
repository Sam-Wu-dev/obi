package com.example.obi.network;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.example.obi.activities.MainActivity;
import com.example.obi.databinding.ActivitySignInBinding;
import com.example.obi.utilities.Constants;
import com.example.obi.utilities.PreferenceManager;
import com.google.gson.JsonObject;

import java.io.IOException;

public class SignInTask extends AsyncTask<Void,Boolean,Boolean> {
    final private String email;
    final private String password;
    final private TcpClient tcpClient;
    final private Context context;
    final private ActivitySignInBinding binding;
    final private PreferenceManager preferenceManager;
    private JsonObject user;
    public SignInTask(Context context,ActivitySignInBinding binding, String email, String password){
        super();
        this.context=context;
        this.binding=binding;
        this.email=email;
        this.password=password;
        this.tcpClient=TcpClient.getInstance();
        this.preferenceManager=new PreferenceManager(context);
    }
    public void signIn(String email,String password) {
        Log.d("test", "Signing In");
        JsonObject obj = new JsonObject();
        obj.addProperty("email",email);
        obj.addProperty("password",password);
        tcpClient.sendMessage("signIn",obj);
    }

    private void loading(Boolean isLoading) {
        if (isLoading) {
            binding.buttonSignIn.setVisibility(View.INVISIBLE);
            binding.signIpProgressBar.setVisibility(View.VISIBLE);
        } else {
            binding.buttonSignIn.setVisibility(View.VISIBLE);
            binding.signIpProgressBar.setVisibility(View.INVISIBLE);
        }
    }

    @Override
    protected void onProgressUpdate(Boolean... values) {
        super.onProgressUpdate(values);
        loading(values[0]);
    }

    @Override
    protected Boolean doInBackground(Void... voids) {
        publishProgress(true);
        signIn(email,password);
        JsonObject reply= tcpClient.getReplyOnce();
        publishProgress(false);
        user=reply.get("user").getAsJsonObject();
        return !reply.isJsonNull() &&reply.get("success").getAsBoolean();
    }

    @Override
    protected void onPostExecute(Boolean aBoolean) {
        super.onPostExecute(aBoolean);
        if(aBoolean){
            Toast.makeText(context, "Sign in success!", Toast.LENGTH_SHORT).show();
            Log.d("test", "Sign in success!");
            Log.d("test", user.toString());
            preferenceManager.putBoolean(Constants.KEY_IS_SIGNED_IN,true);
            preferenceManager.putString(Constants.KEY_USER_ID,user.get(Constants.KEY_USER_ID).getAsString());
            preferenceManager.putString(Constants.KEY_NAME,user.get(Constants.KEY_NAME).getAsString());
            preferenceManager.putString(Constants.KEY_IMAGE,user.get(Constants.KEY_IMAGE).getAsString());
            preferenceManager.putString(Constants.KEY_EMAIL,user.get(Constants.KEY_EMAIL).getAsString());
            Intent intent = new Intent(context, MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            context.startActivity(intent);
        }else{
            Toast.makeText(context, "Sign in fail!", Toast.LENGTH_SHORT).show();
            Log.d("test", "Sign in fail!");
        }
    }
}