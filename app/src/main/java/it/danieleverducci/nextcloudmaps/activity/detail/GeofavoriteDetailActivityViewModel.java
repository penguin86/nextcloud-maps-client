package it.danieleverducci.nextcloudmaps.activity.detail;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import it.danieleverducci.nextcloudmaps.model.Geofavorite;
import it.danieleverducci.nextcloudmaps.repository.GeofavoriteRepository;

public class GeofavoriteDetailActivityViewModel extends ViewModel implements GeofavoriteRepository.OnFinished {
    private GeofavoriteRepository mRepo;
    private MutableLiveData<Boolean> mIsUpdating = new MutableLiveData<>();
    private MutableLiveData<Boolean> mIsFailed = new MutableLiveData<>();

    public void init() {
        mRepo = GeofavoriteRepository.getInstance();
        mRepo.setOnFinishedListener(this);
    }

    public Geofavorite getGeofavorite(int id) {
        return mRepo.getGeofavorite(id);
    }

    public void saveGeofavorite(Geofavorite geofav) {
        mRepo.saveGeofavorite(geofav);
    }


    public LiveData<Boolean> getIsUpdating(){
        return mIsUpdating;
    }

    public LiveData<Boolean> getIsFailed(){
        return mIsFailed;
    }

    @Override
    public void onLoading() {
        mIsUpdating.postValue(true);
    }
    @Override
    public void onSuccess() {
        mIsUpdating.postValue(false);
        mIsFailed.postValue(false);
    }

    @Override
    public void onFailure() {
        mIsUpdating.postValue(false);
        mIsFailed.postValue(true);
    }
}
