package com.example.obi.activities;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Base64;
import android.util.Patterns;
import android.widget.Toast;
import com.example.obi.R;
import com.example.obi.databinding.ActivitySignUpBinding;
import com.example.obi.network.SignUpTask;
import com.example.obi.utilities.PreferenceManager;
import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;

public class SignUpActivity extends AppCompatActivity {

    private ActivitySignUpBinding binding;
    private String encodedImage;
    private PreferenceManager preferenceManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySignUpBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        preferenceManager = new PreferenceManager(getApplicationContext());
        setListeners();
    }

    private void setListeners() {
        binding.textSignIn.setOnClickListener(v -> onBackPressed());
        binding.buttonSignUp.setOnClickListener(view -> {
            if (isValidSignUpDetails()) {
                if(encodedImage==null){
                    defaultImage();
                }
                signUp();
            }
        });
        binding.imageProfile.setOnClickListener(view -> {
            Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            pickImage.launch(intent);
        });
    }

    private void signUp() {
        SignUpTask signUpTask = new SignUpTask(this,binding,binding.inputName.getText().toString(),
                binding.inputEmail.getText().toString(),binding.inputPassword.getText().toString(),encodedImage);
        signUpTask.execute();
    }

    private final ActivityResultLauncher<Intent> pickImage = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getData() != null) {
                    Uri imageUri = result.getData().getData();
                    try {
                        InputStream inputStream = getContentResolver().openInputStream(imageUri);
                        Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
                        binding.imageProfile.setImageBitmap(bitmap);
                        encodedImage = encodeImage(bitmap);
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    }
                }
            }
    );
    private void defaultImage(){
        Bitmap icon = BitmapFactory.decodeResource(this.getResources(),
                R.drawable.user_icon_default);
        binding.imageProfile.setImageBitmap(icon);
        encodedImage = encodeImage(icon);
    }
    private String encodeImage(Bitmap bitmap) {
        int previewWidth = 150;
        int previewHeight = bitmap.getHeight() * previewWidth / bitmap.getWidth();
        Bitmap previewBitmap = Bitmap.createScaledBitmap(bitmap, previewWidth, previewHeight, false);
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        previewBitmap.compress(Bitmap.CompressFormat.JPEG, 50, byteArrayOutputStream);
        byte[] bytes = byteArrayOutputStream.toByteArray();
        return Base64.encodeToString(bytes, Base64.DEFAULT);
    }

    private void notifyToast(String s) {
        Toast.makeText(getApplicationContext(), s, Toast.LENGTH_SHORT).show();
    }

    private boolean isValidSignUpDetails() {
        if (binding.inputName.getText().toString().isEmpty()) {
            notifyToast("Please enter your name");
            return false;
        } else if (binding.inputEmail.getText().toString().isEmpty()) {
            notifyToast("Please enter your email");
            return false;
        } else if (!Patterns.EMAIL_ADDRESS.matcher(binding.inputEmail.getText().toString()).matches()) {
            notifyToast("Please enter valid Email");
            return false;
        } else if (binding.inputPassword.getText().toString().isEmpty()) {
            notifyToast("Please enter your password");
            return false;
        } else if (binding.inputConfirmPassword.getText().toString().isEmpty()) {
            notifyToast("Please enter confirm password");
            return false;
        } else if (!binding.inputPassword.getText().toString().equals(binding.inputConfirmPassword.getText().toString())) {
            notifyToast("Confirm password must be same with password");
            return false;
        } else {
            return true;
        }
    }
}