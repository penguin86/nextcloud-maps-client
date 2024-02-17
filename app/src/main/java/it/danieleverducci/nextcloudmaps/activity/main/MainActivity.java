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

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatImageButton;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.ViewModelProvider;
import androidx.preference.PreferenceManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

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
import it.danieleverducci.nextcloudmaps.model.Geofavorite;
import it.danieleverducci.nextcloudmaps.utils.GeoUriParser;
import it.danieleverducci.nextcloudmaps.utils.IntentGenerator;
import it.danieleverducci.nextcloudmaps.utils.SettingsManager;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;
import static it.danieleverducci.nextcloudmaps.activity.main.GeofavoriteAdapter.*;
import static it.danieleverducci.nextcloudmaps.activity.main.GeofavoriteAdapter.SORT_BY_CREATED;
import static it.danieleverducci.nextcloudmaps.activity.main.GeofavoriteAdapter.SORT_BY_TITLE;
import androidx.lifecycle.Observer;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

public class MainActivity extends NextcloudMapsStyledActivity implements OnSortingOrderListener {

    private static final String TAG = "MainActivity";

    private static final String NAVIGATION_KEY_ADD_GEOFAVORITE_FROM_GPS = "add_from_gps";
    private static final String NAVIGATION_KEY_ADD_GEOFAVORITE_FROM_MAP = "add_from_map";
    private static final String NAVIGATION_KEY_SHOW_ABOUT = "about";
    private static final String NAVIGATION_KEY_SWITCH_ACCOUNT = "switch_account";

    private SharedPreferences preferences;

    private DrawerLayout drawerLayout;
    private Toolbar toolbar;
    private MaterialCardView homeToolbar;
    private SearchView searchView;
    private SwipeRefreshLayout swipeRefresh;
    private RecyclerView recyclerView;
    private StaggeredGridLayoutManager layoutManager;
    private FloatingActionButton fab;

    private GeofavoriteAdapter geofavoriteAdapter;
    private ItemClickListener rvItemClickListener;
    private MainActivityViewModel mMainActivityViewModel;

    private boolean isFabOpen = false;

