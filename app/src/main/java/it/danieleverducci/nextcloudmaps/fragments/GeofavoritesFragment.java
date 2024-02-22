package it.danieleverducci.nextcloudmaps.fragments;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.AppCompatImageButton;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import it.danieleverducci.nextcloudmaps.R;
import it.danieleverducci.nextcloudmaps.activity.detail.GeofavoriteDetailActivity;
import it.danieleverducci.nextcloudmaps.activity.main.GeofavoritesFragmentViewModel;
import it.danieleverducci.nextcloudmaps.activity.main.MainActivity;
import it.danieleverducci.nextcloudmaps.model.Geofavorite;
import it.danieleverducci.nextcloudmaps.utils.IntentGenerator;

/**
 * Separates the specific list/map implementation details providing a standard interface
 * to communicate with the activity
 */
public abstract class GeofavoritesFragment extends Fragment {

    protected GeofavoritesFragmentViewModel mGeofavoritesFragmentViewModel;

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
    }

    @Override
    public void onStart() {
        super.onStart();

        mGeofavoritesFragmentViewModel.updateGeofavorites();
    }


    abstract public void onSearch(String query);


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

}
