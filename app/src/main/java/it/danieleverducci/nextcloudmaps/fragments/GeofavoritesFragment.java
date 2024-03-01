package it.danieleverducci.nextcloudmaps.fragments;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.AppCompatImageButton;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.appcompat.widget.SearchView;
import androidx.core.graphics.drawable.DrawableCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import com.nextcloud.android.sso.helper.SingleAccountHelper;
import com.nextcloud.android.sso.model.SingleSignOnAccount;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import it.danieleverducci.nextcloudmaps.R;
import it.danieleverducci.nextcloudmaps.activity.detail.CategoriesAdapter;
import it.danieleverducci.nextcloudmaps.activity.detail.GeofavoriteDetailActivity;
import it.danieleverducci.nextcloudmaps.activity.main.GeofavoritesFragmentViewModel;
import it.danieleverducci.nextcloudmaps.activity.main.MainActivity;
import it.danieleverducci.nextcloudmaps.model.Geofavorite;
import it.danieleverducci.nextcloudmaps.utils.GeofavoritesFilter;
import it.danieleverducci.nextcloudmaps.utils.IntentGenerator;

/**
 * Separates the specific list/map implementation details providing a standard interface
 * to communicate with the activity
 */
public abstract class GeofavoritesFragment extends Fragment {
    private final String TAG = "GeofavoritesFragment";

    protected GeofavoritesFragmentViewModel mGeofavoritesFragmentViewModel;
    private View toolbar;
    private View homeToolbar;
    private SearchView searchView;
    private ImageButton filterButton;
    private List<Geofavorite> geofavorites = new ArrayList<>();
    private HashSet<String> categories = new HashSet<>();

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Load data
        mGeofavoritesFragmentViewModel = new ViewModelProvider(this).get(GeofavoritesFragmentViewModel.class);
        mGeofavoritesFragmentViewModel.init(requireContext());

