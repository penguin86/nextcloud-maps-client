package it.danieleverducci.nextcloudmaps.utils;

import android.net.Uri;
import android.util.Log;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import it.danieleverducci.nextcloudmaps.model.Geofavorite;

public class GeoUriParser {
    private static final Pattern PATTERN_GEO = Pattern.compile("geo:(-?[\\d.]+),(-?[\\d.]+)");

    public static double[] parseUri(Uri uri) throws IllegalArgumentException {
        if (uri == null)
            throw new IllegalArgumentException("no uri");

        Matcher m = PATTERN_GEO.matcher(uri.toString());
        if (m.find() && m.groupCount() == 2) {
            String sLat = m.group(1);
            String sLon = m.group(2);
            try {
                return new double[]{Double.parseDouble(sLat), Double.parseDouble(sLon)};
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException("unable to parse uri");
            }
        } else {
            throw new IllegalArgumentException("unable to parse uri");
        }
    }
}
