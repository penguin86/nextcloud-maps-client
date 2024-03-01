package it.danieleverducci.nextcloudmaps.views;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.View;

import androidx.appcompat.content.res.AppCompatResources;
import androidx.core.graphics.drawable.DrawableCompat;

import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.infowindow.InfoWindow;
import org.osmdroid.views.overlay.infowindow.MarkerInfoWindow;

import it.danieleverducci.nextcloudmaps.R;
import it.danieleverducci.nextcloudmaps.model.Geofavorite;

public class GeofavMarkerInfoWindow extends MarkerInfoWindow implements View.OnClickListener {
    private OnGeofavMarkerInfoWindowClickListener onGeofavMarkerInfoWindowClickListener;

    public GeofavMarkerInfoWindow(MapView mapView, Geofavorite geofavorite) {
        super(R.layout.infowindow_geofav, mapView);
        Context context = getView().getContext();

        // Set category color
        View category = getView().findViewById(R.id.bubble_subdescription);
        Drawable backgroundDrawable = category.getBackground();
        DrawableCompat.setTint(backgroundDrawable, geofavorite.categoryColor() == 0 ? context.getColor(R.color.defaultBrand) : geofavorite.categoryColor());

        // Set listeners
        getView().findViewById(R.id.action_icon_share).setOnClickListener(this);
        getView().findViewById(R.id.action_icon_nav).setOnClickListener(this);
        getView().findViewById(R.id.action_icon_delete).setOnClickListener(this);
        getView().findViewById(R.id.action_icon_edit).setOnClickListener(this);
    }

    public void setOnGeofavMarkerInfoWindowClickListener(OnGeofavMarkerInfoWindowClickListener l) {
        this.onGeofavMarkerInfoWindowClickListener = l;
    }

    @Override
    public void onOpen(Object item) {
        InfoWindow.closeAllInfoWindowsOn(getMapView());
        super.onOpen(item);
    }


    @Override
    public void onClick(View v) {
        if (onGeofavMarkerInfoWindowClickListener == null)
            return;

        if (v.getId() == R.id.action_icon_share)
            onGeofavMarkerInfoWindowClickListener.onGeofavMarkerInfoWindowShareClick();

        if (v.getId() == R.id.action_icon_nav)
            onGeofavMarkerInfoWindowClickListener.onGeofavMarkerInfoWindowNavClick();

        if (v.getId() == R.id.action_icon_delete)
            onGeofavMarkerInfoWindowClickListener.onGeofavMarkerInfoWindowDeleteClick();

        if (v.getId() == R.id.action_icon_edit)
            onGeofavMarkerInfoWindowClickListener.onGeofavMarkerInfoWindowEditClick();
    }


    public interface OnGeofavMarkerInfoWindowClickListener {
        public void onGeofavMarkerInfoWindowEditClick();
        public void onGeofavMarkerInfoWindowShareClick();
        public void onGeofavMarkerInfoWindowNavClick();
        public void onGeofavMarkerInfoWindowDeleteClick();
    }
}
