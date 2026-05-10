package com.example.markcleanlaundry;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;
import com.google.android.material.textfield.TextInputEditText;
import androidx.appcompat.app.AppCompatActivity;

public class SignInActivity extends AppCompatActivity {

    private static final String ADMIN_USERNAME = "admin";
    private static final String ADMIN_PASSWORD = "admin123";

    Button btnSignIn, btnCreateAccount;
    TextInputEditText etUsername, etPassword;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_in);

        OrderManager.init(this);
        NotificationManager.init(this);

        btnSignIn        = findViewById(R.id.btnSignIn);
        btnCreateAccount = findViewById(R.id.btnCreateAccount);
        etUsername       = findViewById(R.id.etUsername);
        etPassword       = findViewById(R.id.etPassword);

        btnSignIn.setOnClickListener(v -> {
            String username = etUsername.getText().toString().trim();
            String password = etPassword.getText().toString().trim();

            if (username.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Please enter username and password", Toast.LENGTH_SHORT).show();
                return;
            }

            // Admin check
            if (username.equals(ADMIN_USERNAME) && password.equals(ADMIN_PASSWORD)) {
                startActivity(new Intent(this, AdminHomeActivity.class));
                return;
            }

            // Regular user
            DatabaseHelper db = new DatabaseHelper(this);
            Cursor cursor = db.loginUser(username, password);

            if (cursor.moveToFirst()) {
                // Credentials matched — save session
                int    userId   = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_USER_ID));
                String uname    = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_USER_USERNAME));
                String fullName = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_USER_FULLNAME));
                cursor.close();

                SessionManager session = new SessionManager(this);
                session.saveSession(userId, uname);
                session.saveFullName(fullName != null ? fullName : "");

                Intent intent = new Intent(this, HomeActivity.class);
                intent.putExtra("username", uname);
                intent.putExtra("isNewUser", false);
                startActivity(intent);
            } else {
                cursor.close();
                Toast.makeText(this, "Incorrect username or password", Toast.LENGTH_SHORT).show();
            }
        });

        btnCreateAccount.setOnClickListener(v ->
                startActivity(new Intent(this, CreateAccountActivity.class)));
    }
}