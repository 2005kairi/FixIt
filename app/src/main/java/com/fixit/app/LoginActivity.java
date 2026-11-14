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

        seedDemoUsers();

        btnLogin.setOnClickListener(v -> attemptLogin());
        tvGoSignup.setOnClickListener(v ->
                startActivity(new Intent(this, SignupActivity.class)));
    }

    // Demo accounts and requests, inserted once when the database is empty.
    private void seedDemoUsers() {
        executor.execute(() -> {
            AppDatabase db = AppDatabase.getInstance(getApplicationContext());
            UserDao users = db.userDao();
            RequestDao requests = db.requestDao();
            if (users.countUsers() > 0) return;

            int landlord1 = addUser(users, "Landlord 1", "landlord1", "landlord123",
                    User.ROLE_LANDLORD, null, null);
            int landlord2 = addUser(users, "Landlord 2", "landlord2", "landlord123",
                    User.ROLE_LANDLORD, null, null);

            int tenant1 = addUser(users, "Tenant 1", "tenant1", "tenant123",
                    User.ROLE_TENANT, "A-101", landlord1);
            int tenant2 = addUser(users, "Tenant 2", "tenant2", "tenant123",
                    User.ROLE_TENANT, "A-102", landlord1);
            int tenant3 = addUser(users, "Tenant 3", "tenant3", "tenant123",
                    User.ROLE_TENANT, "B-201", landlord2);

            addRequest(requests, tenant1, "Kitchen sink is leaking",
                    "Water drips from the pipe under the kitchen sink every time the tap is on. The cabinet floor is getting wet.",
                    Request.STATUS_PENDING, 2, null);
            addRequest(requests, tenant1, "Air conditioner not cooling",
                    "The bedroom AC runs but only blows warm air since yesterday evening.",
                    Request.STATUS_DONE, 12, null);
            addRequest(requests, tenant1, "Front door lock is stuck",
                    "The key turns only halfway and the door is hard to lock at night.",
                    Request.STATUS_PENDING, 1, null);

            addRequest(requests, tenant2, "Bathroom light keeps flickering",
                    "The ceiling light in the bathroom flickers and sometimes turns off by itself.",
                    Request.STATUS_DONE, 8, null);
            addRequest(requests, tenant2, "Repaint the living room walls",
                    "I would like the living room walls repainted in a lighter color.",
                    Request.STATUS_REJECTED, 5, Request.REJECT_REASONS[0]);

            addRequest(requests, tenant3, "Water heater is not working",
                    "There has been no hot water for two days. The heater makes a clicking sound but does not heat.",
                    Request.STATUS_PENDING, 1, null);
            addRequest(requests, tenant3, "Broken window latch",
                    "The latch on the bedroom window broke off and the window does not close tightly.",
                    Request.STATUS_PENDING, 3, null);
            addRequest(requests, tenant3, "Wardrobe door came off",
                    "The left door of the built-in wardrobe came off its hinge.",
                    Request.STATUS_REJECTED, 9,
                    Request.REJECT_REASONS[1] + Request.REASON_SEPARATOR + Request.REJECT_REASONS[3]);
        });
    }

    private int addUser(UserDao dao, String name, String username, String password,
                        String role, String unit, Integer landlordId) {
        User user = new User();
        user.name = name;
        user.username = username;
        user.password = password;
        user.email = username + "@example.com";
        user.role = role;
        user.unitNumber = unit;
        user.landlordId = landlordId;
        return (int) dao.insert(user);
    }

    private void addRequest(RequestDao dao, int tenantId, String title, String description,
                            String status, int daysAgo, String rejectReasons) {
        Request request = new Request();
        request.tenantId = tenantId;
        request.title = title;
        request.description = description;
        request.status = status;
        request.createdAt = System.currentTimeMillis() - daysAgo * 24L * 60L * 60L * 1000L;
        request.rejectReasons = rejectReasons;
        dao.insert(request);
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
