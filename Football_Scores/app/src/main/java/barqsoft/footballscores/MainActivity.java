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

    public static int selected_match_id;
    public static int current_fragment = 2;
    private PagerFragment my_main;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Log.d(TAG, "Reached MainActivity onCreate");

        if (savedInstanceState == null) {

            my_main = new PagerFragment();
            getSupportFragmentManager().beginTransaction().add(R.id.container, my_main).commit();

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
        Log.v(TAG, "fragment: " + String.valueOf(my_main.mPagerHandler.getCurrentItem()));
        Log.v(TAG, "selected id: " + selected_match_id);

        // Save the current states
        outState.putInt(getResources().getString(R.string.PAGER_CURRENT), my_main.mPagerHandler.getCurrentItem());
        outState.putInt(getResources().getString(R.string.SELECTED_MATCH), selected_match_id);
        getSupportFragmentManager().putFragment(outState, "my_main", my_main);

        super.onSaveInstanceState(outState);

    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {

        // Extract the saved states
        current_fragment = savedInstanceState.getInt(getResources().getString(R.string.PAGER_CURRENT));
        selected_match_id = savedInstanceState.getInt(getResources().getString(R.string.SELECTED_MATCH));
        my_main = (PagerFragment) getSupportFragmentManager().getFragment(savedInstanceState, "my_main");

        // Log the states that will be extracted
        Log.v(TAG, "will retrieve");
        Log.v(TAG, "fragment: " + current_fragment);
        Log.v(TAG, "selected id: " + selected_match_id);

        super.onRestoreInstanceState(savedInstanceState);

    }

}

