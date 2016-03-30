package it.jaschke.alexandria;

import android.content.ComponentName;
import android.content.pm.PackageManager;
import android.support.v4.app.Fragment;
import android.util.Log;

import it.jaschke.alexandria.receivers.NetworkReceiver;

/**
 * This Fragment layer was put here in order to co-locate common functionality that ever fragment
 * in this app will need to perform.  Essentially, starting and stopping the BroadcastReceiver of
 * network status intents the Android OS will provide to us.  It is good practice to stop listening
 * to these network status updates when you don't care about them.  Basically, when the app is
 * paused for some reason.  If you were to not stop listening the app would keep running and be a
 * resource drain to the device.
 */
public abstract class AlexandriaFragment extends Fragment {

    /**
     * The logging tag string to be associated with log data for this class
     */
    private static final String TAG = AlexandriaFragment.class.getSimpleName();

    /**
     * Overridden in order to start this app's {@link NetworkReceiver} in order to receive
     * network status intents.  The reason being that it will keep the application alive when it is
     * paused for some reason.  So, we don't want that.  Hence, we turn it back on once the app
     * resumes.
     */
    @Override
    public void onResume() {

        super.onResume();

        // We now care about being informed of the network state
        ComponentName receiver = new ComponentName(getContext(), NetworkReceiver.class);
        PackageManager pm = getContext().getPackageManager();
        pm.setComponentEnabledSetting(receiver, PackageManager.COMPONENT_ENABLED_STATE_ENABLED, PackageManager.DONT_KILL_APP);

        // Log the status
        Log.d(TAG, "Enabling the " + NetworkReceiver.class.getSimpleName() + "...");
    }

    /**
     * Overridden in order to stop this app's {@link NetworkReceiver} from continually receiving
     * network status intents.  The reason being that it will keep the application alive when it is
     * paused for some reason.  So, we don't want that.  Hence, we turn off our receiver while
     * this app is paused.
     */
    @Override
    public void onPause() {

        super.onPause();

        // While paused we don't care about being informed of the network state
        ComponentName receiver = new ComponentName(getContext(), NetworkReceiver.class);
        PackageManager pm = getContext().getPackageManager();
        pm.setComponentEnabledSetting(receiver, PackageManager.COMPONENT_ENABLED_STATE_DISABLED, PackageManager.DONT_KILL_APP );

        // Log the status
        Log.d(TAG, "Disabling the " + NetworkReceiver.class.getSimpleName() + "...");

    }

}
