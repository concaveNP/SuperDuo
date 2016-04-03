package barqsoft.footballscores.widget;

import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.widget.RemoteViews;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import barqsoft.footballscores.R;
import barqsoft.footballscores.service.FetchScoresService;

/**
 * Implementation of App Widget functionality.
 * App Widget Configuration implemented in {@link ScoresAppWidgetConfigureActivity ScoresAppWidgetConfigureActivity}
 */
public class ScoresAppWidgetProvider extends AppWidgetProvider {

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {

        // There may be multiple widgets active, so update all of them
        for (int index = 0; index < appWidgetIds.length; ++index) {

            // Set up the intent that starts the StackViewService, which will provide the views for this collection.
            Intent intent = new Intent(context, ScoresAppWidgetService.class);

            // Add the app widget ID to the intent extras
            intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetIds[index]);
            intent.setData(Uri.parse(intent.toUri(Intent.URI_INTENT_SCHEME)));

            // Instantiate the RemoteViews object for the app widget layout.
            RemoteViews rv = new RemoteViews(context.getPackageName(), R.layout.scores_app_widget);

            // Set up the RemoteViews object to use a RemoteViews adapter.
            // This adapter connects to a RemoteViewsService  through the specified intent.
            // This is how you populate the data.
            rv.setRemoteAdapter(index, R.id.widget_Score_ListView, intent);

            // The empty view is displayed when the collection has no items.
            // It should be in the same layout used to instantiate the RemoteViews
            // object above.
            rv.setEmptyView(R.id.widget_Score_ListView, R.id.empty_view);

            CharSequence widgetText = ScoresAppWidgetConfigureActivity.loadTitlePref(context, appWidgetIds[index]);

            // Get the current date
            Date date = Calendar.getInstance().getTime();

            // Get the Day of the Week
            SimpleDateFormat dowFormat = new SimpleDateFormat(context.getResources().getString(R.string.DAY_OF_WEEK_SHORT_FORMAT));
            String finalDay = dowFormat.format(date);

            // Construct the RemoteViews object
            rv.setTextViewText(R.id.appwidget_text, widgetText);

            // Update the "header" part of the widget with the day of the week and date
            rv.setTextViewText(R.id.dayOfTheWeek_TextView, finalDay);

            // Instruct the widget manager to update the widget
            appWidgetManager.updateAppWidget(appWidgetIds[index], rv);

        }

    }

    /**
     * Removes the preferences configuration associated with this widget instance.
     *
     * @param context
     * @param appWidgetIds
     */
    @Override
    public void onDeleted(Context context, int[] appWidgetIds) {

        // When the user deletes the widget, delete the preference associated with it.
        for (int appWidgetId : appWidgetIds) {

            ScoresAppWidgetConfigureActivity.deleteTitlePref(context, appWidgetId);

        }
    }

    /**
     * This will be called with the widget is first created.  It will issue an intent for the score
     * fetching service to provide updates with relevant data.
     *
     * NOTE FROM GENERATED CODE: Enter relevant functionality for when the first widget is created
     *
     * @param context
     */
    @Override
    public void onEnabled(Context context) {

        // Start the scores retrieval service (if not already done)
        Intent service_start = new Intent(context, FetchScoresService.class);
        context.startService(service_start);

    }

    /**
     * This will be called with the widget is disabled.  It will issue an intent for the score
     * fetching service to stop providing updates.  The service will stop for the app as well, but
     * the app will restart the service upon bringing up the main fragment again.
     *
     * NOTE FROM GENERATED CODE: Enter relevant functionality for when the last widget is disabled
     *
     * @param context
     */
    @Override
    public void onDisabled(Context context) {

        // Stop the scores retrieval service (the app will restart it if needed)
        Intent service_stop = new Intent(context, FetchScoresService.class);
        context.stopService(service_stop);

        // TODO: 3/15/16 - NOTE: if there are multiple instances of widgets deployed it might make
        // sense to keep a static count of them in order not to interrupt the service (investigate
        // if time allows)

    }

}

