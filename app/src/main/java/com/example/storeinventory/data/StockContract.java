package com.example.storeinventory.data;

import android.content.ContentResolver;
import android.net.Uri;
import android.provider.BaseColumns;

public class StockContract {

    // To prevent someone from accidentally instantiating the contract class,
    // give it an empty constructor.
    private StockContract(){}

    /**
     * The "Content authority" is a name for the entire content provider, similar to the
     * relationship between a domain name and its website.  A convenient string to use for the
     * content authority is the package name for the app, which is guaranteed to be unique on the
     * device.
     */
    public static final String CONTENT_AUTHORITY = "com.example.storeinventory";

    /**
     * Use CONTENT_AUTHORITY to create the base of all URI's which apps will use to contact
     * the content provider.
     */
    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);

    /**
     * Possible path (appended to base content URI for possible URI's)
     * For instance, content://com.example.storeinventory/stock/ is a valid path for
     * looking at stock data. content://com.example.storeinventory/staff/ will fail,
     * as the ContentProvider hasn't been given any information on what to do with "staff".
     */
    public static final String PATH_STOCK = "stock";

    public static abstract class StockEntry implements BaseColumns{
        /** The content URI to access the pet data in the provider */
        public static final Uri CONTENT_URI = Uri.withAppendedPath(BASE_CONTENT_URI, PATH_STOCK);

        /**
         * The MIME type of the {@link #CONTENT_URI} for a list of stock.
         */
        public static final String CONTENT_LIST_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_STOCK;

        /**
         * The MIME type of the {@link #CONTENT_URI} for a single stock.
         */
        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_STOCK;

        /** Name of database table for stock */
        public final static String TABLE_NAME = "stock";
        /**
         * Values for the Columns of the stock table.
         */
        public static final String _ID = BaseColumns._ID;
        public static final String COLUMN_STOCK_NAME = "name";
        public static final String COLUMN_STOCK_BRAND = "brand";
        public static final String COLUMN_STOCK_DESCRIPTION = "description";
        public static final String COLUMN_STOCK_QUANTITY = "quantity";
        public static final String COLUMN_STOCK_UPDATED_DATE = "updated_date";

    }

}
