package it.danieleverducci.nextcloudmaps.utils;

import androidx.annotation.MainThread;
import androidx.annotation.NonNull;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Events implementation in LiveData
 * From: https://gist.github.com/teegarcs/319a3e7e4736a0cce8eba2216c52b0ca#file-singleliveevent
 * @param <T>
 */
public class SingleLiveEvent<T> extends MutableLiveData<T> {
    AtomicBoolean mPending = new AtomicBoolean(false);

    @MainThread
    @Override
    public void setValue(T value) {
        mPending.set(true);
        super.setValue(value);
    }

    @MainThread
    @Override
    public void observe(@NonNull LifecycleOwner owner, @NonNull Observer<? super T> observer) {
        super.observe(owner, new Observer<T>() {
            @Override
            public void onChanged(T t) {
                if (mPending.compareAndSet(true, false)) {
                    observer.onChanged(t);
                }
            }
        });
    }

    /**
     * Util function for Void implementations.
     */
    public void call() {
        setValue(null);
    }
}
