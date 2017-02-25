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
import com.nostalgia.persistence.model.User;

import org.apache.commons.io.IOUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;

public class UserSearchThread extends Thread {
    private final String searchQuery;

    public List<User> getMatching() {
        return matching;
    }

    private List<User> matching;
    private static final ObjectMapper mapper = new ObjectMapper();

    public UserSearchThread(String searchQuery) {
       this.searchQuery = searchQuery;

    }

    public void run() {


        URL httpGet = null;
        try {

        httpGet = new URL(Constants.API_URL + ":" + Constants.API_PORT + "/api/v0/friends/search?friendName=" + searchQuery);

// 2. Open connection
            HttpURLConnection conn = (HttpURLConnection) httpGet.openConnection();

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

                matching = mapper.readValue(response.toString(), new TypeReference<List<User>>(){});



        } catch (Exception e) {
            e.printStackTrace();
        }

        if(matching != null){
            Log.d("prefs", "lnaum users found: " + matching.size());
        } else {
            Log.d("prefs", "no results found");
        }

    }
}



