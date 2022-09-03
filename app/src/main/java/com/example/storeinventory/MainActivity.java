package com.example.storeinventory;

import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.app.LoaderManager;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import com.example.storeinventory.data.StockContract.StockEntry;

import com.example.storeinventory.data.StockDbHelper;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.Calendar;
import java.util.Date;

public class MainActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    private StockDbHelper mDbHelper;

    private StockCursorAdapter stockAdapter;
    // Identifies a particular Loader being used in this class
    private final int STOCK_LOADER=0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Setup FAB to open EditorActivity
        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent editorIntent = new Intent(MainActivity.this,EditorActivity.class);
                startActivity(editorIntent);
            }
        });

        // Find a reference to the {@link ListView} in the layout
        ListView stockListView = findViewById(R.id.list);
        //Initialize the Cursor Adapter
        stockAdapter = new StockCursorAdapter(this,null);
        // Find the empty view in activity_main.xml
        View emptyView = findViewById(R.id.empty_view);
        // set empty view on the ListView, so that it only shows when the list has 0 items.
        stockListView.setEmptyView(emptyView);
        // Attach cursor adapter to the ListView
        stockListView.setAdapter(stockAdapter);
        // Set listner on listview items
        stockListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                // Append database id of clicked pet to CONTENT_URI
                Uri currentPetUri = ContentUris.withAppendedId(StockEntry.CONTENT_URI,id);

                // Create Intent to start editor activity,
                Intent editorIntent = new Intent(MainActivity.this,EditorActivity.class);
                // intent will send uri as data
                editorIntent.setData(currentPetUri);
                // Start Intent
                startActivity(editorIntent);
            }
        });
        // To access our database, we instantiate our subclass of SQLiteOpenHelper
        // and pass the context, which is the current activity.
        mDbHelper = new StockDbHelper(this);

        getLoaderManager().initLoader(STOCK_LOADER,null,this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu options from the res/menu/menu_catalog.xml file.
        // This adds menu items to the app bar.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // User clicked on a menu option in the app bar overflow menu
        switch (item.getItemId()) {
            // Respond to a click on the "Insert Dummy Data" menu option
            case R.id.action_insert_dummy_data:
                insertStock();
                return true;
            // Respond to a click on the "Delete all" menu option
            case R.id.action_delete_all_entries:
                showDeleteConfirmationDialog();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void showDeleteConfirmationDialog(){
        // Create an AlertDialog.Builder and set the message, and click listeners
        // for the positive and negative buttons on the dialog.
        AlertDialog.Builder builder =new AlertDialog.Builder(this);
        builder.setMessage(R.string.delete_all_dialog_msg);
        builder.setPositiveButton(R.string.action_delete, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int id) {
                // User clicked the "Delete All" button, so delete the pet.
                deleteAllStockItems();
            }
        });
        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int id) {
                // User clicked the "Cancel" button, so dismiss the dialog
                // and continue editing the pet.
                if (dialogInterface != null) {
                    dialogInterface.dismiss();
                }
            }
        });

        // Create and show the AlertDialog
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    private void insertStock(){
        // Get current date
        Calendar cal = Calendar.getInstance();
        int currentYear = cal.get(Calendar.YEAR);
        int month = cal.get(Calendar.MONTH);
        int currentMonth = month+1;
        int currentDay = cal.get(Calendar.DAY_OF_MONTH);
        // Create date String to save current Date
        String date = currentDay + "-" + currentMonth + "-" + currentYear;

        // Create a new map of values, where column names are the keys
        ContentValues contentValues = new ContentValues();
        contentValues.put(StockEntry.COLUMN_STOCK_NAME, "Oil");
        contentValues.put(StockEntry.COLUMN_STOCK_BRAND, "Dalda");
        contentValues.put(StockEntry.COLUMN_STOCK_QUANTITY, 1);
        contentValues.put(StockEntry.COLUMN_STOCK_UPDATED_DATE, date);
        // Insert Data in Database using Content Provider
        Uri newUri = getContentResolver().insert(StockEntry.CONTENT_URI,contentValues);

    }

    private void deleteAllStockItems(){
        // Delete all data from pets table and return number of rows deleted
        int rowsDeleted = getContentResolver().delete(StockEntry.CONTENT_URI,null,null);
        // If there were no rows deleted, show a toast message
        if (rowsDeleted == 0){
            Toast.makeText(this, R.string.no_data_to_delete, Toast.LENGTH_SHORT).show();
        }
        else{
            Toast.makeText(this, R.string.delete_stock_table_successful, Toast.LENGTH_SHORT).show();
        }
    }



    @Override
    public Loader<Cursor> onCreateLoader(int loaderId, Bundle bundle) {
        switch (loaderId)
        {
            case STOCK_LOADER:
                // Selection columns from the table
                String[] projection = { StockEntry._ID,
                        StockEntry.COLUMN_STOCK_NAME,
                        StockEntry.COLUMN_STOCK_BRAND,
                        StockEntry.COLUMN_STOCK_DESCRIPTION,
                        StockEntry.COLUMN_STOCK_QUANTITY,
                        StockEntry.COLUMN_STOCK_UPDATED_DATE
                        };

                return new CursorLoader(
                        this,
                        StockEntry.CONTENT_URI,
                        projection,
                        null,
                        null,
                        null);
            default:
                return null;
        }
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        stockAdapter.swapCursor(cursor);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        stockAdapter.swapCursor(null);
    }


}