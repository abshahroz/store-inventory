package com.example.storeinventory;

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
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NavUtils;

import com.example.storeinventory.data.StockContract.StockEntry;

import java.util.Calendar;

/**
 * Allows user to create a new pet or edit an existing one.
 */
public class EditorActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    /** EditText field to enter the stock item's name */
    private EditText nameEditText;

    /** EditText field to enter the stock item's brand */
    private EditText brandEditText;

    /** EditText field to enter the stock item's description */
    private EditText descriptionEditText;

    /** EditText field to enter the stock item's quantity */
    private EditText quantityEditText;

    // Stock Id to be used for updating stock item
    private int stockItemId=0;
    //
    private Uri currentStockItemUri;

    private final int STOCK_LOADER=0;

    private boolean stockHasChanged = false;
    // OnTouchListener that listens for any user touches on a View, implying that they are modifying
    // the view, and we change the stockHasChanged boolean to true.

    private final View.OnTouchListener mTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            stockHasChanged = true;
            return false;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editor);

        Intent catalogIntent = getIntent();
        currentStockItemUri = catalogIntent.getData();

        // Find all relevant views that we will need to read user input from
        nameEditText = findViewById(R.id.edit_item_name);
        brandEditText = findViewById(R.id.edit_item_brand);
        descriptionEditText = findViewById(R.id.edit_item_desc);
        quantityEditText = findViewById(R.id.edit_item_quantity);

        nameEditText.setOnTouchListener(mTouchListener);
        brandEditText.setOnTouchListener(mTouchListener);
        descriptionEditText.setOnTouchListener(mTouchListener);
        quantityEditText.setOnTouchListener(mTouchListener);


        if (currentStockItemUri != null){
            this.setTitle(R.string.editor_activity_title_edit_stock);

            getLoaderManager().initLoader(STOCK_LOADER,null,this);
        }
        else {
            this.setTitle(R.string.editor_activity_title_add_stock);
            // Invalidate the options menu, so the "Delete" menu option can be hidden.
            // (It doesn't make sense to delete a pet that hasn't been created yet.)
            invalidateOptionsMenu();
        }
    }



    // Get user input from editor and save it to database
    private void saveStockItem(){
        Uri updatedUri;
        // Get text from Edit view edit_item_name, convert to String and
        // remove white space at the end or the beginning
        String name = nameEditText.getText().toString().trim();
        // Get text from Edit view edit_item_brand, convert to String and
        // remove white space at the end or the beginning
        String brand = brandEditText.getText().toString().trim();
        // Get text from Edit view edit_item_quantity, convert to String and
        // remove white space at the end or the beginning
        String quantityString = quantityEditText.getText().toString().trim();
        // Get text from Edit view edit_item_desc, convert to String and
        // remove white space at the end or the beginning
        String description = descriptionEditText.getText().toString().trim();
        // If user didn't enter weight, it will be default weight of the pet
        double quantity = 0;

        if (currentStockItemUri == null &&
                TextUtils.isEmpty(name) && TextUtils.isEmpty(brand) &&
                TextUtils.isEmpty(description) ) {return;}

        //If the quantity is not provided by the user, don't try to parse the string into an
        // integer value. Use 0 by default.
        if (!TextUtils.isEmpty(quantityString)) {
            quantity = Double.parseDouble(quantityString);
        }

        // Get current date
        Calendar cal = Calendar.getInstance();
        int currentYear = cal.get(Calendar.YEAR);
        int month = cal.get(Calendar.MONTH);
        int currentMonth = month+1;
        int currentDay = cal.get(Calendar.DAY_OF_MONTH);
        // Create date String to save current Date
        String date = currentDay + "-" + currentMonth + "-" + currentYear;

        // Create a new map of values, where column names are the keys
        ContentValues values = new ContentValues();
        values.put(StockEntry.COLUMN_STOCK_NAME, name);
        values.put(StockEntry.COLUMN_STOCK_BRAND, brand);
        values.put(StockEntry.COLUMN_STOCK_DESCRIPTION, description);
        values.put(StockEntry.COLUMN_STOCK_QUANTITY, quantity);
        values.put(StockEntry.COLUMN_STOCK_UPDATED_DATE, date);

        // If Stock item doesn't exist already, StockId would be 0
        if (stockItemId==0){
            // Insert Data in Database using Content Provider
            updatedUri = getContentResolver().insert(StockEntry.CONTENT_URI,values);
            // Check if updatedUri contains uri of newly inserted pet
            if (updatedUri == null){
                Toast.makeText(this, R.string.insert_stock_failed, Toast.LENGTH_SHORT).show();
            }
            else {
                Toast.makeText(this, R.string.insert_stock_successful, Toast.LENGTH_SHORT).show();
            }
        }
        // If Pet exists already and needs to be updated
        else {
            String selection = StockEntry._ID + "=?";
            String[] selectionArgs = new String[] { String.valueOf(ContentUris.parseId(currentStockItemUri)) };
            // Update Data in Database using Content Provider
            int updatedRows = getContentResolver().update(currentStockItemUri,values,selection,selectionArgs);

            if (updatedRows == -1){
                Toast.makeText(this, R.string.update_stock_failed, Toast.LENGTH_SHORT).show();
            }
            else {
                Toast.makeText(this, R.string.update_stock_successful, Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void deleteStockItem(){
        if (currentStockItemUri != null){
            // Delete Data in Database using Content Provider
            int rowsDeleted= getContentResolver().delete(currentStockItemUri,null,null);
            // Show a toast message depending on whether or not the delete was successful.
            if (rowsDeleted == 0){
                // If no rows were deleted, then there was an error with the delete.
                Toast.makeText(this, R.string.delete_stock_failed, Toast.LENGTH_SHORT).show();
            }
            else {
                // Otherwise, the delete operation was successful and we can display a toast
                Toast.makeText(this, R.string.delete_stock_successful, Toast.LENGTH_SHORT).show();
            }
        }
        // Close the activity
        finish();

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu options from the res/menu/menu_editor.xml file.
        // This adds menu items to the app bar.
        getMenuInflater().inflate(R.menu.menu_editor, menu);
        return true;
    }
    @Override

    public boolean onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        // If this is a new pet, hide the "Delete" menu item.
        if (currentStockItemUri == null) {
            MenuItem menuItem = menu.findItem(R.id.action_delete);
            menuItem.setVisible(false);
        }
        return true;
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // User clicked on a menu option in the app bar overflow menu
        switch (item.getItemId()) {
            // Respond to a click on the "Save" menu option
            case R.id.action_save:
                // Save Stock item to database
                saveStockItem();
                // Exit the activity
                finish();
                return true;
            // Respond to a click on the "Delete" menu option
            case R.id.action_delete:
                // Pop up confirmation dialog for deletion
                showDeleteConfirmationDialog();
                return true;
            // Respond to a click on the "Up" arrow button in the app bar
            case android.R.id.home:
                // If the pet hasn't changed, continue with navigating up to parent activity
                // which is the {@link CatalogActivity}.
                if (!stockHasChanged) {
                    NavUtils.navigateUpFromSameTask(EditorActivity.this);
                    return true;
                }
                // Otherwise if there are unsaved changes, setup a dialog to warn the user.
                // Create a click listener to handle the user confirming that
                // changes should be discarded.
                DialogInterface.OnClickListener discardButtonClickListener =
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                // User clicked "Discard" button, navigate to parent activity.
                                NavUtils.navigateUpFromSameTask(EditorActivity.this);
                            }
                        };

                // Show a dialog that notifies the user they have unsaved changes
                showUnsavedChangesDialog(discardButtonClickListener);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    // To show Confirmation Dialogue after user selects Delete from menu
    private void showDeleteConfirmationDialog(){
        // Create an AlertDialog.Builder and set the message, and click listeners
        // for the positive and negative buttons on the dialog.
        AlertDialog.Builder builder =new AlertDialog.Builder(this);
        builder.setMessage(R.string.delete_dialog_msg);
        builder.setPositiveButton(R.string.action_delete, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int id) {
                // User clicked the "Delete" button, so delete the stock item.
                deleteStockItem();
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
    // Shows Dialogue if user presses "UP" button after making some changes
    private void showUnsavedChangesDialog(
            DialogInterface.OnClickListener discardButtonClickListener) {
        // Create an AlertDialog.Builder and set the message, and click listeners
        // for the positive and negative buttons on the dialog.
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.unsaved_changes_dialog_msg);
        builder.setPositiveButton(R.string.discard, discardButtonClickListener);
        builder.setNegativeButton(R.string.keep_editing, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Keep editing" button, so dismiss the dialog
                // and continue editing the pet.
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });

        // Create and show the AlertDialog
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

        @Override
        public void onBackPressed() {
            // If the pet hasn't changed, continue with handling back button press
            if (!stockHasChanged) {
                super.onBackPressed();
                return;
            }

            // Otherwise if there are unsaved changes, setup a dialog to warn the user.
            // Create a click listener to handle the user confirming that changes should be discarded.
            DialogInterface.OnClickListener discardButtonClickListener =
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            // User clicked "Discard" button, close the current activity.
                            finish();
                        }
                    };

            // Show dialog that there are unsaved changes
            showUnsavedChangesDialog(discardButtonClickListener);
        }




    @Override
    public Loader<Cursor> onCreateLoader(int loaderId, Bundle bundle) {
        switch (loaderId) {
            case STOCK_LOADER:
                // Selection columns from the table
                String[] projection = {StockEntry._ID,
                        StockEntry.COLUMN_STOCK_NAME,
                        StockEntry.COLUMN_STOCK_BRAND,
                        StockEntry.COLUMN_STOCK_DESCRIPTION,
                        StockEntry.COLUMN_STOCK_QUANTITY,
                        StockEntry.COLUMN_STOCK_UPDATED_DATE};

                return new CursorLoader(
                        this,
                        currentStockItemUri,
                        projection,
                        null,
                        null,
                        null
                );


            default:
                return null;
        }
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        // Proceed with moving to the first row of the cursor and reading data from it
        // (This should be the only row in the cursor)
        if (cursor.moveToFirst()) {
            // Figure out the index of each column
            int idColumnIndex = cursor.getColumnIndex(StockEntry._ID);
            int nameColumnIndex = cursor.getColumnIndex(StockEntry.COLUMN_STOCK_NAME);
            int brandColumnIndex = cursor.getColumnIndex(StockEntry.COLUMN_STOCK_BRAND);
            int quantityColumnIndex = cursor.getColumnIndex(StockEntry.COLUMN_STOCK_QUANTITY);
            int descColumnIndex = cursor.getColumnIndex(StockEntry.COLUMN_STOCK_DESCRIPTION);

            stockItemId = cursor.getInt(idColumnIndex);
            String name = cursor.getString(nameColumnIndex);
            String breed = cursor.getString(brandColumnIndex);
            String description = cursor.getString(descColumnIndex);
            double quantity = cursor.getDouble(quantityColumnIndex);
            String quantityString = Double.toString(quantity);

            nameEditText.setText(name);
            brandEditText.setText(breed);
            quantityEditText.setText(quantityString);
            descriptionEditText.setText(description);
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
            nameEditText.setText("");
            brandEditText.setText("");
            quantityEditText.setText("");
            descriptionEditText.setText("");
    }

}
