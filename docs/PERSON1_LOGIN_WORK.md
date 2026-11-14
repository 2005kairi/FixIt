# Person 1 — Login (complete work)

Everything Person 1 owns in the FixIt app, in one place.

## What it does

- Login screen with username and password fields, a Login button and a "Sign Up" link.
- Empty fields: toast "Please fill in all fields".
- Wrong credentials: toast "Username or password is incorrect".
- Success: saves `user_id` and `role` to SharedPreferences (`fixit_prefs`), then opens the Tenant Home or Landlord Home screen.
- `User` entity with `user_id`, `name`, `username`, `password`, `email`, `role`, `unit_number` (username is unique).
- `UserDao` with `insert`, `login`, `getByUsername` (for Sign Up's uniqueness check) and `getById` (for Request Detail's tenant lookup).

## Test accounts (temporary)

Created automatically the first time the app opens. Remove the `seedDemoUsers()` code once Sign Up works.

| Role | Username | Password |
|---|---|---|
| Tenant | `tenant1` | `tenant123` |
| Landlord | `landlord1` | `landlord123` |

## For the rest of the team

- Read the logged-in user with `LoginActivity.PREFS_NAME`, `LoginActivity.KEY_USER_ID` and `LoginActivity.KEY_ROLE`.
- Roles are `User.ROLE_TENANT` and `User.ROLE_LANDLORD`.
- Passwords are stored as plain text, as the spec has them.

---

## LoginActivity.java

Path: `app/src/main/java/com/fixit/app/LoginActivity.java`

```java
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

    // TEMPORARY: demo accounts so Login can be tested before Sign Up exists. Remove before final submission.
    private void seedDemoUsers() {
        executor.execute(() -> {
            UserDao dao = AppDatabase.getInstance(getApplicationContext()).userDao();
            if (dao.getByUsername("tenant1") == null) {
                User tenant = new User();
                tenant.name = "Demo Tenant";
                tenant.username = "tenant1";
                tenant.password = "tenant123";
                tenant.email = "tenant1@example.com";
                tenant.role = User.ROLE_TENANT;
                tenant.unitNumber = "A-101";
                dao.insert(tenant);
            }
            if (dao.getByUsername("landlord1") == null) {
                User landlord = new User();
                landlord.name = "Demo Landlord";
                landlord.username = "landlord1";
                landlord.password = "landlord123";
                landlord.email = "landlord1@example.com";
                landlord.role = User.ROLE_LANDLORD;
                dao.insert(landlord);
            }
        });
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
```

## activity_login.xml

Path: `app/src/main/res/layout/activity_login.xml`

```xml
<?xml version="1.0" encoding="utf-8"?>
<ScrollView xmlns:android="http://schemas.android.com/apk/res/android"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    android:fillViewport="true">

    <LinearLayout
        android:layout_width="match_parent"
        android:layout_height="match_parent"
        android:gravity="center_vertical"
        android:orientation="vertical"
        android:padding="24dp">

        <TextView
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            android:layout_marginBottom="32dp"
            android:gravity="center"
            android:text="FixIt"
            android:textColor="@color/fixit_primary"
            android:textSize="40sp"
            android:textStyle="bold" />

        <EditText
            android:id="@+id/etUsername"
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            android:autofillHints="username"
            android:hint="Username"
            android:imeOptions="actionNext"
            android:inputType="text"
            android:maxLines="1"
            android:minHeight="48dp" />

        <EditText
            android:id="@+id/etPassword"
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            android:layout_marginTop="12dp"
            android:autofillHints="password"
            android:hint="Password"
            android:imeOptions="actionDone"
            android:inputType="textPassword"
            android:maxLines="1"
            android:minHeight="48dp" />

        <Button
            android:id="@+id/btnLogin"
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            android:layout_marginTop="24dp"
            android:text="Login"
            android:textSize="16sp" />

        <TextView
            android:id="@+id/tvGoSignup"
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            android:layout_marginTop="16dp"
            android:clickable="true"
            android:focusable="true"
            android:gravity="center"
            android:minHeight="48dp"
            android:text="Don't have an account? Sign Up"
            android:textColor="@color/fixit_primary"
            android:textSize="14sp" />

    </LinearLayout>
</ScrollView>
```

## User.java

Path: `app/src/main/java/com/fixit/app/User.java`

```java
package com.fixit.app;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity(tableName = "users", indices = {@Index(value = "username", unique = true)})
public class User {

    public static final String ROLE_TENANT = "tenant";
    public static final String ROLE_LANDLORD = "landlord";

    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "user_id")
    public int userId;

    public String name;

    public String username;

    public String password;

    public String email;

    public String role;

    @ColumnInfo(name = "unit_number")
    public String unitNumber;
}
```

## UserDao.java

Path: `app/src/main/java/com/fixit/app/UserDao.java`

```java
package com.fixit.app;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

@Dao
public interface UserDao {

    @Insert
    long insert(User user);

    @Query("SELECT * FROM users WHERE username = :username AND password = :password LIMIT 1")
    User login(String username, String password);

    @Query("SELECT * FROM users WHERE username = :username LIMIT 1")
    User getByUsername(String username);

    @Query("SELECT * FROM users WHERE user_id = :userId LIMIT 1")
    User getById(int userId);
}
```

