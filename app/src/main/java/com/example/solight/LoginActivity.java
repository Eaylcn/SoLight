package com.example.solight;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.example.solight.databinding.ActivityLoginBinding;

public class LoginActivity extends AppCompatActivity {

    ActivityLoginBinding binding;
    SQLiteDBHelper SQLiteDBHelper;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityLoginBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        SQLiteDBHelper = new SQLiteDBHelper(this);

        binding.loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String id = binding.userID.getText().toString();
                String password = binding.password.getText().toString();
                if (id.equals("") && password.equals("")){
                    Toast.makeText(LoginActivity.this, "All fields are mandatory!", Toast.LENGTH_SHORT).show();
                } else {
                    Boolean checkCredentials = SQLiteDBHelper.checkIDPassword(id, password);

                    if (checkCredentials == true){
                        Toast.makeText(LoginActivity.this, "Login Successful!", Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                        SharedPreferences sharedPref = getSharedPreferences("user_info", MODE_PRIVATE);
                        SharedPreferences.Editor editor = sharedPref.edit();
                        editor.putString("loginned_user_id", SQLiteDBHelper.getID(id));
                        editor.apply();
                        resetTextFields();
                        startActivity(intent);
                    } else {
                        Toast.makeText(LoginActivity.this, "Your ID or password is wrong!", Toast.LENGTH_SHORT).show();
                        resetTextFields();
                    }
                }
            }
        });
        binding.registerRedirectText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), RegisterActivity.class);
                resetTextFields();
                startActivity(intent);
            }
        });
    }

    private void resetTextFields(){
        binding.userID.setText("");
        binding.password.setText("");
    }
}