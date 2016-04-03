package barqsoft.footballscores.service;

import android.app.IntentService;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
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
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;
import java.util.Vector;

import barqsoft.footballscores.R;
import barqsoft.footballscores.database.DatabaseContract;

/**
 * Created by yehya khaled on 3/2/2015.
 */
public class FetchScoresService extends IntentService {

    /**
     * Simple class name used for debug logging.
     */
    public static final String LOG_TAG = FetchScoresService.class.getSimpleName();

    /**
     * Default constructor
     */
    public FetchScoresService() {

        // Give the class name for debug logging purposes
        super(LOG_TAG);

    }

    @Override
    protected void onHandleIntent(Intent intent) {

        getData(getResources().getString(R.string.N2_TIMEFRAME));
        getData(getResources().getString(R.string.P2_TIMEFRAME));

        return;
    }

    private void getData(String timeFrame) {

        // Creating the fetch URL by building from the Base URL and the time frame parameter to determine days
        final String baseUrl = getBaseContext().getString(R.string.BASE_URL);
        final String queryTimeFrame = getBaseContext().getString(R.string.QUERY_TIME_FRAME );

        Uri fetch_build = Uri.parse(baseUrl).buildUpon().appendQueryParameter(queryTimeFrame, timeFrame).build();

        HttpURLConnection m_connection = null;
        BufferedReader reader = null;
        String JSON_data = null;

        // Opening Connection
        try {
            URL fetch = new URL(fetch_build.toString());
            m_connection = (HttpURLConnection) fetch.openConnection();
            m_connection.setRequestMethod("GET");

            //
            // The suggestion by the "Boss" would have been a BUG as it would have been a source
            // controlled file that would have an API key visible to the public.  I've place it
            // into an "ignored" file by Git.
            //
            m_connection.addRequestProperty("X-Auth-Token", getResources().getString(R.string.API_FOOTBALL_DATA_ORG));

            // Perform the connection to the data service
            m_connection.connect();

            Log.d(LOG_TAG, "Performing connection to service for Football data.");

            // Read the input stream into a String
            InputStream inputStream = m_connection.getInputStream();
            StringBuffer buffer = new StringBuffer();
            if (inputStream == null) {
                // Nothing to do.
                return;
            }
            reader = new BufferedReader(new InputStreamReader(inputStream));

            String line;
            while ((line = reader.readLine()) != null) {
                // Since it's JSON, adding a newline isn't necessary (it won't affect parsing)
                // But it does make debugging a *lot* easier if you print out the completed
                // buffer for debugging.
                buffer.append(line + "\n");
            }
            if (buffer.length() == 0) {
                // Stream was empty.  No point in parsing.
                return;
            }
            JSON_data = buffer.toString();
        } catch (Exception e) {
            Log.e(LOG_TAG, "Exception here" + e.getMessage());
        } finally {
            if (m_connection != null) {
                m_connection.disconnect();
            }
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    Log.e(LOG_TAG, "Error Closing Stream");
                }
            }
        }

        try {
            if (JSON_data != null) {
                //This bit is to check if the data contains any matches. If not, we call processJson on the dummy data
                JSONArray matches = new JSONObject(JSON_data).getJSONArray("fixtures");
                if (matches.length() == 0) {
                    //if there is no data, call the function on dummy data
                    //this is expected behavior during the off season.
                    processJSONdata(getString(R.string.dummy_data), getApplicationContext(), false);
                    return;
                }

                processJSONdata(JSON_data, getApplicationContext(), true);

            } else {
                //Could not Connect
                Log.d(LOG_TAG, "Could not connect to server.");
            }
        } catch (Exception e) {
            Log.e(LOG_TAG, e.getMessage());
        }
    }

    private void processJSONdata(String JSONdata, Context mContext, boolean isReal) {

        //Match data
        String League = null;
        String mDate = null;
        String mTime = null;
        String Home = null;
        String Away = null;
        String Home_goals = null;
        String Away_goals = null;
        String match_id = null;
        String match_day = null;

        try {
            JSONArray matches = new JSONObject(JSONdata).getJSONArray(getResources().getString(R.string.FIXTURES));

            //ContentValues to be inserted
            Vector<ContentValues> values = new Vector<ContentValues>(matches.length());
            for (int index = 0; index < matches.length(); index++) {

                JSONObject match_data = matches.getJSONObject(index);
                League = match_data.getJSONObject(getResources().getString(R.string.LINKS)).getJSONObject(getResources().getString(R.string.SOCCER_SEASON)).getString("href");
                League = League.replace(getResources().getString(R.string.SEASON_LINK), "");

                //This if statement controls which leagues we're interested in the data from.
                //add leagues here in order to have them be added to the DB.
                // If you are finding no data in the app, check that this contains all the leagues.
                // If it doesn't, that can cause an empty DB, bypassing the dummy data routine.
                if (League.equals(getResources().getString(R.string.PREMIER_LEAGUE_JSON)) ||
                        League.equals(getResources().getString(R.string.SERIE_A)) ||
                        League.equals(getResources().getString(R.string.BUNDESLIGA1)) ||
                        League.equals(getResources().getString(R.string.BUNDESLIGA2)) ||
                        League.equals(getResources().getString(R.string.PRIMERA_DIVISION))) {
                    match_id = match_data.getJSONObject(getResources().getString(R.string.LINKS)).getJSONObject(getResources().getString(R.string.SELF)).getString("href");
                    match_id = match_id.replace(getResources().getString(R.string.MATCH_LINK), "");
                    if (!isReal) {
                        //This if statement changes the match ID of the dummy data so that it all goes into the database
                        match_id = match_id + Integer.toString(index);
                    }

                    mDate = match_data.getString(getResources().getString(R.string.MATCH_DATE_JSON));
                    mTime = mDate.substring(mDate.indexOf("T") + 1, mDate.indexOf("Z"));
                    mDate = mDate.substring(0, mDate.indexOf("T"));
                    SimpleDateFormat match_date = new SimpleDateFormat(getResources().getString(R.string.MATCH_DATE));
                    match_date.setTimeZone(TimeZone.getTimeZone(getResources().getString(R.string.TIME_ZONE)));

                    try {

                        Date parsedDate = match_date.parse(mDate + mTime);
                        SimpleDateFormat newDate = new SimpleDateFormat(getResources().getString(R.string.NEW_DATE));
                        newDate.setTimeZone(TimeZone.getDefault());
                        mDate = newDate.format(parsedDate);
                        mTime = mDate.substring(mDate.indexOf(":") + 1);
                        mDate = mDate.substring(0, mDate.indexOf(":"));

                        if (!isReal) {

                            //This if statement changes the dummy data's date to match our current date range.
                            Date fragmentDate = new Date(System.currentTimeMillis() + ((index - 2) * getResources().getInteger(R.integer.NUMBER_OF_MILISECONDS_IN_A_DAY)));
                            SimpleDateFormat simpleDateFormat = new SimpleDateFormat(getResources().getString(R.string.NOT_REAL_DATE));
                            mDate = simpleDateFormat.format(fragmentDate);

                        }
                    } catch (Exception e) {

                        Log.d(LOG_TAG, "error here!");
                        Log.e(LOG_TAG, e.getMessage());

                    }

                    Home = match_data.getString(getResources().getString(R.string.HOME_TEAM));
                    Away = match_data.getString(getResources().getString(R.string.AWAY_TEAM));
                    Home_goals = match_data.getJSONObject(getResources().getString(R.string.RESULT)).getString(getResources().getString(R.string.HOME_GOALS));
                    Away_goals = match_data.getJSONObject(getResources().getString(R.string.RESULT)).getString(getResources().getString(R.string.AWAY_GOALS));
                    match_day = match_data.getString(getResources().getString(R.string.MATCH_DAY));
                    ContentValues match_values = new ContentValues();
                    match_values.put(DatabaseContract.scores_table.MATCH_ID, match_id);
                    match_values.put(DatabaseContract.scores_table.DATE_COL, mDate);
                    match_values.put(DatabaseContract.scores_table.TIME_COL, mTime);
                    match_values.put(DatabaseContract.scores_table.HOME_COL, Home);
                    match_values.put(DatabaseContract.scores_table.AWAY_COL, Away);
                    match_values.put(DatabaseContract.scores_table.HOME_GOALS_COL, Home_goals);
                    match_values.put(DatabaseContract.scores_table.AWAY_GOALS_COL, Away_goals);
                    match_values.put(DatabaseContract.scores_table.LEAGUE_COL, League);
                    match_values.put(DatabaseContract.scores_table.MATCH_DAY, match_day);
                    //log spam

                    //Log.d(LOG_TAG,match_id);
                    //Log.d(LOG_TAG,mDate);
                    //Log.d(LOG_TAG,mTime);
                    //Log.d(LOG_TAG,Home);
                    //Log.d(LOG_TAG,Away);
                    //Log.d(LOG_TAG,Home_goals);
                    //Log.d(LOG_TAG,Away_goals);

                    values.add(match_values);
                }
            }

            //  The number of values that were inserted.
            int inserted_data = 0;

            // Put the values into and array for DB insertion
            ContentValues[] insert_data = new ContentValues[values.size()];
            values.toArray(insert_data);

            // Put data into the DB
            inserted_data = mContext.getContentResolver().bulkInsert(DatabaseContract.BASE_CONTENT_URI, insert_data);

            // Log entries
            Log.d(LOG_TAG,"Successfully Inserted : " + String.valueOf(inserted_data));

        } catch (JSONException e) {

            Log.e(LOG_TAG, e.getMessage());

        }

    }
}

