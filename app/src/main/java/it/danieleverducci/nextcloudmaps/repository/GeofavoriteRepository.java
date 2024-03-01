package it.danieleverducci.nextcloudmaps.repository;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.MutableLiveData;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import it.danieleverducci.nextcloudmaps.api.ApiProvider;
import it.danieleverducci.nextcloudmaps.model.Geofavorite;
import it.danieleverducci.nextcloudmaps.utils.SingleLiveEvent;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Singleton pattern
 */
public class GeofavoriteRepository {

    private static final String TAG = "GeofavoriteRepository";
    private static GeofavoriteRepository instance;
    private MutableLiveData<List<Geofavorite>> mGeofavorites;
    private MutableLiveData<HashSet<String>> mCategories = new MutableLiveData<HashSet<String>>();
    private MutableLiveData<Boolean> mIsUpdating = new MutableLiveData<>(false);
    private SingleLiveEvent<Boolean> mOnFinished = new SingleLiveEvent<>();

    private Context applicationContext;

    public GeofavoriteRepository(Context applicationContext) {
        this.applicationContext = applicationContext;
    }

    public static GeofavoriteRepository getInstance(Context applicationContext) {
        if(instance == null){
            instance = new GeofavoriteRepository(applicationContext);
        }
        return instance;
    }

    public static void resetInstance() {
        instance = null;
    }

    public MutableLiveData<List<Geofavorite>> getGeofavorites(){
        if (mGeofavorites == null) {
            mGeofavorites = new MutableLiveData<>();
            mGeofavorites.setValue(new ArrayList<>());
        }
        return mGeofavorites;
    }

    public MutableLiveData<HashSet<String>> getCategories() {
        return mCategories;
    }

    public MutableLiveData<Boolean> isUpdating() {
        return mIsUpdating;
    }

    public SingleLiveEvent<Boolean> onFinished() {
        return mOnFinished;
    }

    public void updateGeofavorites() {
        mIsUpdating.postValue(true);
        // Obtain geofavorites
        Call<List<Geofavorite>> call = ApiProvider.getAPI(this.applicationContext).getGeofavorites();
        call.enqueue(new Callback<List<Geofavorite>>() {
            @Override
            public void onResponse(@NonNull Call<List<Geofavorite>> call, @NonNull Response<List<Geofavorite>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    mGeofavorites.postValue(response.body());
                    updateCategories(response.body());
                    mIsUpdating.postValue(false);
                    mOnFinished.postValue(true);
                } else {
                    onFailure(call, new Throwable("Dataset is empty"));
                }
            }

            @Override
            public void onFailure(@NonNull Call<List<Geofavorite>> call, @NonNull Throwable t) {
                mIsUpdating.postValue(false);
                mOnFinished.postValue(false);
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
        mIsUpdating.postValue(true);
        Call<Geofavorite> call;
        if (geofav.getId() == 0) {
            // New geofavorite
            call = ApiProvider.getAPI(this.applicationContext).createGeofavorite(geofav);
        } else {
            // Update existing geofavorite
            call = ApiProvider.getAPI(this.applicationContext).updateGeofavorite(geofav.getId(), geofav);
        }
        call.enqueue(new Callback<Geofavorite>() {
            @Override
            public void onResponse(Call<Geofavorite> call, Response<Geofavorite> response) {
                if (response.isSuccessful()) {
                    List<Geofavorite> geofavs = mGeofavorites.getValue();
                    if (geofav.getId() != 0) {
                        geofavs.remove(geofav);
                    }
                    geofavs.add(response.body());
                    mGeofavorites.postValue(geofavs);
                    mIsUpdating.postValue(false);
                    mOnFinished.postValue(true);
                } else {
                    mIsUpdating.postValue(false);
                    mOnFinished.postValue(false);
                }
            }

            @Override
            public void onFailure(Call<Geofavorite> call, Throwable t) {
                Log.e(TAG, t.getMessage());
                mIsUpdating.postValue(false);
                mOnFinished.postValue(false);
            }
        });
    }

    public void deleteGeofavorite(Geofavorite geofav) {
        mIsUpdating.postValue(true);
        // Delete Geofavorite
        Call<Geofavorite> call = ApiProvider.getAPI(this.applicationContext).deleteGeofavorite(geofav.getId());
        call.enqueue(new Callback<Geofavorite>() {
            @Override
            public void onResponse(Call<Geofavorite> call, Response<Geofavorite> response) {
                List<Geofavorite> geofavs = mGeofavorites.getValue();
                if (geofavs.remove(geofav)) {
                    mGeofavorites.postValue(geofavs);
                    mIsUpdating.postValue(false);
                    mOnFinished.postValue(true);
                } else {
                    // Should never happen
                    mIsUpdating.postValue(false);
                    mOnFinished.postValue(false);
                }
            }

            @Override
            public void onFailure(Call<Geofavorite> call, Throwable t) {
                mIsUpdating.postValue(false);
                mOnFinished.postValue(false);
            }
        });
    }

    private void updateCategories(List<Geofavorite> geofavs) {
        HashSet<String> categories = new HashSet<>();
        for (Geofavorite g : geofavs) {
            String cat = g.getCategory();
            if (cat != null)
                categories.add(cat);
        }
        mCategories.postValue(categories);
    }

}
