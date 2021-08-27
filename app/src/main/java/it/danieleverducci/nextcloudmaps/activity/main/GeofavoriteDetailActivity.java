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

package it.danieleverducci.nextcloudmaps.activity.main;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import it.danieleverducci.nextcloudmaps.R;

public class GeofavoriteDetailActivity extends AppCompatActivity implements LocationListener, ActivityCompat.OnRequestPermissionsResultCallback {

    public static final String ARG_GEOFAVORITE_ID = "geofavid";
    private static final int PERMISSION_REQUEST_CODE = 9999;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_geofavorite_detail);

        int id = getIntent().getIntExtra(ARG_GEOFAVORITE_ID, 0);
        if (id == 0) {
            // New geofavorite: precompile location
            getLocation();
        }

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
                LocationManager.GPS_PROVIDER, 5000, 10, this
        );
    }

    /**
     * Compiles the geofavorite location field with the provided location object
     * @param location to set in the geofavorite
     */
    private void updateLocationField(Location location) {
        Log.d("Location", location.toString());
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
