package com.example.solight;

import static android.content.ContentValues.TAG;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class ProfileActivity extends AppCompatActivity implements LightStatusCallback{

    Boolean isLightON;
    EditText password, lightID;
    Button setPassword, setLightID, logoutButton;
    String loginnedUser, selectedLightID;
    TextView userIDText, userLevelText, idText;
    // Declare database helper
    private SQLiteDBHelper databaseHelper;

    @Override
    public void onError(Exception e) {

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        databaseHelper = new SQLiteDBHelper(this);

        password = findViewById(R.id.password);
        lightID = findViewById(R.id.lightID);
        setPassword = findViewById(R.id.setPassword);
        setLightID = findViewById(R.id.setLightID);
        logoutButton = findViewById(R.id.logoutButton);
        userIDText = findViewById(R.id.userIDText);
        userLevelText = findViewById(R.id.userLevelText);
        idText = findViewById(R.id.checkCanChangeID);

        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.getMenu().findItem(R.id.navigation_profile).setChecked(true);

        SharedPreferences sharedPref = getSharedPreferences("user_info", MODE_PRIVATE);

        loginnedUser = sharedPref.getString("loginned_user_id", "default value");

        SharedPreferences lightPref = getSharedPreferences("light_status_info", MODE_PRIVATE);
        SharedPreferences.Editor editLight = lightPref.edit();
        isLightON =  sharedPref.getBoolean("light_status", false);

        FirebaseFirestore lightDB = FirebaseFirestore.getInstance();

        selectedLightID = sharedPref.getString("current_light_id", "default value");

        MenuItem lightIcon = bottomNavigationView.getMenu().findItem(R.id.navigation_light);

        if(isLightON != null){
            isLightON =  lightPref.getBoolean("light_status", false);
            if(!isLightON){
                lightIcon.setIcon(R.drawable.ic_light_off);
            }else{
                lightIcon.setIcon(R.drawable.ic_light_on);
            }
        }

        userIDText.setText(loginnedUser);
        userLevelText.setText(databaseHelper.getAuthorityLevel(loginnedUser));

        if(!selectedLightID.equals("default value")){
            idText.setText("Your light id is : " + selectedLightID);
        }

        setPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String newPassword = password.getText().toString();
                boolean success = databaseHelper.changePassword(loginnedUser, newPassword);
                if (success) {
                    Toast.makeText(ProfileActivity.this, "Your password successfully changed", Toast.LENGTH_SHORT).show();
                    password.setText("");
                } else {
                    Toast.makeText(ProfileActivity.this, "Your password doesn't change please try again.", Toast.LENGTH_SHORT).show();
                    password.setText("");
                }
            }
        });

        setLightID.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String newLightID = lightID.getText().toString();
                SharedPreferences sharedPref = getSharedPreferences("light_id", MODE_PRIVATE);
                SharedPreferences.Editor editor = sharedPref.edit();
                editor.putString("current_light_id", newLightID);
                editor.apply();

                selectedLightID = sharedPref.getString("current_light_id", "default value");

                // Firestore'da belirtilen ID'ye sahip ışığı kontrol et
                DocumentReference lightRef = lightDB.collection("Lights").document(selectedLightID);
                lightRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (task.isSuccessful()) {
                            DocumentSnapshot document = task.getResult();
                            if (document.exists()) {
                                Toast.makeText(ProfileActivity.this, "Current Light is : " + selectedLightID, Toast.LENGTH_SHORT).show();
                                lightID.setText("");
                                getLightStatus(lightDB, selectedLightID, lightPref, editLight, ProfileActivity.this, lightIcon);

                                isLightON = lightPref.getBoolean("light_status", false);

                                idText.setText("Your light id is : " + selectedLightID);
                            } else {
                                // Belirtilen ID'ye sahip bir ışık yok, işlemleri geri al
                                Toast.makeText(ProfileActivity.this, "Invalid Light ID", Toast.LENGTH_SHORT).show();
                                lightID.setText("");
                            }
                        } else {
                            // Firestore erişiminde hata oluştu, işlemleri geri al
                            Toast.makeText(ProfileActivity.this, "Error accessing Firestore", Toast.LENGTH_SHORT).show();
                            lightID.setText("");
                        }
                    }
                });
            }
        });

        logoutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SharedPreferences sharedPref = getSharedPreferences("user_info", MODE_PRIVATE);
                SharedPreferences.Editor editor = sharedPref.edit();
                editor.putString("loginned_user_id", null);
                editor.apply();
                Intent intentHome = new Intent(getApplicationContext(), LoginActivity.class);
                startActivity(intentHome);
            }
        });

        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int itemId = item.getItemId();
                if (itemId == R.id.navigation_home) {
                    item.setChecked(true); // Profil öğesinin seçili olduğunu işaretleyin
                    Intent intentProfile = new Intent(getApplicationContext(), MainActivity.class);
                    startActivity(intentProfile);
                } else if (itemId == R.id.navigation_profile) {
                    Toast.makeText(ProfileActivity.this, "You are already on profile page!", Toast.LENGTH_SHORT).show();
                } else if (itemId == R.id.navigation_light) {
                    if(!databaseHelper.getAuthorityLevel(loginnedUser).equals("User") && selectedLightID != null && !selectedLightID.equals("default value")) {
                        isLightON = lightPref.getBoolean("light_status", false);
                        if (!isLightON) {
                            item.setIcon(R.drawable.ic_light_on); // alternative icon
                            editLight.putBoolean("light_status", true);
                            editLight.apply();
                        } else {
                            item.setIcon(R.drawable.ic_light_off); // original icon
                            isLightON = false;
                            editLight.putBoolean("light_status", false);
                            editLight.apply();
                        }
                        isLightON = lightPref.getBoolean("light_status", false);
                        updateLightStatusInFirestore(lightDB, isLightON, selectedLightID);
                    }
                }
                return true;
            }
        });
    }

    private void getLightStatus(FirebaseFirestore lightDB, String documentId, SharedPreferences lightPref, SharedPreferences.Editor editLight, LightStatusCallback callback, MenuItem lightIcon) {
        DocumentReference docRef = lightDB.collection("Lights").document(documentId);
        docRef.get().addOnSuccessListener(documentSnapshot -> {
            if (documentSnapshot.exists()) {
                Boolean isLightOn = documentSnapshot.getBoolean("isLightOn");
                if (isLightOn != null) {
                    editLight.putBoolean("light_status", isLightON);
                    editLight.apply();
                    isLightON = lightPref.getBoolean("light_status", false);
                    callback.onLightStatusReceived(isLightOn, lightIcon);
                }
            } else {
                Log.d(TAG, "Document not found");
            }
        }).addOnFailureListener(e -> {
            Log.d(TAG, "Failed to retrieve light status: " + e.getMessage());
            callback.onError(e);
        }).addOnCompleteListener(task -> {
            // Update the value in SharedPreferences after the Firestore query is complete
        });
    }

    private void updateLightStatusInFirestore(FirebaseFirestore lightDB, boolean newStatus, String selectedLightID) {
        DocumentReference docRef = lightDB.collection("Lights").document(selectedLightID);
        docRef.update("isLightOn", newStatus)
                .addOnSuccessListener(aVoid -> {
                    // Başarı durumunda yapılacak işlemleri burada gerçekleştirin
                    Toast.makeText(ProfileActivity.this, "Light status updated successfully", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    // Hata durumunda yapılacak işlemleri burada gerçekleştirin
                    Log.d(TAG, "Failed to update light status: " + e.getMessage());
                    Toast.makeText(ProfileActivity.this, "Failed to update light status", Toast.LENGTH_SHORT).show();
                });
    }

    @Override
    public void onLightStatusReceived(boolean isLightOn) {

    }

    @Override
    public void onLightStatusReceived(boolean isLightOn, MenuItem lightIcon) {
        isLightON = isLightOn;

        if (!isLightON) {
            lightIcon.setIcon(R.drawable.ic_light_off);
        } else {
            lightIcon.setIcon(R.drawable.ic_light_on);
        }
    }
}