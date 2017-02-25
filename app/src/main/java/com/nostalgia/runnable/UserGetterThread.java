package com.nostalgia.runnable;

/**
 * Created by alex on 11/7/15.
 */

import android.util.Log;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nostalgia.Constants;
import com.nostalgia.persistence.model.MediaCollection;
import com.nostalgia.persistence.model.User;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class UserGetterThread extends Thread {
    private final String userId;

    public User getMatching() {
        return matching;
    }

    private User matching = null;
    private static final ObjectMapper mapper = new ObjectMapper();

    public UserGetterThread(String userId) {
        this.userId = userId;

    }

    public void run() {


        try {
       URL httpGet = null;

        httpGet = new URL(Constants.API_URL + ":" + Constants.API_PORT + "/api/v0/user/id?userId=" + userId);


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

                matching = mapper.readValue(response.toString(), User.class);

        } catch (Exception e) {
            e.printStackTrace();
        }

        if(matching != null){
            Log.d("prefs", "collection found: " + matching.getUsername());
        } else {
            Log.d("prefs", "collection not found " + userId);
        }

    }
}



