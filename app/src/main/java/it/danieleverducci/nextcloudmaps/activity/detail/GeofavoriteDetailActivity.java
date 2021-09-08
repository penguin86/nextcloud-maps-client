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
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import it.danieleverducci.nextcloudmaps.R;
import it.danieleverducci.nextcloudmaps.api.ApiProvider;
import it.danieleverducci.nextcloudmaps.databinding.ActivityGeofavoriteDetailBinding;
import it.danieleverducci.nextcloudmaps.model.Geofavorite;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class GeofavoriteDetailActivity extends AppCompatActivity implements LocationListener, ActivityCompat.OnRequestPermissionsResultCallback {

    public static final String TAG = "GeofavDetail";
    public static final String ARG_GEOFAVORITE = "geofav";
    private static final int PERMISSION_REQUEST_CODE = 9999;

    private ActivityGeofavoriteDetailBinding binding;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityGeofavoriteDetailBinding.inflate(getLayoutInflater());
        setContentView(binding.root);

        if (getIntent().hasExtra(ARG_GEOFAVORITE)) {
            // Opening geofavorite from list
            Geofavorite gfFromList = (Geofavorite) getIntent().getSerializableExtra(ARG_GEOFAVORITE);
            binding.setGeofavorite(gfFromList);
        } else {
            // New geofavorite
            Geofavorite gf = new Geofavorite();
            gf.setDateCreated(System.currentTimeMillis());
            gf.setDateModified(System.currentTimeMillis());
            binding.setGeofavorite(gf);
            // Precompile location
            getLocation();
        }

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
        Geofavorite gf = binding.getGeofavorite();
        gf.setName(binding.nameEt.getText().toString());
        gf.setComment(binding.descriptionEt.getText().toString());
        gf.setDateModified(System.currentTimeMillis());

        if (!gf.valid()) {
            Toast.makeText(GeofavoriteDetailActivity.this, R.string.incomplete_geofavorite, Toast.LENGTH_SHORT).show();
            return;
        }

        Call<Geofavorite> call;
        if (gf.getId() == 0) {
            // New geofavorite
            call = ApiProvider.getAPI().createGeofavorite(gf);
        } else {
            // Update existing geofavorite
            call = ApiProvider.getAPI().updateGeofavorite(gf.getId(), gf);
        }
        call.enqueue(new Callback<Geofavorite>() {
            @Override
            public void onResponse(Call<Geofavorite> call, Response<Geofavorite> response) {
                finish();
            }

            @Override
            public void onFailure(Call<Geofavorite> call, Throwable t) {
                Toast.makeText(GeofavoriteDetailActivity.this, R.string.error_saving_geofavorite, Toast.LENGTH_SHORT).show();
                Log.e(TAG, "Unable to update geofavorite: " + t.getMessage());
            }
        });
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
        binding.getGeofavorite().setLat(location.getLatitude());
        binding.getGeofavorite().setLng(location.getLongitude());
        binding.notifyChange();
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
}
