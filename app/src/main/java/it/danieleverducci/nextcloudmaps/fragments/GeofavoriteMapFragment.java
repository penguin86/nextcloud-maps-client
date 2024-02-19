package it.danieleverducci.nextcloudmaps.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import it.danieleverducci.nextcloudmaps.R;
import it.danieleverducci.nextcloudmaps.activity.main.MainActivity;

public class GeofavoriteMapFragment extends GeofavoritesFragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_geofavorite_map, container, false);


        // Setup view listeners
        View showListButton = v.findViewById(R.id.view_mode_list);
        showListButton.setOnClickListener(View -> ((MainActivity)requireActivity()).showMap());

        return v;
    }

    @Override
    public void onSearch(String query) {

    }
}
