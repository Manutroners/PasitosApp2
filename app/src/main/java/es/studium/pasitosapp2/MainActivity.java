package es.studium.pasitosapp2;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;

import android.Manifest;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.sqlite.SQLiteDatabase;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.BatteryManager;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;


public class MainActivity extends FragmentActivity implements OnMapReadyCallback, LocationListener {
    private GoogleMap mapa;
    Handler handler = new Handler();
    protected LocationManager locationManager;
    protected LocationListener locationListener;
    double longi;
    double lati;
    int batteryHealth;
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Obtenemos el mapa de forma asíncrona (notificará cuando esté listo)
        SupportMapFragment mapFragment = (SupportMapFragment)
                getSupportFragmentManager().findFragmentById(R.id.mapa);
        assert mapFragment != null;
        mapFragment.getMapAsync(this);
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, this);

        ejecutarTarea();

    }
    private void bateria() {
        BatteryManager batteryManager = (BatteryManager) getSystemService(BATTERY_SERVICE);
         batteryHealth = batteryManager.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY);

    }

    private final int TIEMPO = 10000;
    public void ejecutarTarea() {
        handler.postDelayed(new Runnable() {
            public void run() {

                if (ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    // TODO: Consider calling
                    //    ActivityCompat#requestPermissions
                    // here to request the missing permissions, and then overriding
                    //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                    //                                          int[] grantResults)
                    // to handle the case where the user grants the permission. See the documentation
                    // for ActivityCompat#requestPermissions for more details.
                    return;
                }
                Location lastLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                double longitude = lastLocation.getLongitude();
                double latitude = lastLocation.getLatitude();
                final LatLng ubicacion = new LatLng(latitude,longitude);
                longi = longitude;
                lati = latitude;
                mapa.moveCamera(CameraUpdateFactory.newLatLngZoom(ubicacion, 15));
                mapa.addMarker(new MarkerOptions()
                        .position(ubicacion)
                        .icon(BitmapDescriptorFactory
                       .fromResource(R.drawable.pasadordeubicacion))
                        .anchor(0.5f, 0.5f));
                bateria();
                baseDatos();

                handler.postDelayed(this, TIEMPO);
            }

        }, TIEMPO);
    }
    public void onLocationChanged(Location location) {
        double longitude = location.getLongitude();
        double latitude = location.getLatitude();
        Log.d("longitude",longitude+"");
        Log.d("latitude",latitude+"");
    }
    public void onProviderDisabled(String provider) {
    }
    public void onProviderEnabled(String provider) {
    }
    public void onStatusChanged(String provider, int status, Bundle extras) {
    }
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mapa = googleMap;
        mapa.setMapType(GoogleMap.MAP_TYPE_SATELLITE);
        mapa.getUiSettings().setZoomControlsEnabled(false);
        //mapa.moveCamera(CameraUpdateFactory.newLatLngZoom(grupoStudium, 15));
    }
    public void baseDatos()
    {
        MyDatabaseHelper dbHelper = new MyDatabaseHelper(this);
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put("bateria", batteryHealth);
        values.put("posLon", longi);
        values.put("posLat", lati);
        long newRowId = db.insert("datos", null, values);

    }
}