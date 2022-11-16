package com.cache.cachego;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

import com.cache.cachego.databinding.ActivityWelcomeBinding;

import java.util.HashMap;

public class WelcomeActivity extends AppCompatActivity {

    private ActivityWelcomeBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //Check if Shared Preferences are set
        SharedPreferences sp1 = this.getSharedPreferences("Login", MODE_PRIVATE);

        String email = sp1.getString("Email", null);
        String pass = sp1.getString("Password", null);

        if (email == null || pass == null) {
            //Email or Password is null | Invalid Login Saved
        } else {
            //Login Details are set | send to login
            HashMap<String, Object> hashMap = new HashMap<>();
            hashMap.put("email", email);
            hashMap.put("password", pass);

            Intent i = new Intent(getApplicationContext(), LoginActivity.class);
            i.putExtra("loginDetails", hashMap);

            startActivity(i);
        }


        binding = ActivityWelcomeBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(getApplicationContext(), LoginActivity.class);
                startActivity(i);
            }
        });

        binding.btnRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(getApplicationContext(), RegisterActivity.class);
                startActivity(i);
            }
        });
    }
}