        mGeofavoritesFragmentViewModel.getOnFinished().observe(this, new Observer<Boolean>() {
            @Override
            public void onChanged(@Nullable Boolean success) {
                if(success == null || !success){
                    Toast.makeText(requireContext(), R.string.list_geofavorite_connection_error, Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Set views
        AppCompatImageButton menuButton = view.findViewById(R.id.menu_button);
        menuButton.setOnClickListener(v -> ((MainActivity)requireActivity()).openDrawer());

        View userBadgeContainer = view.findViewById(R.id.user_badge_container);
        userBadgeContainer.setOnClickListener(v -> showSwitchAccountDialog());

        // Setup toolbar/searchbar
        toolbar = view.findViewById(R.id.toolbar);
        homeToolbar = view.findViewById(R.id.home_toolbar);
        filterButton = view.findViewById(R.id.search_filter);
        filterButton.setOnClickListener(v -> showCategoryFilterDialog());

        searchView = view.findViewById(R.id.search_view);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String query) {
                onDatasetChange(
                    (new GeofavoritesFilter(geofavorites)).byText(query)
                );
                return false;
            }
        });

        mGeofavoritesFragmentViewModel.getGeofavorites().observe(getViewLifecycleOwner(), new Observer<List<Geofavorite>>() {
            @Override
            public void onChanged(List<Geofavorite> geofavorites) {
                GeofavoritesFragment.this.geofavorites = geofavorites;
                onDatasetChange(geofavorites);
            }
        });
        mGeofavoritesFragmentViewModel.getCategories().observe(getViewLifecycleOwner(), new Observer<HashSet<String>>() {
            @Override
            public void onChanged(HashSet<String> categories) {
                GeofavoritesFragment.this.categories = categories;
            }
        });

        searchView.setOnCloseListener(() -> {
            if (toolbar.getVisibility() == VISIBLE && TextUtils.isEmpty(searchView.getQuery())) {
                updateToolbars(true);
                return true;
            }
            return false;
        });

        homeToolbar.setOnClickListener(v -> updateToolbars(false));
        // Set user badge (async)
        Handler h = new Handler();
        h.post(() -> {
            try {
                SingleSignOnAccount ssoAccount = SingleAccountHelper.getCurrentSingleSignOnAccount(requireContext());
                String userBadgePath = ssoAccount.url + "/index.php/avatar/" + ssoAccount.userId + "/64";
                if (getActivity() != null)
                    getActivity().runOnUiThread(
                            () -> Picasso.get().load(userBadgePath).into((AppCompatImageView)userBadgeContainer.findViewById(R.id.user_badge))
                    );
            } catch (Exception e) {
                Log.e(TAG, "Unable to load user image: " + e.toString());
            }
        });
    }

    @Override
    public void onStart() {
        super.onStart();

        // Reset filter and update data
        filterButton.setImageResource(R.drawable.ic_filter_off);
        mGeofavoritesFragmentViewModel.updateGeofavorites();
    }


    abstract public void onDatasetChange(List<Geofavorite> items);


    protected void openGeofavorite(Geofavorite item) {
        showGeofavoriteDetailActivity(item);
    }

    protected void shareGeofavorite(Geofavorite item) {
        startActivity(Intent.createChooser(IntentGenerator.newShareIntent(requireActivity(), item), getString(R.string.share_via)));
    }

    protected void navigateToGeofavorite(Geofavorite item) {
        startActivity(IntentGenerator.newGeoUriIntent(requireActivity(), item));
    }

    protected void deleteGeofavorite(Geofavorite item) {
        showGeofavoriteDeteleDialog(item);
    }



    private void showGeofavoriteDetailActivity(Geofavorite item) {
        Intent i = new Intent(requireContext(), GeofavoriteDetailActivity.class);
        i.putExtra(GeofavoriteDetailActivity.ARG_GEOFAVORITE, item.getId());
        startActivity(i);
    }

    private void showGeofavoriteDeteleDialog(Geofavorite item) {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setMessage(getString(R.string.dialog_delete_message).replace("{name}", item.getName() != null ? item.getName() : ""))
                .setTitle(R.string.dialog_delete_title)
                .setPositiveButton(R.string.dialog_delete_delete, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        mGeofavoritesFragmentViewModel.deleteGeofavorite(item);
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

    private void showSwitchAccountDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setMessage(R.string.dialog_logout_message)
                .setTitle(R.string.dialog_logout_title)
                .setPositiveButton(android.R.string.yes, (dialog, id) -> {
                    if (getActivity() != null)
                        ((MainActivity) getActivity()).switch_account();
                    dialog.dismiss();
                })
                .setNegativeButton(android.R.string.no, (dialog, id) -> dialog.dismiss());
        AlertDialog ad = builder.create();
        ad.show();
    }

    private void showCategoryFilterDialog() {
        if (categories.isEmpty()) {
            Toast.makeText(requireContext(), R.string.filtering_unavailable, Toast.LENGTH_SHORT).show();
            return;
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle(R.string.filtering_dialog_title);
        CategoriesAdapter ca = new CategoriesAdapter(requireContext());
        ca.setCategoriesList(categories);
        builder.setAdapter(ca, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // Set filter button enabled icon and color
                String categoryName = ca.getItem(which);
                filterButton.setImageResource(R.drawable.ic_filter);
                Drawable d = filterButton.getDrawable();
                DrawableCompat.setTint(
                        d,
                        Geofavorite.categoryColorFromName(categoryName) == 0
                                ? requireContext().getColor(R.color.defaultBrand)
                                : Geofavorite.categoryColorFromName(categoryName)
                );
                filterByCategory(categoryName);
            }
        });

        builder.setPositiveButton(R.string.filtering_dialog_cancel, (dialog, id) -> {
            dialog.dismiss();
            filterButton.setImageResource(R.drawable.ic_filter_off);
            filterByCategory(null);
        });
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void updateToolbars(boolean disableSearch) {
        homeToolbar.setVisibility(disableSearch ? VISIBLE : GONE);
        toolbar.setVisibility(disableSearch ? GONE : VISIBLE);
        if (disableSearch) {
            searchView.setQuery(null, true);
        }
        searchView.setIconified(disableSearch);
    }

    private void filterByCategory(String category) {
        onDatasetChange(
                (new GeofavoritesFilter(geofavorites)).byCategory(category)
        );
    }

}
