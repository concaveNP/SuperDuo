package it.jaschke.alexandria.services;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;

import java.util.ArrayList;

import it.jaschke.alexandria.AddBook;
import it.jaschke.alexandria.MainActivity;
import it.jaschke.alexandria.data.AlexandriaContract;

/**
 * Created by dave on 2/19/16.
 *
 * References:
 *
 * - http://developer.android.com/training/monitoring-device-state/connectivity-monitoring.html
 * - http://developer.android.com/training/monitoring-device-state/manifest-receivers.html
 *
 *   Need to get a context to the class, so I did the following:
 * - http://stackoverflow.com/questions/2002288/static-way-to-get-context-on-android
 */
public class NetworkConnectivityStatus {

    /**
     * The logging tag string to be associated with log data for this class
     */
    private static final String TAG = NetworkConnectivityStatus.class.getSimpleName();

    /**
     * Private in order to prevent instantiation by others than this class.
     */
    protected NetworkConnectivityStatus() {
        // Do nothing
    }

    static public void performNetworkRequiredLookups(boolean connected) {

        Log.d(TAG, "Connection status: " + connected);

        if (connected) {

            Cursor cursor = null;

            try {

                Context context = MainActivity.getAppContext();

                if (context != null) {
                    // Do DB lookup
                    cursor = context.getContentResolver().query(
                            AlexandriaContract.EanEntry.CONTENT_URI,
                            null, // leaving "columns" null just returns all the columns.
                            null, // cols for "where" clause
                            null, // values for "where" clause
                            null  // sort order
                    );

                    ArrayList<String> eanList = new ArrayList<>();

                    // Any entries indicates that EANs were found
                    if (cursor.getCount() > 0) {
                        // Add EANs to string array
                        for (cursor.moveToFirst(); !cursor.isAfterLast(); cursor.moveToNext()) {
                            // Get the primary key from the result table, which is the EAN number
                            long eanLong = cursor.getLong(cursor.getColumnIndexOrThrow(AlexandriaContract.EanEntry._ID));
                            String eanString = Long.toString(eanLong);
                            eanList.add(eanString);
                        }
                    }

                    if (!eanList.isEmpty()) {
                        //Once we have an ISBN, start a book intent
                        Intent bookIntent = new Intent(context, BookService.class);
                        bookIntent.putStringArrayListExtra(BookService.EANS, eanList);
                        bookIntent.setAction(BookService.FETCH_BOOKS);
                        context.startService(bookIntent);
                    }
                }
                else {
                    Log.e(TAG, "There is no context to work with.");
                }
            }
            finally {
                if (cursor != null) {
                   cursor.close();
                }
            }
        }
    }

    static public boolean checkConnected() {
        boolean connected = false;

        Context context = MainActivity.getAppContext();

        if (context != null) {
            ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);

            NetworkInfo activeNetwork = cm.getActiveNetworkInfo();

            connected = activeNetwork != null && activeNetwork.isConnectedOrConnecting();

            Log.d(TAG, "The init context connection status: " + connected);
        }
        else {
            Log.e(TAG, "The context was unavailable for use");
        }

        return connected;
    }

}
