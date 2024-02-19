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

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatImageButton;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.ViewModelProvider;
import androidx.preference.PreferenceManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.card.MaterialCardView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.nextcloud.android.sso.helper.SingleAccountHelper;

import java.util.ArrayList;
import java.util.List;

import it.danieleverducci.nextcloudmaps.R;
import it.danieleverducci.nextcloudmaps.activity.NextcloudMapsStyledActivity;
import it.danieleverducci.nextcloudmaps.activity.about.AboutActivity;
import it.danieleverducci.nextcloudmaps.activity.detail.GeofavoriteDetailActivity;
import it.danieleverducci.nextcloudmaps.activity.login.LoginActivity;
import it.danieleverducci.nextcloudmaps.activity.main.NavigationAdapter.NavigationItem;
import it.danieleverducci.nextcloudmaps.activity.main.SortingOrderDialogFragment.OnSortingOrderListener;
import it.danieleverducci.nextcloudmaps.activity.mappicker.MapPickerActivity;
import it.danieleverducci.nextcloudmaps.api.ApiProvider;
import it.danieleverducci.nextcloudmaps.fragments.GeofavoriteListFragment;
import it.danieleverducci.nextcloudmaps.fragments.GeofavoriteMapFragment;
import it.danieleverducci.nextcloudmaps.fragments.GeofavoritesFragment;
import it.danieleverducci.nextcloudmaps.model.Geofavorite;
import it.danieleverducci.nextcloudmaps.utils.SettingsManager;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;
import static it.danieleverducci.nextcloudmaps.activity.main.GeofavoriteAdapter.*;
import static it.danieleverducci.nextcloudmaps.activity.main.GeofavoriteAdapter.SORT_BY_CREATED;
import static it.danieleverducci.nextcloudmaps.activity.main.GeofavoriteAdapter.SORT_BY_TITLE;
import androidx.lifecycle.Observer;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

public class MainActivity extends NextcloudMapsStyledActivity {

    private static final String TAG = "MainActivity";

    private static final String NAVIGATION_KEY_ADD_GEOFAVORITE_FROM_GPS = "add_from_gps";
    private static final String NAVIGATION_KEY_ADD_GEOFAVORITE_FROM_MAP = "add_from_map";
    private static final String NAVIGATION_KEY_SHOW_ABOUT = "about";
    private static final String NAVIGATION_KEY_SWITCH_ACCOUNT = "switch_account";

    private DrawerLayout drawerLayout;

    private boolean isFabOpen = false;

    NavigationAdapter navigationCommonAdapter;

    public void openDrawer() {
        drawerLayout.openDrawer(GravityCompat.START);
    }

    public void showMap() {
        replaceFragment(new GeofavoriteMapFragment());
    }

    public void showList() {
        replaceFragment(new GeofavoriteListFragment());
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

}