package it.danieleverducci.nextcloudmaps.utils;

public class Color {

    /**
     * Based on Nextcloud Maps's getLetterColor util
     * @see "https://github.com/nextcloud/maps/blob/master/src/utils.js"
     * @param catName category name
     */
    public static generareCategoryColor(String catName) {
        // If category is default, return default color

        // Else
        int letter1Index = letter1.toLowerCase().charCodeAt(0);
        int letter2Index = letter2.toLowerCase().charCodeAt(0);
        var letterCoef = ((letter1Index * letter2Index) % 100) / 100;
        var h = letterCoef * 360;
        var s = 75 + letterCoef * 10;
        var l = 50 + letterCoef * 10;
        return {h: Math.round(h), s: Math.round(s), l: Math.round(l)};
    }
}
