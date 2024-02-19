package it.danieleverducci.nextcloudmaps.fragments;

import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.core.graphics.drawable.DrawableCompat;
import androidx.lifecycle.Observer;

import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;
import org.osmdroid.views.overlay.infowindow.MarkerInfoWindow;

import java.util.List;

import it.danieleverducci.nextcloudmaps.R;
import it.danieleverducci.nextcloudmaps.activity.main.MainActivity;
import it.danieleverducci.nextcloudmaps.model.Geofavorite;
import it.danieleverducci.nextcloudmaps.utils.MapUtils;
import it.danieleverducci.nextcloudmaps.views.GeofavMarkerInfoWindow;

public class GeofavoriteMapFragment extends GeofavoritesFragment {
    private MapView map;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        MapUtils.configOsmdroid(requireContext());
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_geofavorite_map, container, false);
        map = v.findViewById(R.id.map);

        // Setup view listeners
        View showListButton = v.findViewById(R.id.view_mode_list);
        showListButton.setOnClickListener(View -> ((MainActivity)requireActivity()).showList());

        View loadingWall = v.findViewById(R.id.loading_wall);

        // Register for data source events
        mGeofavoritesFragmentViewModel.getIsUpdating().observe(getViewLifecycleOwner(), new Observer<Boolean>() {
            @Override
            public void onChanged(@Nullable Boolean changed) {
                if(Boolean.TRUE.equals(changed)){
                    loadingWall.setVisibility(View.VISIBLE);
                }
                else{
                    loadingWall.setVisibility(View.GONE);
                }
            }
        });
        mGeofavoritesFragmentViewModel.getGeofavorites().observe(getViewLifecycleOwner(), new Observer<List<Geofavorite>>() {
            @Override
            public void onChanged(List<Geofavorite> geofavorites) {
                for(Geofavorite gf : geofavorites)
                    addMarker(gf);
            }
        });

        return v;
    }

    @Override
    public void onSearch(String query) {
        // TODO: filter
    }

    private void addMarker(Geofavorite geofavorite){
        GeoPoint pos = new GeoPoint(geofavorite.getLat(), geofavorite.getLng());

        Drawable icon = DrawableCompat.wrap(AppCompatResources.getDrawable(requireContext(), R.drawable.ic_list_pin));
        DrawableCompat.setTint(icon, geofavorite.categoryColor());

        Marker m = new Marker(map);
        m.setPosition(pos);
        m.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
        m.setIcon(icon);
        m.setTitle(geofavorite.getName());
        m.setSnippet(geofavorite.getComment());
        m.setSubDescription(geofavorite.getCategory());
        m.setInfoWindow(new GeofavMarkerInfoWindow(map));
        map.getOverlays().add(m);
    }
}
