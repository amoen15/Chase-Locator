package com.neptune.pluto.chase_locator;

import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polygon;
import com.google.android.gms.maps.model.PolygonOptions;
import com.google.android.gms.maps.model.Polyline;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends Activity implements
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener, LocationListener {

    static final int POLYGON_POINTS = 5;
    GoogleMap mGoogleMap;
    GoogleApiClient mGoogleApiClient;
    EditText et;
    Marker marker;
    Marker marker1;
    Marker marker2;
    Polyline line;
    ArrayList<Marker> markers = new ArrayList<Marker>();
    Polygon polygon;
    Circle shape;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (googleServicesAvailable()) {
            setContentView(R.layout.activity_main);
            et = (EditText) findViewById(R.id.editText1);

            et.setOnKeyListener(new View.OnKeyListener() {

                @Override
                public boolean onKey(View v, int keyCode, KeyEvent event) {
                    // TODO Auto-generated method stub
                    if ((event.getAction() == KeyEvent.ACTION_DOWN) &&
                            (keyCode == KeyEvent.KEYCODE_ENTER)) {
                        try {
                            MainActivity.this.takeMeThere(et);
                        } catch (IOException e) {
                            // TODO Auto-generated catch block
                            e.printStackTrace();
                        }
                        return true;
                    }
                    return false;
                }
            });

            if (initMap()) {
                Toast.makeText(this, "Perfect - Maps Working", Toast.LENGTH_LONG).show();
                //				goToLocation(38.883308, -77.015949, 13);
                mGoogleMap.setMyLocationEnabled(true);

                mGoogleApiClient = new GoogleApiClient.Builder(this)
                        .addApi(LocationServices.API)
                        .addConnectionCallbacks(this)
                        .addOnConnectionFailedListener(this)
                        .build();
                mGoogleApiClient.connect();
            } else {
                Toast.makeText(this, "Map not available", Toast.LENGTH_LONG).show();
            }
        } else {
            //some other layout
        }
    }

    private void goToLocation(double lat, double lng, float zoom) {
        LatLng ll = new LatLng(lat, lng);
        CameraUpdate update = CameraUpdateFactory.newLatLngZoom(ll, zoom);
        mGoogleMap.moveCamera(update);

    }

//	private void drawLine() {
//		PolylineOptions options = new PolylineOptions()
//			.add(marker1.getPosition())
//			.add(marker2.getPosition())
//			.color(Color.BLUE)
//			.width(2);
//
//		line = mGoogleMap.addPolyline(options);
//	}
//
//
//	private void removeEverything() {
//		// TODO Auto-generated method stub
//		marker1.remove();
//		marker1 = null;
//		marker2.remove();
//		marker2 = null;
//		line.remove();
//	}

    public void takeMeThere(View v) throws IOException {
        String location = et.getText().toString();

        Geocoder gc = new Geocoder(this);
        List<Address> list = gc.getFromLocationName(location, 1);
        Address add = list.get(0);
        String locality = add.getLocality();
        String postalCode = add.getPostalCode();

        Toast.makeText(this, locality, Toast.LENGTH_LONG).show();

        double lat = add.getLatitude();
        double lng = add.getLongitude();
        goToLocation(lat, lng, 15);

        setMarker(locality, postalCode, lat, lng);
    }

    private void drawPolygon() {
        PolygonOptions options = new PolygonOptions()
                .fillColor(0x330000FF)
                .strokeWidth(3)
                .strokeColor(Color.BLUE);

        for (int i = 0; i < POLYGON_POINTS; i++) {
            options.add(markers.get(i).getPosition());
        }
        polygon = mGoogleMap.addPolygon(options);

    }

    private void removeEverything() {
        for (Marker marker : markers) {
            marker.remove();
        }
        markers.clear();
        polygon.remove();
        polygon = null;

    }

    private void setMarker(String locality, String postalCode, double lat,
                           double lng) {

        if (markers.size() == POLYGON_POINTS) {
            removeEverything();
        }

        MarkerOptions options = new MarkerOptions()
                .title(locality)
                .snippet(postalCode)
                .icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_cast_light))
                .draggable(true)
                .position(new LatLng(lat, lng));

        markers.add(mGoogleMap.addMarker(options));

        if (markers.size() == POLYGON_POINTS) {
            drawPolygon();
        }

//		if(marker1 == null) {
//			marker1 = mGoogleMap.addMarker(options);
//		}
//		else if (marker2 == null) {
//			marker2 = mGoogleMap.addMarker(options);
//			drawLine();
//		} else {
//			removeEverything();
//			marker1 = mGoogleMap.addMarker(options);
//		}


//		if(marker != null){
//			marker.remove();
//			shape.remove();
//		}

