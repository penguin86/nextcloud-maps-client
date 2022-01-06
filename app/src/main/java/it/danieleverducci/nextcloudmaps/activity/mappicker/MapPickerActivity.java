package it.danieleverducci.nextcloudmaps.activity.mappicker;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.PreferenceManager;

import org.osmdroid.config.Configuration;
import org.osmdroid.config.IConfigurationProvider;

import it.danieleverducci.nextcloudmaps.BuildConfig;
import it.danieleverducci.nextcloudmaps.databinding.ActivityMapPickerBinding;

public class MapPickerActivity extends AppCompatActivity {
    public static final String TAG = "MapPickerActivity";
    private ViewHolder mViewHolder;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // OSMDroid config
        IConfigurationProvider osmdroidConfig = Configuration.getInstance();
        osmdroidConfig.load(getApplicationContext(),
                PreferenceManager.getDefaultSharedPreferences(getApplicationContext()));
        osmdroidConfig.setUserAgentValue(BuildConfig.APPLICATION_ID);

        mViewHolder = new MapPickerActivity.ViewHolder(getLayoutInflater());
        setContentView(mViewHolder.getRootView());
    }

    private class ViewHolder implements View.OnClickListener {
        private final ActivityMapPickerBinding binding;

        public ViewHolder(LayoutInflater inflater) {
            this.binding = ActivityMapPickerBinding.inflate(inflater);
        }

        public View getRootView() {
            return this.binding.root;
        }

            @Override
        public void onClick(View view) {

        }
    }

}
