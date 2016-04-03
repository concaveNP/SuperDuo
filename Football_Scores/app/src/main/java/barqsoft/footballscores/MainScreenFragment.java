package barqsoft.footballscores;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import barqsoft.footballscores.database.DatabaseContract;
import barqsoft.footballscores.service.FetchScoresService;

/**
 * A placeholder fragment containing a simple view.
 */
public class MainScreenFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {

    public static final int SCORES_LOADER = 0;
    public ScoresAdapter mAdapter;
    private int last_selected_item = -1;

    /**
     * The date that this fragment will display data for.
     *
     * NOTE:  The array type is to support the loader call.  There will only ever be one.
     */
    private String[] mFragmentDate = new String[1];

    /**
     * The default constructor (that is encourage to remain without args, thus the setters...)
     */
    public MainScreenFragment() {
        // Do nothing
    }

    /**
     * Setter for the date that this fragment will display data for.
     *
     * @param date - The date of this display's data
     */
    public void setFragmentDate(String date) {
        // There is only ever one fragment date associated with this instance (see member comment)
        mFragmentDate[0] = date;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, final Bundle savedInstanceState) {

        // Starts the service that provides data updates to the DB
        Intent service_start = new Intent(getActivity(), FetchScoresService.class);
        getActivity().startService(service_start);

        View rootView = inflater.inflate(R.layout.fragment_main, container, false);

        final ListView score_list = (ListView) rootView.findViewById(R.id.scores_list);
        mAdapter = new ScoresAdapter(getActivity(), null, 0, true);
        score_list.setAdapter(mAdapter);

        getLoaderManager().initLoader(SCORES_LOADER, null, this);

        mAdapter.detail_match_id = MainActivity.mSelectedMatchId;

        score_list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                ViewHolder selected = (ViewHolder) view.getTag();
                mAdapter.detail_match_id = selected.match_id;
                MainActivity.mSelectedMatchId = (int) selected.match_id;
                mAdapter.notifyDataSetChanged();
            }
        });

        return rootView;

    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        return new CursorLoader(
                getActivity(),
                DatabaseContract.scores_table.buildScoreWithDate(),
                null,
                null,
                mFragmentDate,
                null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> cursorLoader, Cursor cursor) {
        //
        // BUG: Lots of extra code in here that was not needed.
        //
        mAdapter.swapCursor(cursor);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> cursorLoader) {
        mAdapter.swapCursor(null);
    }


}
