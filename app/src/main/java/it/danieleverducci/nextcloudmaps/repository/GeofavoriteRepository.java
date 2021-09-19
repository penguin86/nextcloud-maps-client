package it.danieleverducci.nextcloudmaps.repository;

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
    private ArrayList<Geofavorite> dataSet = new ArrayList<>();

    public static GeofavoriteRepository getInstance() {
        if(instance == null){
            instance = new GeofavoriteRepository();
        }
        return instance;
    }

    public MutableLiveData<List<Geofavorite>> getGeofavorites(){
        // Obtain geofavorites
        Call<List<Geofavorite>> call = ApiProvider.getAPI().getGeofavorites();
        call.enqueue(new Callback<List<Geofavorite>>() {
            @Override
            public void onResponse(@NonNull Call<List<Geofavorite>> call, @NonNull Response<List<Geofavorite>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    dataSet.addAll(response.body());
                } else {
                    onFailure(call, new Throwable("Dataset is empty"));
                }
            }

            @Override
            public void onFailure(@NonNull Call<List<Geofavorite>> call, @NonNull Throwable t) {
                // TODO
            }
        });

        MutableLiveData<List<Geofavorite>> data = new MutableLiveData<>();
        data.setValue(dataSet);
        return data;
    }


}
