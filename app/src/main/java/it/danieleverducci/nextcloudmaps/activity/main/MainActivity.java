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
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatImageButton;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.preference.PreferenceManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.android.material.card.MaterialCardView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.nextcloud.android.sso.helper.SingleAccountHelper;

import java.util.ArrayList;
import java.util.List;

import it.danieleverducci.nextcloudmaps.R;
import it.danieleverducci.nextcloudmaps.activity.about.AboutActivity;
import it.danieleverducci.nextcloudmaps.activity.login.LoginActivity;
import it.danieleverducci.nextcloudmaps.activity.main.NavigationAdapter.NavigationItem;
import it.danieleverducci.nextcloudmaps.activity.main.SortingOrderDialogFragment.OnSortingOrderListener;
import it.danieleverducci.nextcloudmaps.api.ApiProvider;
import it.danieleverducci.nextcloudmaps.model.Geofavorite;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;
import static it.danieleverducci.nextcloudmaps.activity.main.GeofavoriteAdapter.*;
import static it.danieleverducci.nextcloudmaps.activity.main.GeofavoriteAdapter.SORT_BY_CREATED;
import static it.danieleverducci.nextcloudmaps.activity.main.GeofavoriteAdapter.SORT_BY_TITLE;

public class MainActivity extends AppCompatActivity implements MainView, OnSortingOrderListener {

    private static final int INTENT_ADD = 100;
    private static final int INTENT_EDIT = 200;

    private static final String NAVIGATION_KEY_ADD_GEOFAVORITE = "add";
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

    private MainPresenter presenter;
    private GeofavoriteAdapter geofavoriteAdapter;
    private ItemClickListener itemClickListener;

    NavigationAdapter navigationCommonAdapter;

    private ApiProvider mApi;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        preferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());

        int sortRule = preferences.getInt(getString(R.string.setting_sort_by), SORT_BY_CREATED);
        boolean gridViewEnabled = preferences.getBoolean(getString(R.string.setting_grid_view_enabled), false);

        recyclerView = findViewById(R.id.recycler_view);
        layoutManager = new StaggeredGridLayoutManager(gridViewEnabled ? 2 : 1, StaggeredGridLayoutManager.VERTICAL);
        recyclerView.setLayoutManager(layoutManager);

        presenter = new MainPresenter(this);

        itemClickListener = ((view, position) -> {
            Geofavorite geofavorite = geofavoriteAdapter.get(position);
            Intent i = new Intent();
            i.setAction(Intent.ACTION_VIEW);
            i.setData(geofavorite.getGeoUri());
            startActivity(i);
        });

        geofavoriteAdapter = new GeofavoriteAdapter(getApplicationContext(), itemClickListener);
        recyclerView.setAdapter(geofavoriteAdapter);

        geofavoriteAdapter.setSortRule(sortRule);

        swipeRefresh = findViewById(R.id.swipe_refresh);
        swipeRefresh.setOnRefreshListener(() -> presenter.getGeofavorites());

        fab = findViewById(R.id.add);
        fab.setOnClickListener(view -> addGeofavorite());

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

        mApi = new ApiProvider(getApplicationContext());
    }

    @Override
    protected void onStart() {
        super.onStart();

        // Update list
        presenter.getGeofavorites();
    }

    private void setupNavigationMenu() {
        ArrayList<NavigationItem> navItems = new ArrayList<>();

        navigationCommonAdapter = new NavigationAdapter(this, item -> {
            switch (item.id) {
                case NAVIGATION_KEY_ADD_GEOFAVORITE:
                    addGeofavorite();
                    break;
                case NAVIGATION_KEY_SHOW_ABOUT:
                    show_about();
                    break;
                case NAVIGATION_KEY_SWITCH_ACCOUNT:
                    switch_account();
                    break;
            }
        });

        navItems.add(new NavigationItem(NAVIGATION_KEY_ADD_GEOFAVORITE, getString(R.string.new_geobookmark), R.drawable.ic_add));
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

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == INTENT_ADD && resultCode == RESULT_OK) {
            presenter.getGeofavorites();
        } else if (requestCode == INTENT_EDIT && resultCode == RESULT_OK) {
            presenter.getGeofavorites();
        }
    }

    private void addGeofavorite() {
        startActivityForResult(
                new Intent(this, GeofavoriteDetailActivity.class), INTENT_ADD);
    }

    private void show_about() {
        startActivity(new Intent(this, AboutActivity.class));
    }

    private void switch_account() {
        SingleAccountHelper.setCurrentAccount(this, null);
        Intent intent = new Intent(MainActivity.this, LoginActivity.class);
        startActivity(intent);
    }

    @Override
    public void showLoading() {
        swipeRefresh.setRefreshing(true);
    }

    @Override
    public void hideLoading() {
        swipeRefresh.setRefreshing(false);
    }

    @Override
    public void onGetResult(List<Geofavorite> geofavorite_list) {
        geofavoriteAdapter.setGeofavoriteList(geofavorite_list);
    }

    @Override
    public void onErrorLoading(String message) {
        Toast.makeText(MainActivity.this, message, Toast.LENGTH_LONG).show();
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

}