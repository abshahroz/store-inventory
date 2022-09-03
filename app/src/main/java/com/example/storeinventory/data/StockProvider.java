package com.example.storeinventory.data;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;

import com.example.storeinventory.data.StockContract.StockEntry;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class StockProvider extends ContentProvider {

    /** Tag for the log messages */
    public static final String LOG_TAG = StockProvider.class.getSimpleName();
    /* Database Helper Object */
    private StockDbHelper mDbHelper;
    /** URI matcher code for the content URI for the stock table */
    private static final int STOCK = 100;

    /** URI matcher code for the content URI for a single pet in the stock table */
    private static final int STOCK_ID = 101;

    /**
     * UriMatcher object to match a content URI to a corresponding code.
     * The input passed into the constructor represents the code to return for the root URI.
     * It's common to use NO_MATCH as the input for this case.
     */
    private static final UriMatcher sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);

    // Static initializer. This is run the first time anything is called from this class.
    static {
        // The calls to addURI() go here, for all of the content URI patterns that the provider
        // should recognize. All paths added to the UriMatcher have a corresponding code to return
        // when a match is found.
        sUriMatcher.addURI(StockContract.CONTENT_AUTHORITY, StockContract.PATH_STOCK, STOCK);

        sUriMatcher.addURI(StockContract.CONTENT_AUTHORITY, StockContract.PATH_STOCK +"/#", STOCK_ID);
        // TODO: Add 2 content URIs to URI matcher
    }
    /**
     * Initialize the provider and the database helper object.
     */
    @Override
    public boolean onCreate() {
        mDbHelper = new StockDbHelper(getContext());
        return true;
    }

    @Nullable
    @Override
    public Cursor query(@NonNull Uri uri, String[] projection, String selection, String[] selectionArgs,
                        String sortOrder) {

        SQLiteDatabase database = mDbHelper.getReadableDatabase();
        Cursor cursor;

        int match = sUriMatcher.match(uri);
        switch (match)
        {
            case STOCK:
                cursor = database.query(StockContract.StockEntry.TABLE_NAME,
                        projection,null,
                        null,
                        null,null,null);
                break;
            case STOCK_ID:
                // For the STOCK_ID code, extract out the ID from the URI.
                // For an example URI such as "content://com.example.stockinventory/stock/3",
                // the selection will be "_id=?" and the selection argument will be a
                // String array containing the actual ID of 3 in this case.
                //
                // For every "?" in the selection, we need to have an element in the selection
                // arguments that will fill in the "?". Since we have 1 question mark in the
                // selection, we have 1 String in the selection arguments' String array.
                selection = StockEntry._ID + "=?";
                selectionArgs = new String[] {String.valueOf(ContentUris.parseId(uri))} ;

                cursor = database.query(StockEntry.TABLE_NAME,
                        projection,
                        selection,
                        selectionArgs,null,null,null);
                break;
            default:
                throw new IllegalArgumentException("Can not query unknown URI " + uri);
        }

        cursor.setNotificationUri(getContext().getContentResolver(),uri);

        return cursor;
    }


    @Nullable
    @Override
    public Uri insert(@NonNull Uri uri, ContentValues contentValues) {
        final int match = sUriMatcher.match(uri);
        switch (match){
            case STOCK:
                return insertStock(uri,contentValues);
            default:
                throw new IllegalArgumentException("Can not query unknown URI " + uri);
        }
    }

    private Uri insertStock(Uri uri, ContentValues values){
        // Check that the name is not null
        String name = values.getAsString(StockEntry.COLUMN_STOCK_NAME);
        if (TextUtils.isEmpty(name)) {
            throw new IllegalArgumentException("Stock Item requires a name");
        }

        // Check that the quantity is not invalid
        Integer quantity = values.getAsInteger(StockEntry.COLUMN_STOCK_QUANTITY);
        if ( quantity != null && quantity<0){
            throw new IllegalArgumentException("Stock requires valid quantity");
        }

        SQLiteDatabase database = mDbHelper.getWritableDatabase();
        // Insert the new row, returning the primary key value of the new row
        long newRowId = database.insert(StockEntry.TABLE_NAME, null, values);
        // If the ID is -1, then the insertion failed. Log an error and return null.
        if (newRowId == -1) {
            Log.e(LOG_TAG, "Failed to insert row for " + uri);
            return null;
        }
        // If 1 or more rows were deleted, then notify all listeners that the data at the
        // given URI has changed
        else{
            getContext().getContentResolver().notifyChange(uri,null);

            return ContentUris.withAppendedId(uri,newRowId);
        }

    }


    @Override
    public int update(@NonNull Uri uri, ContentValues contentValues, String selection, String[] selectionArgs) {
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case STOCK:
                return updateStock(uri, contentValues, selection, selectionArgs);
            case STOCK_ID: {// For the PET_ID code, extract out the ID from the URI,
                // so we know which row to update. Selection will be "_id=?" and selection
                // arguments will be a String array containing the actual ID.
                selection = StockEntry._ID + "=?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};
                return updateStock(uri, contentValues, selection, selectionArgs);
            }
            default:
                throw new IllegalArgumentException("Update is not supported for " + uri);
        }
    }

    private int updateStock(Uri uri, ContentValues values, String selection, String[] selectionArgs){
        // If the {@link StockEntry#COLUMN_STOCK_NAME} key is present,
        // check that the name value is not null.
        if (values.containsKey(StockEntry.COLUMN_STOCK_NAME)) {
            String name = values.getAsString(StockEntry.COLUMN_STOCK_NAME);
            if (TextUtils.isEmpty(name)) {
                throw new IllegalArgumentException("Pet requires a name");
            }
        }

        // If the {@link StockEntry#COLUMN_STOCK_QUANTITY} key is present,
        // check that the quantity value is valid.
        if (values.containsKey(StockEntry.COLUMN_STOCK_QUANTITY)) {
            // Check that the quantity is greater than or equal to 0 kg
            Integer quantity = values.getAsInteger(StockEntry.COLUMN_STOCK_QUANTITY);
            if (quantity != null && quantity < 0) {
                throw new IllegalArgumentException("Pet requires valid weight");
            }
        }
        // If there are no values to update, then don't try to update the database
        if (values.size() == 0) {
            return 0;
        }
        // Otherwise, get writeable database to update the data
        SQLiteDatabase database = mDbHelper.getWritableDatabase();

        // Returns the number of database rows affected by the update statement
        int rowsUpdated = database.update(StockEntry.TABLE_NAME,values,selection,selectionArgs);
        // If 1 or more rows were updated, then notify all listeners that the data at the
        // given URI has changed
        if (rowsUpdated !=0){
            getContext().getContentResolver().notifyChange(uri,null);
        }
        return  rowsUpdated;
    }


    @Override
    public int delete(@NonNull Uri uri,String selection,String[] selectionArgs) {
        // Get writeable database
        SQLiteDatabase database = mDbHelper.getWritableDatabase();
        int rowsDeleted=0;
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case STOCK:
                // Delete all rows that match the selection and selection args
                rowsDeleted = database.delete(StockEntry.TABLE_NAME, selection, selectionArgs);
                if (rowsDeleted!=0){
                    getContext().getContentResolver().notifyChange(uri,null);
                }

                return rowsDeleted;
            case STOCK_ID:
                // Delete a single row given by the ID in the URI
                selection = StockEntry._ID + "=?";
                selectionArgs = new String[] { String.valueOf(ContentUris.parseId(uri)) };
                // Return the number of rows affected by delete statement
                rowsDeleted = database.delete(StockEntry.TABLE_NAME, selection, selectionArgs);
                if (rowsDeleted!=0){
                    getContext().getContentResolver().notifyChange(uri,null);
                }

                return rowsDeleted;
            default:
                throw new IllegalArgumentException("Deletion is not supported for " + uri);
        }
    }

    @Nullable
    @Override
    public String getType(@NonNull Uri uri) {
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case STOCK:
                return StockEntry.CONTENT_LIST_TYPE;
            case STOCK_ID:
                return StockEntry.CONTENT_ITEM_TYPE;
            default:
                throw new IllegalStateException("Unknown URI " + uri + " with match " + match);
        }
    }


}
