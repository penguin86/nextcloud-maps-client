package it.danieleverducci.nextcloudmaps.activity.main;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import java.util.List;

import it.danieleverducci.nextcloudmaps.model.Geofavorite;
import it.danieleverducci.nextcloudmaps.repository.GeofavoriteRepository;

public class MainActivityViewModel extends ViewModel {
    private GeofavoriteRepository mRepo;

    public void init() {
        mRepo = GeofavoriteRepository.getInstance();
    }

    public LiveData<List<Geofavorite>> getGeofavorites(){
        mRepo.updateGeofavorites();
        return mRepo.getGeofavorites();
    }

    public void updateGeofavorites() {
        mRepo.updateGeofavorites();
    }

    public void deleteGeofavorite(Geofavorite geofav) {
        mRepo.deleteGeofavorite(geofav);
    }

    public LiveData<Boolean> getIsUpdating(){
        return mRepo.isUpdating();
    }

    public LiveData<Boolean> getOnFinished(){
        return mRepo.onFinished();
    }

}
