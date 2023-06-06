package com.example.solight;

import static android.content.ContentValues.TAG;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.mikhaellopez.circularprogressbar.CircularProgressBar;

import java.util.ArrayList;
import java.util.Date;
import java.util.Map;

public class MainActivity extends AppCompatActivity implements LightStatusCallback, LightDataCallback {

    TextView activityHeader, activitySubHeader, activityInformation, chartHeader;
    Button activityButton, turnOnOffButton;
    ImageButton option1, option2, option3, option4, option5, option6, option7;
    BarChart barChart;
    CircularProgressBar circularProgressBar;
    RecyclerView userList;
    String selectedUser, selectedOption = "option7", loginnedUser, selectedLightID, durationString = "";
    Boolean isLightON;
    int current = 0, voltage = 0, batteryLevel = 0, resistance = 0, temperature = 0, totalHours = 0;
    private SQLiteDBHelper databaseHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        SharedPreferences lightPref = getSharedPreferences("light_status_info", MODE_PRIVATE);
        SharedPreferences sharedPref = getSharedPreferences("user_info", MODE_PRIVATE);
        SharedPreferences lightIDPref = getSharedPreferences("light_id", MODE_PRIVATE);

        SharedPreferences.Editor editLight = lightPref.edit();
        databaseHelper = new SQLiteDBHelper(this);

        FirebaseFirestore lightDB = FirebaseFirestore.getInstance();

        selectedLightID = lightIDPref.getString("current_light_id", "default value");


        if (selectedLightID.equals("default value")){
            selectedLightID = "001";
        }

        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.getMenu().findItem(R.id.navigation_home).setChecked(true);

        Drawable defaultTurnOffIcon = ContextCompat.getDrawable(MainActivity.this, R.drawable.ic_light_off);
        Drawable defaultTurnOnIcon = ContextCompat.getDrawable(MainActivity.this, R.drawable.ic_light_on);

        activityHeader = findViewById(R.id.activityHeader);
        activitySubHeader = findViewById(R.id.activitySubHeader);
        activityInformation = findViewById(R.id.activityInformation);
        activityButton = findViewById(R.id.activityButton);
        turnOnOffButton = findViewById(R.id.turnOnOffButton);
        chartHeader = findViewById(R.id.chartHeader);
        barChart = findViewById(R.id.barChart);
        circularProgressBar = findViewById(R.id.circularProgressBar);
        userList = findViewById(R.id.userList);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        userList.setLayoutManager(layoutManager);
        userList.setHasFixedSize(true);

        option1 = findViewById(R.id.option1);
        option2 = findViewById(R.id.option2);
        option3 = findViewById(R.id.option3);
        option4 = findViewById(R.id.option4);
        option5 = findViewById(R.id.option5);
        option6 = findViewById(R.id.option6);
        option7 = findViewById(R.id.option7);

        loginnedUser = sharedPref.getString("loginned_user_id", "default value");

        MenuItem lightIcon = bottomNavigationView.getMenu().findItem(R.id.navigation_light);

        getLightData(lightDB, selectedLightID, this);
        getLightStatus(lightDB, selectedLightID, lightPref, editLight, this, defaultTurnOffIcon, defaultTurnOnIcon, lightIcon);

        activityHeader.setText("Light Status of " + selectedLightID);

        View.OnClickListener optionClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Define new icons for option5
                Drawable deleteIcon = ContextCompat.getDrawable(MainActivity.this, R.drawable.ic_delete);
                Drawable upgradeIcon = ContextCompat.getDrawable(MainActivity.this, R.drawable.ic_upgrade);
                Drawable defaultUpdateIcon = ContextCompat.getDrawable(MainActivity.this, R.drawable.ic_update);

                // Reset background for all options to default
                option1.setBackgroundResource(R.drawable.menu_item_bg);
                option2.setBackgroundResource(R.drawable.menu_item_bg);
                option3.setBackgroundResource(R.drawable.menu_item_bg);
                option4.setBackgroundResource(R.drawable.menu_item_bg);
                option5.setBackgroundResource(R.drawable.menu_item_bg);
                option6.setBackgroundResource(R.drawable.menu_item_bg);
                option7.setBackgroundResource(R.drawable.menu_item_bg);

