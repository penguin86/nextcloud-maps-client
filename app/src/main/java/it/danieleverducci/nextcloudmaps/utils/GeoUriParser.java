package it.danieleverducci.nextcloudmaps.utils;

import android.net.Uri;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import it.danieleverducci.nextcloudmaps.model.Geofavorite;

public class GeoUriParser {
    private static final Pattern PATTERN_GEO = Pattern.compile("geo:[\\d.]+,[\\d.]+");

    public static double[] parseUri(Uri uri) throws IllegalArgumentException {
        Matcher m = PATTERN_GEO.matcher(uri.getPath());
        if (m.find()) {
            return new double[]{0, 0};
        } else {
            throw new IllegalArgumentException("unable to parse uri");
        }
    }
}
