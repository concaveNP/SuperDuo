package it.jaschke.alexandria.services;

import android.app.IntentService;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;

import it.jaschke.alexandria.MainActivity;
import it.jaschke.alexandria.R;
import it.jaschke.alexandria.data.AlexandriaContract;


/**
 * An {@link IntentService} subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 * <p/>
 */
public class BookService extends IntentService {

    private final String TAG = BookService.class.getSimpleName();

    public static final String FETCH_BOOK = "it.jaschke.alexandria.services.action.FETCH_BOOK";
    public static final String FETCH_BOOKS = "it.jaschke.alexandria.services.action.FETCH_BOOKS";
    public static final String DELETE_BOOK = "it.jaschke.alexandria.services.action.DELETE_BOOK";

    public static final String EAN = "it.jaschke.alexandria.services.extra.EAN";
    public static final String EANS = "it.jaschke.alexandria.services.extra.EANS";

    public BookService() {
        super("Alexandria");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            final String action = intent.getAction();
            if (FETCH_BOOK.equals(action)) {
                final String ean = intent.getStringExtra(EAN);
                Log.d(TAG, "FETCH_BOOK: " + ean);
                fetchBook(ean);
            } if (FETCH_BOOKS.equals(action)) {
                final List<String> eans = intent.getStringArrayListExtra(EANS);
                for (String ean : eans) {
                    final String temp = ean;
                    Log.d(TAG, "FETCH_BOOKS: " + ean);
                    fetchBook(temp);
                }
            } else if (DELETE_BOOK.equals(action)) {
                final String ean = intent.getStringExtra(EAN);
                Log.d(TAG, "DELETE_BOOK: " + ean);
                deleteBook(ean);
                deleteEanBook(ean);
            }
        }
    }

    /**
     * Handle action Foo in the provided background thread with the provided
     * parameters.
     */
    private void deleteBook(String ean) {
        if (ean != null) {
            Log.d(TAG, "deleteBook: " + ean);
            getContentResolver().delete(AlexandriaContract.BookEntry.buildBookUri(Long.parseLong(ean)), null, null);
        }
    }

    /**
     * Delete from the EAN DB table the given EAN entry.
     *
     * @param ean - The EAN number to remove from the table
     */
    private void deleteEanBook(String ean) {
        if (ean != null) {
            Log.d(TAG, "deleteEanBook: " + ean);
            getContentResolver().delete(AlexandriaContract.EanEntry.buildEanUri(Long.parseLong(ean)), null, null);
        }
    }

    /**
     * Handle action fetchBook in the provided background thread with the provided
     * parameters.
     *
     * todo - fix param
     * @return True for successfully retrieving the book's information and adding it to the
     * DB and false otherwise
     */
    private void fetchBook(String ean) {
// TODO: 2/29/16 - remove if needed
//        // Was the book successfully processed?  Default to no until success.
//        boolean result = false;

        Log.d(TAG, "fetchBook: " + ean);

        // Check for correct length EAN number
        if (ean.length() != 13) {
            return;
        }

        // TODO: 2/27/16 - potentially toast here in order to let the user know that book already exists in DB table X

        // Check to see if it exists in the Book Table already.  If it does then there is nothing to do.
        if (isInBookTable(ean)) {
            return;
        }

        // Check to see if it exists in the EAN Table as a yet unprocessed entry.  If it does then
        // remove it and try to get info about it from the network again.  If the network is down
        // again then just re-add it back to the EAN table.
        if (isInEanTable(ean)) {
            deleteEanBook(ean);
        }

        // Check to see if there is network connectivity
        if (NetworkConnectivityStatus.checkConnected()) {

            Log.d(TAG, "fetchBook: connectivity available, fetching book");

            HttpURLConnection urlConnection = null;
            BufferedReader reader = null;

            try {

                String bookJsonString = getJsonString(ean, urlConnection, reader);
                processJson(ean, bookJsonString);

            } catch (IOException e) {

                // Does not look like a connection was established so put the EAN into the
                // DB table for later processing
                writeBackEan(ean);

                Log.e(TAG, "Error ", e);

            } catch (JSONException e) {

                // TODO: 2/29/16 - could not process the returned data, hmmmmmm

                // TODO: 3/1/16 - comment describing that this book's data could not be determine, decide about retrying???, toast

                Log.e(TAG, "Error ", e);

            } finally {
                if (urlConnection != null) {
                    urlConnection.disconnect();
                }
                if (reader != null) {
                    try {
                        reader.close();
                    } catch (final IOException e) {
                        Log.e(TAG, "Error closing stream", e);
                    }
                }
            }
        }
        else {

            Log.d(TAG, "fetchBook: connectivity is NOT available, putting book on todo table");

            // Put the EAN on the to do list table
            writeBackEan(ean);

            Log.d(TAG, "no network connectivity");
        }
    }

    @Nullable
    private String getJsonString(String ean, HttpURLConnection urlConnection, BufferedReader reader) throws IOException {

        final String FORECAST_BASE_URL = "https://www.googleapis.com/books/v1/volumes?";
        final String QUERY_PARAM = "q";
        final String ISBN_PARAM = "isbn:" + ean;

        Uri builtUri = Uri.parse(FORECAST_BASE_URL).buildUpon().appendQueryParameter(QUERY_PARAM, ISBN_PARAM).build();

        URL url = new URL(builtUri.toString());

        urlConnection = (HttpURLConnection) url.openConnection();
        urlConnection.setRequestMethod("GET");
        urlConnection.connect();

        InputStream inputStream = urlConnection.getInputStream();
        StringBuffer buffer = new StringBuffer();
        if (inputStream == null) {
            return null;
        }

        reader = new BufferedReader(new InputStreamReader(inputStream));
        String line;
        while ((line = reader.readLine()) != null) {
            buffer.append(line);
            buffer.append("\n");
        }

        if (buffer.length() == 0) {
            return null;
        }

        return buffer.toString();
    }

    private void processJson(String ean, String bookJsonString) throws JSONException {
        final String ITEMS = "items";
        final String VOLUME_INFO = "volumeInfo";
        final String TITLE = "title";
        final String SUBTITLE = "subtitle";
        final String AUTHORS = "authors";
        final String DESC = "description";
        final String CATEGORIES = "categories";
        final String IMG_URL_PATH = "imageLinks";
        final String IMG_URL = "thumbnail";

        JSONObject bookJson = new JSONObject(bookJsonString);
        JSONArray bookArray;
        if(bookJson.has(ITEMS)){
            bookArray = bookJson.getJSONArray(ITEMS);
        }else{
            Intent messageIntent = new Intent(MainActivity.MESSAGE_EVENT);
            messageIntent.putExtra(MainActivity.MESSAGE_KEY,getResources().getString(R.string.not_found));
            LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(messageIntent);
            return;
        }

        JSONObject bookInfo = ((JSONObject) bookArray.get(0)).getJSONObject(VOLUME_INFO);

        String title = bookInfo.getString(TITLE);

        String subtitle = "";
        if(bookInfo.has(SUBTITLE)) {
            subtitle = bookInfo.getString(SUBTITLE);
        }

        String desc="";
        if(bookInfo.has(DESC)){
            desc = bookInfo.getString(DESC);
        }

        String imgUrl = "";
        if(bookInfo.has(IMG_URL_PATH) && bookInfo.getJSONObject(IMG_URL_PATH).has(IMG_URL)) {
            imgUrl = bookInfo.getJSONObject(IMG_URL_PATH).getString(IMG_URL);
        }

        // TODO: 2/27/16 - is there a bug with not using the other DB tables????

        writeBackBook(ean, title, subtitle, desc, imgUrl);

        if(bookInfo.has(AUTHORS)) {
            writeBackAuthors(ean, bookInfo.getJSONArray(AUTHORS));
        }
        if(bookInfo.has(CATEGORIES)){
            writeBackCategories(ean,bookInfo.getJSONArray(CATEGORIES) );
        }
    }

    private void processTodoBook(String ean) {

    }

    /**
     * Check to see if the given EAN number already exists in the Books table.
     *
     * @param ean - The EAN number to check for in the DB
     * @return True if the EAN already exists and false otherwise
     */
    private boolean isInBookTable(String ean) {

        boolean found = false;

        Cursor bookEntry = getContentResolver().query(
                AlexandriaContract.BookEntry.buildBookUri(Long.parseLong(ean)),
                null, // leaving "columns" null just returns all the columns.
                null, // cols for "where" clause
                null, // values for "where" clause
                null  // sort order
        );

        // Any entries indicates that the book was found
        if (bookEntry.getCount() > 0) {
            bookEntry.close();

            // Book found!
            found = true;
        }

        bookEntry.close();

        return found;
    }

    /**
     * Check to see if the given EAN number already exists in the EAN table.
     *
     * @param ean - The EAN number to check for in the DB
     * @return True if the EAN already exists and false otherwise
     */
    private boolean isInEanTable(String ean) {
        boolean found = false;

        Cursor eanEntry = getContentResolver().query(
                AlexandriaContract.EanEntry.buildEanUri(Long.parseLong(ean)),
                null, // leaving "columns" null just returns all the columns.
                null, // cols for "where" clause
                null, // values for "where" clause
                null  // sort order
        );

        // Any entries indicates that the book was found
        if (eanEntry.getCount() > 0) {
            eanEntry.close();

            // Book found!
            found = true;
        }

        eanEntry.close();

        return found;

    }

    private void writeBackBook(String ean, String title, String subtitle, String desc, String imgUrl) {
        Log.d(TAG, "Writing book to Book table " + ean);
        ContentValues values= new ContentValues();
        values.put(AlexandriaContract.BookEntry._ID, ean);
        values.put(AlexandriaContract.BookEntry.TITLE, title);
        values.put(AlexandriaContract.BookEntry.IMAGE_URL, imgUrl);
        values.put(AlexandriaContract.BookEntry.SUBTITLE, subtitle);
        values.put(AlexandriaContract.BookEntry.DESC, desc);
        getContentResolver().insert(AlexandriaContract.BookEntry.CONTENT_URI,values);
    }

    private void writeBackAuthors(String ean, JSONArray jsonArray) throws JSONException {
        Log.d(TAG, "Writing book to Authors table " + ean);
        ContentValues values= new ContentValues();
        for (int i = 0; i < jsonArray.length(); i++) {
            values.put(AlexandriaContract.AuthorEntry._ID, ean);
            values.put(AlexandriaContract.AuthorEntry.AUTHOR, jsonArray.getString(i));
            getContentResolver().insert(AlexandriaContract.AuthorEntry.CONTENT_URI, values);
            values= new ContentValues();
        }
    }

    private void writeBackCategories(String ean, JSONArray jsonArray) throws JSONException {
        Log.d(TAG, "Writing book to Categories table " + ean);
        ContentValues values= new ContentValues();
        for (int i = 0; i < jsonArray.length(); i++) {
            values.put(AlexandriaContract.CategoryEntry._ID, ean);
            values.put(AlexandriaContract.CategoryEntry.CATEGORY, jsonArray.getString(i));
            getContentResolver().insert(AlexandriaContract.CategoryEntry.CONTENT_URI, values);
            values= new ContentValues();
        }
    }

    /**
     * Puts the given EAN number into the EAN DB table.  This table stores the entries for EANs
     * that cannot be looked up online due to inadequate network connectivity.  Instead, the
     * entries will be stored for later.
     *
     * @param ean - The EAN to store for later lookup
     */
    private void writeBackEan(String ean) {
        Log.d(TAG, "Writing book to EAN table " + ean);
        ContentValues values= new ContentValues();
        values.put(AlexandriaContract.EanEntry._ID, ean);
        Uri uri = getContentResolver().insert(AlexandriaContract.EanEntry.CONTENT_URI,values);


        Log.d(TAG, "The resulting URI of writing the EAN to the EAN table " + uri);

    }

 }