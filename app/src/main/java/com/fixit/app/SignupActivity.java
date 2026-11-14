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
    private EditText etLandlordUsername;
    private RadioGroup rgRole;
    private Button btnSignup;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        findViewById(R.id.btnBack).setOnClickListener(v -> finish());

        etName = findViewById(R.id.etName);
        etSignupUsername = findViewById(R.id.etSignupUsername);
        etEmail = findViewById(R.id.etEmail);
        etSignupPassword = findViewById(R.id.etSignupPassword);
        etUnit = findViewById(R.id.etUnit);
        etLandlordUsername = findViewById(R.id.etLandlordUsername);
        rgRole = findViewById(R.id.rgRole);
        btnSignup = findViewById(R.id.btnSignup);

        // Tenant is selected by default in activity_signup.xml.
        rgRole.setOnCheckedChangeListener((group, checkedId) -> {
            int visibility = checkedId == R.id.rbLandlord ? View.GONE : View.VISIBLE;
            etUnit.setVisibility(visibility);
            etLandlordUsername.setVisibility(visibility);
        });

        btnSignup.setOnClickListener(v -> attemptSignup());
    }

    private void attemptSignup() {
        String name = etName.getText().toString().trim();
        String username = etSignupUsername.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        String password = etSignupPassword.getText().toString();
        String unitNumber = etUnit.getText().toString().trim();
        String landlordUsername = etLandlordUsername.getText().toString().trim();

        int selectedRoleId = rgRole.getCheckedRadioButtonId();
        if (selectedRoleId == -1) {
            Toast.makeText(this, "Please select a role",
                    Toast.LENGTH_SHORT).show();
            return;
        }

        boolean isTenant = selectedRoleId == R.id.rbTenant;

        if (name.isEmpty() || username.isEmpty() || email.isEmpty()
                || password.isEmpty()
                || (isTenant && (unitNumber.isEmpty() || landlordUsername.isEmpty()))) {
            Toast.makeText(this, "Please fill in all fields",
                    Toast.LENGTH_SHORT).show();
            return;
        }

        if (!email.contains("@")) {
            Toast.makeText(this, "Enter a valid email",
                    Toast.LENGTH_SHORT).show();
            return;
        }

        int letters = 0;
        int digits = 0;
        for (char c : password.toCharArray()) {
            if (Character.isLetter(c)) {
                letters++;
            } else if (Character.isDigit(c)) {
                digits++;
            }
        }
        if (!Character.isLetter(password.charAt(0)) || letters < 4 || digits < 3) {
            Toast.makeText(this,
                    "Password must start with a letter and have at least 4 letters and 3 numbers",
                    Toast.LENGTH_LONG).show();
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

                Integer landlordId = null;
                if (isTenant) {
                    User landlord = dao.getLandlordByUsername(landlordUsername);
                    if (landlord == null) {
                        runOnUiThread(() -> {
                            btnSignup.setEnabled(true);
                            Toast.makeText(this, "Landlord not found",
                                    Toast.LENGTH_SHORT).show();
                        });
                        return;
                    }
                    landlordId = landlord.userId;
                }

                User user = new User();
                user.landlordId = landlordId;
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
