package it.jaschke.alexandria;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
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

import it.jaschke.alexandria.barcode.BarcodeCaptureActivity;
import it.jaschke.alexandria.data.AlexandriaContract;
import it.jaschke.alexandria.receivers.NetworkReceiver;
import it.jaschke.alexandria.services.BookService;
import it.jaschke.alexandria.services.DownloadImage;


import com.google.android.gms.common.api.CommonStatusCodes;
import com.google.android.gms.vision.barcode.Barcode;


public class AddBook extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {

    private static final String TAG = "INTENT_TO_SCAN_ACTIVITY";

    private EditText eanEditText;

    private final int FOUND_LOADER_ID = 1;
    private final int NOT_FOUND_LOADER_ID = 2;

    private View rootView;

    private final String EAN_CONTENT="eanContent";

    private static final String SCAN_FORMAT = "scanFormat";
    private static final String SCAN_CONTENTS = "scanContents";

    private String mScanFormat = "Format:";
    private String mScanContents = "Contents:";

    private static final int RC_BARCODE_CAPTURE = 9001;

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

                String ean = s.toString();

                //catch isbn10 numbers
                if(ean.length()==10 && !ean.startsWith("978")){
                    ean="978"+ean;
                }

                if(ean.length()<13){
                    clearFields();
                    return;
                }

                //Once we have an ISBN, start a book intent
                Intent bookIntent = new Intent(getActivity(), BookService.class);
                bookIntent.putExtra(BookService.EAN, ean);
                bookIntent.setAction(BookService.FETCH_BOOK);
                getActivity().startService(bookIntent);
                AddBook.this.restartLoaders();





//               need to have another loader that is monitoring the EAN table






            }
        });

        rootView.findViewById(R.id.scan_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                // TODO: 2/23/16 - put in words explaining the missing functionality now being added


                // Get the preference settings for auto focus and auto flash
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

        rootView.findViewById(R.id.save_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                eanEditText.setText("");
            }
        });

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

        if(savedInstanceState!=null){
            eanEditText.setText(savedInstanceState.getString(EAN_CONTENT));
            eanEditText.setHint("");
        }

        return rootView;
    }

    // TODO: 2/17/16 - put in fixed comment from example app
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
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
        getLoaderManager().restartLoader(FOUND_LOADER_ID, null, this);
        getLoaderManager().restartLoader(NOT_FOUND_LOADER_ID, null, this);
    }

    @Override
    public android.support.v4.content.Loader<Cursor> onCreateLoader(int id, Bundle args) {

        // Check for a valid EAN number to work with
        if (eanEditText.getText().length() == 0) {
            return null;
        }

        android.support.v4.content.Loader<Cursor> result = null;

        // There are different loaders
        switch (id) {
            case FOUND_LOADER_ID: {
                // TODO: 2/25/16 - not sure, but i thing the if below is a bug, http://www.makebarcode.com/specs/bookland.html

                String eanStr= eanEditText.getText().toString();
                if(eanStr.length()==10 && !eanStr.startsWith("978")){
                    eanStr="978"+eanStr;
                }

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
            case NOT_FOUND_LOADER_ID: {
                break;
            }
            default: {
                break;
            }
        }

        return result;
    }

    @Override
    public void onLoadFinished(android.support.v4.content.Loader<Cursor> loader, Cursor data) {

        // There are different loaders
        switch (loader.getId()) {
            case FOUND_LOADER_ID: {

                if (!data.moveToFirst()) {
                    return;
                }

                String bookTitle = data.getString(data.getColumnIndex(AlexandriaContract.BookEntry.TITLE));
                ((TextView) rootView.findViewById(R.id.bookTitle)).setText(bookTitle);

                String bookSubTitle = data.getString(data.getColumnIndex(AlexandriaContract.BookEntry.SUBTITLE));
                ((TextView) rootView.findViewById(R.id.bookSubTitle)).setText(bookSubTitle);

                String authors = data.getString(data.getColumnIndex(AlexandriaContract.AuthorEntry.AUTHOR));
                String[] authorsArr = authors.split(",");
                ((TextView) rootView.findViewById(R.id.authors)).setLines(authorsArr.length);
                ((TextView) rootView.findViewById(R.id.authors)).setText(authors.replace(",","\n"));
                String imgUrl = data.getString(data.getColumnIndex(AlexandriaContract.BookEntry.IMAGE_URL));
                if(Patterns.WEB_URL.matcher(imgUrl).matches()){
                    new DownloadImage((ImageView) rootView.findViewById(R.id.bookCover)).execute(imgUrl);
                    rootView.findViewById(R.id.bookCover).setVisibility(View.VISIBLE);
                }

                String categories = data.getString(data.getColumnIndex(AlexandriaContract.CategoryEntry.CATEGORY));
                ((TextView) rootView.findViewById(R.id.categories)).setText(categories);

                rootView.findViewById(R.id.save_button).setVisibility(View.VISIBLE);
                rootView.findViewById(R.id.delete_button).setVisibility(View.VISIBLE);

                break;
            }
            case NOT_FOUND_LOADER_ID: {


                break;
            }
            default: {
                break;
            }
        }

    }

    @Override
    public void onLoaderReset(android.support.v4.content.Loader<Cursor> loader) {
        // Do nothing
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

    @Override
    public void onResume() {
        super.onResume();

        ComponentName receiver = new ComponentName(getContext(), NetworkReceiver.class);
        PackageManager pm = getContext().getPackageManager();
        pm.setComponentEnabledSetting(receiver, PackageManager.COMPONENT_ENABLED_STATE_ENABLED, PackageManager.DONT_KILL_APP);
    }

    @Override
    public void onPause() {
        super.onPause();

        ComponentName receiver = new ComponentName(getContext(), NetworkReceiver.class);
        PackageManager pm = getContext().getPackageManager();
        pm.setComponentEnabledSetting(receiver, PackageManager.COMPONENT_ENABLED_STATE_DISABLED, PackageManager.DONT_KILL_APP );
    }

    // TODO: 2/28/16 - fix the deprecated interface
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        activity.setTitle(R.string.scan);
    }
}
