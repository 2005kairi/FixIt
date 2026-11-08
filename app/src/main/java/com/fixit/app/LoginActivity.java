package com.fixit.app;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class LoginActivity extends AppCompatActivity {

    public static final String PREFS_NAME = "fixit_prefs";
    public static final String KEY_USER_ID = "user_id";
    public static final String KEY_ROLE = "role";

    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    private EditText etUsername;
    private EditText etPassword;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        etUsername = findViewById(R.id.etUsername);
        etPassword = findViewById(R.id.etPassword);
        Button btnLogin = findViewById(R.id.btnLogin);
        TextView tvGoSignup = findViewById(R.id.tvGoSignup);

        btnLogin.setOnClickListener(v -> attemptLogin());
        tvGoSignup.setOnClickListener(v ->
                startActivity(new Intent(this, SignupActivity.class)));
    }

    private void attemptLogin() {
        String username = etUsername.getText().toString().trim();
        String password = etPassword.getText().toString();

        if (username.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        executor.execute(() -> {
            User user = AppDatabase.getInstance(getApplicationContext())
                    .userDao().login(username, password);
            runOnUiThread(() -> {
                if (user == null) {
                    Toast.makeText(this, "Username or password is incorrect",
                            Toast.LENGTH_SHORT).show();
                } else {
                    onLoginSuccess(user);
                }
            });
        });
    }

    private void onLoginSuccess(User user) {
        getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                .edit()
                .putInt(KEY_USER_ID, user.userId)
                .putString(KEY_ROLE, user.role)
                .apply();

        Class<?> home = User.ROLE_LANDLORD.equals(user.role)
                ? LandlordHomeActivity.class
                : TenantHomeActivity.class;
        startActivity(new Intent(this, home));
        finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        executor.shutdown();
    }
}
