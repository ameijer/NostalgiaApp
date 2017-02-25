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
import com.nostalgia.persistence.model.LocationUpdate;
import com.nostalgia.persistence.model.User;

import org.geojson.GeoJsonObject;
import org.geojson.Point;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;

public class UserLocationUpdaterThread extends Thread {
    private GeoJsonObject location;
    private String userId;
    final LocationUpdate cmd;

    public UserLocationUpdaterThread(String userId, Point location) {
        this.userId = userId;
        this.location = location;
        cmd = new LocationUpdate(location, userId);
    }

    public void run() {

        ObjectMapper mapper = new ObjectMapper();
        try {
            System.out.println(mapper.writeValueAsString(cmd));
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        URL httpPost = null;

        try {
        httpPost = new URL(Constants.API_URL + ":" + Constants.API_PORT + "/api/v0/user/location/update?userId=" + userId);

            // 2. Open connection
            HttpURLConnection conn = (HttpURLConnection) httpPost.openConnection();

            // 3. Specify POST method
            conn.setRequestMethod("POST");

            // 4. Set the headers
            conn.setRequestProperty("Content-Type", "application/json; charset=UTF-8");


            conn.setDoOutput(true);

            // 5. Add JSON data into POST request body
            String json = mapper.writeValueAsString(cmd);
            byte[] outputBytes = json.getBytes("UTF-8");
            OutputStreamWriter os = new OutputStreamWriter(conn.getOutputStream());
            os.write(json);

            // 5.2 Get connection output stream

            // 5.3 Copy Content "JSON" into

            Log.i("LOCUPDATE", "sending string: " + json);
            os.flush();


            // 6. Get the response
            int responseCode = conn.getResponseCode();
            System.out.println("\nSending 'POST' request to URL : " + httpPost);
            System.out.println("Response Code : " + responseCode);

            BufferedReader in = new BufferedReader(
                    new InputStreamReader(conn.getInputStream()));
            String inputLine;
            StringBuffer response = new StringBuffer();

            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }
            in.close();
            os.close();
            // 7. Print result
            System.out.println(response.toString());



        } catch (Exception e1) {
            e1.printStackTrace();
        }

    }
}



