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

public class ViewInfoThread extends Thread {
    private final String targetId;

    private final User checking;

    boolean hasViewed;
    long dateViewed;

    public boolean hasViewed() {
        return hasViewed;
    }

    public ViewInfoThread(User vieweing, String idOfObjToCheck) {
        this.targetId = idOfObjToCheck;
        checking = vieweing;

    }

    String resp;
    public void run() {


        try {
       URL httpGet = null;

        httpGet = new URL(Constants.API_URL + ":" + Constants.API_PORT + "/api/v0/atomic/checkviewed?userId=" + checking.get_id() + "&target=" + targetId);


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


            resp = response.toString();
            System.out.println("response: " + resp);


        } catch (Exception e) {
            e.printStackTrace();
        }

        try {
           dateViewed = Long.parseLong(resp);
        } catch (NumberFormatException e){
            Log.e("hasviewed", "error pariding resp: " + resp, e);
            dateViewed = -1;
        }

        if(dateViewed < 0){
            Log.d("hasviewed", "neagtive date, has not viewed");
            hasViewed = false;
        } else {
            Log.d("hasviewed", " has viewed: " + targetId);
            hasViewed = true;
        }

    }
}



