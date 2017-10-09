package com.tanbaron_coffee.myphonecall;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.BaseColumns;

/**
 * Created by Owner on 07/10/2017.
 */

public class SQLite extends SQLiteOpenHelper {
    // If you change the database schema, you must increment the database version.
    public static final int DATABASE_VERSION = 1;
    public static final String DATABASE_NAME = "Schedule.db";

    private static final String SQL_CREATE_ENTRIES =
            "CREATE TABLE " + ScheduleTable.ScheduleEntry.TABLE_NAME + " (" +
                    ScheduleTable.ScheduleEntry._ID + " INTEGER PRIMARY KEY," +
                    ScheduleTable.ScheduleEntry.COLUMN_NAME_TITLE + " TEXT," +
                    ScheduleTable.ScheduleEntry.COLUMN_NAME_SUBTITLE + " TEXT)";

    private static final String SQL_DELETE_ENTRIES =
            "DROP TABLE IF EXISTS " + ScheduleTable.ScheduleEntry.TABLE_NAME;


    public SQLite(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(SQL_CREATE_ENTRIES);
    }
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // This database is only a cache for online data, so its upgrade policy is
        // to simply to discard the data and start over
        db.execSQL(SQL_DELETE_ENTRIES);
        onCreate(db);
    }
    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        onUpgrade(db, oldVersion, newVersion);
    }
}

final class ScheduleTable {
    // To prevent someone from accidentally instantiating the contract class,
    // make the constructor private.
    private ScheduleTable() {}

    /* Inner class that defines the table contents */
    public static class ScheduleEntry implements BaseColumns {
        public static final String TABLE_NAME = "entry";
        public static final String COLUMN_NAME_TITLE = "title";
        public static final String COLUMN_NAME_SUBTITLE = "subtitle";
    }
}