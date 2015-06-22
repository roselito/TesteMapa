package rfs.testemapa;

import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

public class Mapa extends FragmentActivity implements LocationListener, AsyncResponse {
    private static final long MIN_DISTANCE_CHANGE_FOR_UPDATES = 10; // 10 metters
    private static final long MIN_TIME_BW_UPDATES = 1000 * 20 * 1; // 1 minute
    private Location loc = null;
    private Location locAnt = null;
    private LatLng l1, l2;
    private LocationManager locationManager = null;
    private String distancia = "";

    private GoogleMap mMap; // Might be null if Google Play services APK is not available.

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mapa);
        if (loc == null) {
            loc = new Location("Inicial");
            loc.setLatitude(-22d);
            loc.setLongitude(-47.89);
        }
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
        }
        setUpMap();
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
        mMap.getUiSettings().setCompassEnabled(true);
        mMap.getUiSettings().setZoomControlsEnabled(true);
        mMap.getUiSettings().setMapToolbarEnabled(true);
        Boolean gps = false, rede = false;
        loc = null;
        locationManager = (LocationManager) getApplicationContext().getSystemService(LOCATION_SERVICE);
        if (locationManager != null) {
            if (!obterLocalizacao(LocationManager.NETWORK_PROVIDER)) {
                if (!obterLocalizacao(LocationManager.GPS_PROVIDER)) {
                    Toast.makeText(getApplicationContext(), "Não foi possível determinar sua localização atual. Verifique sua rede de dados ou a conexão com o sistema de GPS.", Toast.LENGTH_LONG).show();
                }
            }
        }
        String mensagem = "Local:";
        if (loc == null) {
            loc = new Location("Teste");
            loc.setLatitude(-22.0124113);
            loc.setLongitude(-47.8943344);
        }
        mensagem += "\nloc: " + loc.getLatitude() + "," + loc.getLongitude();
        Geocoder gco = new Geocoder(getBaseContext(), Locale.getDefault());
        List<Address> addresses;
        try {
            addresses = gco.getFromLocation(loc.getLatitude(),
                    loc.getLongitude(), 1);
            if (addresses.size() > 0) {
                mensagem += "\nCidade:" + addresses.get(0).getLocality();
                mensagem += "\nEndereco:" + addresses.get(0).getThoroughfare() + ", " + addresses.get(0).getFeatureName();
            }
        } catch (IOException e) {
            System.out.println("erro!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
            e.printStackTrace();
        }
        l1 = new LatLng(loc.getLatitude(), loc.getLongitude());
        l2 = new LatLng(-22.0971, -47.889);
        RotaAsyncTask rat = new RotaAsyncTask(this, mMap, locAnt);
        rat.delegate = this;
        rat.execute(l1.latitude, l1.longitude, l2.latitude, l2.longitude);
    }

    private boolean obterLocalizacao(String tipoAcesso) {
        locationManager = (LocationManager) getApplicationContext().getSystemService(LOCATION_SERVICE);
        Boolean acesso = locationManager.isProviderEnabled(tipoAcesso);
        if (!acesso) {
            if (tipoAcesso.equals(LocationManager.NETWORK_PROVIDER)) {
                Toast.makeText(getApplicationContext(), "Sem acesso à rede de dados ou wi-fi", Toast.LENGTH_LONG).show();
            }
            if (tipoAcesso.equals(LocationManager.GPS_PROVIDER)) {
                Toast.makeText(getApplicationContext(), "Sem acesso à rede GPS", Toast.LENGTH_LONG).show();
            }
            return false;
        } else {
            locationManager.requestLocationUpdates(
                    tipoAcesso, 20000, 300, this);
            Location l = locationManager
                    .getLastKnownLocation(tipoAcesso);
            if (l != null) {
                if (loc != null) {
                    Toast.makeText(getApplicationContext(), "loc", Toast.LENGTH_LONG).show();
                    locAnt.setLatitude(loc.getLatitude());
                    locAnt.setLongitude(loc.getLongitude());
                }
                loc = l;
                return true;
            } else {
                return false;
            }
        }
    }

    public void onStatusChanged(String provider, int status, Bundle extras) {
    }

    public void onProviderDisabled(String provider) {
    }

    public void onProviderEnabled(String provider) {
        setUpMap();
    }

    public void onLocationChanged(Location location) {
        //mMap.clear();
        setUpMap();
    }

    @Override
    public void processFinish(String saida) {
        distancia = saida;
    }
}
