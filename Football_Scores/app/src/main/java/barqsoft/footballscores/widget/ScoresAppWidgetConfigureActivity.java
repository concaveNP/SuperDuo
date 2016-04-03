package barqsoft.footballscores.widget;

import android.app.Activity;
import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Pair;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;

import java.util.HashMap;
import java.util.Map;

import barqsoft.footballscores.R;

/**
 * The configuration screen for the {@link ScoresAppWidgetProvider ScoresAppWidgetProvider} AppWidget.
 */
public class ScoresAppWidgetConfigureActivity extends Activity {

    int mAppWidgetId = AppWidgetManager.INVALID_APPWIDGET_ID;
    Spinner mAppWidgetSpinner;

    private static final String PREFS_NAME = "barqsoft.footballscores.widget.ScoresAppWidgetProvider";
    private static final String PREF_PREFIX_KEY = "appwidget_";

    private static Map<Integer, Pair<String,Integer>> sDayMapping;

    /**
     * Default constructor
     */
    public ScoresAppWidgetConfigureActivity() {
        super();
    }

    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);

        // Set the result to CANCELED.  This will cause the widget host to cancel
        // out of the widget placement if the user presses the back button.
        setResult(RESULT_CANCELED);

        setContentView(R.layout.scores_app_widget_configure);

        // Locate the views within layout
        Button addButton = (Button)findViewById(R.id.add_button);
        mAppWidgetSpinner = (Spinner)findViewById(R.id.day_Choice_Spinner);

        // Attach a listener object to clicks on the add widget button
        addButton.setOnClickListener(mOnClickListener);

        // Populate the spinner the model data and attach selection listener
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this, R.array.widget_day_choice_titles, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mAppWidgetSpinner.setAdapter(adapter);

        // Find the widget id from the intent.
        Intent intent = getIntent();
        Bundle extras = intent.getExtras();
        if (extras != null) {
            mAppWidgetId = extras.getInt(AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID);
        }

        // If this activity was started with an intent without an app widget ID, finish with an error.
        if (mAppWidgetId == AppWidgetManager.INVALID_APPWIDGET_ID) {
            finish();
            return;
        }

        mAppWidgetSpinner.setSelection(loadDayPositionPreference(ScoresAppWidgetConfigureActivity.this, mAppWidgetId));
    }

    View.OnClickListener mOnClickListener = new View.OnClickListener() {
        public void onClick(View v) {
            final Context context = ScoresAppWidgetConfigureActivity.this;

            // When the button is clicked, store the day offset locally
            int position = mAppWidgetSpinner.getSelectedItemPosition();
            saveDayPositionPreference(context, mAppWidgetId, position);

            // It is the responsibility of the configuration activity to update the app widget
            AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);

            // Update the provider
            ScoresAppWidgetProvider.updateAppWidget(context, appWidgetManager, mAppWidgetId);

            // Make sure we pass back the original appWidgetId
            Intent resultValue = new Intent();
            resultValue.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, mAppWidgetId);
            setResult(RESULT_OK, resultValue);
            finish();
        }
    };

    // Write the day selection position to the SharedPreferences object for this widget
    private static void saveDayPositionPreference(Context context, int appWidgetId, int position) {

        SharedPreferences.Editor prefs = context.getSharedPreferences(PREFS_NAME, 0).edit();
        prefs.putInt(PREF_PREFIX_KEY + appWidgetId, position);
        prefs.apply();

    }

    private static int loadDayPositionPreference(Context context, int appWidgetId) {

        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, 0);
        int position = prefs.getInt(PREF_PREFIX_KEY + appWidgetId, 0);
        return position;

    }

    public static int getDayOffset(Context context, int appWidgetId) {

        // Get the spinner position of the day selection
        int position = loadDayPositionPreference(context, appWidgetId);

        // Get the pair object that contains the day title and day offset
        Pair<String, Integer> pair = getDayMapping(context).get(position);

        // Return the day offset
        return pair.second;

    }

    public static String getDayTitle(Context context, int appWidgetId) {

        // Get the spinner position of the day selection
        int position = loadDayPositionPreference(context, appWidgetId);

        // Get the pair object that contains the day title and day offset
        Pair<String, Integer> pair = getDayMapping(context).get(position);

        // Return the day offset
        return pair.first;

    }

    public static void deleteDayPositionPref(Context context, int appWidgetId) {

        SharedPreferences.Editor prefs = context.getSharedPreferences(PREFS_NAME, 0).edit();
        prefs.remove(PREF_PREFIX_KEY + appWidgetId);
        prefs.apply();

    }

    /**
     * Static lazy getter that will initialize a mapping of Day Selection to the Day Offset value.
     * I did this in order to just make life a little more simple and easier to understand.
     *
     * Essentially the resources will create the following mapping, but the string titles can be
     * of a different language:
     *      - Yesterday = -1
     *      - Today = 0
     *      - Tomorrow = 1
     *
     * @param context - The context of this activity in order to get resources
     */
    private static Map<Integer,Pair<String,Integer>> getDayMapping(Context context) {

        // If the mapping already exists then there is nothing to do
        if (sDayMapping == null) {
            String[] strArray = context.getResources().getStringArray(R.array.widget_day_choice_titles);
            int[] intArray = context.getResources().getIntArray(R.array.widget_day_choice_values);
            sDayMapping = new HashMap<>();
            for (int index = 0; index < strArray.length; index++) {
               sDayMapping.put(index, new Pair<>(strArray[index], intArray[index]));
            }
        }

        return sDayMapping;

    }

}

