package it.danieleverducci.nextcloudmaps.utils;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.util.Log;

import it.danieleverducci.nextcloudmaps.R;
import it.danieleverducci.nextcloudmaps.model.Geofavorite;

public class IntentGenerator {
    public static Intent newShareIntent(Context context, Geofavorite item) {
        Intent i = new Intent();
        i.setAction(Intent.ACTION_SEND);
        i.setType("text/plain");
        String shareMessage = context.getString(R.string.share_message)
                .replace("{title}", item.getName() == null ? context.getString(R.string.share_message_default_title) : item.getName())
                .replace("{lat}", ""+item.getLat())
                .replace("{lng}", ""+item.getLng());
        i.putExtra(Intent.EXTRA_TEXT, shareMessage );
        return i;
    }

    public static Intent newGeoUriIntent(Context context, Geofavorite item) {
        Intent i = new Intent();
        i.setAction(Intent.ACTION_VIEW);
        i.setData(isGoogleMapsInstalled(context) ? item.getGmapsUri() : item.getGeoUri());
        return i;
    }
    
    public static boolean isGoogleMapsInstalled(Context context) {
        try {
            context.getPackageManager().getApplicationInfo("com.google.android.apps.maps", 0);
            return true;
        } catch (PackageManager.NameNotFoundException e) {
            return false;
        }
    }
}
