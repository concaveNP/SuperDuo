package it.jaschke.alexandria.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.util.Log;

import it.jaschke.alexandria.services.NetworkConnectivityStatus;

/**
 * This receiver will be notified when the network connectivity state of Android device changes.
 * It is specifically used to interact with the {@link NetworkConnectivityStatus} in order to
 * check the state for the purpose of processing any scanned IBSN numbers that have not yet been
 * processed.
 *
 * References:
 * - http://developer.android.com/training/monitoring-device-state/connectivity-monitoring.html
 * - http://developer.android.com/training/monitoring-device-state/manifest-receivers.html
 */
public class NetworkReceiver extends BroadcastReceiver {

    /**
     * The logging tag string to be associated with log data for this class
     */
    private static final String TAG = NetworkReceiver.class.getSimpleName();

    /**
     * Default constructor
     */
    public NetworkReceiver() {
        // Do nothing
    }

    /**
     * This method is called when the BroadcastReceiver is receiving an Intent broadcast for a
     * network connectivity change.
     *
     * @param context - The context in which the receiver is running
     * @param intent - The intent being received
     */
    @Override
    public void onReceive(Context context, Intent intent) {

        // We just received a change in state for the network connectivity
        boolean noConnection = intent.getBooleanExtra(ConnectivityManager.EXTRA_NO_CONNECTIVITY, false);

        if (noConnection) {
            Log.i(TAG, "ConnectivityChangeReceiver" + ": " + "No Connection");
        }
        else {
            Log.i(TAG, "ConnectivityChangeReceiver" + ": " + "Connection Established");
        }

        // Notify the singleton of the device status (I changed the polarity for my sanity)
        NetworkConnectivityStatus.performNetworkRequiredLookups(!noConnection);

    }
}
