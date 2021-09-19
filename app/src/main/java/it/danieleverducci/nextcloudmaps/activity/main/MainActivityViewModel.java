package it.danieleverducci.nextcloudmaps.activity.main;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import java.util.List;

import it.danieleverducci.nextcloudmaps.model.Geofavorite;
import it.danieleverducci.nextcloudmaps.repository.GeofavoriteRepository;

public class MainActivityViewModel extends ViewModel {

    private MutableLiveData<List<Geofavorite>> mGeofavorites;
    private GeofavoriteRepository mRepo;
    private MutableLiveData<Boolean> mIsUpdating = new MutableLiveData<>();

    public void init() {
        if (mGeofavorites != null)
            return;

        mRepo = GeofavoriteRepository.getInstance();
        mGeofavorites = mRepo.getGeofavorites();
    }

    public LiveData<List<Geofavorite>> getGeofavorites(){
        return mGeofavorites;
    }

    public LiveData<Boolean> getIsUpdating(){
        return mIsUpdating;
    }
}
