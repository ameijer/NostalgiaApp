package com.nostalgia.runnable;

/**
 * Created by alex on 11/7/15.
 */

import android.util.Log;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nostalgia.Constants;
import com.nostalgia.persistence.model.MediaCollection;
import com.nostalgia.persistence.model.Video;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class VideoGetterThread extends Thread {
    private final String videoId;

    public Video getMatching() {
        return matching;
    }

    private Video matching = null;
    private static final ObjectMapper mapper = new ObjectMapper();

    public VideoGetterThread(String videoId) {
        this.videoId = videoId;

    }

    public void run() {


        try {
       URL httpGet = null;

        httpGet = new URL(Constants.API_URL + ":" + Constants.API_PORT + "/api/v0/video/id?vidId=" + videoId);


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


                matching = mapper.readValue(response.toString(), Video.class);



        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}



