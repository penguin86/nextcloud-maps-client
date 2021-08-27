/*
 * Nextcloud Notes Tutorial for Android
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

import it.danieleverducci.nextcloudmaps.model.Note;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;

public interface API {
    String mApiEndpoint = "/index.php/apps/notestutorial/api/0.1";

    @GET("/notes")
    Call<List<Note>> getNotes();

    @POST("/notes")
    Call<Note> create(
            @Body Note note
    );

    @PUT("/notes/{id}")
    Call<Note> updateNote(
            @Path("id") int id,
            @Body Note note
    );

    @DELETE("/notes/{id}")
    Call<Note> deleteNote(
            @Path("id") int id
    );
}
