package it.danieleverducci.nextcloudmaps.views;

import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.infowindow.MarkerInfoWindow;

import it.danieleverducci.nextcloudmaps.R;

public class GeofavMarkerInfoWindow extends MarkerInfoWindow {
    /**
     * @param mapView
     */
    public GeofavMarkerInfoWindow(MapView mapView) {
        super(R.layout.infowindow_geofav, mapView);
    }

    @Override
    public void onOpen(Object item) {
        super.onOpen(item);
    }

    @Override
    public void onClose() {
        super.onClose();
    }
}
