package ruoyun.brandeis.edu.mymaps;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import android.database.sqlite.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import static ruoyun.brandeis.edu.mymaps.ProgressGenerator.*;

public class MainActivity extends AppCompatActivity implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,OnCompleteListener

{
    private static final int ERROR_DIALOG_REQUEST = 9001;
    GoogleMap mMap;

    //previous class named LocationApi has been solidated into GoogleApiClient class
    private GoogleApiClient mLocationClient;

    private Marker marker;
    private Marker client_marker;

    //added by
    private MySqliteHelper mySqlite;
    //TextView tv;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (serviceOk()) {
            setContentView(R.layout.activity_map);

            // code for custom toolbar
            android.support.v7.widget.Toolbar myToolbar =
                    (android.support.v7.widget.Toolbar) findViewById(R.id.my_toolbar);
            setSupportActionBar(myToolbar);
            myToolbar.setLogo(R.mipmap.ic_pink_car);
            getSupportActionBar().setDisplayShowTitleEnabled(false);
            //myToolbar.setTitleTextAppearance(this, QText);
            Typeface face= Typeface.createFromAsset(getAssets(), "emmasophia.ttf");
            TextView mTitle = (TextView) myToolbar.findViewById(R.id.toolbar_title);
            mTitle.setTypeface(face);

            final ProgressGenerator progressGenerator = new ProgressGenerator(this);
            final SubmitProcessButton btn1 = (SubmitProcessButton) findViewById(R.id.button1);

            TextView tv = (TextView) findViewById(R.id.editText1);


            final EditText et = (EditText) findViewById(R.id.editText1);
            et.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.showSoftInput(et, InputMethodManager.SHOW_IMPLICIT);
                }
            });

            btn1.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    progressGenerator.start(btn1);
                    btn1.setEnabled(true);
                    et.setEnabled(true);
                    try {
                        geoLocate(v);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            });

            ImageButton imgBtn = (ImageButton)findViewById(R.id.imageButton);
            imgBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    
                }
            });

