package com.example.solight;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import androidx.annotation.Nullable;

public class SQLiteDBHelper extends SQLiteOpenHelper {

    public static final String databaseName = "UserList.db";

    public SQLiteDBHelper(@Nullable Context context) {
        super(context, "UserList.db", null, 1);
    }

    @Override
    public void onCreate(SQLiteDatabase MyDatabase) {
        MyDatabase.execSQL("create Table users(id TEXT primary key, password TEXT, authorityLevel TEXT)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase MyDatabase, int oldVersion, int newVersion) {
        MyDatabase.execSQL("drop Table if exists users");
        onCreate(MyDatabase);
    }


    public Boolean insertData(String id, String password, String authorityLevel){
        SQLiteDatabase MyDatabase = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put("id", id);
        contentValues.put("password", password);
        contentValues.put("authorityLevel", authorityLevel);
        long result = MyDatabase.insert("users", null, contentValues);

        if (result == -1){
            return false;
        } else {
            return true;
        }
    }

    public Boolean checkID(String id){
        SQLiteDatabase MyDatabase = this.getWritableDatabase();
        Cursor cursor = MyDatabase.rawQuery("SELECT * from users where id = ?", new String[]{id});

        if (cursor.getCount() > 0){
            return true;
        } else {
            return false;
        }
    }

    public Boolean checkIDPassword(String id, String password){
        SQLiteDatabase MyDatabase = this.getWritableDatabase();
        Cursor cursor = MyDatabase.rawQuery("SELECT * from users where id = ? and password = ?", new String[]{id,  password});

        if (cursor.getCount() > 0){
            return true;
        } else {
            return false;
        }
    }

    public String getID(String id){
        SQLiteDatabase MyDatabase = this.getWritableDatabase();
        Cursor cursor = MyDatabase.rawQuery("SELECT * from users where id = ?", new String[]{id});

        if (cursor != null && cursor.moveToFirst()) {
            String userID = cursor.getString(cursor.getColumnIndexOrThrow("id"));
            cursor.close();
            return userID;
        } else {
            return null;
        }
    }

    public String getAuthorityLevel(String id){
        SQLiteDatabase MyDatabase = this.getWritableDatabase();
        Cursor cursor = MyDatabase.rawQuery("SELECT * from users where id = ?", new String[]{id});

        if (cursor != null && cursor.moveToFirst()) {
            String authorityLevel = cursor.getString(cursor.getColumnIndexOrThrow("authorityLevel"));
            cursor.close();
            return authorityLevel;
        } else {
            return null;
        }
    }

    public boolean deleteUser(String id) {
        SQLiteDatabase MyDatabase = this.getWritableDatabase();
        return MyDatabase.delete("users", "id=?", new String[]{id}) > 0;
    }

    public Cursor getAllUsers(){
        SQLiteDatabase MyDatabase = this.getWritableDatabase();
        Cursor cursor = MyDatabase.rawQuery("SELECT * from users", null);
        return cursor;
    }

    public Boolean incrementAuthorityLevel(String id){
        SQLiteDatabase MyDatabase = this.getWritableDatabase();
        String currentAuthorityLevel = getAuthorityLevel(id);;
        ContentValues contentValues = new ContentValues();
        String newAuthorityLevel;

        switch (currentAuthorityLevel) {
            case "User":
                newAuthorityLevel = "Authorized User";
                break;
            case "Authorized User":
                newAuthorityLevel = "Admin";
                break;
            case "Admin":
                newAuthorityLevel = "User";
                break;
            default:
                return false;
        }

        contentValues.put("authorityLevel", newAuthorityLevel);
        long result = MyDatabase.update("users", contentValues, "id = ?", new String[]{id});

        return result != -1;
    }

    public Boolean checkisAdmin(String id){
        SQLiteDatabase MyDatabase = this.getWritableDatabase();
        Cursor cursor = MyDatabase.rawQuery("SELECT * from users where id = ?", new String[]{id});

        if (cursor != null && cursor.moveToFirst()) {
            String authorityLevel = cursor.getString(cursor.getColumnIndexOrThrow("authorityLevel"));
            cursor.close();

            // Sadece "Admin" yetkisine sahip bir kullanıcı başka bir kullanıcının yetki seviyesini yükseltebilir.
            if (authorityLevel.equals("Admin")) {
                return true;
            } else {
                return false;
            }
        } else {
            return false;
        }
    }

    public Boolean checkCanControlLight(String id){
        SQLiteDatabase MyDatabase = this.getWritableDatabase();
        Cursor cursor = MyDatabase.rawQuery("SELECT * from users where id = ?", new String[]{id});

        if (cursor != null && cursor.moveToFirst()) {
            String authorityLevel = cursor.getString(cursor.getColumnIndexOrThrow("authorityLevel"));
            cursor.close();

            if ((authorityLevel.equals("Admin")) || (authorityLevel.equals("Authorized User"))) {
                return true;
            } else {
                return false;
            }
        } else {
            return false;
        }
    }

    public boolean changePassword(String id, String newPassword) {
        SQLiteDatabase MyDatabase = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put("password", newPassword);
        int rowsAffected = MyDatabase.update("users", contentValues, "id = ?", new String[]{id});
        return rowsAffected > 0;
    }

}
