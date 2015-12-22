package bonnier.hvadsynes;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.widget.Toast;

import java.lang.ref.WeakReference;
import java.util.ArrayList;

import bonnier.android.Registry;
import bonnier.android.models.CategoryModel;
import bonnier.android.models.QuestionModel;
import bonnier.hvadsynes.db.HvadSynesDbHelper;

/**
 * Created by sessingo on 05/09/15.
 */
public class Settings {

    public static final String SHARED_PREF = "hvadsynes";

    SharedPreferences settings;
    SharedPreferences.Editor settingsEditor;
    HvadSynesDbHelper db;

    private String name;
    private String email;
    private int age;
    private int gender;
    private ArrayList<CategoryModel> categories;

    private static Settings instance;

    public Settings(Context context) {
        db = new HvadSynesDbHelper(context);
        settings = context.getSharedPreferences(Settings.SHARED_PREF, 0);
        settingsEditor = settings.edit();

        SQLiteDatabase read = db.getReadableDatabase();

        Cursor nameCursor = read.rawQuery("SELECT value FROM data WHERE key = 'name' LIMIT 1", null);
        if(nameCursor.moveToFirst()) {
            this.name = nameCursor.getString(0);
        }

        Cursor emailCursor = read.rawQuery("SELECT value FROM data WHERE key = 'email' LIMIT 1", null);
        if(emailCursor.moveToFirst()) {
            this.email = emailCursor.getString(0);
        }

        Cursor ageCursor = read.rawQuery("SELECT value FROM data WHERE key = 'age' LIMIT 1", null);
        if(ageCursor.moveToFirst()) {
            this.age = ageCursor.getInt(0);
        }

        Cursor genderCursor = read.rawQuery("SELECT value FROM data WHERE key = 'gender' LIMIT 1", null);
        if(genderCursor.moveToFirst()) {
            this.gender = genderCursor.getInt(0);
        }
    }

    public ArrayList<CategoryModel> getCategories() {
        return categories;
    }

    public void setCategories(ArrayList<CategoryModel> categories) {
        this.categories = categories;
    }

    public boolean getSetup() {
        return settings.getBoolean("setup", false);
    }

    public void setSetup(boolean value) {
        settingsEditor.putBoolean("setup", value);
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getGender() {
        return gender;
    }

    public void setGender(int gender) {
        this.gender = gender;
    }

    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        this.age = age;
    }

    public void save() {
        settingsEditor.commit();

        SQLiteDatabase write = db.getWritableDatabase();

        write.execSQL("INSERT OR REPLACE INTO " + HvadSynesDbHelper.TABLE_DATA + " (key, value) VALUES (?, ?)", new String[] {"name", name });

        write.execSQL("INSERT OR REPLACE INTO " + HvadSynesDbHelper.TABLE_DATA + " (key, value) VALUES (?, ?)", new String[] {"email", email });

        write.execSQL("INSERT OR REPLACE INTO " + HvadSynesDbHelper.TABLE_DATA + " (key, value) VALUES (?, ?)", new String[] {"age", Integer.toString(age) });

        write.execSQL("INSERT OR REPLACE INTO " + HvadSynesDbHelper.TABLE_DATA + " (key, value) VALUES (?, ?)", new String[] {"gender", Integer.toString(gender) });

    }

    public static Settings getInstance(Context context) {
        if(instance == null) {
            instance = new Settings(context);
        }
        return instance;
    }

}