                // Reset Buttons
                if (isLightON != null){
                    if(isLightON){
                        turnOnOffButton.setText("Turn Off");
                        turnOnOffButton.setCompoundDrawablesWithIntrinsicBounds(defaultTurnOnIcon, null, null, null);
                    }else{
                        turnOnOffButton.setText("Turn On");
                        turnOnOffButton.setCompoundDrawablesWithIntrinsicBounds(defaultTurnOffIcon, null, null, null);
                    }
                }
                activityButton.setText("Update");
                activityButton.setCompoundDrawablesWithIntrinsicBounds(defaultUpdateIcon, null, null, null);

                // Change background for the clicked option
                v.setBackgroundResource(R.drawable.menu_item_bg2);

                String newSubHeaderText, newInformationText, newChartHeaderText;

                int id = v.getId();
                if (id == R.id.option1) {
                    newSubHeaderText = "Current is";
                    newInformationText = current + "A";
                    newChartHeaderText = "Current Chart";
                    selectedOption = "option1";
                    barChart.setVisibility(View.VISIBLE);
                    userList.setVisibility(View.GONE);
                    circularProgressBar.setVisibility(View.GONE);
                    setLightStatus(defaultTurnOffIcon, defaultTurnOnIcon, lightIcon, lightPref, editLight);
                } else if (id == R.id.option2) {
                    newSubHeaderText = "Voltage is";
                    newInformationText = voltage + "V";
                    newChartHeaderText = "Voltage Chart";
                    selectedOption = "option2";
                    barChart.setVisibility(View.VISIBLE);
                    userList.setVisibility(View.GONE);
                    circularProgressBar.setVisibility(View.GONE);
                    setLightStatus(defaultTurnOffIcon, defaultTurnOnIcon, lightIcon, lightPref, editLight);
                } else if (id == R.id.option3) {
                    newSubHeaderText = "Resistance is";
                    newInformationText = resistance + "R";
                    newChartHeaderText = "Resistance Chart";
                    selectedOption = "option3";
                    barChart.setVisibility(View.VISIBLE);
                    userList.setVisibility(View.GONE);
                    circularProgressBar.setVisibility(View.GONE);
                    setLightStatus(defaultTurnOffIcon, defaultTurnOnIcon, lightIcon, lightPref, editLight);
                } else if (id == R.id.option4) {
                    newSubHeaderText = "Temperature is";
                    newInformationText = temperature + "'C";
                    newChartHeaderText = "Temperature Chart";
                    selectedOption = "option4";
                    barChart.setVisibility(View.VISIBLE);
                    userList.setVisibility(View.GONE);
                    circularProgressBar.setVisibility(View.GONE);
                    setLightStatus(defaultTurnOffIcon, defaultTurnOnIcon, lightIcon, lightPref, editLight);
                } else if (id == R.id.option5) {
                    newSubHeaderText = "Your authorization is";
                    newInformationText = databaseHelper.getAuthorityLevel(loginnedUser);
                    newChartHeaderText = "List of users";
                    barChart.setVisibility(View.GONE);
                    circularProgressBar.setVisibility(View.GONE);
                    userList.setVisibility(View.VISIBLE);
                    selectedOption = "option5";
                    turnOnOffButton.setText("Delete");
                    activityButton.setText("Upgrade");
                    turnOnOffButton.setCompoundDrawablesWithIntrinsicBounds(deleteIcon, null, null, null);
                    activityButton.setCompoundDrawablesWithIntrinsicBounds(upgradeIcon, null, null, null);

                    // Query all users and create an ArrayList to store User objects
                    Cursor cursor = databaseHelper.getAllUsers();
                    ArrayList<User> users = new ArrayList<>();
                    if (cursor.moveToFirst()) {
                        do {
                            String userId = cursor.getString(cursor.getColumnIndexOrThrow("id"));
                            String authorityLevel = cursor.getString(cursor.getColumnIndexOrThrow("authorityLevel"));
                            users.add(new User(userId, authorityLevel));
                        } while (cursor.moveToNext());
                    }
                    cursor.close();

                    // Create a UserAdapter and set it on the userList (RecyclerView)
                    UserAdapter userAdapter = new UserAdapter(users, new UserAdapter.OnUserClickListener() {
                        @Override
                        public void onUserClick(int position) {
                            selectedUser = users.get(position).getId();
                        }
                    });
                    userList.setAdapter(userAdapter);
                }  else if (id == R.id.option6) {
                    newSubHeaderText = "Light battery level is";
                    newInformationText = "%" + batteryLevel;
                    //int batteryPercentage = 99;
                    newChartHeaderText = "Battery Level Chart";
                    selectedOption = "option6";
                    barChart.setVisibility(View.GONE);
                    userList.setVisibility(View.GONE);
                    circularProgressBar.setProgress(batteryLevel);
                    circularProgressBar.setVisibility(View.VISIBLE);
                    setLightStatus(defaultTurnOffIcon, defaultTurnOnIcon, lightIcon, lightPref, editLight);
                } else if (id == R.id.option7) {
                    newSubHeaderText = "It's working for";
                    newInformationText = durationString;
                    newChartHeaderText = "Light's Duration Chart";
                    selectedOption = "option7";
                    barChart.setVisibility(View.VISIBLE);
                    userList.setVisibility(View.GONE);
                    circularProgressBar.setVisibility(View.GONE);
                    setLightStatus(defaultTurnOffIcon, defaultTurnOnIcon, lightIcon, lightPref, editLight);
                } else {
                    return;
                }

