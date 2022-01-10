package it.danieleverducci.nextcloudmaps.activity.mappicker;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.PreferenceManager;

import org.osmdroid.api.IGeoPoint;
import org.osmdroid.api.IMapController;
import org.osmdroid.config.Configuration;
import org.osmdroid.config.IConfigurationProvider;
import org.osmdroid.events.MapEventsReceiver;
import org.osmdroid.events.MapListener;
import org.osmdroid.events.ScrollEvent;
import org.osmdroid.events.ZoomEvent;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.CustomZoomButtonsController;
import org.osmdroid.views.MapView;
import org.osmdroid.views.Projection;
import org.osmdroid.views.overlay.MapEventsOverlay;

import it.danieleverducci.nextcloudmaps.BuildConfig;
import it.danieleverducci.nextcloudmaps.R;
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
        private final MapView map;

        public ViewHolder(LayoutInflater inflater) {
            this.binding = ActivityMapPickerBinding.inflate(inflater);

            // Show confirm button on lat/lng edit
            View.OnFocusChangeListener latlonFocusListener = new View.OnFocusChangeListener() {
                @Override
                public void onFocusChange(View view, boolean focused) {
                    if (!focused)
                        return;

                    (ViewHolder.this).binding.latlonConfirmBtn.setVisibility(View.VISIBLE);
                }
            };
            this.binding.latEt.setOnFocusChangeListener(latlonFocusListener);
            this.binding.lonEt.setOnFocusChangeListener(latlonFocusListener);

            // Setup map
            this.map = this.binding.map;
            this.map.getZoomController().setVisibility(CustomZoomButtonsController.Visibility.NEVER);
            this.map.setMultiTouchControls(true);
            IMapController mapController = binding.map.getController();
            mapController.setZoom(7.0f);
            this.map.addMapListener(new MapListener() {
                @Override
                public boolean onScroll(ScrollEvent event) {
                    // Convert XY coords to LatLng
                    Projection p = (ViewHolder.this).map.getProjection();
                    IGeoPoint igp = p.fromPixels(event.getX(), event.getY());
                    (ViewHolder.this).binding.latEt.setText(String.format("%.06f", igp.getLatitude()));
                    (ViewHolder.this).binding.lonEt.setText(String.format("%.06f", igp.getLongitude()));
                    return false;
                }

                @Override
                public boolean onZoom(ZoomEvent event) {
                    return false;
                }
            });

            // Setup onClick
            this.binding.latlonConfirmBtn.setOnClickListener(this);

        }

        public View getRootView() {
            return this.binding.root;
        }

            @Override
        public void onClick(View view) {
            if (view == this.binding.latlonConfirmBtn) {
                // TODO: Move map
                view.setVisibility(View.GONE);
            }
        }
    }

}
