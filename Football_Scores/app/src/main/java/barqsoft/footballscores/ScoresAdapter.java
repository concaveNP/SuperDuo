package barqsoft.footballscores;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.support.v4.widget.CursorAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

/**
 * Created by yehya khaled on 2/26/2015.
 */
public class ScoresAdapter extends CursorAdapter {

    public static final int COL_HOME = 3;
    public static final int COL_AWAY = 4;
    public static final int COL_HOME_GOALS = 6;
    public static final int COL_AWAY_GOALS = 7;
    public static final int COL_DATE = 1;
    public static final int COL_LEAGUE = 5;
    public static final int COL_MATCHDAY = 9;
    public static final int COL_ID = 8;
    public static final int COL_MATCHTIME = 2;

    public double detail_match_id = 0;

    /**
     * In order to reuse this Adapter class for both the app and the widget an additional perameter
     * is being given to distinguish one from the other.  Widget adapters will not additionally
     * give the user the ability to issue sharing messages.
     *
     * @param context
     * @param cursor
     * @param flags
     * @param app - True when this adaption should also be used for placing data within extra view
     *            details displayed to the user and false if this adapter is to be used within a
     *            widget.
     */
    public ScoresAdapter(Context context, Cursor cursor, int flags, boolean app) {

        super(context, cursor, flags);

    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {

        View mItem = LayoutInflater.from(context).inflate(R.layout.scores_list_item, parent, false);
        ViewHolder mHolder = new ViewHolder(mItem);
        mItem.setTag(mHolder);

        return mItem;

    }

    @Override
    public void bindView(View view, final Context context, Cursor cursor) {

        // The tag holds the ViewHolder that will be used to place the data into views
        final ViewHolder mHolder = (ViewHolder) view.getTag();

        // Place data into views
        mHolder.home_name.setText(cursor.getString(COL_HOME));
        mHolder.away_name.setText(cursor.getString(COL_AWAY));
        mHolder.date.setText(cursor.getString(COL_MATCHTIME));
        mHolder.score.setText(Utilies.getScores(cursor.getInt(COL_HOME_GOALS), cursor.getInt(COL_AWAY_GOALS)));
        mHolder.match_id = cursor.getDouble(COL_ID);
        mHolder.home_crest.setImageResource(Utilies.getTeamCrestByTeamName( cursor.getString(COL_HOME)));
        mHolder.away_crest.setImageResource(Utilies.getTeamCrestByTeamName( cursor.getString(COL_AWAY)
        ));

        LayoutInflater vi = (LayoutInflater) context.getApplicationContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View v = vi.inflate(R.layout.detail_fragment, null);
        ViewGroup container = (ViewGroup) view.findViewById(R.id.details_fragment_container);

        if (mHolder.match_id == detail_match_id) {

            container.addView(v, 0, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
            TextView match_day = (TextView) v.findViewById(R.id.matchday_textview);
            match_day.setText(Utilies.getMatchDay(cursor.getInt(COL_MATCHDAY), cursor.getInt(COL_LEAGUE)));
            TextView league = (TextView) v.findViewById(R.id.league_textview);
            league.setText(Utilies.getLeague(cursor.getInt(COL_LEAGUE)));
            Button share_button = (Button) v.findViewById(R.id.share_button);
            share_button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    //add Share Action
                    context.startActivity(
                            createShareForecastIntent(
                                    mHolder.home_name.getText() + " " + mHolder.score.getText() + " " + mHolder.away_name.getText() + " "));
                }
            });
        } else {
            container.removeAllViews();
        }

    }

    /**
     * Method will start an intent that will share the given text with someone.
     *
     * NOTE:  This could be considered a BUG as it was publicly exposed.
     *
     * @param ShareText - The text to share
     * @return The created intent
     */
    private Intent createShareForecastIntent(String ShareText) {

        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        //
        // BUG: (kinda)
        // The FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET flag is deprecated in ver 21 and
        // FLAG_ACTIVITY_NEW_DOCUMENT should be used instead.
        //
        shareIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_DOCUMENT);
        shareIntent.setType(mContext.getResources().getString(R.string.SHARE_MIME_TYPE));
        shareIntent.putExtra(Intent.EXTRA_TEXT, ShareText + mContext.getString(R.string.HASHTAG_FOOTBALL_SCORES));

        return shareIntent;

    }

}

