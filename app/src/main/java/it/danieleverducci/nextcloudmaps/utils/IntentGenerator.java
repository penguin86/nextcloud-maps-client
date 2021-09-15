package it.danieleverducci.nextcloudmaps.utils;

import android.content.Context;
import android.content.Intent;

import it.danieleverducci.nextcloudmaps.R;
import it.danieleverducci.nextcloudmaps.model.Geofavorite;

public class IntentGenerator {
    public static Intent newShareIntent(Context context, Geofavorite item) {
        Intent i = new Intent();
        i.setAction(Intent.ACTION_SEND);
        i.setType("text/plain");
        String shareMessage = context.getString(R.string.share_message)
                .replace("{lat}", ""+item.getLat())
                .replace("{lng}", ""+item.getLng());
        i.putExtra(Intent.EXTRA_TEXT, shareMessage );
        return i;
    }

    public static Intent newGeoUriIntent(Context context, Geofavorite item) {
        Intent i = new Intent();
        i.setAction(Intent.ACTION_VIEW);
        i.setData(item.getGeoUri());
        return i;
    }
}
