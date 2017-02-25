package com.nostalgia.runnable;

/**
 * Created by alex on 11/7/15.
 */

import android.util.Log;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nostalgia.Constants;
import com.nostalgia.persistence.model.KnownLocation;

import org.apache.commons.io.IOUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class KnownLocationUNSubscribeThread extends Thread {
    private static final String TAG = "LocationSubscriber";
    private final String locationToUnSubscribeTo;
    private final String userIdOfSubscriber;
    private boolean unsubscribeSuccess = false;

    public KnownLocationUNSubscribeThread(String locIdToUnSubscribeTo, String subscriberId) {
        super();
        this.locationToUnSubscribeTo = locIdToUnSubscribeTo;
        this.userIdOfSubscriber = subscriberId;

    }

    public boolean isUnsubscribed(){
        return unsubscribeSuccess;
    }

    public void run() {

        try {
        URL httpPost = null;

        httpPost = new URL(Constants.API_URL + ":" + Constants.API_PORT + "/api/v0/user/subscribe/remove?userId=" + userIdOfSubscriber + "&locationId=" + locationToUnSubscribeTo);
            HttpURLConnection conn = (HttpURLConnection) httpPost.openConnection();

            // 3. Specify POST method
            conn.setRequestMethod("POST");

            // 4. Set the headers
            conn.setRequestProperty("Content-Type", "application/json");

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

            // 7. Print result
            System.out.println(response.toString());



        String contents = response.toString();


           this.unsubscribeSuccess = contents.equals(locationToUnSubscribeTo);


        } catch (Exception e) {
            e.printStackTrace();
            Log.e(TAG, "Error reading in created location object", e);
        }

    }
}



