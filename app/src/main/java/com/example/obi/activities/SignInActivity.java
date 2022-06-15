package com.example.obi.activities;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.widget.Toast;
import com.example.obi.databinding.ActivitySignInBinding;
import com.example.obi.network.ConnectTask;
import com.example.obi.network.SignInTask;
import com.example.obi.utilities.PreferenceManager;


public class SignInActivity extends AppCompatActivity {

    private ActivitySignInBinding binding;
    private PreferenceManager preferenceManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.preferenceManager = new PreferenceManager(getApplicationContext());
        //autoSignIn();
        ConnectTask connectTask = new ConnectTask();
        connectTask.execute();
        binding = ActivitySignInBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        setListeners();
    }

    private void signIn(){
        SignInTask signInTask = new SignInTask(this,binding,binding.inputEmail.getText().toString(),binding.inputPassword.getText().toString());
        signInTask.execute();
    }

    private void setListeners() {
        binding.textCreateNewAccount.setOnClickListener(v ->
                startActivity(new Intent(getApplicationContext(), SignUpActivity.class)));
        binding.buttonSignIn.setOnClickListener(view -> {
                if(isValidSignInDetails()){
                    signIn();
                }
        });
    }

    private void notifyToast(String s) {
        Toast.makeText(getApplicationContext(), s, Toast.LENGTH_SHORT).show();
    }

    private boolean isValidSignInDetails(){
        if(binding.inputEmail.getText().toString().trim().isEmpty()){
            notifyToast("Please enter your email");
            return false;
        }else if(!Patterns.EMAIL_ADDRESS.matcher(binding.inputEmail.getText().toString()).matches()){
            notifyToast("Please enter valid email");
            return false;
        }else if(binding.inputPassword.getText().toString().trim().isEmpty()){
            notifyToast("Please enter your password");
            return false;
        }
        else{
            return true;
        }
    }
}