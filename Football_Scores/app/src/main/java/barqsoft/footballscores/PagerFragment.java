package barqsoft.footballscores;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.text.format.Time;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by yehya khaled on 2/27/2015.
 */
public class PagerFragment extends Fragment {

    public static final int NUM_PAGES = 5;
    public ViewPager mPagerHandler;
    private myPageAdapter mPagerAdapter;

    /**
     * The number of {@link MainScreenFragment} to hold and display
     */
    private MainScreenFragment[] viewFragments = new MainScreenFragment[NUM_PAGES];

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.pager_fragment, container, false);
        mPagerHandler = (ViewPager) rootView.findViewById(R.id.pager);
        mPagerAdapter = new myPageAdapter(getChildFragmentManager());
        for (int i = 0; i < NUM_PAGES; i++) {
            Date fragmentdate = new Date(System.currentTimeMillis() + ((i - 2) * getResources().getInteger(R.integer.NUMBER_OF_MILISECONDS_IN_A_DAY)));
            SimpleDateFormat mformat = new SimpleDateFormat(getResources().getString(R.string.DATE_FORMAT));
            viewFragments[i] = new MainScreenFragment();
            viewFragments[i].setFragmentDate(mformat.format(fragmentdate));
        }
        mPagerHandler.setAdapter(mPagerAdapter);
        mPagerHandler.setCurrentItem(MainActivity.mCurrentFragment);

        return rootView;
    }

    private class myPageAdapter extends FragmentStatePagerAdapter {

        public myPageAdapter(FragmentManager fm) {
            super(fm);
        }

        /**
         * Gets the item specified at the given index.  In this case it is a
         * {@link MainScreenFragment} at the specified index.
         *
         * @param index
         * @return The {@link MainScreenFragment} at the given index
         */
        @Override
        public Fragment getItem(int index) {
            return viewFragments[index];
        }

        /**
         * This display will always have a specified number of displayed pages of dated data.
         *
         * This method is overridden because the base class will want to know how many items it has.
         * Thus, we will return the number of pages always.
         *
         * @return The number of days of data that will be displayed
         */
        @Override
        public int getCount() {
            return NUM_PAGES;
        }

        /**
         * Returns the page title for the top indicator.
         *
         * @param position
         * @return
         */
        @Override
        public CharSequence getPageTitle(int position) {
            return getDayName(getActivity(), System.currentTimeMillis() + ((position - 2) * getResources().getInteger(R.integer.NUMBER_OF_MILISECONDS_IN_A_DAY)));
        }

        /**
         * Takes the time in milliseconds and converts it into a named day of the week.
         *
         * @param context
         * @param dateInMillis
         * @return
         */
        public String getDayName(Context context, long dateInMillis) {

            // If the date is today, return the localized version of "Today" instead of the actual day name.

            Time t = new Time();
            t.setToNow();

            int julianDay = Time.getJulianDay(dateInMillis, t.gmtoff);
            int currentJulianDay = Time.getJulianDay(System.currentTimeMillis(), t.gmtoff);

            if (julianDay == currentJulianDay) {
                return context.getString(R.string.today);
            } else if (julianDay == currentJulianDay + 1) {
                return context.getString(R.string.tomorrow);
            } else if (julianDay == currentJulianDay - 1) {
                return context.getString(R.string.yesterday);
            } else {

                // Get the current time and return the named representation of the day of the week (i.e. Friday)
                Time time = new Time();
                time.setToNow();
                SimpleDateFormat dayFormat = new SimpleDateFormat(getResources().getString(R.string.DAY_OF_WEEK_FORMAT));
                return dayFormat.format(dateInMillis);

            }

        }
    }
}
