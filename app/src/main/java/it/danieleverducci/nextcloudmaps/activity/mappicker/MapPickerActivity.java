package it.danieleverducci.nextcloudmaps.activity.mappicker;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.preference.PreferenceManager;

import org.osmdroid.api.IGeoPoint;
import org.osmdroid.api.IMapController;
import org.osmdroid.config.Configuration;
import org.osmdroid.config.IConfigurationProvider;
import org.osmdroid.events.MapListener;
import org.osmdroid.events.ScrollEvent;
import org.osmdroid.events.ZoomEvent;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.CustomZoomButtonsController;
import org.osmdroid.views.MapView;
import org.osmdroid.views.Projection;

import java.util.Locale;

import it.danieleverducci.nextcloudmaps.BuildConfig;
import it.danieleverducci.nextcloudmaps.R;
import it.danieleverducci.nextcloudmaps.databinding.ActivityMapPickerBinding;

public class MapPickerActivity extends AppCompatActivity {
    public static final String TAG = "MapPickerActivity";
    private static final int PERMISSION_REQUEST_CODE = 8888;

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
        Location l = getLastKnownPosition();
        if (l != null)
            mViewHolder.centerMapOn(l.getLatitude(), l.getLongitude());

        setContentView(mViewHolder.getRootView());
    }

    /**
     * May return last known GPS position or null. Used to center the map.
     * @return last known GPS position or null
     */
    private Location getLastKnownPosition() {
        // Check if user granted location permission
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // User didn't grant permission. Ask it.
            requestPermissions(new String[] {Manifest.permission.ACCESS_FINE_LOCATION}, PERMISSION_REQUEST_CODE);
            return null;
        }

        LocationManager locationManager = (LocationManager)
                getSystemService(Context.LOCATION_SERVICE);
        // Try to use last available location
        return locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
    }

    private class ViewHolder implements View.OnClickListener {
        private final ActivityMapPickerBinding binding;
        private final MapView map;
        private boolean coordsEditMode = false;

        public ViewHolder(LayoutInflater inflater) {
            this.binding = ActivityMapPickerBinding.inflate(inflater);

            // Show confirm button on lat/lng edit
            View.OnFocusChangeListener latlonFocusListener = new View.OnFocusChangeListener() {
                @Override
                public void onFocusChange(View view, boolean focused) {
                    if (!focused)
                        return;

                    (ViewHolder.this).coordsEditMode(true);
                }
            };
            this.binding.latEt.setOnFocusChangeListener(latlonFocusListener);
            this.binding.lonEt.setOnFocusChangeListener(latlonFocusListener);

            // Setup map
            this.map = this.binding.map;
            this.map.getZoomController().setVisibility(CustomZoomButtonsController.Visibility.NEVER);
            this.map.setMultiTouchControls(true);
            this.map.addMapListener(new MapListener() {
                @Override
                public boolean onScroll(ScrollEvent event) {
                    // Disable edit mode
                    if ((ViewHolder.this).coordsEditMode)
                        (ViewHolder.this).coordsEditMode(false);
                    // Write coords on edittext
                    IGeoPoint igp = (ViewHolder.this).map.getMapCenter();
                    (ViewHolder.this).binding.latEt.setText(String.format(Locale.ENGLISH, "%.06f", igp.getLatitude()));
                    (ViewHolder.this).binding.lonEt.setText(String.format(Locale.ENGLISH, "%.06f", igp.getLongitude()));
                    return false;
                }

                @Override
                public boolean onZoom(ZoomEvent event) {
                    return false;
                }
            });
            IMapController mapController = binding.map.getController();
            mapController.setZoom(7.0f);

            // Setup onClick
            this.binding.latlonConfirmBtn.setOnClickListener(this);

        }

        public void centerMapOn(Double lat, Double lon ) {
            IMapController mapController = binding.map.getController();
            mapController.setCenter(new GeoPoint(lat, lon));
        }

        public View getRootView() {
            return this.binding.root;
        }

            @Override
        public void onClick(View view) {
            if (view == this.binding.latlonConfirmBtn) {
                this.coordsEditMode(false);
                Double lat;
                Double lon;
                try {
                    lat = Double.parseDouble(this.binding.latEt.getText().toString());
                    lon = Double.parseDouble(this.binding.lonEt.getText().toString());
                } catch (NumberFormatException e) {
                    Log.e(TAG, "Unable to parse coordinates: " + e.getLocalizedMessage());
                    Toast.makeText(MapPickerActivity.this, R.string.coordinates_parse_error, Toast.LENGTH_SHORT).show();
                    return;
                }
                // Validate coordinates
                if (lon <= -180 || lon >= 180 || lat <= -90 || lat >= 90) {
                    Toast.makeText(MapPickerActivity.this, R.string.coordinates_invalid_error, Toast.LENGTH_SHORT).show();
                    return;
                }
                // Move map to coordinates
                this.centerMapOn(lat, lon);
            }
        }

        /**
         * Enters/exits coordinates edit mode.
         * On exit, removes focus from the coordinates ET and hides the button
         */
        private void coordsEditMode(boolean active) {
            this.coordsEditMode = active;
            View btn = this.binding.latlonConfirmBtn;
            if (active) {
                btn.setVisibility(View.VISIBLE);
            } else {
                this.binding.latEt.clearFocus();
                this.binding.lonEt.clearFocus();
                btn.setVisibility(View.GONE);
            }
        }

    }

}
