package it.danieleverducci.nextcloudmaps.fragments;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.core.app.ActivityCompat;
import androidx.core.content.res.ResourcesCompat;
import androidx.core.graphics.drawable.DrawableCompat;
import androidx.lifecycle.Observer;

import org.osmdroid.api.IMapController;
import org.osmdroid.events.MapEventsReceiver;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.CustomZoomButtonsController;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.MapEventsOverlay;
import org.osmdroid.views.overlay.Marker;
import org.osmdroid.views.overlay.Overlay;
import org.osmdroid.views.overlay.infowindow.InfoWindow;
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider;
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay;

import java.util.List;
import java.util.Set;

import it.danieleverducci.nextcloudmaps.R;
import it.danieleverducci.nextcloudmaps.activity.detail.GeofavoriteDetailActivity;
import it.danieleverducci.nextcloudmaps.activity.main.MainActivity;
import it.danieleverducci.nextcloudmaps.activity.mappicker.MapPickerActivity;
import it.danieleverducci.nextcloudmaps.model.Geofavorite;
import it.danieleverducci.nextcloudmaps.utils.GeoUriParser;
import it.danieleverducci.nextcloudmaps.utils.MapUtils;
import it.danieleverducci.nextcloudmaps.utils.SettingsManager;
import it.danieleverducci.nextcloudmaps.views.GeofavMarkerInfoWindow;

public class GeofavoriteMapFragment extends GeofavoritesFragment implements MainActivity.OnGpsPermissionGrantedListener {

    private MapView map;
    private MyLocationNewOverlay mLocationOverlay;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        MapUtils.configOsmdroid(requireContext());
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_geofavorite_map, container, false);

        // Register listeners
        v.findViewById(R.id.center_position).setOnClickListener((cpv) -> moveToUserPosition());

        // Setup map
        map = v.findViewById(R.id.map);
        map.getZoomController().setVisibility(CustomZoomButtonsController.Visibility.NEVER);
        map.setMultiTouchControls(true);
        MapUtils.setTheme(map);
        MapEventsOverlay meo = new MapEventsOverlay(new MapEventsReceiver() {
            @Override
            public boolean singleTapConfirmedHelper(GeoPoint p) {
                InfoWindow.closeAllInfoWindowsOn(map);
                return false;
            }

            @Override
            public boolean longPressHelper(GeoPoint p) {
                // Create new geofavorite (go to geofav creation activity)
                Uri geoUri = GeoUriParser.createGeoUri(p.getLatitude(), p.getLongitude(), null);
                Intent i = new Intent(requireActivity(), GeofavoriteDetailActivity.class);
                i.setData(geoUri);
                startActivity(i);
                return true;
            }
        });
        map.getOverlays().add(0, meo);
        showUserPosition();

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

        return v;
    }

    @Override
    public void onStart() {
        super.onStart();

        ((MainActivity)requireActivity()).addOnGpsPermissionGrantedListener(this);
    }

    @Override
    public void onDatasetChange(List<Geofavorite> items) {
        clearAllMarkers();
        for(Geofavorite gf : items)
            addMarker(gf);
        map.invalidate();
    }

    @Override
    public void onStop() {
        super.onStop();

        ((MainActivity)requireActivity()).removeOnGpsPermissionGrantedListener(this);
        SettingsManager.setLastMapPosition(
            requireContext(),
            map.getMapCenter().getLatitude(),
            map.getMapCenter().getLongitude(),
            map.getZoomLevelDouble()
        );
    }

    @Override
    public void onGpsPermissionGranted() {
        showUserPosition();
    }

    private void showUserPosition() {
        // Center map on last position
        double[] pos = SettingsManager.getLastMapPosition(requireContext());
        IMapController mapController = map.getController();
        mapController.setCenter(new GeoPoint(pos[0], pos[1]));
        mapController.setZoom(pos[2]);

        // Check if user granted location permission
        if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED
        ) {
            // User didn't grant permission. Ask it.
            ((MainActivity)requireActivity()).requestGpsPermissions();
            return;
        }

        // Display user position on screen
        mLocationOverlay = new MyLocationNewOverlay(new GpsMyLocationProvider(requireContext()), map);
        Bitmap personIcon = ((BitmapDrawable)AppCompatResources.getDrawable(requireContext(), R.mipmap.ic_person)).getBitmap();
        mLocationOverlay.setPersonIcon(personIcon);
        mLocationOverlay.setDirectionIcon(personIcon);
        mLocationOverlay.setPersonAnchor(.5f, .5f);
        mLocationOverlay.setDirectionAnchor(.5f, .5f);
        // On first gps fix, show "center to my position" icon
        mLocationOverlay.runOnFirstFix(() -> {
            if(getActivity() != null) {
                getActivity().runOnUiThread(() -> {
                    getView().findViewById(R.id.center_position).setVisibility(View.VISIBLE);
                });
            }
        });
        mLocationOverlay.enableMyLocation();
        map.getOverlays().add(mLocationOverlay);
    }

    void moveToUserPosition() {
        if (mLocationOverlay != null)
            map.getController().animateTo(mLocationOverlay.getMyLocation());
    }

    private void addMarker(Geofavorite geofavorite){
        GeoPoint pos = new GeoPoint(geofavorite.getLat(), geofavorite.getLng());

        // Set icon and color
        Drawable icon = DrawableCompat.wrap(AppCompatResources.getDrawable(requireContext(), R.drawable.ic_map_pin));
        DrawableCompat.setTint(icon, geofavorite.categoryColor() == 0 ? requireContext().getColor(R.color.defaultBrand) : geofavorite.categoryColor());

        // Set infowindow (popup opened on marker click) and its listeners
        GeofavMarkerInfoWindow iw = new GeofavMarkerInfoWindow(map, geofavorite);
        iw.setOnGeofavMarkerInfoWindowClickListener(new GeofavMarkerInfoWindow.OnGeofavMarkerInfoWindowClickListener() {
            @Override
            public void onGeofavMarkerInfoWindowEditClick() {
                openGeofavorite(geofavorite);
            }
            @Override
            public void onGeofavMarkerInfoWindowShareClick() {
                shareGeofavorite(geofavorite);
            }
            @Override
            public void onGeofavMarkerInfoWindowNavClick() {
                navigateToGeofavorite(geofavorite);

            }
            @Override
            public void onGeofavMarkerInfoWindowDeleteClick() {
                deleteGeofavorite(geofavorite);
            }
        });

        // Set marker
        Marker m = new Marker(map);
        m.setPosition(pos);
        m.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
        m.setIcon(icon);
        m.setTitle(geofavorite.getName());
        m.setSnippet(geofavorite.getComment());
        m.setSubDescription(geofavorite.getCategory());
        m.setInfoWindow(iw);
        map.getOverlays().add(m);
    }

    private void clearAllMarkers() {
        // Close any open infowindow before removing related marker
        InfoWindow.closeAllInfoWindowsOn(map);
        // Remove all markers, leaving the other overlays (user position and tap listener ones)
        for(Overlay o : map.getOverlays()) {
            if (o instanceof Marker) {
                map.getOverlays().remove(o);
            }
        }
    }
}
