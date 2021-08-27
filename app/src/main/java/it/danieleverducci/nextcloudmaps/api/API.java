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

package it.danieleverducci.nextcloudmaps.api;

import java.util.List;

import it.danieleverducci.nextcloudmaps.model.Geofavorite;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;

public interface API {
    String mApiEndpoint = "/index.php/apps/maps/api/1.0";

    @GET("/favorites")
    Call<List<Geofavorite>> getGeofavorites();

    @POST("/favorites")
    Call<Geofavorite> createGeofavorite (
            @Body Geofavorite geofavorite
    );

    @PUT("/favorites/{id}")
    Call<Geofavorite> updateGeofavorite (
            @Path("id") int id,
            @Body Geofavorite geofavorite
    );

    @DELETE("/favorites/{id}")
    Call<Geofavorite> deleteGeofavorite (
            @Path("id") int id
    );
}
