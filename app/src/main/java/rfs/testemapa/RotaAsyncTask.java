package rfs.testemapa;

import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.Color;
import android.location.Location;
import android.os.AsyncTask;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;

import org.w3c.dom.Document;

import java.util.List;
import java.util.Locale;

public class RotaAsyncTask extends AsyncTask<Double, Void, String> {

    private ProgressDialog dialog;
    private GoogleMap mapView;
    private Context context;
    private Route rota;
    private String distancia = "";
    private String distanciav = "";
    public AsyncResponse delegate = null;
    private Location locAnt;
    private LatLng l1, l2;

    public RotaAsyncTask(Context ctx, GoogleMap mapa, Location locant) {
        mapView = mapa;
        context = ctx;
        locAnt = locant;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        //dialog = ProgressDialog.show(context, "Aguarde", "Calculando rota");
    }

    @Override
    protected String doInBackground(Double... params) {
        if (((Mapa) context).locAnt != null) {
            System.out.println("================");
            System.out.println(((Mapa) context).locAnt.getLatitude() + ":" + ((Mapa) context).locAnt.getLongitude());
        }
        l1 = new LatLng(params[0], params[1]);
        l2 = new LatLng(params[2], params[3]);
        rota = directions(
                l1,
                l2);
        if (!distancia.isEmpty()) {
            distancia = "Distância: " + distancia + "m";
            return distancia;
        }
        return null;
    }

    @Override
    protected void onPostExecute(String result) {
        super.onPostExecute(result);
        PolylineOptions options = new PolylineOptions()
                .width(5)
                .color(Color.RED)
                .visible(true);

        for (LatLng latlng : rota.getPoints()) {
            options.add(latlng);
        }
        delegate.processFinish(result);
        Boolean continuar = false;
        if (locAnt == null) continuar = true;
        if (!distanciav.isEmpty() && Integer.parseInt(distanciav) > 100) continuar = true;
        if (continuar) {
            mapView.clear();
            mapView.addPolyline(options);
            mapView.addMarker(new MarkerOptions().position(l1).title("Origem"));
            mapView.addMarker(new MarkerOptions().position(l2).title("Destino " + distancia));
        }
        //dialog.dismiss();
    }

    private Route directions(
            final LatLng start, final LatLng dest) {
        GoogleParser parser;
        if (locAnt != null) {
            String urlRotav = String.format(Locale.US,
                    "http://maps.googleapis.com/maps/api/" +
                            "distancematrix/json?origins=%f,%f&" +
                            "destinations=%f,%f&" +
                            "sensor=true&mode=walking",
                    start.latitude,
                    start.longitude,
                    locAnt.getLatitude(),
                    locAnt.getLongitude());
            parser = new GoogleParser(urlRotav);
            distanciav = parser.distancia();
        }
        String urlRotad = String.format(Locale.US,
                "http://maps.googleapis.com/maps/api/" +
                        "distancematrix/json?origins=%f,%f&" +
                        "destinations=%f,%f&" +
                        "sensor=true&mode=walking",
                start.latitude,
                start.longitude,
                dest.latitude,
                dest.longitude);
        parser = new GoogleParser(urlRotad);
        distancia = parser.distancia();
        // Formatando a URL com a latitude e longitude
        // de origem e destino.
        String urlRota = String.format(Locale.US,
                "http://maps.googleapis.com/maps/api/" +
                        "directions/json?origin=%f,%f&" +
                        "destination=%f,%f&" +
                        "sensor=true&mode=walking",
                start.latitude,
                start.longitude,
                dest.latitude,
                dest.longitude);

        parser = new GoogleParser(urlRota);
        return parser.parse();
    }
}
