package com.neptune.pluto.chase_locator;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.Dialog;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

public class MainActivity extends Activity implements
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener {

    GoogleMap mGoogleMap;
    GoogleApiClient mGoogleApiClient;
    Location mLastLocation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (googleServicesAvailable()) {
            setContentView(R.layout.activity_main);
            if (initMap()) {
                mGoogleMap.setMyLocationEnabled(true);
                mGoogleApiClient = new GoogleApiClient.Builder(this)
                        .addApi(LocationServices.API)
                        .addConnectionCallbacks(this)
                        .addOnConnectionFailedListener(this).build();
                mGoogleApiClient.connect();
            } else {
                Toast.makeText(this, "Map not available", Toast.LENGTH_LONG)
                        .show();
            }
        } else {
            setContentView(R.layout.google_play_services_unavailable);
        }
    }

    private boolean initMap() {
        if (mGoogleMap == null) {
            mGoogleMap = ((MapFragment) getFragmentManager().findFragmentById(
                    R.id.map)).getMap();
        }
        return (mGoogleMap != null);
    }

    public boolean googleServicesAvailable() {
        int isAvailable = GooglePlayServicesUtil
                .isGooglePlayServicesAvailable(this);
        if (isAvailable == ConnectionResult.SUCCESS) {
            return true;
        } else if (GooglePlayServicesUtil.isUserRecoverableError(isAvailable)) {
            Dialog dialog = GooglePlayServicesUtil.getErrorDialog(isAvailable,
                    this, 0);
            dialog.show();
        } else {
            Toast.makeText(this, "Cant connect to play services",
                    Toast.LENGTH_LONG).show();
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
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
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

        mLastLocation = LocationServices.FusedLocationApi.getLastLocation(
                mGoogleApiClient);
        if (mLastLocation != null) {
            Log.d("MainActivity", "Calling Service");
            LatLng ll = new LatLng(mLastLocation.getLatitude(), mLastLocation.getLongitude());
            ChaseApiRequestTask task = new ChaseApiRequestTask();
            task.execute(ll);
            CameraUpdate update = CameraUpdateFactory.newLatLngZoom(ll, 15);
            mGoogleMap.moveCamera(update);

        }


    }

    @Override
    public void onConnectionSuspended(int cause) {
        // TODO Auto-generated method stub

    }



    class ChaseApiRequestTask extends AsyncTask<LatLng, Void, Void>{

        String chaseURL = "https://m.chase.com/PSRWeb/location/list.action?";
        ArrayList<ChaseLocation> locationsList;

        @Override
        protected Void doInBackground(LatLng... params) {
            // TODO Auto-generated method stub
            LatLng userLocation = params[0];
            String theUrlString = chaseURL+"lat="+userLocation.latitude+"&lng="+userLocation.longitude;
            locationsList = new ArrayList<MainActivity.ChaseLocation>();

            Log.d("MainActivity", "Downloading Data:::" + theUrlString);
            URL downloadURL;
            HttpURLConnection conn = null;
            InputStream inputStream = null;
            try {
                downloadURL = new URL(theUrlString);
                conn = (HttpURLConnection) downloadURL.openConnection();
                inputStream = conn.getInputStream();
                BufferedReader br = new BufferedReader(new InputStreamReader(inputStream));
                StringBuilder sb = new StringBuilder();
                String line;
                while ((line = br.readLine()) != null) {
                    sb.append(line);
                }
                br.close();

                String json = sb.toString();
                JSONObject jsonObject = new JSONObject(json);
                JSONArray jArray = jsonObject.getJSONArray("locations");
                for (int i = 0; i < jArray.length(); i++) {
                    JSONObject bankObject = jArray.getJSONObject(i);
                    String locationType = bankObject.getString("locType");
                    double distance = bankObject.getDouble("distance");
                    String name = bankObject.getString("name");
                    String address = bankObject.getString("address");
                    String lat = bankObject.getString("lat");
                    String lng = bankObject.getString("lng");

                    ChaseLocation location = new ChaseLocation(locationType, distance, name, address, lat, lng);
                    locationsList.add(location);

                }
            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (JSONException e) {
                e.printStackTrace();
            } finally {
                if (conn != null) {
                    conn.disconnect();
                }
                if (inputStream != null) {
                    try {
                        inputStream.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            // TODO Auto-generated method stub
            super.onPostExecute(result);
            Log.d("MainActivity", "Drawing Markers");
            drawMarkersFor(locationsList);
        }


    }

    public void drawMarkersFor(ArrayList<ChaseLocation> location){
        Log.d("MainActivity", "Locations"+location.size());
        LatLng ll;
        for(int i=0;i <location.size();i++){

            ChaseLocation loc = location.get(i);
            ll = new LatLng(Double.parseDouble(loc.lat), Double.parseDouble(loc.lng));

            MarkerOptions options = new MarkerOptions()
                    .title(loc.name)
                    .snippet(loc.address)
                    .position(ll);
            mGoogleMap.addMarker(options);
        }

    }


    class ChaseLocation {
        String locationType;
        double distance;
        String name, address;
        String lat, lng;

        ChaseLocation(String locType, double dist, String name, String address, String lat,
                      String lng){
            this.locationType = locType;
            this.distance = dist;
            this.name = name;
            this.address = address;
            this.lat = lat;
            this.lng = lng;
        }
    }
}