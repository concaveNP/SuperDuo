package barqsoft.footballscores;

import android.content.Context;

/**
 * Created by yehya khaled on 3/3/2015.
 */
public class Utilies {

    public static String getLeague(int league_num) {

        Context context = MainActivity.getAppContext();

        if (league_num == context.getResources().getInteger(R.integer.SERIE_A_LEAGUE_NUMBER)) {
            return context.getResources().getString(R.string.SERIA_A_LEAGUE);
        } else if (league_num == context.getResources().getInteger(R.integer.PREMIER_LEAGUE_LEAGUE_NUMBER)) {
            return context.getResources().getString(R.string.PREMIER_LEAGUE);
        } else if (league_num == context.getResources().getInteger(R.integer.CHAMPIONS_LEAGUE_LEAGUE_NUMBER)) {
            return context.getResources().getString(R.string.UEFA_CHAMPIONS_LEAGUE);
        } else if (league_num == context.getResources().getInteger(R.integer.PRIMERA_DIVISION_LEAGUE_NUMBER)) {
            return context.getResources().getString(R.string.PRIMERA_DIVISION_LEAGUE);
        } else if (league_num == context.getResources().getInteger(R.integer.BUNDESLIGA_LEAGUE_NUMBER)) {
            return context.getResources().getString(R.string.BUNDESLIGA_LEAGUE);
        } else {
            return context.getResources().getString(R.string.UNKNOWN_LEAGUE);
        }

    }

    public static String getMatchDay(int match_day, int league_num) {

        Context context = MainActivity.getAppContext();

        if (league_num == context.getResources().getInteger(R.integer.CHAMPIONS_LEAGUE_LEAGUE_NUMBER)) {

            if (match_day <= 6) {
                return context.getResources().getString(R.string.LESS_OR_EQUAL_6_MATCH);
            } else if (match_day == 7 || match_day == 8) {
                return context.getResources().getString(R.string.EQUAL_7_OR_8_MATCH);
            } else if (match_day == 9 || match_day == 10) {
                return context.getResources().getString(R.string.EQUAL_9_OR_10_MATCH);
            } else if (match_day == 11 || match_day == 12) {
                return context.getResources().getString(R.string.EQUAL_11_OR_12_MATCH);
            } else {
                return context.getResources().getString(R.string.ELSE_MATCH);
            }

        } else {

            return context.getResources().getString(R.string.NON_CHAMPIONS_LEAGUE_MATCH);

        }

    }

    public static String getScores(int home_goals, int awaygoals) {

        if (home_goals < 0 || awaygoals < 0) {
            return " - ";
        } else {
            return String.valueOf(home_goals) + " - " + String.valueOf(awaygoals);
        }

    }

    public static int getTeamCrestByTeamName(String teamName) {

        if (teamName == null) {
            return R.drawable.no_icon;
        }

        Context context = MainActivity.getAppContext();

        //This is the set of icons that are currently in the app. Feel free to find and add more as you go.
        if (teamName.equals(context.getResources().getString(R.string.ARSENAL_LONDON_FC_TEAM))) {
            return R.drawable.arsenal;
        } else if (teamName.equals(context.getResources().getString(R.string.MANCHESTER_UNITED_FC_TEAM))) {
            return R.drawable.manchester_united;
        } else if (teamName.equals(context.getResources().getString(R.string.SWANSEA_CITY_TEAM))) {
            return R.drawable.swansea_city_afc;
        } else if (teamName.equals(context.getResources().getString(R.string.LEICESTER_CITY_TEAM))) {
            return R.drawable.leicester_city_fc_hd_logo;
        } else if (teamName.equals(context.getResources().getString(R.string.EVERTON_FC_TEAM))) {
            return R.drawable.everton_fc_logo1;
        } else if (teamName.equals(context.getResources().getString(R.string.WEST_HAM_UNITED_FC_TEAM))) {
            return R.drawable.west_ham;
        } else if (teamName.equals(context.getResources().getString(R.string.TOTTENHAM_HOTSPUR_FC_TEAM))) {
            return R.drawable.tottenham_hotspur;
        } else if (teamName.equals(context.getResources().getString(R.string.WEST_BROMWICH_ALBION_TEAM))) {
            return R.drawable.west_bromwich_albion_hd_logo;
        } else if (teamName.equals(context.getResources().getString(R.string.SUNDERLAND_AFC_TEAM))) {
            return R.drawable.sunderland;
        } else if (teamName.equals(context.getResources().getString(R.string.STOKE_CITY_FC_TEAM_TEAM))) {
            return R.drawable.stoke_city;
        } else {
            return R.drawable.no_icon;
        }

    }

}
