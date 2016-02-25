package it.jaschke.alexandria.services;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;

import it.jaschke.alexandria.MainActivity;

/**
 * Created by dave on 2/19/16.
 *
 * References:
 *
 * - http://developer.android.com/training/monitoring-device-state/connectivity-monitoring.html
 * - http://developer.android.com/training/monitoring-device-state/manifest-receivers.html
 *
 *   Need to get a context to the class, so I did the following:
 * - http://stackoverflow.com/questions/2002288/static-way-to-get-context-on-android
 */
public class NetworkConnectivityStatus {

    /**
     * The logging tag string to be associated with log data for this class
     */
    private static final String TAG = NetworkConnectivityStatus.class.getSimpleName();

    private static NetworkConnectivityStatus sInstance = null;

    private boolean mConnected;

    /**
     * Private in order to prevent instantiation by others than this class.
     *
     * This constructor will make an initial call for the state of connectivity.  Thereafter, the
     * {@link it.jaschke.alexandria.receivers.NetworkReceiver} will update the connectivity state.
     */
    protected NetworkConnectivityStatus() {

        // TODO: 2/24/16 - there is an error here, dunno why, but it looks like the context is null

        Context context = MainActivity.getAppContext();

        if (context != null) {
            ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);

            NetworkInfo activeNetwork = cm.getActiveNetworkInfo();

            setConnected(activeNetwork != null && activeNetwork.isConnectedOrConnecting());
        }
        else {
            Log.e(TAG, "The context was unavailable for use");
        }
    }

    public static NetworkConnectivityStatus getInstance() {

        if (sInstance == null) {
            sInstance = new NetworkConnectivityStatus();
        }

        return sInstance;
    }

    public boolean isConnected() {
        return mConnected;
    }

    public void setConnected(boolean connected) {
        this.mConnected = connected;
    }

}
