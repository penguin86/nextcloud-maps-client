package it.danieleverducci.nextcloudmaps.utils;

import android.content.Context;
import android.content.res.Configuration;

import androidx.preference.PreferenceManager;

import org.osmdroid.config.IConfigurationProvider;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.TilesOverlay;

import it.danieleverducci.nextcloudmaps.BuildConfig;

public class MapUtils {

    public static void configOsmdroid(Context context) {
        IConfigurationProvider osmdroidConfig = org.osmdroid.config.Configuration.getInstance();
        osmdroidConfig.load(context,
                PreferenceManager.getDefaultSharedPreferences(context));
        osmdroidConfig.setUserAgentValue(BuildConfig.APPLICATION_ID);
    }

    public static void setTheme(MapView mapView) {
        int currentNightMode = mapView.getContext().getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK;
        switch (currentNightMode) {
            case Configuration.UI_MODE_NIGHT_NO:
                // Night mode is not active, we're using the light theme
                mapView.getOverlayManager().getTilesOverlay().setColorFilter(null);
                break;
            case Configuration.UI_MODE_NIGHT_YES:
                // Night mode is active, we're using dark theme
                mapView.getOverlayManager().getTilesOverlay().setColorFilter(TilesOverlay.INVERT_COLORS);
                break;
        }
    }
}
