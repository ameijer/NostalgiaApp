package com.nostalgia.runnable;

/**
 * Created by alex on 11/7/15.
 */

import android.util.Log;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nostalgia.Constants;
import com.nostalgia.persistence.model.KnownLocation;

import org.apache.commons.io.IOUtils;

import org.json.JSONArray;
import org.json.JSONException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

public class DiscreteLocationBboxThread extends Thread {
    private final JSONArray bboxArr;

    public List<KnownLocation> getMatching() {
        return matching;
    }

    List<KnownLocation> matching;
    private static final ObjectMapper mapper = new ObjectMapper();

    public DiscreteLocationBboxThread(double[] bboxToQuery) throws JSONException {

        bboxArr = new JSONArray();

        for(double dim : bboxToQuery){
            bboxArr.put(dim);
        }

    }

    public void run() {

    try {
        URL httpGet = null;

        httpGet = new URL(Constants.API_URL + ":" + Constants.API_PORT + "/api/v0/location/discrete/bbox?bbox=" + bboxArr.toString());

// 2. Open connection
        HttpURLConnection conn = (HttpURLConnection) httpGet.openConnection();

        // 3. Specify POST method
        conn.setRequestMethod("GET");

        // 4. Set the headers
        conn.setRequestProperty("Content-Type", "application/json");


        // 6. Get the response
        int responseCode = conn.getResponseCode();
        System.out.println("\nSending 'GET' request to URL : " + httpGet);
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


            matching = mapper.readValue(response.toString(), new TypeReference<List<KnownLocation>>() {
            });


    }catch (Exception e) {
            e.printStackTrace();
        }

        if(matching != null){
            Log.d("prefs", "locations found: " + matching);
        } else {
            Log.d("prefs", "location not found for bbox: " + this.bboxArr.toString());
        }

    }
}



