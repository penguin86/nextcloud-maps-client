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
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.nextcloud.android.sso.helper.SingleAccountHelper;

import java.util.ArrayList;

import it.danieleverducci.nextcloudmaps.R;
import it.danieleverducci.nextcloudmaps.activity.NextcloudMapsStyledActivity;
import it.danieleverducci.nextcloudmaps.activity.about.AboutActivity;
import it.danieleverducci.nextcloudmaps.activity.detail.GeofavoriteDetailActivity;
import it.danieleverducci.nextcloudmaps.activity.login.LoginActivity;
import it.danieleverducci.nextcloudmaps.activity.main.NavigationAdapter.NavigationItem;
import it.danieleverducci.nextcloudmaps.activity.mappicker.MapPickerActivity;
import it.danieleverducci.nextcloudmaps.api.ApiProvider;
import it.danieleverducci.nextcloudmaps.fragments.GeofavoriteListFragment;
import it.danieleverducci.nextcloudmaps.fragments.GeofavoriteMapFragment;
import it.danieleverducci.nextcloudmaps.utils.SettingsManager;

public class MainActivity extends NextcloudMapsStyledActivity {

    private static final String TAG = "MainActivity";
    private static final int PERMISSION_REQUEST_CODE = 3890;

    private static final String NAVIGATION_KEY_ADD_GEOFAVORITE_FROM_GPS = "add_from_gps";
    private static final String NAVIGATION_KEY_ADD_GEOFAVORITE_FROM_MAP = "add_from_map";
    private static final String NAVIGATION_KEY_SHOW_ABOUT = "about";
    private static final String NAVIGATION_KEY_SWITCH_ACCOUNT = "switch_account";

    private ArrayList<OnGpsPermissionGrantedListener> onGpsPermissionGrantedListener = new ArrayList<>();
    private DrawerLayout drawerLayout;

    private boolean isFabOpen = false;

    NavigationAdapter navigationCommonAdapter;

    public void openDrawer() {
        drawerLayout.openDrawer(GravityCompat.START);
    }

    public void showMap() {
        replaceFragment(new GeofavoriteMapFragment());
        SettingsManager.setGeofavoriteListShownAsMap(this, true);
    }

    public void showList() {
        replaceFragment(new GeofavoriteListFragment());
        SettingsManager.setGeofavoriteListShownAsMap(this, false);
    }

    public void addOnGpsPermissionGrantedListener(OnGpsPermissionGrantedListener l) {
        onGpsPermissionGrantedListener.add(l);
    }

    public void removeOnGpsPermissionGrantedListener(OnGpsPermissionGrantedListener l) {
        onGpsPermissionGrantedListener.remove(l);
    }

    public void requestGpsPermissions() {
        ActivityCompat.requestPermissions(
            this,
            new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
            PERMISSION_REQUEST_CODE
        );
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == PERMISSION_REQUEST_CODE && permissions[0].equals(Manifest.permission.ACCESS_FINE_LOCATION)) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                for (OnGpsPermissionGrantedListener l : onGpsPermissionGrantedListener) {
                    l.onGpsPermissionGranted();
                }
            }
        }
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        boolean showMap = SettingsManager.isGeofavoriteListShownAsMap(this);
        if (showMap)
            showMap();
        else
            showList();

        FloatingActionButton fab = findViewById(R.id.open_fab);
        fab.setOnClickListener(view -> openFab(!this.isFabOpen));

        fab = findViewById(R.id.add_from_gps);
        fab.setOnClickListener(view -> addGeofavoriteFromGps());

        fab = findViewById(R.id.add_from_map);
        fab.setOnClickListener(view -> addGeofavoriteFromMap());

        setupNavigationMenu();

        drawerLayout = findViewById(R.id.drawerLayout);
    }

    @Override
    protected void onPause() {
        openFab(false);
        super.onPause();
    }

    private void replaceFragment(Fragment fragment) {
        FragmentManager manager = getSupportFragmentManager();
        FragmentTransaction transaction = manager.beginTransaction();
        transaction.replace(R.id.fragment_container, fragment);
        transaction.commit();
    }

    private void setupNavigationMenu() {
        ArrayList<NavigationItem> navItems = new ArrayList<>();

        navigationCommonAdapter = new NavigationAdapter(this, item -> {
            switch (item.id) {
                case NAVIGATION_KEY_ADD_GEOFAVORITE_FROM_GPS:
                    addGeofavoriteFromGps();
                    break;
                case NAVIGATION_KEY_ADD_GEOFAVORITE_FROM_MAP:
                    addGeofavoriteFromMap();
                    break;
                case NAVIGATION_KEY_SHOW_ABOUT:
                    show_about();
                    break;
                case NAVIGATION_KEY_SWITCH_ACCOUNT:
                    switch_account();
                    break;
            }
        });

        navItems.add(new NavigationItem(NAVIGATION_KEY_ADD_GEOFAVORITE_FROM_GPS, getString(R.string.new_geobookmark_gps), R.drawable.ic_add_gps));
        navItems.add(new NavigationItem(NAVIGATION_KEY_ADD_GEOFAVORITE_FROM_MAP, getString(R.string.new_geobookmark_map), R.drawable.ic_add_map));
        navItems.add(new NavigationItem(NAVIGATION_KEY_SHOW_ABOUT, getString(R.string.about), R.drawable.ic_info_grey));
        navItems.add(new NavigationItem(NAVIGATION_KEY_SWITCH_ACCOUNT, getString(R.string.switch_account), R.drawable.ic_logout_grey));
        navigationCommonAdapter.setItems(navItems);

        RecyclerView navigationMenuCommon = findViewById(R.id.navigationCommon);
        navigationMenuCommon.setAdapter(navigationCommonAdapter);
    }

    private void addGeofavoriteFromGps() {
        startActivity(
                new Intent(this, GeofavoriteDetailActivity.class)
        );
    }

    private void addGeofavoriteFromMap() {
        startActivity(
                new Intent(this, MapPickerActivity.class)
        );
    }

    private void show_about() {
        startActivity(new Intent(this, AboutActivity.class));
    }

    private void switch_account() {
        ApiProvider.logout();
        SingleAccountHelper.applyCurrentAccount(this, null);
        Intent intent = new Intent(MainActivity.this, LoginActivity.class);
        startActivity(intent);
        finish();
    }

    private void openFab(boolean open) {
        View fab = findViewById(R.id.open_fab);
        View addFromGpsFab = findViewById(R.id.add_from_gps);
        View addFromMapFab = findViewById(R.id.add_from_map);

        if (open) {
            this.isFabOpen = true;
            fab.animate().rotation(45.0f);
            addFromGpsFab.animate().translationY(-getResources().getDimension(R.dimen.fab_vertical_offset));
            addFromMapFab.animate().translationY(-getResources().getDimension(R.dimen.fab_vertical_offset) * 2);
        } else {
            this.isFabOpen = false;
            fab.animate().rotation(0f);
            addFromGpsFab.animate().translationY(0);
            addFromMapFab.animate().translationY(0);
        }

    }

    public interface OnGpsPermissionGrantedListener {
        public void onGpsPermissionGranted();
    }

}