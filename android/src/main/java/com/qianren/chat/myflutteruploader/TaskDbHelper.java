package com.qianren.chat.myflutteruploader;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.qianren.chat.myflutteruploader.TaskContract.TaskEntry;

public class TaskDbHelper extends SQLiteOpenHelper {
    public static final int DATABASE_VERSION = 2;
    public static final String DATABASE_NAME = "upload_tasks.db";

    private static TaskDbHelper instance = null;

    private static final String SQL_CREATE_ENTRIES =
            "CREATE TABLE " + TaskEntry.TABLE_NAME + " (" +
                    TaskEntry._ID + " INTEGER PRIMARY KEY," +
                    TaskEntry.COLUMN_NAME_UPLOAD_TASK_ID + " VARCHAR(256), " +
                    TaskEntry.COLUMN_NAME_UPLOAD_STATUS + " INTEGER DEFAULT 0, " +
                    TaskEntry.COLUMN_NAME_UPLOAD_PROGRESS + " INTEGER DEFAULT 0, " +
                    TaskEntry.COLUMN_NAME_UPLOAD_URL + " TEXT, " +
                    TaskEntry.COLUMN_NAME_UPLOAD_RESPONSE + " TEXT, " +
                    TaskEntry.COLUMN_NAME_LOCALE_PATH + " TEXT, " +
                    TaskEntry.COLUMN_NAME_FILE_TYPE + " INTEGER DEFAULT 0, " +
                    TaskEntry.COLUMN_NAME_FIELD_NAME + " TEXT, " +
                    TaskEntry.COLUMN_NAME_METHOD + " VARCHAR(8), " +
                    TaskEntry.COLUMN_NAME_HEADERS + " TEXT, " +
                    TaskEntry.COLUMN_NAME_DATA + " TEXT, " +
                    TaskEntry.COLUMN_NAME_REQUEST_TIMEOUT_IN_SECONDS + " INTEGER DEFAULT 0, " +
                    TaskEntry.COLUMN_NAME_SHOW_NOTIFICATION + " TINYINT DEFAULT 0, " +
                    TaskEntry.COLUMN_NAME_BINARY_UPLOAD + " TINYINT DEFAULT 0, " +
                    TaskEntry.COLUMN_NAME_RESUMABLE + " TINYINT DEFAULT 0, " +
                    TaskEntry.COLUMN_NAME_UPLOAD_TIME_CREATED + " INTEGER DEFAULT 0," +
                    TaskEntry.COLUMN_NAME_COMPRESS_TASK_ID + " VARCHAR(256), " +
                    TaskEntry.COLUMN_NAME_COMPRESS_STATUS + " INTEGER DEFAULT 0, " +
                    TaskEntry.COLUMN_NAME_COMPRESS_PROGRESS + " INTEGER DEFAULT 0, " +
                    TaskEntry.COLUMN_NAME_COMPRESS_PATH + " TEXT, " +
                    TaskEntry.COLUMN_NAME_COMPRESS_TIME_CREATED + " INTEGER DEFAULT 0"
                    + ")";

    private static final String SQL_DELETE_ENTRIES =
            "DROP TABLE IF EXISTS " + TaskEntry.TABLE_NAME;


    public static TaskDbHelper getInstance(Context ctx) {

        // Use the application context, which will ensure that you
        // don't accidentally leak an Activity's context.
        // See this article for more information: http://bit.ly/6LRzfx
        if (instance == null) {
            instance = new TaskDbHelper(ctx.getApplicationContext());
        }
        return instance;
    }


    private TaskDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(SQL_CREATE_ENTRIES);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL(SQL_DELETE_ENTRIES);
        onCreate(db);
    }

    @Override
    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        onUpgrade(db, oldVersion, newVersion);
    }
}
