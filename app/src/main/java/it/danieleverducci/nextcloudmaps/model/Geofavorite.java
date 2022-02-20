/*
 * Nextcloud Maps Geofavorites for Android
 *
 * @copyright Copyright (c) 2020 John Doe <john@doe.com>
 * @author John Doe <john@doe.com>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package it.danieleverducci.nextcloudmaps.model;

import android.graphics.Color;
import android.net.Uri;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import org.threeten.bp.Instant;
import org.threeten.bp.LocalDate;
import org.threeten.bp.ZoneId;

import java.io.Serializable;
import java.util.Comparator;

import it.danieleverducci.nextcloudmaps.utils.GeoUriParser;

public class Geofavorite implements Serializable {
    public static final String DEFAULT_CATEGORY = "Personal";
    private static final double EARTH_RADIUS = 6371; // https://en.wikipedia.org/wiki/Earth_radius

    /**
     * JSON Definition:
     *     {
     *         "id": 20,
     *         "name": "Ipermercato Collestrada",
     *         "date_modified": 1626798839,
     *         "date_created": 1626798825,
     *         "lat": 43.08620320282127,
     *         "lng": 12.481070617773184,
     *         "category": "Personal",
     *         "comment": "Strada Centrale Umbra 06135 Collestrada Umbria Italia",
     *         "extensions": ""
     *     }
     */

    @Expose
    @SerializedName("id") private int id;

    @Expose
    @Nullable
    @SerializedName("name") private String name;

    @Expose
    @SerializedName("date_modified") private long dateModified;

    @Expose
    @SerializedName("date_created") private long dateCreated;

    @Expose
    @SerializedName("lat") private double lat;

    @Expose
    @SerializedName("lng") private double lng;

    @Expose
    @SerializedName("category") private String category;

    @Expose
    @Nullable
    @SerializedName("comment") private String comment;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    @Nullable
    public String getName() {
        return name;
    }

    public void setName(String name) {
        if (!name.equals(this.name))
            this.name = name;
    }

    public long getDateModified() {
        return dateModified;
    }
    public void setDateModified(long dateModified) {
        this.dateModified = dateModified;
    }
    public LocalDate getLocalDateModified() {
        return Instant.ofEpochSecond(getDateCreated()).atZone(ZoneId.systemDefault()).toLocalDate();
    }

    public long getDateCreated() {
        return dateCreated;
    }
    public LocalDate getLocalDateCreated() {
        return Instant.ofEpochSecond(getDateCreated()).atZone(ZoneId.systemDefault()).toLocalDate();
    }

    public void setDateCreated(long dateCreated) {
        this.dateCreated = dateCreated;
    }

    public double getLat() {
        return lat;
    }
    public void setLat(double lat) {
        this.lat = lat;
    }

    public double getLng() {
        return lng;
    }
    public void setLng(double lng) {
        this.lng = lng;
    }

    public String getCategory() {
        return category;
    }
    public void setCategory(String category) {
        this.category = category;
    }

    @Nullable
    public String getComment() {
        return comment;
    }
    public void setComment(String comment) {
        this.comment = comment;
    }

    /**
     * Comparators for list order
     */
    public static Comparator<Geofavorite> ByTitleAZ = (gf0, gf1) -> gf0.name.compareTo(gf1.name);
    public static Comparator<Geofavorite> ByLastCreated = (gf0, gf1) -> (int) (gf1.dateCreated - gf0.dateCreated);
    public static Comparator<Geofavorite> ByCategory = (gf0, gf1) -> (gf0.category + gf0.name).compareTo(gf1.category + gf1.name);
    public static Comparator<Geofavorite> ByDistance = (gf0, gf1) -> 0; // (int) ((gf1.getDistanceFrom(userPosition) - gf0.getDistanceFrom(userPosition)) * 1000);

    public String getCoordinatesString() {
        return this.lat + " N, " + this.lng + " E";
    }

    public Uri getGeoUri() {
        return GeoUriParser.createUri(this.lat, this.lng, this.name);
    }

    public boolean valid() {
        return
                getLat() != 0 && getLng() != 0 &&
                getName() != null && getName().length() > 0 &&
                getCategory() != null && getCategory().length() > 0;
    }

    /**
     * Returns the distance between the current Geofavorite and the provided one, in kilometers.
     * @param other Geovavorite
     * @return the distance in kilometers
     */
    public double getDistanceFrom(Geofavorite other) {
        double latDistance = (other.lat-lat) * Math.PI / 180;
        double lonDistance = (other.lng-lng) * Math.PI / 180;
        double a =
                Math.sin(latDistance / 2) * Math.sin(latDistance / 2) +
                Math.cos(lat * Math.PI / 180) * Math.cos(other.lat * Math.PI / 180) *
                Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a));
        return EARTH_RADIUS * c;
    }

    /**
     * Based on Nextcloud Maps's getLetterColor util.
     * Assigns a color to a category based on its two first letters.
     *
     * @see "https://github.com/nextcloud/maps/blob/master/src/utils.js"
     * @return the generated color or null for the default category
     */
    public int  categoryColor() {
        // If category is default, return null: will be used Nextcloud's accent
        if (this.category == null || this.category.equals(DEFAULT_CATEGORY) || this.category.length() == 0)
            return 0;

        float letter1Index = this.category.toLowerCase().charAt(0);
        float letter2Index = this.category.toLowerCase().charAt(1);
        float letterCoef = ((letter1Index * letter2Index) % 100) / 100;
        float h = letterCoef * 360;
        float s = 75 + letterCoef * 10;
        float l = 50 + letterCoef * 10;
        return Color.HSVToColor( new float[]{ Math.round(h), Math.round(s), Math.round(l) });
    }

    public String categoryLetter() {
        if (category == null || category.length() == 0 || category.equals(DEFAULT_CATEGORY))
            return "\u2022";
        return category.substring(0,1);
    }

    @NonNull
    @Override
    public String toString() {
        return "[" + getName() + " (" + getLat() + "," + getLng() + ")]";
    }

}
