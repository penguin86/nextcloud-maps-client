package it.danieleverducci.nextcloudmaps.utils;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.preference.PreferenceManager;
import it.danieleverducci.nextcloudmaps.activity.main.GeofavoriteAdapter;

public class SettingsManager {
    static private final String SETTING_SORT_BY = "SETTING_SORT_BY";
    static private final String SETTING_LAST_SELECTED_LIST_VIEW = "SETTING_LAST_SELECTED_LIST_VIEW";
    static private final String SETTING_LAST_MAP_POSITION_LAT = "SETTING_LAST_MAP_POSITION_LAT";
    static private final String SETTING_LAST_MAP_POSITION_LNG = "SETTING_LAST_MAP_POSITION_LNG";
    static private final String SETTING_LAST_MAP_POSITION_ZOOM = "SETTING_LAST_MAP_POSITION_ZOOM";

    public static int getGeofavoriteListSortBy(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context)
                .getInt(SETTING_SORT_BY, GeofavoriteAdapter.SORT_BY_CREATED);
    }

    public static void setGeofavoriteListSortBy(Context context, int value) {
        PreferenceManager.getDefaultSharedPreferences(context)
            .edit().putInt(SETTING_SORT_BY, value).apply();
    }

    public static boolean isGeofavoriteListShownAsMap(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context)
                .getBoolean(SETTING_LAST_SELECTED_LIST_VIEW, false);
    }

    public static void setGeofavoriteListShownAsMap(Context context, boolean value) {
        PreferenceManager.getDefaultSharedPreferences(context)
            .edit().putBoolean(SETTING_LAST_SELECTED_LIST_VIEW, value).apply();
    }

    /**
     * Returns the last saved position
     * @param context
     * @return double[lat,lng,zoom]
     */
    public static double[] getLastMapPosition(Context context) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        return new double[] {
            (double) sp.getFloat(SETTING_LAST_MAP_POSITION_LAT, 0.0f),
            (double) sp.getFloat(SETTING_LAST_MAP_POSITION_LNG, 0.0f),
            (double) sp.getFloat(SETTING_LAST_MAP_POSITION_ZOOM, 10.0f),
        };
    }

    public static void setLastMapPosition(Context context, double lat, double lng, double zoom) {
        PreferenceManager.getDefaultSharedPreferences(context).edit()
                .putFloat(SETTING_LAST_MAP_POSITION_LAT, (float)lat)
                .putFloat(SETTING_LAST_MAP_POSITION_LNG, (float)lng)
                .putFloat(SETTING_LAST_MAP_POSITION_ZOOM, (float)zoom)
                .apply();
    }
}
