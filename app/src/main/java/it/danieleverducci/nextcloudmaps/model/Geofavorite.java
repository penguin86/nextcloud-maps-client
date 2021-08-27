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

import android.net.Uri;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.Comparator;

public class Geofavorite implements Serializable {
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
    @SerializedName("name") private String name;

    @Expose
    @SerializedName("date_modified") private long dateModified;

    @Expose
    @SerializedName("date_created") private long dateCreated;

    @Expose
    @SerializedName("lat") private float lat;

    @Expose
    @SerializedName("lng") private float lng;

    @Expose
    @SerializedName("category") private String category;

    @Expose
    @SerializedName("comment") private String comment;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public long getDateModified() {
        return dateModified;
    }

    public void setDateModified(long dateModified) {
        this.dateModified = dateModified;
    }

    public long getDateCreated() {
        return dateCreated;
    }

    public void setDateCreated(long dateCreated) {
        this.dateCreated = dateCreated;
    }

    public float getLat() {
        return lat;
    }

    public void setLat(float lat) {
        this.lat = lat;
    }

    public float getLng() {
        return lng;
    }

    public void setLng(float lng) {
        this.lng = lng;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public static Comparator<Geofavorite> ByTitleAZ = (note, t1) -> note.name.compareTo(t1.name);

    public static Comparator<Geofavorite> ByLastCreated = (note, t1) -> t1.id - note.id;

    public Uri getGeoUri() {
        return Uri.parse("geo:" + this.lat + "," + this.lng + "(" + this.name + ")");
    }

}
