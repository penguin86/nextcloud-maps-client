/*
 * Nextcloud Maps Geofavorites for Android
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package it.danieleverducci.nextcloudmaps.activity.detail;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.core.app.ActivityCompat;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.preference.PreferenceManager;

import org.osmdroid.api.IMapController;
import org.osmdroid.config.Configuration;
import org.osmdroid.config.IConfigurationProvider;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.CustomZoomButtonsController;
import org.osmdroid.views.overlay.Marker;
import org.threeten.bp.format.DateTimeFormatter;
import org.threeten.bp.format.FormatStyle;

import java.util.HashSet;

import it.danieleverducci.nextcloudmaps.BuildConfig;
import it.danieleverducci.nextcloudmaps.R;
import it.danieleverducci.nextcloudmaps.activity.NextcloudMapsStyledActivity;
import it.danieleverducci.nextcloudmaps.activity.mappicker.MapPickerActivity;
import it.danieleverducci.nextcloudmaps.databinding.ActivityGeofavoriteDetailBinding;
import it.danieleverducci.nextcloudmaps.model.Geofavorite;
import it.danieleverducci.nextcloudmaps.utils.GeoUriParser;
import it.danieleverducci.nextcloudmaps.utils.IntentGenerator;
import it.danieleverducci.nextcloudmaps.utils.MapUtils;

public class GeofavoriteDetailActivity extends NextcloudMapsStyledActivity implements LocationListener, ActivityCompat.OnRequestPermissionsResultCallback {

    public static final String TAG = "GeofavDetail";
    public static final int MINIMUM_ACCEPTABLE_ACCURACY = 50;  // In meters
    public static final String ARG_GEOFAVORITE = "geofav";
    private static final int PERMISSION_REQUEST_CODE = 9999;

    private ViewHolder mViewHolder;
    private Geofavorite mGeofavorite;
    private GeofavoriteDetailActivityViewModel mViewModel;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        MapUtils.configOsmdroid(this);

        mViewHolder = new ViewHolder(getLayoutInflater());
        setContentView(mViewHolder.getRootView());
        mViewHolder.setOnSubmitListener(new OnSubmitListener() {
            @Override
            public void onBackPressed() {
                finish();
            }

            @Override
            public void onMapEditPressed() {
                //TODO
            }

            @Override
            public void onActionIconShareClicked() {
                startActivity(Intent.createChooser(IntentGenerator.newShareIntent(GeofavoriteDetailActivity.this, mGeofavorite), getString(R.string.share_via)));
            }

            @Override
            public void onActionIconNavClicked() {
                startActivity(IntentGenerator.newGeoUriIntent(GeofavoriteDetailActivity.this, mGeofavorite));
            }

            @Override
            public void onActionIconDeleteClicked() {
                // TODO
            }

            @Override
            public void onSubmit() {
                saveGeofavorite();
            }

            @Override
            public void onMapClicked() {
                // TODO: Open map activity with pin
                startActivity(IntentGenerator.newGeoUriIntent(GeofavoriteDetailActivity.this, mGeofavorite));
            }
        });

        mViewModel = new ViewModelProvider(this).get(GeofavoriteDetailActivityViewModel.class);
        mViewModel.init();
        mViewModel.getCategories().observe(this, new Observer<HashSet<String>>() {
            @Override
            public void onChanged(HashSet<String> categories) {
                mViewHolder.setCategories(categories);
            }
        });
        mViewModel.getIsUpdating().observe(this, new Observer<Boolean>() {
            @Override
            public void onChanged(@Nullable Boolean updating) {
                mViewHolder.setUpdating(updating);
            }
        });
        mViewModel.getOnFinished().observe(this, new Observer<Boolean>() {
            @Override
            public void onChanged(@Nullable Boolean success) {
                if(success){
                    Toast.makeText(GeofavoriteDetailActivity.this, R.string.geofavorite_saved, Toast.LENGTH_SHORT).show();
                    finish();
                } else {
                    Toast.makeText(GeofavoriteDetailActivity.this, R.string.error_saving_geofavorite, Toast.LENGTH_SHORT).show();
                }
            }
        });

        if (getIntent().hasExtra(ARG_GEOFAVORITE) && getIntent().getIntExtra(ARG_GEOFAVORITE, 0) != 0) {
            // Opening geofavorite from list
            mGeofavorite = mViewModel.getGeofavorite(
                    getIntent().getIntExtra(ARG_GEOFAVORITE, 0)
            );
            mViewHolder.hideAccuracy();
        } else {
            // New geofavorite
            mGeofavorite = new Geofavorite();
            mGeofavorite.setCategory(Geofavorite.DEFAULT_CATEGORY);
            mGeofavorite.setDateCreated(System.currentTimeMillis() / 1000);
            mGeofavorite.setDateModified(System.currentTimeMillis() / 1000);
            mViewHolder.hideActions();

            if (getIntent().getData() != null) {
                // Opened by external generic intent: parse URI
                try {
                    double[] coords = GeoUriParser.parseUri(getIntent().getData());
                    mGeofavorite.setLat(coords[0]);
                    mGeofavorite.setLng(coords[1]);
                    mViewHolder.hideAccuracy();
                } catch (IllegalArgumentException e) {
                    Toast.makeText(this, R.string.error_unsupported_uri, Toast.LENGTH_SHORT).show();
                    finish();
                }
            } else {
                // Precompile location with current one
                getLocation();
            }
        }

        mViewHolder.updateView(mGeofavorite);

    }

    @Override
    protected void onResume() {
        super.onResume();
        // OSMDroid config
        Configuration.getInstance().load(getApplicationContext(),
                PreferenceManager.getDefaultSharedPreferences(getApplicationContext()));
        mViewHolder.onResume();
    }

    @Override
    protected void onPause() {
        // OSMDroid config
        Configuration.getInstance().save(getApplicationContext(),
                PreferenceManager.getDefaultSharedPreferences(getApplicationContext()));
        mViewHolder.onPause();
        super.onPause();
    }

    /**
     * Called when the submit button is clicked
     * @param v The button
     */
    public void onSubmit(View v) {
        saveGeofavorite();
    }

    /**
     * Checks fields and sends updated geofavorite to Nextcloud instance
     */
    private void saveGeofavorite() {
        mViewHolder.updateModel(mGeofavorite);

        if (!mGeofavorite.valid()) {
            Toast.makeText(GeofavoriteDetailActivity.this, R.string.incomplete_geofavorite, Toast.LENGTH_SHORT).show();
            return;
        }

        mViewModel.saveGeofavorite(mGeofavorite);
    }

    /**
     * Obtains the current location (requesting user's permission, if necessary)
     * and calls updateLocationField()
     */
    private void getLocation() {
        // Check if user granted location permission
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // User didn't grant permission. Ask it.
            requestPermissions(new String[] {Manifest.permission.ACCESS_FINE_LOCATION}, PERMISSION_REQUEST_CODE);
            return;
        }

        LocationManager locationManager = (LocationManager)
                getSystemService(Context.LOCATION_SERVICE);
        // Try to use last available location
        Location lastKnown = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        if (lastKnown != null)
            updateLocationField(lastKnown);
        // Register for location updates in case the user moves before saving
        locationManager.requestLocationUpdates(
                LocationManager.GPS_PROVIDER, 1000, 10, this
        );
    }

    /**
     * Compiles the geofavorite location field with the provided location object
     * @param location to set in the geofavorite
     */
    private void updateLocationField(Location location) {
        // Update model
        mGeofavorite.setLat(location.getLatitude());
        mGeofavorite.setLng(location.getLongitude());
        // Update view
        mViewHolder.updateViewCoords(mGeofavorite);
        mViewHolder.setAccuracy(location.getAccuracy());
    }


    /** Location updates callbacks **/

    @Override
    public void onLocationChanged(@NonNull Location location) {
        updateLocationField(location);
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {}

    @Override
    public void onProviderEnabled(@NonNull String provider) {}

    @Override
    public void onProviderDisabled(@NonNull String provider) {}

    @Override
    public void onPointerCaptureChanged(boolean hasCapture) {}


    /** Position permission request result **/

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_REQUEST_CODE && permissions[0].equals(Manifest.permission.ACCESS_FINE_LOCATION)) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                getLocation();
            } else {
                Toast.makeText(this, R.string.location_permission_required, Toast.LENGTH_LONG).show();
                finish();
            }
        }
    }



    private class ViewHolder implements View.OnClickListener {
        private final ActivityGeofavoriteDetailBinding binding;
        private DateTimeFormatter dateFormatter = DateTimeFormatter.ofLocalizedDate(FormatStyle.LONG);
        private OnSubmitListener listener;
        private Marker mapMarker;


        public ViewHolder(LayoutInflater inflater) {

            this.binding = ActivityGeofavoriteDetailBinding.inflate(inflater);
            this.binding.submitBt.setOnClickListener(this);
            this.binding.mapBt.setOnClickListener(this);
            this.binding.backBt.setOnClickListener(this);
            this.binding.actionIconShare.setOnClickListener(this);
            this.binding.actionIconDelete.setOnClickListener(this);
            this.binding.actionIconNav.setOnClickListener(this);

            // Set categories adapter
            CategoriesSpinnerAdapter categoriesAdapter = new CategoriesSpinnerAdapter(binding.root.getContext());
            this.binding.categoryAt.setAdapter(categoriesAdapter);
            this.binding.categoryAt.setText(Geofavorite.DEFAULT_CATEGORY);

            // Set map properties
            this.binding.map.getZoomController().setVisibility(CustomZoomButtonsController.Visibility.NEVER);
            this.binding.map.setMultiTouchControls(true);
            MapUtils.setTheme(this.binding.map);
//            this.binding.map.setTilesScaledToDpi(true);

            // Create marker
            mapMarker = new Marker(binding.map);
            mapMarker.setIcon(AppCompatResources.getDrawable(GeofavoriteDetailActivity.this, R.drawable.ic_map_pin));
            mapMarker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
            binding.map.getOverlays().add(mapMarker);
        }

        public View getRootView() {
            return this.binding.root;
        }

        public void updateView(Geofavorite item) {
            binding.collapsingToolbar.setTitle(item.getName() != null ? item.getName() : getString(R.string.new_geobookmark));
            binding.nameEt.setText(item.getName());
            binding.descriptionEt.setText(item.getComment());
            binding.createdTv.setText(item.getLocalDateCreated().format(dateFormatter));
            binding.modifiedTv.setText(item.getLocalDateCreated().format(dateFormatter));
            binding.categoryAt.setText(item.getCategory());
            updateViewCoords(item);
        }

        public void updateViewCoords(Geofavorite item) {
            binding.coordsTv.setText(item.getCoordinatesString());

            // Center map
            GeoPoint position = new GeoPoint(item.getLat(), item.getLng());
            IMapController mapController = binding.map.getController();
            mapController.setZoom(19.0f);
            mapController.setCenter(position);

            // Set pin
            mapMarker.setPosition(position);
        }

        public void updateModel(Geofavorite item) {
            item.setName(binding.nameEt.getText().toString());
            item.setComment(binding.descriptionEt.getText().toString());
            item.setCategory(binding.categoryAt.getText().toString());
            item.setDateModified(System.currentTimeMillis() / 1000);
        }

        public void setUpdating(boolean updating) {
            binding.progress.setVisibility(updating ? View.VISIBLE : View.GONE);
        }

        public void setAccuracy(float accuracy) {
            // Display accuracy in meters
            binding.accuracyTv.setText(getString(R.string.accuracy).replace("{accuracy}", ((int)accuracy) + ""));
            // Display accuracy in progress bar
            int accuracyPercent = (int)accuracy > 100 ? 0 : Math.abs((int)accuracy - 100);
            binding.accuracyProgress.setIndeterminate(false);
            binding.accuracyProgress.setProgress(accuracyPercent);
        }

        public void setCategories(HashSet<String> categories) {
            ((CategoriesSpinnerAdapter)binding.categoryAt.getAdapter()).setCategoriesList(categories);
        }

        public void hideAccuracy() {
            binding.accuracyTv.setVisibility(View.GONE);
            binding.accuracyProgressContainer.setVisibility(View.GONE);
        }

        public void hideActions() {
            binding.actionIcons.setVisibility(View.GONE);
        }

        public void setOnSubmitListener(OnSubmitListener listener) {
            this.listener = listener;
        }

        public void onResume() {
            binding.map.onResume();
        }

        public void onPause() {
            binding.map.onPause();
        }

        @Override
        public void onClick(View v) {
            if (v.getId() == R.id.submit_bt && this.listener != null) {
                this.listener.onSubmit();
            }
            if (v.getId() == R.id.map_bt && this.listener != null) {
                this.listener.onMapClicked();
            }
            if (v.getId() == R.id.back_bt && this.listener != null) {
                this.listener.onBackPressed();
            }
            if (v.getId() == R.id.manual_pos_bt && this.listener != null) {
                this.listener.onMapEditPressed();
            }

            // Actions
            if (v.getId() == R.id.action_icon_share && this.listener != null) {
                this.listener.onActionIconShareClicked();
            }
            if (v.getId() == R.id.action_icon_nav && this.listener != null) {
                this.listener.onActionIconNavClicked();
            }
            if (v.getId() == R.id.action_icon_delete && this.listener != null) {
                this.listener.onActionIconDeleteClicked();
            }
        }
    }

    protected interface OnSubmitListener {
        void onSubmit();
        void onMapClicked();
        void onBackPressed();
        void onMapEditPressed();
        void onActionIconShareClicked();
        void onActionIconNavClicked();
        void onActionIconDeleteClicked();
    }
}
