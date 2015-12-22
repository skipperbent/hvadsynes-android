package bonnier.hvadsynes.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by sessingo on 05/09/15.
 */
public class HvadSynesDbHelper extends SQLiteOpenHelper {
    // If you change the database schema, you must increment the database version.
    public static final int DATABASE_VERSION = 1;
    public static final String DATABASE_NAME = "hvadsynes.db";

    // Data table
    public static final String TABLE_DATA = "data";
    public static final String DATA_ID = "id";
    public static final String DATA_KEY = "key";
    public static final String DATA_VALUE = "value";

    private static final String CREATE_TABLE_DATA = "CREATE TABLE "
            + TABLE_DATA + "(" + DATA_ID + " INTEGER PRIMARY KEY," + DATA_KEY
            + " VARCHAR(255)," + DATA_VALUE + " LONGTEXT)";

    private static final String CREATE_TABLE_DATA_INDEX = "CREATE INDEX index_"+ TABLE_DATA +" ON "+ TABLE_DATA +" ("+ DATA_KEY +");";

    // Activity table
    public static final String TABLE_ACTIVITY = "activity";
    public static final String ACTIVITY_ID = "id";
    public static final String ACTIVITY_QUESTION_ID = "question_id";
    public static final String ACTIVITY_QUESTION_IDENTIFIER_ID = "identifier_id";

    private static final String CREATE_TABLE_ACTIVITY = "CREATE TABLE "
            + TABLE_ACTIVITY + "(" + ACTIVITY_ID + " INTEGER PRIMARY KEY," + ACTIVITY_QUESTION_ID
            + " INTEGER," + ACTIVITY_QUESTION_IDENTIFIER_ID + " INTEGER)";

    private static final String CREATE_TABLE_ACTIVITY_INDEX = "CREATE INDEX index_"+ TABLE_ACTIVITY +" ON "+ TABLE_ACTIVITY +" ("+ ACTIVITY_ID +", "+ ACTIVITY_QUESTION_IDENTIFIER_ID +");";

    public HvadSynesDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }
    public void onCreate(SQLiteDatabase db) {
        // Create data table
        db.execSQL(CREATE_TABLE_DATA);
        db.execSQL(CREATE_TABLE_DATA_INDEX);

        // Create activity table
        db.execSQL(CREATE_TABLE_ACTIVITY);
        db.execSQL(CREATE_TABLE_ACTIVITY_INDEX);
    }
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

        // TODO: This should be changed to prevent data loss upon update

        db.execSQL("DROP TABLE IF EXISTS " + TABLE_DATA);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_ACTIVITY);

        onCreate(db);
    }
    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        onUpgrade(db, oldVersion, newVersion);
    }
}