                //Call Update Automatically
                if(!selectedOption.equals("option5") && !selectedOption.equals("option6")){
                    activityButton.performClick();
                }

                activitySubHeader.setText(newSubHeaderText);
                activityInformation.setText(newInformationText);
                chartHeader.setText(newChartHeaderText);
            }
        };

        option1.setOnClickListener(optionClickListener);
        option2.setOnClickListener(optionClickListener);
        option3.setOnClickListener(optionClickListener);
        option4.setOnClickListener(optionClickListener);
        option5.setOnClickListener(optionClickListener);
        option6.setOnClickListener(optionClickListener);
        option7.setOnClickListener(optionClickListener);

        turnOnOffButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (selectedOption.equals("option5") && selectedUser != null){
                    if(!selectedUser.equals(loginnedUser) && databaseHelper.checkisAdmin(loginnedUser)) {
                        //Toast.makeText(MainActivity.this, "Selected User: " + selectedUser + "Loginned User: " + loginnedUser, Toast.LENGTH_LONG).show();
                        databaseHelper.deleteUser(selectedUser);
                        Toast.makeText(MainActivity.this, "User: " + selectedUser + " deleted successfully!", Toast.LENGTH_SHORT).show();
                        selectedUser = null;
                        option5.performClick();
                    }else if(selectedUser.equals(loginnedUser)){
                        Toast.makeText(MainActivity.this, "You cannot delete your account!", Toast.LENGTH_SHORT).show();
                    }else{
                        Toast.makeText(MainActivity.this, "You don't have to permission to do this!", Toast.LENGTH_SHORT).show();
                    }
                }else if (databaseHelper.checkCanControlLight(loginnedUser) && !selectedOption.equals("option5")){
                    isLightON = lightPref.getBoolean("light_status", false);
                    if (!isLightON) {
                        editLight.putBoolean("light_status", true);
                        editLight.apply();
                        setLightStatus(defaultTurnOffIcon, defaultTurnOnIcon, lightIcon, lightPref, editLight);
                    } else{
                        editLight.putBoolean("light_status", false);
                        editLight.apply();
                        setLightStatus(defaultTurnOffIcon, defaultTurnOnIcon, lightIcon, lightPref, editLight);
                    }
                    updateLightStatusInFirestore(lightDB, isLightON, selectedLightID);
                }else if(!selectedOption.equals("option5")){
                    Toast.makeText(MainActivity.this, "You don't have to permission to do this!", Toast.LENGTH_SHORT).show();
                }else if(selectedOption.equals("option5") && selectedUser == null) {
                    Toast.makeText(MainActivity.this, "You have to choose someone", Toast.LENGTH_SHORT).show();
                }
            }
        });

        activityButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getLightData(lightDB, selectedLightID, MainActivity.this);

                if(selectedOption.equals("option5") && selectedUser != null){
                    //String currentLevel =  databaseHelper.getAuthorityLevel(selectedUser);
                    if (selectedUser.equals(loginnedUser) && databaseHelper.checkisAdmin(loginnedUser)) {
                        Toast.makeText(MainActivity.this, "You cannot increase or decrease the Admin level", Toast.LENGTH_SHORT).show();
                        selectedUser = null;
                    }else if (databaseHelper.checkisAdmin(loginnedUser)){
                        databaseHelper.incrementAuthorityLevel(selectedUser);
                        //Toast.makeText(MainActivity.this, "User Level: " + currentLevel + " Selected User is: " + selectedUser, Toast.LENGTH_SHORT).show();
                        selectedUser = null;
                        option5.performClick();
                    } else {
                        Toast.makeText(MainActivity.this, "You don't have to permission for this", Toast.LENGTH_SHORT).show();
                        selectedUser = null;
                    }
                }else if(selectedOption.equals("option5") && selectedUser == null) {
                    Toast.makeText(MainActivity.this, "You have to choose someone", Toast.LENGTH_SHORT).show();
                }else {
                    String labelofChart = "null";
                    ArrayList<BarEntry> dataVals = new ArrayList<>();

                    if (selectedOption == "option1") {
                        dataVals.add(new BarEntry(1, current));
                        activityInformation.setText(current + "A");
                        labelofChart = "Current";
                    } else if (selectedOption == "option2") {
                        dataVals.add(new BarEntry(1, voltage));
                        activityInformation.setText(voltage + "V");
                        labelofChart = "Voltage";
                    } else if (selectedOption == "option3") {
                        dataVals.add(new BarEntry(0, resistance));
                        activityInformation.setText(resistance + "R");
                        labelofChart = "Resistance";
                    } else if (selectedOption == "option4") {
                        dataVals.add(new BarEntry(0, temperature));
                        activityInformation.setText(temperature + "'C'");
                        labelofChart = "Temperature";
                    } else if (selectedOption == "option6") {
                        circularProgressBar.setProgress(batteryLevel);
                        activityInformation.setText("%" + batteryLevel);
                    } else if (selectedOption == "option7") {
                        dataVals.add(new BarEntry(0, totalHours));
                        activityInformation.setText(durationString);
                        labelofChart = "Hours";
                    } else {
                        return;
                    }

                    BarDataSet chartDataSet1 = new BarDataSet(dataVals, labelofChart);
                    BarData chartData = new BarData(chartDataSet1);
                    barChart.setFitBars(true);
                    barChart.getDescription().setText("");
                    barChart.setData(chartData);
                    barChart.animateY(2000);
                }
            }
        });

        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int itemId = item.getItemId();
                if (itemId == R.id.navigation_home) {
                    Toast.makeText(MainActivity.this, "You are already on mainpage!", Toast.LENGTH_SHORT).show();
                } else if (itemId == R.id.navigation_profile) {
                    item.setChecked(true); // Profil öğesinin seçili olduğunu işaretleyin
                    Intent intentProfile = new Intent(getApplicationContext(), ProfileActivity.class);
                    startActivity(intentProfile);
                } else if (itemId == R.id.navigation_light) {
                    if(!databaseHelper.getAuthorityLevel(loginnedUser).equals("User") && selectedLightID != null && !selectedOption.equals("option5")) {
                        isLightON = lightPref.getBoolean("light_status", false);
                        if (!isLightON) {
                            item.setIcon(R.drawable.ic_light_on); // alternative icon
                            editLight.putBoolean("light_status", true);
                            editLight.apply();
                        } else {
                            item.setIcon(R.drawable.ic_light_off); // original icon
                            editLight.putBoolean("light_status", false);
                            editLight.apply();
                        }
                        setLightStatus(defaultTurnOffIcon, defaultTurnOnIcon, lightIcon, lightPref, editLight);
                        updateLightStatusInFirestore(lightDB, isLightON, selectedLightID);
                    }
                }
                return true;
            }
        });
        option7.performClick();
    }

    private void setLightStatus(Drawable turnOffIcon, Drawable turnOnIcon, MenuItem lightIcon, SharedPreferences pref, SharedPreferences.Editor editLight) {
        if(!selectedOption.equals("option5")) {
            if (isLightON == null) {
                editLight.putBoolean("light_status", false);
                editLight.apply();
            } else {
                isLightON = pref.getBoolean("light_status", false);
                if (!isLightON) {
                    lightIcon.setIcon(R.drawable.ic_light_off);
                    turnOnOffButton.setText("Turn On");
                    turnOnOffButton.setCompoundDrawablesWithIntrinsicBounds(turnOffIcon, null, null, null);
                } else {
                    lightIcon.setIcon(R.drawable.ic_light_on);
                    turnOnOffButton.setText("Turn Off");
                    turnOnOffButton.setCompoundDrawablesWithIntrinsicBounds(turnOnIcon, null, null, null);
                }
            }
        }
    }

    private void updateLightStatusInFirestore(FirebaseFirestore lightDB, boolean newStatus, String selectedLightID) {
        DocumentReference docRef = lightDB.collection("Lights").document(selectedLightID);
        docRef.update("isLightOn", newStatus)
                .addOnSuccessListener(aVoid -> {
                    // Başarı durumunda yapılacak işlemleri burada gerçekleştirin
                    Toast.makeText(MainActivity.this, "Light status updated successfully", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    // Hata durumunda yapılacak işlemleri burada gerçekleştirin
                    Log.d(TAG, "Failed to update light status: " + e.getMessage());
                    Toast.makeText(MainActivity.this, "Failed to update light status", Toast.LENGTH_SHORT).show();
                });
    }

    private void getLightStatus(FirebaseFirestore lightDB, String documentId, SharedPreferences lightPref, SharedPreferences.Editor editLight, LightStatusCallback callback, Drawable turnOffIcon, Drawable turnOnIcon, MenuItem lightIcon) {
        DocumentReference docRef = lightDB.collection("Lights").document(documentId);
        docRef.get().addOnSuccessListener(documentSnapshot -> {
            if (documentSnapshot.exists()) {
                Boolean isLightOn = documentSnapshot.getBoolean("isLightOn");
                if (isLightOn != null) {
                    callback.onLightStatusReceived(isLightOn);
                    editLight.putBoolean("light_status", isLightON);
                    editLight.apply();
                    isLightON = lightPref.getBoolean("light_status", false);
                    setLightStatus(turnOffIcon, turnOnIcon, lightIcon, lightPref, editLight);
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

    private void getLightData(FirebaseFirestore lightDB, String documentId, LightDataCallback callback) {
        DocumentReference docRef = lightDB.collection("Lights").document(documentId);
        docRef.get().addOnSuccessListener(documentSnapshot -> {
            if (documentSnapshot.exists()) {
                Map<String, Object> data = documentSnapshot.getData();
                if (data != null) {
                    int batteryLevel = ((Long) data.get("BatteryLevel")).intValue();
                    int current = ((Long) data.get("Current")).intValue();
                    int resistance = ((Long) data.get("Resistance")).intValue();
                    int temperature = ((Long) data.get("Temperature")).intValue();
                    int voltage = ((Long) data.get("Voltage")).intValue();
                    Timestamp turnOffTime = documentSnapshot.getTimestamp("turnOffTime");
                    Timestamp turnOnTime = documentSnapshot.getTimestamp("turnOnTime");

                    callback.onLightDataReceived(batteryLevel, current, resistance, temperature, voltage, turnOnTime, turnOffTime);
                }
            } else {
                Log.d(TAG, "Document not found");
            }
        }).addOnFailureListener(e -> {
            Log.d(TAG, "Failed to retrieve light data: " + e.getMessage());
            callback.onError(e);
        });
    }

    private void calculateLightDuration(Timestamp turnOffTime, Timestamp turnOnTime) {
        Date turnOffDate = turnOffTime.toDate();
        Date turnOnDate = turnOnTime.toDate();
        long durationMillis = turnOffDate.getTime() - turnOnDate.getTime();
        long durationSeconds = durationMillis / 1000;
        long durationMinutes = durationSeconds / 60;
        long durationHours = durationMinutes / 60;

        totalHours = (int) durationHours;
        int days = totalHours / 24; // Gün sayısı
        int hours = totalHours % 24; // Saat değeri (24 saatten fazla ise 24'e mod alınır)
        int minutes = (int) (durationMinutes % 60); // Dakika değeri

        if (days > 0) {
            durationString = String.format("%d d %02d h %02d m", days, hours, minutes); // Örneğin: 2 gün 10:30
        } else {
            durationString = String.format("%02d h %02d m", hours, minutes); // Örneğin: 25:30
        }
    }

    @Override
    public void onLightStatusReceived(boolean isLightOn) {
        isLightON = isLightOn;
        activityButton.performClick();
        //Toast.makeText(MainActivity.this, "Your status: " + isLightON, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onLightStatusReceived(boolean isLightOn, MenuItem item) {

    }

    @Override
    public void onLightDataReceived(int batteryLevel, int current, int resistance, int temperature, int voltage, Timestamp turnOnTime, Timestamp turnOffTime) {
        Log.d(TAG, "Battery Level: " + batteryLevel);
        Log.d(TAG, "Current: " + current);
        Log.d(TAG, "Resistance: " + resistance);
        Log.d(TAG, "Temperature: " + temperature);
        Log.d(TAG, "Voltage: " + voltage);
        this.batteryLevel = batteryLevel;
        this.current = current;
        this.resistance = resistance;
        this.temperature = temperature;
        this.voltage = voltage;

        calculateLightDuration(turnOffTime, turnOnTime);
        //activityButton.performClick();
    }

    @Override
    public void onError(Exception e) {
        // Hata durumunda yapılacak işlemleri burada gerçekleştirin
        Log.d(TAG, "Failed to retrieve light data: " + e.getMessage());
    }
}
