package com.example.storeinventory;

import android.content.Context;
import android.database.Cursor;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.TextView;
import com.example.storeinventory.data.StockContract.StockEntry;


public class StockCursorAdapter extends CursorAdapter {
    /**
     * Constructs a new {@link StockCursorAdapter}.
     *
     * @param context The context
     * @param cursor       The cursor from which to get the data.
     */
    public StockCursorAdapter(Context context, Cursor cursor){
        super(context,cursor,0 /* flags */);
    }
/**
 * Makes a new blank list item view. No data is set (or bound) to the views yet.
 *
 * @param context app context
 * @param cursor  The cursor from which to get the data. The cursor is already
 *                moved to the correct position.
 * @param parent  The parent to which the new view is attached to
 * @return the newly created list item view.
 **/
    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        return LayoutInflater.from(context).inflate(R.layout.list_item,parent,false);
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        // Find fields to populate in inflated template
        TextView nameView = view.findViewById(R.id.item_name);
        TextView brandView = view.findViewById(R.id.item_brand);
        TextView quantityView = view.findViewById(R.id.item_quantity);
        TextView dateView = view.findViewById(R.id.item_update_date);
        // Find the columns of pet attributes that we're interested in
        int nameColumnIndex = cursor.getColumnIndex(StockEntry.COLUMN_STOCK_NAME);
        int brandColumnIndex = cursor.getColumnIndex(StockEntry.COLUMN_STOCK_BRAND);
        int quantityColumnIndex = cursor.getColumnIndex(StockEntry.COLUMN_STOCK_QUANTITY);
        int dateColumnIndex = cursor.getColumnIndex(StockEntry.COLUMN_STOCK_UPDATED_DATE);
        // Read the pet attributes from the Cursor for the current pet
        String name = cursor.getString(nameColumnIndex);
        String brand = cursor.getString(brandColumnIndex);
        // If breed is empty or null. show "Unknown breed" as default text
        if (TextUtils.isEmpty(brand)){
            brand = context.getString(R.string.unknown_brand);
        }

        double quantity = cursor.getDouble(quantityColumnIndex);
        String updatedDate = cursor.getString(dateColumnIndex);
        String quantityString = Double.toString(quantity);

        nameView.setText(name);
        brandView.setText(brand);
        quantityView.setText(quantityString);
        dateView.setText(updatedDate);


    }
}
