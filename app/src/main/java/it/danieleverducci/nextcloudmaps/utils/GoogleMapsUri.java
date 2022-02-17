package it.danieleverducci.nextcloudmaps.utils;

import android.content.Context;
import android.content.pm.PackageManager;
import android.net.Uri;

/**
 * Google maps doesn't honor geouri standard, instead implements its own uri.
 * A geouri opens google maps, but the position is ignored, so it is needed to use gmaps own uri.
 * Utility to check if gmaps is installed and generate a gmaps uri
 */
public class GoogleMapsUri {

    public static boolean isGoogleMapsInstalled(Context context) {
        try {
            context.getPackageManager().getApplicationInfo("com.google.android.apps.maps", 0);
            return true;
        } catch (PackageManager.NameNotFoundException e) {
            return false;
        }
    }

    public static Uri createGmapsUri(double lat, double lon) {
        // Check coords validity
        if (lon <= -180 || lon >= 180 )
            throw new IllegalArgumentException("Invalid longitude: " + lon);
        if (lat <= -90 || lat >= 90)
            throw new IllegalArgumentException("Invalid latitude: " + lat);

        String uriStr = "https://www.google.com/maps/search/?api=1&query=" + lat + "," + lon;
        return Uri.parse(uriStr);
    }
}