//            final MenuItem search = (MenuItem)findViewById(R.id.search);
//            search.setOnMenuItemClickListener(new View.OnClickListener(){
//
//            });


            if (initMap()) {
                //mMap.setMyLocationEnabled(true); consumes battery power
                mLocationClient = new GoogleApiClient.Builder(this)
                        .addApi(LocationServices.API)
                        .addConnectionCallbacks(this)
                        .addOnConnectionFailedListener(this)
                        .build();      //create client object

                mLocationClient.connect();

            } else {
                Toast.makeText(this, "Map is not connected!", Toast.LENGTH_LONG).show();
            }
        } else {
            setContentView(R.layout.activity_main);//if service unavailable ,show error screen
        }

        //added by
        mySqlite = new MySqliteHelper(this);
        SQLiteDatabase db = this.openOrCreateDatabase("ParkLocation", MODE_PRIVATE, null);
        mySqlite.onCreate(db);
        //added by, add existed location
        if(mySqlite.countLocations() > 0) {
            ArrayList<String> arr = (ArrayList<String>)mySqlite.queryLocation();
            MainActivity.this.addMarker(null, Double.parseDouble(arr.get(0)), Double.parseDouble(arr.get(1)));
        }

        //click "Find My Vehicle" button,send location data to next activity and store in database
        Button btn2 = (Button) findViewById(R.id.button2);

        btn2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (marker != null) {
                    LatLng latLng = marker.getPosition();
                    Intent my_intent = new Intent(MainActivity.this, FindVehicle.class);
                    my_intent.putExtra("marker_latitude", latLng.latitude);
                    my_intent.putExtra("marker_longtitude", latLng.longitude);
                    startActivity(my_intent);

                    //modified by
                    //mySqlite.addLocation(latLng, 0);

                } else {
                    Toast.makeText(MainActivity.this, "Please long touch the map to place your car",
                            Toast.LENGTH_LONG).show();
                }


            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);

        MenuItem item = menu.findItem(R.id.search);
        item.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {

            @Override
            public boolean onMenuItemClick(MenuItem item) {
                EditText et = (EditText) findViewById(R.id.editText1);
                SubmitProcessButton btn1 = (SubmitProcessButton) findViewById(R.id.button1);
                MainActivity.this.toggleView(btn1);
                MainActivity.this.toggleView(et);

                return true;
            }
        });
        return true;

    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //Add menu handling code
        switch (id) {
            case R.id.mapTypeNone:
                mMap.setMapType(GoogleMap.MAP_TYPE_NONE);
                break;
            case R.id.mapTypeNormal:
                mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
                break;
            case R.id.mapTypeSatellite:
                mMap.setMapType(GoogleMap.MAP_TYPE_SATELLITE);
                break;
            case R.id.mapTypeTerrain:
                mMap.setMapType(GoogleMap.MAP_TYPE_TERRAIN);
                break;
            case R.id.mapTypeHybrid:
                mMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);
                break;
        }


        return super.onOptionsItemSelected(item);
    }


    public boolean serviceOk() {

        int isAvailable = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);

        if (isAvailable == ConnectionResult.SUCCESS) {
            return true;
        } else if (GooglePlayServicesUtil.isUserRecoverableError(isAvailable)) {
            Dialog dialog = GooglePlayServicesUtil.getErrorDialog(isAvailable, this, ERROR_DIALOG_REQUEST);
            dialog.show();

        } else {
            Toast.makeText(this, "Cannot connect to mapping service", Toast.LENGTH_LONG).show();
        }


        return false;

    }

    private boolean initMap() {
        if (mMap == null) {
            //java equivalent to put a fragment tag in xml
            SupportMapFragment mapFragment =
                    (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
            mMap = mapFragment.getMap();

            mMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);


            if (mMap != null) {
                mMap.setOnMapLongClickListener(new GoogleMap.OnMapLongClickListener() {
                    @Override
                    public void onMapLongClick(LatLng latLng) {
                        Geocoder gc = new Geocoder(MainActivity.this);
                        List<Address> list = null;

                        try {
                            list = gc.getFromLocation(latLng.latitude, latLng.longitude, 1);
                        } catch (IOException e) {
                            e.printStackTrace();
                            return;
                        }

                        Address add = list.get(0);

                        MainActivity.this.addMarker(add, latLng.latitude, latLng.longitude);
                        ImageButton imgBtn = (ImageButton)findViewById(R.id.imageButton);
                        imgBtn.setVisibility(imgBtn.VISIBLE);
                        //added by
                        mySqlite.deleteLocations();
                        mySqlite.addLocation(latLng, 0);
                    }
                });

            }
        }

        return (mMap != null);
    }

    private void addMarker(Address add, double latitude, double longitude) {
        MarkerOptions options = new MarkerOptions()

                .position(new LatLng(latitude, longitude))
                .draggable(true)
                .icon(BitmapDescriptorFactory.fromResource(R.mipmap.ic_pink_car));
        if (marker != null) {
            marker.remove();
        }
        marker = mMap.addMarker(options);

    }

    private void gotolocation(double lat, double lng, float zoom) {
        LatLng latLng = new LatLng(lat, lng);
        CameraUpdate update = CameraUpdateFactory.newLatLngZoom(latLng, zoom);
        mMap.moveCamera(update);
    }


    public void showCurrentLocation(MenuItem item) {
        Location currentLocation = LocationServices.FusedLocationApi
                .getLastLocation(mLocationClient);
        if (currentLocation == null) {
            Toast.makeText(this, "Couldn't connect", Toast.LENGTH_SHORT).show();
        } else {
            LatLng latLng = new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude());
            CameraUpdate update = CameraUpdateFactory.newLatLngZoom(latLng, 18);
            mMap.animateCamera(update);

            addFlashMarker(latLng);

        }
    }

    @Override
    public void onConnected(Bundle bundle) {
        Toast.makeText(this, "Ready to map!", Toast.LENGTH_LONG).show();//only see this msg if only connected to Google location services
        startPage();

    }



    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {

    }



    public void geoLocate(View v) throws IOException {

        hideSoftKeyboard(v);

        Toast.makeText(this, "geolocate!!!", Toast.LENGTH_LONG).show();

        TextView tv = (TextView) findViewById(R.id.editText1);

        String searchString = tv.getText().toString();

        Geocoder gc = new Geocoder(this);
        List<Address> list = gc.getFromLocationName(searchString, 1);

        if (list.size() > 0) {
            Address add = list.get(0);
            String locality = add.getLocality();
            Toast.makeText(this, "Found:" + locality, Toast.LENGTH_SHORT).show();

            double lat = add.getLatitude();
            double lng = add.getLongitude();
            gotolocation(lat, lng, 18);

            //add vehicle marker automatically
            if (marker != null) {
                marker.remove();
            }
            MarkerOptions options = new MarkerOptions()
                    .title(locality)
                    .position(new LatLng(lat, lng))
                    .draggable(true)
                    .icon(BitmapDescriptorFactory.fromResource(R.mipmap.ic_pink_car));
            marker = mMap.addMarker(options);

            EditText et = (EditText) findViewById(R.id.editText1);
            SubmitProcessButton btn1 = (SubmitProcessButton) findViewById(R.id.button1);
            et.setVisibility(et.GONE);
            btn1.setVisibility(btn1.GONE);

        }
    }

    private void hideSoftKeyboard(View v) {
        InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
    }


    private void addFlashMarker(LatLng latLng){

        MarkerOptions options = new MarkerOptions()

                .position(latLng)
                .draggable(true)
                .icon(BitmapDescriptorFactory.fromResource(R.mipmap.ic_flash_client));
        if (client_marker != null) {
            client_marker.remove();
        }
        client_marker = mMap.addMarker(options);

        Timer markertimer = new Timer();
        markertimer.scheduleAtFixedRate(new TimerTask() {

            @Override
            public void run() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        blinkMarker();
                    }
                });
            }
        }, 0, 500);
    }

    //make the marker blink
    private boolean blinkMarker(){

        if(client_marker.isVisible()){
            client_marker.setVisible(false);
        }
        else if(!client_marker.isVisible()){
            client_marker.setVisible(true);
        }
        return client_marker.isVisible();
    }

    public void startPage(){
        Location currentLocation = LocationServices.FusedLocationApi
                .getLastLocation(mLocationClient);

        LatLng latLng = new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude());

        addFlashMarker(latLng);

        gotolocation(latLng.latitude, latLng.longitude, 16);

        Toast.makeText(MainActivity.this,"Please long touch the map to place your car",
                Toast.LENGTH_LONG).show();

    }

    //added by
    @Override
    public void onRestart() {
        if(true == StartActivity.terminatorFlag) {
            this.finish();
        }

        super.onRestart();
    }

    @Override
    public void onComplete() throws IOException {
        //geoLocate(tv);
        final EditText et = (EditText) findViewById(R.id.editText1);
        et.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.showSoftInput(et, InputMethodManager.SHOW_IMPLICIT);
            }
        });

    }

    public void toggleView(View view){
        if(view.getVisibility()==View.GONE)
            view.setVisibility(View.VISIBLE);
        else if(view.getVisibility()==View.VISIBLE)
            view.setVisibility(View.GONE);
    }


}
