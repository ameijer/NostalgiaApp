package com.nostalgia.runnable;

/**
 * Created by alex on 11/7/15.
 */

import android.util.Log;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nostalgia.Constants;
import com.nostalgia.persistence.model.KnownLocation;

import org.apache.commons.io.IOUtils;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;

public class SubscriberThread extends Thread {
    private static final String TAG = "SubscriberThread";
    private final String toSubscribeTo;
    private final String userIdOfSubscriber;
    private String subscribed;

    public SubscriberThread(String idToSubscribeTo, String subscriberId) {
        super();
        this.toSubscribeTo = idToSubscribeTo;
        this.userIdOfSubscriber = subscriberId;
    }

    public String getSubscribed(){
        return subscribed;
    }

    public void run() {

        URL httpPost = null;

        try {
        httpPost = new URL(Constants.API_URL + ":" + Constants.API_PORT + "/api/v0/user/subscribe/add?userId=" + userIdOfSubscriber + "&id=" + toSubscribeTo);


            // 2. Open connection
            HttpURLConnection conn = (HttpURLConnection) httpPost.openConnection();

            // 3. Specify POST method
            conn.setRequestMethod("POST");

            // 4. Set the headers
            conn.setRequestProperty("Content-Type", "application/json");

            conn.setDoOutput(true);

            // 5. Add JSON data into POST request body

            //`5.1 Use Jackson object mapper to convert Contnet object into JSON
            ObjectMapper mapper = new ObjectMapper();

            // 5.2 Get connection output stream
            DataOutputStream wr = new DataOutputStream(conn.getOutputStream());


            // 5.4 Send the request
            wr.flush();

            // 5.5 close
            wr.close();

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

            subscribed = response.toString();

        } catch (Exception e) {
            e.printStackTrace();
            Log.e(TAG, "Error reading in created location object", e);
        }

    }
}



