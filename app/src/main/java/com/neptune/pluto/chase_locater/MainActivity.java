package com.neptune.pluto.chase_locater;

import android.app.Activity;
import android.app.Dialog;
import android.os.Bundle;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;


public class MainActivity extends Activity implements OnMapReadyCallback {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        public boolean googleServicesAvailable () {
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

        if(googleServicesAvailable()) {
            setContentView(R.layout.activity_map);
            if(initMap()) {
                Toast.makeText(this, "Perfect - Maps Working", Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(this, "Map not available", Toast.LENGTH_LONG).show();
            }
        } else {
            setContentView(R.layout.activity_main);
        }



        if(googleServicesAvailable()) {
            Toast.makeText(this, "Perfect - Maps Working", Toast.LENGTH_LONG).show();
            setContentView(R.layout.activity_map);
        }

        private boolean initMap(){
            if (mGoogleMap == null) {
                MapFragment mapFrag = (MapFragment) getFragmentManager().findFragmentById(R.id.map);
                mGoogleMap = mapFrag.getMap();
            }
            return (mGoogleMap != null);
        }


    }

    @Override
    public void onMapReady(GoogleMap googleMap) {

    }


//    @Override
//    public boolean onCreateOptionsMenu(Menu menu) {
//        // Inflate the menu; this adds items to the action bar if it is present.
//        getMenuInflater().inflate(R.menu.menu_main, menu);
//        return true;
//    }
//
//    @Override
//    public boolean onOptionsItemSelected(MenuItem item) {
//        // Handle action bar item clicks here. The action bar will
//        // automatically handle clicks on the Home/Up button, so long
//        // as you specify a parent activity in AndroidManifest.xml.
//        int id = item.getItemId();
//
//        //noinspection SimplifiableIfStatement
//        if (id == R.id.action_settings) {
//            return true;
//        }
//
//        return super.onOptionsItemSelected(item);
//    }
}
