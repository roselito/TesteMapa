package rfs.testemapa;

import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

public class Mapa extends FragmentActivity implements LocationListener {
    // The minimum distance to change updates in metters
    private static final long MIN_DISTANCE_CHANGE_FOR_UPDATES = 10; // 10 metters
    // The minimum time beetwen updates in milliseconds
    private static final long MIN_TIME_BW_UPDATES = 1000 * 60 * 1; // 1 minute
    Location loc = null;
    LocationManager locationManager = null;
    String provider = "";

    private GoogleMap mMap; // Might be null if Google Play services APK is not available.

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mapa);
        setUpMapIfNeeded();
    }

    @Override
    protected void onResume() {
        super.onResume();
        setUpMapIfNeeded();
    }

    /**
     * Sets up the map if it is possible to do so (i.e., the Google Play services APK is correctly
     * installed) and the map has not already been instantiated.. This will ensure that we only ever
     * call {@link #setUpMap()} once when {@link #mMap} is not null.
     * <p/>
     * If it isn't installed {@link SupportMapFragment} (and
     * {@link com.google.android.gms.maps.MapView MapView}) will show a prompt for the user to
     * install/update the Google Play services APK on their device.
     * <p/>
     * A user can return to this FragmentActivity after following the prompt and correctly
     * installing/updating/enabling the Google Play services. Since the FragmentActivity may not
     * have been completely destroyed during this process (it is likely that it would only be
     * stopped or paused), {@link #onCreate(Bundle)} may not be called again so we should call this
     * method in {@link #onResume()} to guarantee that it will be called.
     */
    private void setUpMapIfNeeded() {
        // Do a null check to confirm that we have not already instantiated the map.
        if (mMap == null) {
            // Try to obtain the map from the SupportMapFragment.
            mMap = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map))
                    .getMap();
            // Check if we were successful in obtaining the map.
            if (mMap != null) {
                setUpMap();
            }
        }
    }

    /**
     * This is where we can add markers or lines, add listeners or move the camera. In this case, we
     * just add a marker near Africa.
     * <p/>
     * This should only be called once and when we are sure that {@link #mMap} is not null.
     */
    private void setUpMap() {
        mMap.setMapType(GoogleMap.MAP_TYPE_TERRAIN);
        mMap.setMyLocationEnabled(true);
        mMap.getUiSettings().setZoomControlsEnabled(true);
        mMap.getUiSettings().setMapToolbarEnabled(true);
        Boolean gps = false, rede = false;
        loc = null;
        locationManager = (LocationManager) getApplicationContext().getSystemService(LOCATION_SERVICE);
        gps = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        rede = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
        provider = "";
        if (gps) {
            provider = LocationManager.GPS_PROVIDER;
        } else {
            if (rede) {
                provider = LocationManager.NETWORK_PROVIDER;
            }
        }
        if (!provider.equals("")) {
//            locationManager.requestLocationUpdates(
//                    provider, MIN_TIME_BW_UPDATES,
//                    MIN_DISTANCE_CHANGE_FOR_UPDATES, this);
            locationManager.requestSingleUpdate(
                    provider, this, null);
        }
        if (locationManager != null) {
            if (!provider.equals("")) {
                System.out.println("LocationManager:" + locationManager);
                loc = locationManager
                        .getLastKnownLocation(provider);
            }
        }
        String mensagem = "Local:";
        if (loc != null) {
            mensagem += "\nloc: " + loc.getLatitude() + "," + loc.getLongitude();
            Geocoder gco = new Geocoder(getBaseContext(), Locale.getDefault());
            List<Address> addresses;
            try {
                addresses = gco.getFromLocation(loc.getLatitude(),
                        loc.getLongitude(), 1);
                if (addresses.size() > 0) {
                    System.out.println("##########ORIGEM########################");
                    mensagem += "\nCidade:" + addresses.get(0).getLocality();
                    mensagem += "\nEndereco:" + addresses.get(0).getThoroughfare() + ", " + addresses.get(0).getFeatureName();
                    System.out.println(mensagem);
                    System.out.println("##################################");
                }
            } catch (IOException e) {
                System.out.println("erro!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
                e.printStackTrace();
            }
//            LatLng l1 = new LatLng(-22.0124113,-47.8943344);
            LatLng l1 = new LatLng(loc.getLatitude(), loc.getLongitude());
            mMap.addMarker(new MarkerOptions().position(l1).title("Origem"));
            LatLng l2 = new LatLng(-22.00507971,-47.88904883);
            mMap.addMarker(new MarkerOptions().position(l2).title("Destino"));
            new RotaAsyncTask(this, mMap).execute(l1.latitude, l1.longitude, l2.latitude, l2.longitude);
            CameraPosition cameraPosition = new CameraPosition.Builder()
                    .target(l1)      // Sets the center of the map to l1
                    .zoom(17)                   // Sets the zoom
                    .bearing(0)                // Sets the orientation of the camera to north(0)
                    .tilt(30)                   // Sets the tilt of the camera to 30 degrees
                    .build();                   // Creates a CameraPosition from the builder
            mMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
        }
    }

    public void onStatusChanged(String provider, int status, Bundle extras) {
    }

    public void onProviderDisabled(String provider) {
    }

    public void onProviderEnabled(String provider) {
    }

    public void onLocationChanged(Location location) {
        setUpMapIfNeeded();
    }
}
