package  com.example.solight;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.example.solight.databinding.ActivityRegisterBinding;

public class RegisterActivity extends AppCompatActivity {

    ActivityRegisterBinding binding;
    SQLiteDBHelper SQLiteDBHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityRegisterBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        SQLiteDBHelper = new SQLiteDBHelper(this);

        binding.registerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String id = binding.userID.getText().toString();
                String password = binding.password.getText().toString();
                String confirmPassword = binding.confirmPassword.getText().toString();
                String authorityLevel = "User";

                if (isValidID(id) && isValidPassword(password, confirmPassword)) {
                    Boolean checkUserID = SQLiteDBHelper.checkID(id);

                    if (checkUserID == false) {
                        Boolean insert = SQLiteDBHelper.insertData(id, password, authorityLevel);

                        if (insert == true) {
                            Toast.makeText(RegisterActivity.this, "Registration successful!", Toast.LENGTH_SHORT).show();
                            resetTextFields();
                            Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
                            startActivity(intent);
                        } else {
                            Toast.makeText(RegisterActivity.this, "Registration failed!", Toast.LENGTH_SHORT).show();
                            resetTextFields();
                        }
                    } else {
                        Toast.makeText(RegisterActivity.this, "User already exists!", Toast.LENGTH_SHORT).show();
                        resetTextFields();
                    }
                }
            }
        });

        binding.loginRedirectText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
                resetTextFields();
                startActivity(intent);
            }
        });
    }

    private boolean isValidID(String id) {
        if (id.length() < 2 || id.length() > 3 || !id.matches("\\d+")) {
            Toast.makeText(RegisterActivity.this, "ID must be a 2-3 digit number.", Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }

    private boolean isValidPassword(String password, String confirmPassword) {
        if (password.length() < 3 || password.length() > 6) {
            Toast.makeText(RegisterActivity.this, "Password must be a 3-6 character long.", Toast.LENGTH_SHORT).show();
            return false;
        }
        if (!password.equals(confirmPassword)) {
            Toast.makeText(RegisterActivity.this, "Passwords do not match.", Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }

    private void resetTextFields() {
        binding.userID.setText("");
        binding.password.setText("");
        binding.confirmPassword.setText("");
    }
}
