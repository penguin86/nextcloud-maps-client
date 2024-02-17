package it.danieleverducci.nextcloudmaps.utils;

import android.content.Context;
import androidx.preference.PreferenceManager;
import it.danieleverducci.nextcloudmaps.activity.main.GeofavoriteAdapter;

public class SettingsManager {
    static private final String SETTING_SORT_BY = "SETTING_SORT_BY";
    static private final String SETTING_LAST_SELECTED_LIST_VIEW = "SETTING_LAST_SELECTED_LIST_VIEW";

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
}