    NavigationAdapter navigationCommonAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        preferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());


        int sortRule = SettingsManager.getGeofavoriteListSortBy(this);
        boolean gridViewEnabled = SettingsManager.isGeofavoriteListShownAsGrid(this);

        recyclerView = findViewById(R.id.recycler_view);
        layoutManager = new StaggeredGridLayoutManager(gridViewEnabled ? 2 : 1, StaggeredGridLayoutManager.VERTICAL);
        recyclerView.setLayoutManager(layoutManager);

        rvItemClickListener = new ItemClickListener() {
            @Override
            public void onItemClick(Geofavorite item) {
                showGeofavoriteDetailActivity(item);
            }

            @Override
            public void onItemShareClick(Geofavorite item) {
                startActivity(Intent.createChooser(IntentGenerator.newShareIntent(MainActivity.this, item), getString(R.string.share_via)));
            }

            @Override
            public void onItemNavClick(Geofavorite item) {
                startActivity(IntentGenerator.newGeoUriIntent(MainActivity.this, item));
            }

            @Override
            public void onItemDeleteClick(Geofavorite item) {
                showGeofavoriteDeteleDialog(item);
            }
        };

        geofavoriteAdapter = new GeofavoriteAdapter(this, rvItemClickListener);
        recyclerView.setAdapter(geofavoriteAdapter);
        geofavoriteAdapter.setSortRule(sortRule);


        mMainActivityViewModel = new ViewModelProvider(this).get(MainActivityViewModel.class);
        mMainActivityViewModel.init(getApplicationContext());
        mMainActivityViewModel.getIsUpdating().observe(this, new Observer<Boolean>() {
            @Override
            public void onChanged(@Nullable Boolean aBoolean) {
                if(aBoolean){
                    swipeRefresh.setRefreshing(true);
                }
                else{
                    swipeRefresh.setRefreshing(false);
                }
            }
        });
        mMainActivityViewModel.getOnFinished().observe(this, new Observer<Boolean>() {
            @Override
            public void onChanged(@Nullable Boolean success) {
                if(success == null || !success){
                    Toast.makeText(MainActivity.this, R.string.list_geofavorite_connection_error, Toast.LENGTH_LONG).show();
                }
            }
        });
        mMainActivityViewModel.getGeofavorites().observe(this, new Observer<List<Geofavorite>>() {
            @Override
            public void onChanged(List<Geofavorite> geofavorites) {
                geofavoriteAdapter.setGeofavoriteList(geofavorites);
            }
        });
        mMainActivityViewModel.updateGeofavorites();

        swipeRefresh = findViewById(R.id.swipe_refresh);
        swipeRefresh.setOnRefreshListener(() ->
                mMainActivityViewModel.updateGeofavorites());

        fab = findViewById(R.id.open_fab);
        fab.setOnClickListener(view -> openFab(!this.isFabOpen));

        fab = findViewById(R.id.add_from_gps);
        fab.setOnClickListener(view -> addGeofavoriteFromGps());

        fab = findViewById(R.id.add_from_map);
        fab.setOnClickListener(view -> addGeofavoriteFromMap());

        toolbar = findViewById(R.id.toolbar);
        homeToolbar = findViewById(R.id.home_toolbar);

        searchView = findViewById(R.id.search_view);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String query) {
                geofavoriteAdapter.getFilter().filter(query);
                return false;
            }
        });

        searchView.setOnCloseListener(() -> {
            if (toolbar.getVisibility() == VISIBLE && TextUtils.isEmpty(searchView.getQuery())) {
                updateToolbars(true);
                return true;
            }
            return false;
        });

        setSupportActionBar(toolbar);
        setupNavigationMenu();

        homeToolbar.setOnClickListener(view -> updateToolbars(false));

        AppCompatImageView sortButton = findViewById(R.id.sort_mode);
        sortButton.setOnClickListener(view -> openSortingOrderDialogFragment(getSupportFragmentManager(), geofavoriteAdapter.getSortRule()));

        drawerLayout = findViewById(R.id.drawerLayout);
        AppCompatImageButton menuButton = findViewById(R.id.menu_button);
        menuButton.setOnClickListener(view -> drawerLayout.openDrawer(GravityCompat.START));

        AppCompatImageView viewButton = findViewById(R.id.view_mode);
        viewButton.setOnClickListener(view -> {
            boolean gridEnabled = layoutManager.getSpanCount() == 1;
            onGridIconChosen(gridEnabled);
        });

        updateSortingIcon(sortRule);
        updateGridIcon(gridViewEnabled);
    }

    @Override
    protected void onPause() {
        openFab(false);
        super.onPause();
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

    private void updateToolbars(boolean disableSearch) {
        homeToolbar.setVisibility(disableSearch ? VISIBLE : GONE);
        toolbar.setVisibility(disableSearch ? GONE : VISIBLE);
        if (disableSearch) {
            searchView.setQuery(null, true);
        }
        searchView.setIconified(disableSearch);
    }

    private void openSortingOrderDialogFragment(FragmentManager supportFragmentManager, int sortOrder) {
        FragmentTransaction fragmentTransaction = supportFragmentManager.beginTransaction();
        fragmentTransaction.addToBackStack(null);

        SortingOrderDialogFragment.newInstance(sortOrder).show(fragmentTransaction, SortingOrderDialogFragment.SORTING_ORDER_FRAGMENT);
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

    @Override
    public void onSortingOrderChosen(int sortSelection) {
        geofavoriteAdapter.setSortRule(sortSelection);
        updateSortingIcon(sortSelection);

        preferences.edit().putInt(getString(R.string.setting_sort_by), sortSelection).apply();
    }

    public void updateSortingIcon(int sortSelection) {
        AppCompatImageView sortButton = findViewById(R.id.sort_mode);
        switch (sortSelection) {
            case SORT_BY_TITLE:
                sortButton.setImageResource(R.drawable.ic_alphabetical_asc);
                break;
            case SORT_BY_CREATED:
                sortButton.setImageResource(R.drawable.ic_modification_asc);
                break;
            case SORT_BY_CATEGORY:
                sortButton.setImageResource(R.drawable.ic_category_asc);
                break;
            case SORT_BY_DISTANCE:
                sortButton.setImageResource(R.drawable.ic_distance_asc);
                break;
        }
    }

    public void onGridIconChosen(boolean gridEnabled) {
        layoutManager.setSpanCount(gridEnabled ? 2 : 1);
        updateGridIcon(gridEnabled);

        preferences.edit().putBoolean(getString(R.string.setting_grid_view_enabled), gridEnabled).apply();
    }

    public void updateGridIcon(boolean gridEnabled) {
        AppCompatImageView viewButton = findViewById(R.id.view_mode);
        viewButton.setImageResource(gridEnabled ? R.drawable.ic_view_list : R.drawable.ic_view_module);
    }

    private void showGeofavoriteDeteleDialog(Geofavorite item) {
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        builder.setMessage(getString(R.string.dialog_delete_message).replace("{name}", item.getName() != null ? item.getName() : ""))
                .setTitle(R.string.dialog_delete_title)
                .setPositiveButton(R.string.dialog_delete_delete, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        mMainActivityViewModel.deleteGeofavorite(item);
                        dialog.dismiss();
                    }
                })
                .setNegativeButton(R.string.dialog_delete_cancel, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.dismiss();
                    }
                });
        AlertDialog ad = builder.create();
        ad.show();
    }

    private void showGeofavoriteDetailActivity(Geofavorite item) {
        Intent i = new Intent(this, GeofavoriteDetailActivity.class);
        i.putExtra(GeofavoriteDetailActivity.ARG_GEOFAVORITE, item.getId());
        startActivity(i);
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