package it.jaschke.alexandria.services;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

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

    private static NetworkConnectivityStatus sInstance = null;

    private boolean mConnected;

    /**
     * Private in order to prevent instantiation by others than this class.
     *
     * This constructor will make an initial call for the state of connectivity.  Thereafter, the
     * {@link it.jaschke.alexandria.receivers.NetworkReceiver} will update the connectivity state.
     */
    protected NetworkConnectivityStatus() {

        ConnectivityManager cm = (ConnectivityManager) MainActivity.getAppContext().getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();

        setConnected(activeNetwork != null && activeNetwork.isConnectedOrConnecting());
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
