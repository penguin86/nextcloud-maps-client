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

package it.danieleverducci.nextcloudmaps.activity.main;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import java.util.List;

import it.danieleverducci.nextcloudmaps.api.ApiProvider;
import it.danieleverducci.nextcloudmaps.model.Geofavorite;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainPresenter {
    private MainView view;

    public MainPresenter(MainView view) {
        this.view = view;
    }

    public void getGeofavorites() {
        view.showLoading();
        Call<List<Geofavorite>> call = ApiProvider.getAPI().getGeofavorites();
        call.enqueue(new Callback<List<Geofavorite>>() {
            @Override
            public void onResponse(@NonNull Call<List<Geofavorite>> call, @NonNull Response<List<Geofavorite>> response) {
                ((AppCompatActivity) view).runOnUiThread(() -> {
                    view.hideLoading();
                    if (response.isSuccessful() && response.body() != null) {
                        view.onGetResult(response.body());
                    }
                });
            }

            @Override
            public void onFailure(@NonNull Call<List<Geofavorite>> call, @NonNull Throwable t) {
                ((AppCompatActivity) view).runOnUiThread(() -> {
                    view.hideLoading();
                    view.onErrorLoading(t.getLocalizedMessage());
                });
            }
        });
    }

    public void deleteGeofavorite(int id) {
        view.showLoading();
        Call<Geofavorite> call = ApiProvider.getAPI().deleteGeofavorite(id);
        call.enqueue(new Callback<Geofavorite>() {
            @Override
            public void onResponse(Call<Geofavorite> call, Response<Geofavorite> response) {
                view.hideLoading();
                view.onGeofavoriteDeleted(id);
            }

            @Override
            public void onFailure(Call<Geofavorite> call, Throwable t) {
                view.hideLoading();
                view.onErrorLoading(t.getLocalizedMessage());
            }
        });
    }
}
