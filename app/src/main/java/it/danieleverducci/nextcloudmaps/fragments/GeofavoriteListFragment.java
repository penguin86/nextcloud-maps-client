package it.danieleverducci.nextcloudmaps.fragments;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;
import static it.danieleverducci.nextcloudmaps.activity.main.GeofavoriteAdapter.SORT_BY_CATEGORY;
import static it.danieleverducci.nextcloudmaps.activity.main.GeofavoriteAdapter.SORT_BY_CREATED;
import static it.danieleverducci.nextcloudmaps.activity.main.GeofavoriteAdapter.SORT_BY_DISTANCE;
import static it.danieleverducci.nextcloudmaps.activity.main.GeofavoriteAdapter.SORT_BY_TITLE;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatImageButton;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.Observer;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.android.material.card.MaterialCardView;

import java.util.ArrayList;
import java.util.List;

import it.danieleverducci.nextcloudmaps.R;
import it.danieleverducci.nextcloudmaps.activity.main.GeofavoriteAdapter;
import it.danieleverducci.nextcloudmaps.activity.main.MainActivity;
import it.danieleverducci.nextcloudmaps.activity.main.SortingOrderDialogFragment;
import it.danieleverducci.nextcloudmaps.model.Geofavorite;
import it.danieleverducci.nextcloudmaps.utils.GeofavoritesFilter;
import it.danieleverducci.nextcloudmaps.utils.SettingsManager;

public class GeofavoriteListFragment extends GeofavoritesFragment implements SortingOrderDialogFragment.OnSortingOrderListener {

    private SwipeRefreshLayout swipeRefresh;
    private GeofavoriteAdapter geofavoriteAdapter;
    private List<Geofavorite> geofavorites = new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_geofavorite_list, container, false);

        // Setup list
        int sortRule = SettingsManager.getGeofavoriteListSortBy(requireContext());

        RecyclerView recyclerView = v.findViewById(R.id.recycler_view);
        LinearLayoutManager layoutManager = new LinearLayoutManager(requireContext());
        recyclerView.setLayoutManager(layoutManager);

        GeofavoriteAdapter.ItemClickListener rvItemClickListener = new GeofavoriteAdapter.ItemClickListener() {
            @Override
            public void onItemClick(Geofavorite item) {
                openGeofavorite(item);
            }

            @Override
            public void onItemShareClick(Geofavorite item) {
                shareGeofavorite(item);
            }

            @Override
            public void onItemNavClick(Geofavorite item) {
                navigateToGeofavorite(item);
            }

            @Override
            public void onItemDeleteClick(Geofavorite item) {
                deleteGeofavorite(item);
            }
        };

        geofavoriteAdapter = new GeofavoriteAdapter(requireContext(), rvItemClickListener);
        recyclerView.setAdapter(geofavoriteAdapter);
        geofavoriteAdapter.setSortRule(sortRule);

        // Register for data source events
        mGeofavoritesFragmentViewModel.getIsUpdating().observe(getViewLifecycleOwner(), new Observer<Boolean>() {
            @Override
            public void onChanged(@Nullable Boolean changed) {
                if(Boolean.TRUE.equals(changed)){
                    swipeRefresh.setRefreshing(true);
                }
                else{
                    swipeRefresh.setRefreshing(false);
                }
            }
        });
        mGeofavoritesFragmentViewModel.getGeofavorites().observe(getViewLifecycleOwner(), new Observer<List<Geofavorite>>() {
            @Override
            public void onChanged(List<Geofavorite> geofavorites) {
                GeofavoriteListFragment.this.geofavorites = geofavorites;
                geofavoriteAdapter.setGeofavoriteList(geofavorites);
            }
        });

        // Setup view listeners
        swipeRefresh = v.findViewById(R.id.swipe_refresh);
        swipeRefresh.setOnRefreshListener(() ->
                mGeofavoritesFragmentViewModel.updateGeofavorites());

        AppCompatImageView sortButton = v.findViewById(R.id.sort_mode);
        sortButton.setOnClickListener(view -> openSortingOrderDialogFragment(geofavoriteAdapter.getSortRule()));

        View showMapButton = v.findViewById(R.id.view_mode_map);
        showMapButton.setOnClickListener(View -> ((MainActivity)requireActivity()).showMap());

        return v;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Set icons
        int sortRule = SettingsManager.getGeofavoriteListSortBy(requireContext());
        updateSortingIcon(sortRule);
    }

    public void onSearch(String query) {
        geofavoriteAdapter.setGeofavoriteList(
                (new GeofavoritesFilter(geofavorites)).byText(query)
        );
    }

    @Override
    public void onSortingOrderChosen(int sortSelection) {
        geofavoriteAdapter.setSortRule(sortSelection);
        updateSortingIcon(sortSelection);

        SettingsManager.setGeofavoriteListSortBy(requireContext(), sortSelection);
    }

    private void openSortingOrderDialogFragment(int sortOrder) {
        FragmentTransaction fragmentTransaction = requireActivity().getSupportFragmentManager().beginTransaction();
        fragmentTransaction.addToBackStack(null);

        SortingOrderDialogFragment sodf = SortingOrderDialogFragment.newInstance(sortOrder);
        sodf.setOnSortingOrderListener(this);
        sodf.show(fragmentTransaction, SortingOrderDialogFragment.SORTING_ORDER_FRAGMENT);
    }

    private void updateSortingIcon(int sortSelection) {
        AppCompatImageView sortButton = requireView().findViewById(R.id.sort_mode);
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

}
