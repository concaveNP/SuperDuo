package barqsoft.footballscores;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

public class MainActivity extends ActionBarActivity {

    /**
     * The logging tag string to be associated with log data for this class
     */
    private static final String TAG = MainActivity.class.getSimpleName();

    /**
     *
     */
    public static int mSelectedMatchId;

    /**
     *
     */
    public static int mCurrentFragment = 2;

    // TODO: 3/23/16 - This could be a bug, dunno if saving this instance value of a reference is a good idea
    /**
     *
     */
    private PagerFragment pagerFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Log.d(TAG, "Reached MainActivity onCreate");

        if (savedInstanceState == null) {

            pagerFragment = new PagerFragment();
            getSupportFragmentManager().beginTransaction().add(R.id.container, pagerFragment).commit();

        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);

        return true;

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_about) {
            Intent start_about = new Intent(this, AboutActivity.class);
            startActivity(start_about);
            return true;
        }

        return super.onOptionsItemSelected(item);

    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {

        // Log the states that will be saved
        Log.v(TAG, "will save");
        Log.v(TAG, "fragment: " + String.valueOf(pagerFragment.mPagerHandler.getCurrentItem()));
        Log.v(TAG, "selected id: " + mSelectedMatchId);

        // Save the current states
        outState.putInt(getResources().getString(R.string.PAGER_CURRENT), pagerFragment.mPagerHandler.getCurrentItem());
        outState.putInt(getResources().getString(R.string.SELECTED_MATCH), mSelectedMatchId);
        getSupportFragmentManager().putFragment(outState, getResources().getString(R.string.PAGER_FRAGMENT), pagerFragment);

        super.onSaveInstanceState(outState);

    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {

        // Extract the saved states
        mCurrentFragment = savedInstanceState.getInt(getResources().getString(R.string.PAGER_CURRENT));
        mSelectedMatchId = savedInstanceState.getInt(getResources().getString(R.string.SELECTED_MATCH));
        pagerFragment = (PagerFragment) getSupportFragmentManager().getFragment(savedInstanceState, getResources().getString(R.string.PAGER_FRAGMENT));

        // Log the states that will be extracted
        Log.v(TAG, "will retrieve");
        Log.v(TAG, "fragment: " + mCurrentFragment);
        Log.v(TAG, "selected id: " + mSelectedMatchId);

        super.onRestoreInstanceState(savedInstanceState);

    }

}

