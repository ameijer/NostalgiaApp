package com.nostalgia.runnable;

/**
 * Created by alex on 11/7/15.
 */
import android.util.Log;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nostalgia.Constants;
import com.nostalgia.persistence.model.LocationUpdate;
import com.nostalgia.persistence.model.User;

import org.geojson.GeoJsonObject;
import org.geojson.Point;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Map;

public class UserAttributeUpdaterThread extends Thread {

    private final String userId;
    private final Attribute attr;
    private final Map<String, String> toChange;

    private User returned;

    public User getReturned() {
        return returned;
    }

    public enum Attribute{
        ICON, HOME, EMAIL, NAME, SETTING
    }

    public UserAttributeUpdaterThread(String userId, Attribute toChange, Map<String, String> fieldsToChange) {
        this.userId = userId;
        this.attr = toChange;
        this.toChange = fieldsToChange;
    }

    public void run() {

        ObjectMapper mapper = new ObjectMapper();
        try {
            System.out.println(mapper.writeValueAsString(toChange));
        } catch (JsonProcessingException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        URL httpPost = null;

        try {
        httpPost = new URL(Constants.API_URL + ":" + Constants.API_PORT + "/api/v0/user/update/" + attr +"?userId=" + userId);

            // 2. Open connection
            HttpURLConnection conn = (HttpURLConnection) httpPost.openConnection();

            // 3. Specify POST method
            conn.setRequestMethod("POST");

            // 4. Set the headers
            conn.setRequestProperty("Content-Type", "application/json; charset=UTF-8");


            conn.setDoOutput(true);

            // 5. Add JSON data into POST request body
            String json = mapper.writeValueAsString(toChange);
            byte[] outputBytes = json.getBytes("UTF-8");
            OutputStreamWriter os = new OutputStreamWriter(conn.getOutputStream());
            os.write(json);

            Log.i("ATTRUPDATE", "sending string: " + json);

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

            returned = mapper.readValue(response.toString(), User.class);

        } catch (Exception e1) {
            e1.printStackTrace();
        }

    }
}



