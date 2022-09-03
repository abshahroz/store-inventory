package com.example.storeinventory.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.example.storeinventory.data.StockContract.StockEntry;

public class StockDbHelper extends SQLiteOpenHelper {

    public static final int DATABASE_VERSION = 1;
    public static final String DATABASE_NAME = "store.db";

    public StockDbHelper(Context context){ super(context,DATABASE_NAME,null,DATABASE_VERSION);}

    @Override
    public void onCreate(SQLiteDatabase db) {
        String SQL_CREATE_STOCK_TABLE = "CREATE TABLE " +
                StockEntry.TABLE_NAME + "(" +
                StockEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                StockEntry.COLUMN_STOCK_NAME + " TEXT NOT NULL, " +
                StockEntry.COLUMN_STOCK_BRAND + " TEXT, " +
                StockEntry.COLUMN_STOCK_DESCRIPTION + " TEXT, " +
                StockEntry.COLUMN_STOCK_QUANTITY + " DOUBLE NOT NULL DEFAULT 0, " +
                StockEntry.COLUMN_STOCK_UPDATED_DATE + " TEXT NOT NULL" +
                ");"
                ;
        db.execSQL(SQL_CREATE_STOCK_TABLE);
        Log.v("StockDbHelper", "Statement to create table is " + SQL_CREATE_STOCK_TABLE);

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        String SQL_DELETE_STOCK_TABLE =
                "DROP TABLE IF EXISTS " + StockEntry.TABLE_NAME;

        db.execSQL(SQL_DELETE_STOCK_TABLE);
        onCreate(db);
    }
}
