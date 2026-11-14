package com.fixit.app;

import android.database.sqlite.SQLiteConstraintException;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class SignupActivity extends AppCompatActivity {

    private final ExecutorService executor =
            Executors.newSingleThreadExecutor();

    private EditText etName;
    private EditText etSignupUsername;
    private EditText etEmail;
    private EditText etSignupPassword;
    private EditText etUnit;
    private RadioGroup rgRole;
    private Button btnSignup;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        etName = findViewById(R.id.etName);
        etSignupUsername = findViewById(R.id.etSignupUsername);
        etEmail = findViewById(R.id.etEmail);
        etSignupPassword = findViewById(R.id.etSignupPassword);
        etUnit = findViewById(R.id.etUnit);
        rgRole = findViewById(R.id.rgRole);
        btnSignup = findViewById(R.id.btnSignup);

        // Tenant is selected by default in activity_signup.xml.
        rgRole.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.rbLandlord) {
                etUnit.setVisibility(View.GONE);
            } else {
                etUnit.setVisibility(View.VISIBLE);
            }
        });

        btnSignup.setOnClickListener(v -> attemptSignup());
    }

    private void attemptSignup() {
        String name = etName.getText().toString().trim();
        String username = etSignupUsername.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        String password = etSignupPassword.getText().toString();
        String unitNumber = etUnit.getText().toString().trim();

        int selectedRoleId = rgRole.getCheckedRadioButtonId();
        if (selectedRoleId == -1) {
            Toast.makeText(this, "Please select a role",
                    Toast.LENGTH_SHORT).show();
            return;
        }

        boolean isTenant = selectedRoleId == R.id.rbTenant;

        if (name.isEmpty() || username.isEmpty() || email.isEmpty()
                || password.isEmpty() || (isTenant && unitNumber.isEmpty())) {
            Toast.makeText(this, "Please fill in all fields",
                    Toast.LENGTH_SHORT).show();
            return;
        }

        if (!email.contains("@")) {
            Toast.makeText(this, "Enter a valid email",
                    Toast.LENGTH_SHORT).show();
            return;
        }

        String role = isTenant ? User.ROLE_TENANT : User.ROLE_LANDLORD;

        btnSignup.setEnabled(false);

        executor.execute(() -> {
            UserDao dao = AppDatabase.getInstance(
                    getApplicationContext()).userDao();

            try {
                // Check username before inserting.
                if (dao.getByUsername(username) != null) {
                    runOnUiThread(() -> {
                        btnSignup.setEnabled(true);
                        Toast.makeText(this, "Username already taken",
                                Toast.LENGTH_SHORT).show();
                    });
                    return;
                }

                User user = new User();
                user.name = name;
                user.username = username;
                user.email = email;
                user.password = password;
                user.role = role;
                user.unitNumber = isTenant ? unitNumber : null;

                dao.insert(user);

                runOnUiThread(() -> {
                    Toast.makeText(this, "Account created",
                            Toast.LENGTH_SHORT).show();
                    finish(); // Return to the Login screen.
                });

            } catch (SQLiteConstraintException e) {
                // Also protects against a duplicate username inserted
                // between the username check and the insert.
                runOnUiThread(() -> {
                    btnSignup.setEnabled(true);
                    Toast.makeText(this, "Username already taken",
                            Toast.LENGTH_SHORT).show();
                });
            } catch (Exception e) {
                runOnUiThread(() -> {
                    btnSignup.setEnabled(true);
                    Toast.makeText(this, "Could not create account",
                            Toast.LENGTH_SHORT).show();
                });
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        executor.shutdown();
    }
}
