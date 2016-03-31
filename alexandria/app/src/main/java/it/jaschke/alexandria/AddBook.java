package it.jaschke.alexandria;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.os.ResultReceiver;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.common.api.CommonStatusCodes;
import com.google.android.gms.vision.barcode.Barcode;

import it.jaschke.alexandria.barcode.BarcodeCaptureActivity;
import it.jaschke.alexandria.data.AlexandriaContract;
import it.jaschke.alexandria.services.BookService;
import it.jaschke.alexandria.services.DownloadImage;


public class AddBook extends AlexandriaFragment implements LoaderManager.LoaderCallbacks<Cursor> {

    /**
     * The logging tag string to be associated with log data for this class
     */
    private static final String TAG = "INTENT_TO_SCAN_ACTIVITY";

    private EditText eanEditText;

    private final int BOOK_LOADER_ID = 1;
    private final int TODO_LOADER_ID = 2;

    private View rootView;

    private final String EAN_CONTENT = "eanContent";

    private static final int RC_BARCODE_CAPTURE = 9001;

    /**
     * The number of characters in an EAN number
     */
    private static final int EAN_LENGTH = 13;

    /**
     * Default constructor
     */
    public AddBook(){
        // Do nothing
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {

        super.onSaveInstanceState(outState);

        if (eanEditText != null) {
            outState.putString(EAN_CONTENT, eanEditText.getText().toString());
        }

    }

    @Override
    public View onCreateView(final LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        rootView = inflater.inflate(R.layout.fragment_add_book, container, false);
        eanEditText = (EditText) rootView.findViewById(R.id.ean);

        eanEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                //no need
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                //no need
            }

            @Override
            public void afterTextChanged(Editable s) {

                // Get the starting ISBN digits to work with
                final String ISBN_START = getContext().getResources().getString(R.string.STARTING_ISBN);

                String ean = s.toString();

                // Catch ISBN_10 numbers
                if ((ean.length() == 10) && (!ean.startsWith(ISBN_START))) {
                    ean = ISBN_START + ean;
                }

                if (ean.length() < EAN_LENGTH) {
                    clearFields();
                    return;
                }

                Log.d(TAG, "Issuing BookService Intent");

                //
                // Once we have an ISBN, start a book intent.
                //
                // BUG: In addition to the EAN number, provide the intent a {@link ResultReceiver}
                // object in order to inform us of the completed service.  In any situation,
                // if the IntentService indicates a result then we should re-init/restart the
                // loaders.  As the {@link BookService} will add a DB entry of one of the two
                // loaders that this object listens for, we guarantee the loader processing by
                // restarting them after the IntentService completes.  We need to perform this
                // action because the restarting design solution for this object cannot make
                // the guarantee of the loader state upon creation of the intent.  It is possible
                // for the loaders to not yet be restarted and miss the DB update the BookService
                // performs.  So, we make sure it does not get missed.
                //
                Intent bookIntent = new Intent(getActivity(), BookService.class);
                bookIntent.putExtra(BookService.EAN, ean);
                bookIntent.putExtra(BookService.RESULT, new ResultReceiver(new Handler()){
                    @Override
                    protected void onReceiveResult(int resultCode, Bundle resultData) {
                        restartLoaders();
                    }
                });
                bookIntent.setAction(BookService.FETCH_BOOK);
                getActivity().startService(bookIntent);

                // Restart the loaders by looking for fresh data to display
                AddBook.this.restartLoaders();
            }
        });

        rootView.findViewById(R.id.scan_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                // Getting the preference settings for auto focus and auto flash.  Currently, there
                // is no option exposed to the user to manage these settings.  Future enhancement.
                SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getContext());
                Boolean autoFocus = prefs.getBoolean(getResources().getString(R.string.auto_focus_setting), true);
                Boolean autoFlash = prefs.getBoolean(getResources().getString(R.string.auto_flash_setting), false);

                // Launch barcode activity
                Intent intent = new Intent(getContext(), BarcodeCaptureActivity.class);
                intent.putExtra(BarcodeCaptureActivity.AutoFocus, autoFlash);
                intent.putExtra(BarcodeCaptureActivity.UseFlash, autoFocus);
                startActivityForResult(intent, RC_BARCODE_CAPTURE);

            }
        });

        // Nothing is really because the book has already been added to a DB table
        rootView.findViewById(R.id.save_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                eanEditText.setText("");
            }
        });

        // We have to specifically go and remove this book from the DB as the user does not want it
        rootView.findViewById(R.id.delete_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent bookIntent = new Intent(getActivity(), BookService.class);
                bookIntent.putExtra(BookService.EAN, eanEditText.getText().toString());
                bookIntent.setAction(BookService.DELETE_BOOK);
                getActivity().startService(bookIntent);
                eanEditText.setText("");
            }
        });

        // Save the current EAN number to the instant state information
        if (savedInstanceState != null) {
            eanEditText.setText(savedInstanceState.getString(EAN_CONTENT));
            eanEditText.setHint("");
        }

        return rootView;
    }

    // TODO: 2/17/16 - put in fixed comment from example app
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {

        Log.d(TAG, "onActivityResult, with requestCode=" + requestCode + ", and resultCode=" + resultCode);

        if (requestCode == RC_BARCODE_CAPTURE) {
            if (resultCode == CommonStatusCodes.SUCCESS) {
                if (data != null) {
                    Barcode barcode = data.getParcelableExtra(BarcodeCaptureActivity.BarcodeObject);
                    // TODO: 2/17/16 - fix
                    //statusMessage.setText(R.string.barcode_success);

                    //barcodeValue.setText(barcode.displayValue);
                    eanEditText.setText(barcode.displayValue);

                    Log.d(TAG, "Barcode read: " + barcode.displayValue);
                } else {
                    // TODO: 2/17/16 - fix
                    //statusMessage.setText(R.string.barcode_failure);
                    Log.d(TAG, "No barcode captured, intent data is null");
                }
            } else {
                // TODO: 2/17/16 - fix
                //statusMessage.setText(String.format(getString(R.string.barcode_error),
//                        CommonStatusCodes.getStatusCodeString(resultCode)));
            }
        }
        else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    private void restartLoaders(){

        Log.d(TAG, "Restarting loaders");

        getLoaderManager().restartLoader(BOOK_LOADER_ID, null, this);
        getLoaderManager().restartLoader(TODO_LOADER_ID, null, this);

    }

    @Override
    public android.support.v4.content.Loader<Cursor> onCreateLoader(int id, Bundle args) {

        //
        // BUG: I've seen some situations where field can be null/uninitialized and will require
        // checking for a valid EAN number to work with.
        //
        if ((eanEditText == null) || (eanEditText.getText() == null) || (eanEditText.getText().length() == 0)) {
            Log.d(TAG, "the eanEditText appears to be empty, so we are returning");
            return null;
        }

        android.support.v4.content.Loader<Cursor> result = null;

        String eanStr = eanEditText.getText().toString();
        if (eanStr.length() == 10 && !eanStr.startsWith("978")) {
            eanStr = "978" + eanStr;
        }

        switch (id) {
            case BOOK_LOADER_ID: {

                Log.d(TAG, "onCreateLoader: BOOK_LOADER_ID loader entry found");

                // TODO: 2/25/16 - not sure, but i thing the if below is a bug, http://www.makebarcode.com/specs/bookland.html
                result = new CursorLoader(
                        getActivity(),
                        AlexandriaContract.BookEntry.buildFullBookUri(Long.parseLong(eanStr)),
                        null,
                        null,
                        null,
                        null
                );

                break;
            }
            case TODO_LOADER_ID: {

                Log.d(TAG, "onCreateLoader: TODO_LOADER_ID loader entry found");

                result = new CursorLoader(
                        getActivity(),
                        AlexandriaContract.EanEntry.buildEanUri(Long.parseLong(eanStr)),
                        null,
                        null,
                        null,
                        null
                );

                break;
            }
        }

        return result;
    }

    @Override
    public void onLoadFinished(android.support.v4.content.Loader<Cursor> loader, Cursor data) {

        if (!data.moveToFirst()) {
            return;
        }

        switch (loader.getId()) {
            case BOOK_LOADER_ID: {

                Log.d(TAG, "onLoadFinished: BOOK_LOADER_ID loader entry found");

                String bookTitle = data.getString(data.getColumnIndex(AlexandriaContract.BookEntry.TITLE));
                ((TextView) rootView.findViewById(R.id.bookTitle)).setText(bookTitle);

                String bookSubTitle = data.getString(data.getColumnIndex(AlexandriaContract.BookEntry.SUBTITLE));
                ((TextView) rootView.findViewById(R.id.bookSubTitle)).setText(bookSubTitle);

                String authors = data.getString(data.getColumnIndex(AlexandriaContract.AuthorEntry.AUTHOR));
                //
                // BUG: The authors must be checked for as sometimes a null string creeps through
                //
                if ((authors != null) && (!authors.isEmpty())) {
                    String[] authorsArr = authors.split(",");
                    ((TextView) rootView.findViewById(R.id.authors)).setLines(authorsArr.length);
                    ((TextView) rootView.findViewById(R.id.authors)).setText(authors.replace(",", "\n"));
                }
                String imgUrl = data.getString(data.getColumnIndex(AlexandriaContract.BookEntry.IMAGE_URL));
                if (Patterns.WEB_URL.matcher(imgUrl).matches()) {
                    new DownloadImage((ImageView) rootView.findViewById(R.id.bookCover)).execute(imgUrl);
                    rootView.findViewById(R.id.bookCover).setVisibility(View.VISIBLE);
                }

                String categories = data.getString(data.getColumnIndex(AlexandriaContract.CategoryEntry.CATEGORY));
                ((TextView) rootView.findViewById(R.id.categories)).setText(categories);

                rootView.findViewById(R.id.save_button).setVisibility(View.VISIBLE);
                rootView.findViewById(R.id.delete_button).setVisibility(View.VISIBLE);

                break;
            }

            case TODO_LOADER_ID: {

                Log.d(TAG, "onLoadFinished: TODO_LOADER_ID loader entry found");

                ((TextView) rootView.findViewById(R.id.bookTitle)).setText("Unable to establish link");

                ((TextView) rootView.findViewById(R.id.bookSubTitle)).setText("Save book for later processing?");

                rootView.findViewById(R.id.save_button).setVisibility(View.VISIBLE);
                rootView.findViewById(R.id.delete_button).setVisibility(View.VISIBLE);

                break;
            }
        }
    }

    @Override
    public void onLoaderReset(android.support.v4.content.Loader<Cursor> loader) {
        Log.d(TAG, "onLoaderReset: loader being reset");

        // Do nothing, this loader design solution does not require it
    }

    private void clearFields(){
        ((TextView) rootView.findViewById(R.id.bookTitle)).setText("");
        ((TextView) rootView.findViewById(R.id.bookSubTitle)).setText("");
        ((TextView) rootView.findViewById(R.id.authors)).setText("");
        ((TextView) rootView.findViewById(R.id.categories)).setText("");
        rootView.findViewById(R.id.bookCover).setVisibility(View.INVISIBLE);
        rootView.findViewById(R.id.save_button).setVisibility(View.INVISIBLE);
        rootView.findViewById(R.id.delete_button).setVisibility(View.INVISIBLE);
    }

    // TODO: 2/28/16 - fix the deprecated interface
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        activity.setTitle(R.string.scan);
    }

}
