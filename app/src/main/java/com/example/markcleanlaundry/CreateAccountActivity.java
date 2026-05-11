package com.example.markcleanlaundry;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;
import com.google.android.material.textfield.TextInputEditText;
import androidx.appcompat.app.AppCompatActivity;

public class CreateAccountActivity extends AppCompatActivity {

    Button btnSignUp, btnGoToSignIn;
    TextInputEditText etFullName, etUsername, etPhone, etPassword;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_account);

        btnSignUp     = findViewById(R.id.btnSignUp);
        btnGoToSignIn = findViewById(R.id.btnGoToSignIn);
        etFullName    = findViewById(R.id.etFullName);
        etUsername    = findViewById(R.id.etUsername);
        etPhone       = findViewById(R.id.etPhone);
        etPassword    = findViewById(R.id.etPassword);

        btnSignUp.setOnClickListener(v -> {
            String fullName = etFullName.getText().toString().trim();
            String username = etUsername.getText().toString().trim();
            String phone    = etPhone.getText().toString().trim();
            String password = etPassword.getText().toString().trim();

            if (fullName.isEmpty() || username.isEmpty() || phone.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show();
                return;
            }

            DatabaseHelper db = new DatabaseHelper(this);

            if (db.usernameExists(username)) {
                Toast.makeText(this, "Username already taken", Toast.LENGTH_SHORT).show();
                return;
            }

            long result = db.registerUser(username, fullName, "", phone, password, "");

            if (result != -1) {
                Toast.makeText(this, "Account created! Please sign in.", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(this, SignInActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
                finish();
            } else {
                Toast.makeText(this, "Registration failed. Try again.", Toast.LENGTH_SHORT).show();
            }
        });

        btnGoToSignIn.setOnClickListener(v -> {
            startActivity(new Intent(this, SignInActivity.class));
            finish();
        });
    }
}