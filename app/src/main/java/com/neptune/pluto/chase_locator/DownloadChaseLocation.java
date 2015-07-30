package com.neptune.pluto.chase_locator;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * Created by Pluto on 7/29/2015.
 */

public class DownloadChaseLocation implements Runnable {

    String chaseUrl = "https://m.chase.com/PSRWeb/location/list.action?lat="+lat+"&lng"+lng+;
    ArrayList<Deal> deals;
    MainActivity con;

    public DownloadChaseLocation(Context c) {
        deals = new ArrayList<Deal>();
        this.con = (MainActivity) c;
    }

    @Override
    public void run() {
        URL url;
        try {
            url = new URL(chaseUrl);
            Log.d("JSON", chaseUrl);
            HttpURLConnection urlConnection = (HttpURLConnection) url
                    .openConnection();
            InputStream in = new BufferedInputStream(
                    urlConnection.getInputStream());
            BufferedReader reader = new BufferedReader(
                    new InputStreamReader(in));
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line);
            }
            reader.close();
            String json = sb.toString();
            Log.d("JSON", json);
            try {
                JSONObject jsonObject = new JSONObject(json);
                JSONArray jArray = jsonObject.getJSONArray("deals");

                for (int i = 0; i < jArray.length(); i++) {

                    final int val = (int) (((float) i / (jArray.length() - 1)) * 100);
                    Log.d("VALUE", "Val: " + val);
                    con.runOnUiThread(new Runnable() {

                        @Override
                        public void run() {
                            // TODO Auto-generated method stub
                            con.pd.setProgress(val);
                        }
                    });


                    JSONObject oneObject = jArray.getJSONObject(i);
                    String title = oneObject.getString("announcementTitle");

                    JSONArray optionsArray = oneObject
                            .getJSONArray("options");
                    JSONObject optionsFirstObject = optionsArray
                            .getJSONObject(0);
                    JSONObject priceObject = optionsFirstObject
                            .getJSONObject("price");
                    String price = priceObject.getString("formattedAmount");

                    String imageURL = oneObject.getString("largeImageUrl");

                    URL downloadURL = null;
                    HttpURLConnection conn = null;
                    InputStream inputStream = null;
                    Bitmap bmp = null;

                    try {
                        downloadURL = new URL(imageURL);
                        conn = (HttpURLConnection) downloadURL
                                .openConnection();
                        inputStream = conn.getInputStream();
                        bmp = BitmapFactory.decodeStream(inputStream);
                    } catch (MalformedURLException e) {
                        e.printStackTrace();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

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

                    Deal theDeal = new Deal(title, price, bmp);
                    deals.add(theDeal);

                }
            } catch (JSONException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
                Log.d("JSON", e.toString());

            }

            con.runOnUiThread(new Runnable() {

                @Override
                public void run() {
                    String[] titleArray = new String[deals.size()];
                    int count = 0;
                    for (Deal thedeal : deals) {
                        titleArray[count++] = thedeal.title;
                    }
                    con.drawDeals(deals);
                }

            });
        } catch (MalformedURLException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

}


