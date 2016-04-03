package barqsoft.footballscores.widget;

import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

import java.text.SimpleDateFormat;
import java.util.Date;

import barqsoft.footballscores.R;
import barqsoft.footballscores.Utilies;
import barqsoft.footballscores.database.DatabaseContract;

/**
 * The file contains two classes as explained
 *
 * References:
 * - Android Developers: App Widgets,
 *      http://developer.android.com/guide/topics/appwidgets/index.html,
 *      This article explains how the widget works in the eco system of android and it recommends
 *      a design solution done here.  Specifically, doing an extension of the
 *      {@link RemoteViewsService} that creates a new
 *      {@link android.widget.RemoteViewsService.RemoteViewsFactory}.
 * - Google sample code projects: WeatherListWidget,
 *      https://android.googlesource.com/platform/development/+/master/samples/WeatherListWidget
 *
 * Created by dave on 3/15/16.
 */
public class ScoresAppWidgetService extends RemoteViewsService {
    @Override
    public RemoteViewsFactory onGetViewFactory(Intent intent) {
        return new StackRemoteViewsFactory(this.getApplicationContext(), intent);
    }
}

class StackRemoteViewsFactory implements RemoteViewsService.RemoteViewsFactory {

    /**
     * The Android context for the widget
     */
    private Context mContext;

    /**
     * The DB cursor result for the query of data specific to this widget
     */
    private Cursor mCursor;

    /**
     * The widget ID used to distinguish one from another
     */
    private int mAppWidgetId;

    /**
     * Constructor for the factory that caches the context and extracts the widget ID from the
     * intent.
     *
     * @param context
     * @param intent
     */
    public StackRemoteViewsFactory(Context context, Intent intent) {

        // Save the context for later use
        mContext = context;

        // Get the widget ID from the intent and note the default widget ID is "invalid"
        mAppWidgetId = intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID);

    }

    /**
     * Called when your factory is first constructed. The same factory may be shared across
     * multiple RemoteViewAdapters depending on the intent passed.
     *
     * Since we reload the cursor in onDataSetChanged() which gets called immediately after
     * onCreate(), we do nothing here.
     */
    public void onCreate() {
        // Do nothing
    }

    /**
     * Called when the last RemoteViewsAdapter that is associated with this factory is unbound.
     *
     * We will clean up the cursor if there is one left in use.
     */
    public void onDestroy() {

        // Clean up if needed
        if (mCursor != null) {
            mCursor.close();
        }

    }

    /**
     * How many items are in the data set represented by this Adapter.
     *
     * @return The number of the items to be displayed
     */
    public int getCount() {
        return mCursor.getCount();
    }

    public RemoteViews getViewAt(int position) {

        // The values that will be displayed to the user
        String homeName = "";
        String awayName = "";
        String dateString = "";
        String score = "";

        // Extract the data from the DB cursor
        if (mCursor.moveToPosition(position)) {

            //
            // Use named columns - not indexes!  The reason being that we might change the code in
            // the future to not request all of the columns, just some of them.  Thus, our hard
            // coded column indexes would be wrong.
            //

            // Use the names of the DB columns to get the index used for this cursor
            final int homeNameIndex = mCursor.getColumnIndex(DatabaseContract.scores_table.HOME_COL);
            final int awayNameIndex = mCursor.getColumnIndex(DatabaseContract.scores_table.AWAY_COL);
            final int dateStringIndex = mCursor.getColumnIndex(DatabaseContract.scores_table.TIME_COL);
            final int homeGoalsIndex = mCursor.getColumnIndex(DatabaseContract.scores_table.HOME_GOALS_COL);
            final int awayGoalsIndex = mCursor.getColumnIndex(DatabaseContract.scores_table.AWAY_GOALS_COL);

            // Use the indexes to extract the needed data
            homeName = mCursor.getString(homeNameIndex);
            awayName = mCursor.getString(awayNameIndex);
            dateString = mCursor.getString(dateStringIndex);
            score = Utilies.getScores(mCursor.getInt(homeGoalsIndex), mCursor.getInt(awayGoalsIndex));

        }

        RemoteViews remoteViews = new RemoteViews(mContext.getPackageName(), R.layout.widget_scores_list_item);

        remoteViews.setTextViewText(R.id.home_name, homeName);
        remoteViews.setTextViewText(R.id.away_name, awayName);
        remoteViews.setTextViewText(R.id.score_textview, score);
        remoteViews.setTextViewText(R.id.data_textview, dateString);

        return remoteViews;

    }

    public RemoteViews getLoadingView() {
        // We aren't going to return a default loading view in this sample
        return null;
    }

    public int getViewTypeCount() {
        return 1;
    }

    public long getItemId(int position) {
        return position;
    }

    public boolean hasStableIds() {
        return true;
    }

    public void onDataSetChanged() {

        // Refresh the cursor
        if (mCursor != null) {
            mCursor.close();
        }

        // Extract configuration information for this widget
        int dayOffset = ScoresAppWidgetConfigureActivity.getDayOffset(mContext, mAppWidgetId);

        Date date = new Date(System.currentTimeMillis() + ((dayOffset) * mContext.getResources().getInteger(R.integer.NUMBER_OF_MILISECONDS_IN_A_DAY)));
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(mContext.getResources().getString(R.string.DATE_FORMAT));
        String[] selectionArgs = new String[1];
        selectionArgs[0] = simpleDateFormat.format(date);

        // Query the DB for the games for today
        mCursor = mContext.getContentResolver().query(DatabaseContract.scores_table.buildScoreWithDate(), null, null, selectionArgs, null);
    }

}
