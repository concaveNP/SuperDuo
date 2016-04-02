package it.jaschke.alexandria;

import android.app.Activity;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.widget.CursorAdapter;
import android.support.v4.widget.SimpleCursorAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import it.jaschke.alexandria.api.Callback;
import it.jaschke.alexandria.data.AlexandriaContract;
import it.jaschke.alexandria.services.DownloadImage;


public class ListOfBooks extends AlexandriaFragment implements LoaderManager.LoaderCallbacks<Cursor> {

    private ListView bookList;
    private int position = ListView.INVALID_POSITION;
    private EditText searchText;

    /**
     * The Adapter which will be used to populate the ListView with Views.
     */
    private SimpleCursorAdapter mAdapter;

    private static final ResultsViewBinder VIEW_BINDER = new ResultsViewBinder();

    private static final String[] FROM = {
            AlexandriaContract.BookEntry.IMAGE_URL,
            AlexandriaContract.BookEntry.TITLE,
            AlexandriaContract.BookEntry.SUBTITLE
    };

    private static final int[] TO = {
            R.id.fullBookCover,
            R.id.listBookTitle,
            R.id.listBookSubTitle
    };

    private final int LOADER_ID = 4;

    /**
     * Default constructor
     */
    public ListOfBooks() {
        // Do nothing
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        // Initialize the content starting a DB loader
        getLoaderManager().initLoader(LOADER_ID, null, this);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        //
        // BUG: This is an unfinished implementation bug, the class was built with the intent of
        // using a LoadManager, but never uses it.  This DB query was being done on the GUI thread.
        //

        // Obtain view references
        View rootView = inflater.inflate(R.layout.fragment_list_of_books, container, false);
        searchText = (EditText) rootView.findViewById(R.id.searchText);
        bookList = (ListView) rootView.findViewById(R.id.listOfBooks);

        // Link data to views
        mAdapter = new SimpleCursorAdapter(getActivity(), R.layout.book_list_item, null, FROM, TO, 0);
        mAdapter.setViewBinder(VIEW_BINDER);
        bookList.setAdapter(mAdapter);

        // Listen for the search button click
        rootView.findViewById(R.id.searchButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ListOfBooks.this.restartLoader();
            }
        });

        // Listen for individual item selection
        bookList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                Cursor cursor = mAdapter.getCursor();
                if (cursor != null && cursor.moveToPosition(position)) {
                    ((Callback)getActivity())
                            .onItemSelected(cursor.getString(cursor.getColumnIndex(AlexandriaContract.BookEntry._ID)));
                }
            }
        });

        return rootView;
    }

    private void restartLoader(){
        getLoaderManager().restartLoader(LOADER_ID, null, this);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {

        final String selection = AlexandriaContract.BookEntry.TITLE +" LIKE ? OR " + AlexandriaContract.BookEntry.SUBTITLE + " LIKE ? ";
        String searchString = "";

        // Check to see if we have something to work with yet.  It could be the loader is being
        // put into action before the views are created.
        if ((searchText != null) && (searchText.getText() != null)) {
            searchString = searchText.getText().toString();
        }

        // There are two different CursorLoaders that could be used.  First check to see if the
        // user is searching for a specific book by using a filter.  Otherwise, grab everything.

        // Filtered books
        if(searchString.length()>0){
            searchString = "%"+searchString+"%";
            return new CursorLoader(
                    getActivity(),
                    AlexandriaContract.BookEntry.CONTENT_URI,
                    null,
                    selection,
                    new String[]{searchString,searchString},
                    null
            );
        }

        // All books
        return new CursorLoader(
                getActivity(),
                AlexandriaContract.BookEntry.CONTENT_URI,
                null,
                null,
                null,
                null
        );
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {

        mAdapter.swapCursor(data);

        if (position != ListView.INVALID_POSITION) {
            bookList.smoothScrollToPosition(position);
        }

    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mAdapter.swapCursor(null);
    }

    // TODO: 2/23/16 - fix bug with using older interface call
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        activity.setTitle(R.string.books);
    }

    private static class ResultsViewBinder implements SimpleCursorAdapter.ViewBinder {

        @Override
        public boolean setViewValue(View view, Cursor cursor, int columnIndex) {

            switch (view.getId()) {

                case R.id.fullBookCover: {
                    String imgUrl = cursor.getString(columnIndex);
                    new DownloadImage((ImageView)view).execute(imgUrl);
                    return true;
                }
                case R.id.listBookTitle: {
                    String bookTitle = cursor.getString(columnIndex);
                    ((TextView) view).setText(bookTitle);
                    return true;
                }
                case R.id.listBookSubTitle: {
                    String bookSubTitle = cursor.getString(columnIndex);
                    ((TextView) view).setText(bookSubTitle);
                    return true;
                }
            }

            return false;
        }
    }

}