//		marker = mGoogleMap.addMarker(options);
//
//		shape = drawCircle(new LatLng(lat, lng));


    }

    private Circle drawCircle(LatLng ll) {
        CircleOptions options = new CircleOptions()
                .center(ll)
                .radius(1000)
                .fillColor(0x330000FF)
                .strokeColor(Color.BLUE)
                .strokeWidth(3);


        return mGoogleMap.addCircle(options);
    }


    private boolean initMap() {
        if (mGoogleMap == null) {
            MapFragment mapFrag = (MapFragment) getFragmentManager().findFragmentById(R.id.map);
            mGoogleMap = mapFrag.getMap();

            if (mGoogleMap != null) {
                mGoogleMap.setInfoWindowAdapter(new GoogleMap.InfoWindowAdapter() {

                    @Override
                    public View getInfoWindow(Marker arg0) {
                        return null;
                    }

                    @Override
                    public View getInfoContents(Marker marker) {
                        View v = getLayoutInflater().inflate(R.layout.info_window, null);
                        TextView tvLocality = (TextView) v.findViewById(R.id.tv_locality);
                        TextView tvLat = (TextView) v.findViewById(R.id.tv_lat);
                        TextView tvLng = (TextView) v.findViewById(R.id.tv_lng);
                        TextView tvSnippet = (TextView) v.findViewById(R.id.tv_snippet);

                        LatLng ll = marker.getPosition();
                        tvLocality.setText(marker.getTitle());
                        tvLat.setText("Latitude: " + ll.latitude);
                        tvLng.setText("Longitude: " + ll.longitude);
                        tvSnippet.setText(marker.getSnippet());

                        return v;
                    }
                });

                mGoogleMap.setOnMapLongClickListener(new GoogleMap.OnMapLongClickListener() {

                    @Override
                    public void onMapLongClick(LatLng ll) {
                        // TODO Auto-generated method stub
                        Geocoder gc = new Geocoder(MainActivity.this);
                        List<Address> list = null;

                        try {
                            list = gc.getFromLocation(ll.latitude, ll.longitude, 1);
                        } catch (IOException e) {
                            // TODO Auto-generated catch block
                            e.printStackTrace();
                            return;
                        }

                        Address add = list.get(0);
                        MainActivity.this.setMarker(add.getLocality(), add.getPostalCode(), ll.latitude, ll.longitude);
                    }
                });

                mGoogleMap.setOnMarkerDragListener(new GoogleMap.OnMarkerDragListener() {

                    @Override
                    public void onMarkerDragStart(Marker marker) {
                        // TODO Auto-generated method stub
                        marker.hideInfoWindow();
                    }

                    @Override
                    public void onMarkerDragEnd(Marker marker) {
                        Geocoder gc = new Geocoder(MainActivity.this);
                        List<Address> list = null;

                        LatLng ll = marker.getPosition();
                        try {
                            list = gc.getFromLocation(ll.latitude, ll.longitude, 1);
                        } catch (IOException e) {
                            // TODO Auto-generated catch block
                            e.printStackTrace();
                            return;
                        }

                        Address add = list.get(0);
                        marker.setTitle(add.getLocality());
                        marker.setSnippet(add.getCountryName());
                        marker.showInfoWindow();
                    }

                    @Override
                    public void onMarkerDrag(Marker arg0) {
                        // TODO Auto-generated method stub

                    }
                });
            }
        }
        return (mGoogleMap != null);
    }

    public boolean googleServicesAvailable() {
        int isAvailable = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);
        if (isAvailable == ConnectionResult.SUCCESS) {
            return true;
        } else if (GooglePlayServicesUtil.isUserRecoverableError(isAvailable)) {
            Dialog dialog = GooglePlayServicesUtil.getErrorDialog(isAvailable, this, 0);
            dialog.show();
        } else {
            Toast.makeText(this, "Cant connect to play services", Toast.LENGTH_LONG).show();
        }
        return false;
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.mapTypeNone:
                mGoogleMap.setMapType(GoogleMap.MAP_TYPE_NONE);
                break;
            case R.id.mapTypeNormal:
                mGoogleMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
                break;
            case R.id.mapTypeSatellite:
                mGoogleMap.setMapType(GoogleMap.MAP_TYPE_SATELLITE);
                break;
            case R.id.mapTypeTerrain:
                mGoogleMap.setMapType(GoogleMap.MAP_TYPE_TERRAIN);
                break;
            case R.id.mapTypeHybrid:
                mGoogleMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);
                break;

            default:
                break;
        }

        return super.onOptionsItemSelected(item);

    }

    @Override
    public void onConnectionFailed(ConnectionResult result) {
        // TODO Auto-generated method stub

    }

    @Override
    public void onConnected(Bundle connectionHint) {
        // TODO Auto-generated method stub

        //		Toast.makeText(this, "Connected", Toast.LENGTH_LONG).show();
        //
        //		LocationRequest mLocationRequest = LocationRequest.create();
        //		mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        //      	mLocationRequest.setInterval(1000); // Update location every second
        //		LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);


    }

    @Override
    public void onConnectionSuspended(int cause) {
        // TODO Auto-generated method stub

    }


    @Override
    public void onLocationChanged(Location location) {
        // TODO Auto-generated method stub
        if (location == null) {
            Toast.makeText(this, "Cant get Current location", Toast.LENGTH_LONG).show();
        } else {
            LatLng ll = new LatLng(location.getLatitude(), location.getLongitude());
            CameraUpdate update = CameraUpdateFactory.newLatLngZoom(ll, 15);
            mGoogleMap.animateCamera(update);
        }
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {

    }


    @Override
    public void onLocationInput() {

        setContentView(R.layout.activity_main);

        ListView chaseLocation = (ListView) findViewById(R.id.listView1);
        cl = new ProgressDialog(this);
        cl.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        cl.setMessage("Loading - Please Wait");
        cl.setIndeterminate(false);
        cl.setCancelable(false);
        cl.show();

        Thread myThread = new Thread(new DownloadChaseLocation(this));
        myThread.start();


    }

    public void drawDeals(final ArrayList<Deal> deals){
        MyAdapter adapter = new MyAdapter(this, deals);
        grouponDealsList.setAdapter(adapter);
        pd.dismiss();

        grouponDealsList.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {
                Intent intent = new Intent(MainActivity.this, DetailActivity.class);
                Deal deal = deals.get(position);
                intent.putExtra("dealItem", deal);
                startActivity(intent);

            }
        });


    }



}
