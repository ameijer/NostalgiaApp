package com.nostalgia.runnable;

/**
 * Created by alex on 11/7/15.
 */

import android.util.Log;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nostalgia.Constants;
import com.nostalgia.persistence.model.KnownLocation;
import com.nostalgia.persistence.model.LocationUpdate;
import com.nostalgia.persistence.model.LoginResponse;

import org.apache.commons.io.IOUtils;
import org.geojson.GeoJsonObject;
import org.geojson.Point;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;

public class LocationGetterThread extends Thread {
    private final String locationId;

    public KnownLocation getMatching() {
        return matching;
    }

    KnownLocation matching;
    private static final ObjectMapper mapper = new ObjectMapper();

    public LocationGetterThread(String locationId) {
        this.locationId = locationId;

    }

    public void run() {


        try {
        URL httpGet = null;

        httpGet = new URL(Constants.API_URL + ":" + Constants.API_PORT + "/api/v0/location/id?locID=" + locationId);



            HttpURLConnection conn = (HttpURLConnection) httpGet.openConnection();

            // 3. Specify POST method
            conn.setRequestMethod("GET");

            // 4. Set the headers
            conn.setRequestProperty("Content-Type", "application/json");


            // 6. Get the response
            int responseCode = conn.getResponseCode();
            System.out.println("\nSending 'POST' request to URL : " + httpGet);
            System.out.println("Response Code : " + responseCode);

            BufferedReader in = new BufferedReader(
                    new InputStreamReader(conn.getInputStream()));
            String inputLine;
            StringBuffer response = new StringBuffer();

            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }
            in.close();

            // 7. Print result
            System.out.println(response.toString());


                matching = mapper.readValue(response.toString(), KnownLocation.class);



        } catch (Exception e) {
            e.printStackTrace();
        }

        if(matching != null){
            Log.d("prefs", "location found: " + matching.getName());
        } else {
            Log.d("prefs", "location not found: " + locationId);
        }

    }
}



