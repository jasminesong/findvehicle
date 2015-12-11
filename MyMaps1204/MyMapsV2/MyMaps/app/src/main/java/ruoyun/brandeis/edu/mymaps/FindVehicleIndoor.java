package ruoyun.brandeis.edu.mymaps;

import android.content.Intent;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

public class FindVehicleIndoor extends AppCompatActivity implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener, LocationListener {


    GoogleMap mMap;
    Marker client_marker;
    Marker vehicle_marker;
    private GoogleApiClient mLocationClient;
    LatLng latLng;
    private LocationRequest mLocationRequest;
    private LocationManager mLocationManager;
    int toastID = 0;
    //added by
    String level = "";

    //added by
    private MySqliteHelper mySqlite;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_find_vehicle_indoor);

        Intent my_intent = getIntent();
        latLng = new LatLng(my_intent.getDoubleExtra("marker_latitude", 0.0),
                my_intent.getDoubleExtra("marker_longtitude", 0.0));

        //added by
        level = my_intent.getStringExtra("marker_level") + "  level";
        EditText editText2 =(EditText)findViewById(R.id.editText);
        editText2.setText(level.toCharArray(), 0, level.length());


        if (mMap == null) {
            //java equivalent to put a fragment tag in xml
            SupportMapFragment mapFragment =
                    (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
            mMap = mapFragment.getMap();

            mMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);

            mLocationClient = new GoogleApiClient.Builder(this)
                    .addApi(LocationServices.API)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .build();      //create client object

            mLocationClient.connect();


            //gotolocation(cameraLatLng,18);
            //addClientMarker(latLngClient);

        }

        // Create the LocationRequest object
        mLocationRequest = LocationRequest.create()
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
                .setInterval(1 * 1000)        // 10 seconds, in milliseconds
                .setFastestInterval(1 * 1000); // 1 second, in milliseconds


        mLocationManager = (LocationManager) getSystemService(LOCATION_SERVICE);

        //added by
        mySqlite = new MySqliteHelper(this);
        //click "Done" button, delete data table and quit app
        Button btn3 = (Button) findViewById(R.id.button3);
        btn3.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                mySqlite.deleteLocations();
                StartActivity.terminatorFlag = true;
                FindVehicleIndoor.this.finish();
            }
        });

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_find_vehicle, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void gotolocation(LatLng latLng, float zoom) {
        //LatLng latLng = new LatLng(lat, lng);
        CameraUpdate update = CameraUpdateFactory.newLatLngZoom(latLng, zoom);
        mMap.moveCamera(update);
    }

    @Override
    public void onConnected(Bundle bundle) {
        //Toast.makeText(this, "Ready to find vehicle!", Toast.LENGTH_LONG).show();
        Location currentLocation = LocationServices.FusedLocationApi
                .getLastLocation(mLocationClient);

        LatLng latLngClient = new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude());
        //LatLng latLngClient = new LatLng(0.0,0.0);
        addClientMarker(latLngClient);
        addVehicleMarker(latLng);

        LatLng cameraLatLng = new LatLng(0.5 * (latLng.latitude + latLngClient.latitude),
                0.5 * (latLng.longitude + latLngClient.longitude));
        Toast.makeText(this, "mLocationClient is connected!", Toast.LENGTH_LONG).show();

        //gotolocation(cameraLatLng,18);
        gotolocation(cameraLatLng, 16);
        Toast.makeText(this, "mLocationClient is connected!" + latLngClient + "----" + cameraLatLng, Toast.LENGTH_LONG).show();

        //addVehicleMarker(latLng);





        Location location = LocationServices.FusedLocationApi.getLastLocation(mLocationClient);
        if (location == null) {
            Toast.makeText(this,"Location null!",Toast.LENGTH_LONG).show();

            //LocationServices.FusedLocationApi.requestLocationUpdates(mLocationClient, mLocationRequest, this);
        } else {
            Toast.makeText(this,"Location not null!",Toast.LENGTH_LONG).show();

            // Register the listener with the Location Manager to receive location updates
            LocationServices.FusedLocationApi.requestLocationUpdates(mLocationClient, mLocationRequest, this);

            handleNewLocation(location);
        }
    }

    private void handleNewLocation(Location location) {
        Toast.makeText(this,"handle New!"+ this.toastID++,Toast.LENGTH_LONG).show();


        double currentLatitude = location.getLatitude();
        double currentLongitude = location.getLongitude();
        LatLng latLng = new LatLng(currentLatitude, currentLongitude);

        addClientMarker(latLng);
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {

    }

    private void addVehicleMarker(LatLng latLng) {
        MarkerOptions options = new MarkerOptions()

                .position(latLng)
                .draggable(true)
                .icon(BitmapDescriptorFactory.fromResource(R.mipmap.ic_launcher));
        if (vehicle_marker != null) {
            vehicle_marker.remove();
        }
        vehicle_marker = mMap.addMarker(options);

    }

    private void addClientMarker(LatLng latLng) {
        MarkerOptions options = new MarkerOptions()

                .position(latLng)
                .draggable(true)
                .icon(BitmapDescriptorFactory.fromResource(R.mipmap.ic_client));
        if (client_marker != null) {
            client_marker.remove();
        }
        client_marker = mMap.addMarker(options);
    }

    @Override
    public void onLocationChanged(Location location) {
        Toast.makeText(this,"Location changed!" + this.toastID++,Toast.LENGTH_LONG).show();

        handleNewLocation(location);

    }


}


