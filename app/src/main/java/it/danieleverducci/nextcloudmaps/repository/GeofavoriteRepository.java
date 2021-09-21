package it.danieleverducci.nextcloudmaps.repository;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.MutableLiveData;

import java.util.ArrayList;
import java.util.List;

import it.danieleverducci.nextcloudmaps.api.ApiProvider;
import it.danieleverducci.nextcloudmaps.model.Geofavorite;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Singleton pattern
 */
public class GeofavoriteRepository {

    private static GeofavoriteRepository instance;
    private MutableLiveData<List<Geofavorite>> mGeofavorites;
    private OnFinished listener;

    public static GeofavoriteRepository getInstance() {
        if(instance == null){
            instance = new GeofavoriteRepository();
        }
        return instance;
    }

    public MutableLiveData<List<Geofavorite>> getGeofavorites(){
        if (mGeofavorites == null) {
            mGeofavorites = new MutableLiveData<>();
            mGeofavorites.setValue(new ArrayList<>());
        }
        return mGeofavorites;
    }

    public void updateGeofavorites() {
        if (listener != null) listener.onLoading();
        // Obtain geofavorites
        Call<List<Geofavorite>> call = ApiProvider.getAPI().getGeofavorites();
        call.enqueue(new Callback<List<Geofavorite>>() {
            @Override
            public void onResponse(@NonNull Call<List<Geofavorite>> call, @NonNull Response<List<Geofavorite>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    mGeofavorites.postValue(response.body());
                    if (listener != null) listener.onSuccess();
                } else {
                    onFailure(call, new Throwable("Dataset is empty"));
                }
            }

            @Override
            public void onFailure(@NonNull Call<List<Geofavorite>> call, @NonNull Throwable t) {
                if (listener != null) listener.onFailure();
            }
        });
    }

    public Geofavorite getGeofavorite(int id) {
        for (Geofavorite g : mGeofavorites.getValue()) {
            if (g.getId() == id)
                return g;
        }
        return null;
    }

    public void saveGeofavorite(Geofavorite geofav) {
        Call<Geofavorite> call;
        if (geofav.getId() == 0) {
            // New geofavorite
            call = ApiProvider.getAPI().createGeofavorite(geofav);
        } else {
            // Update existing geofavorite
            call = ApiProvider.getAPI().updateGeofavorite(geofav.getId(), geofav);
        }
        call.enqueue(new Callback<Geofavorite>() {
            @Override
            public void onResponse(Call<Geofavorite> call, Response<Geofavorite> response) {
                if (response.isSuccessful()) {
                    List<Geofavorite> geofavs = mGeofavorites.getValue();
                    if (geofav.getId() != 0) {
                        geofavs.remove(geofav);
                    }
                    geofavs.add(geofav);
                    mGeofavorites.postValue(geofavs);
                    if (listener != null) listener.onSuccess();
                } else if (listener != null) {
                    listener.onFailure();
                }
            }

            @Override
            public void onFailure(Call<Geofavorite> call, Throwable t) {
                if (listener != null) listener.onFailure();
            }
        });
    }

    public void deleteGeofavorite(Geofavorite geofav) {
        if (listener != null) listener.onLoading();
        // Delete Geofavorite
        Call<Geofavorite> call = ApiProvider.getAPI().deleteGeofavorite(geofav.getId());
        call.enqueue(new Callback<Geofavorite>() {
            @Override
            public void onResponse(Call<Geofavorite> call, Response<Geofavorite> response) {
                List<Geofavorite> geofavs = mGeofavorites.getValue();
                if (geofavs.remove(geofav)) {
                    mGeofavorites.postValue(geofavs);
                    if (listener != null) listener.onSuccess();
                } else {
                    // Should never happen
                    if (listener != null) listener.onFailure();
                }
            }

            @Override
            public void onFailure(Call<Geofavorite> call, Throwable t) {
                if (listener != null) listener.onFailure();
            }
        });
    }

    public void setOnFinishedListener(OnFinished listener) {
        this.listener = listener;
    }

    public interface OnFinished {
        void onLoading();
        void onSuccess();
        void onFailure();
    }


}
