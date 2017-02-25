package com.nostalgia.runnable;

/**
 * Created by alex on 11/7/15.
 */

import android.util.Log;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nostalgia.Constants;
import com.nostalgia.persistence.model.MediaCollection;
import com.nostalgia.persistence.model.User;

import org.apache.commons.io.IOUtils;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;

public class MediaCollectionUpdaterThread extends Thread {
    private static final String TAG = "MediaCollUpdater";
    private final MediaCollection toUpdate;
    private final User updating;
    private MediaCollection updated;

    public MediaCollectionUpdaterThread(MediaCollection toUpdate, User updating) {
        this.toUpdate = toUpdate;
        this.updating = updating;
    }

    public MediaCollection getUpdated(){
        return updated;
    }

    public void run() {

        ObjectMapper mapper = new ObjectMapper();
        try {
            System.out.println(mapper.writeValueAsString(toUpdate));
        } catch (JsonProcessingException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        try {
        URL httpPost = null;

        httpPost = new URL(Constants.API_URL + ":" + Constants.API_PORT + "/api/v0/admin/mediacollection/update?updaterToken="+updating.get_id());



            HttpURLConnection conn = (HttpURLConnection) httpPost.openConnection();

            // 3. Specify POST method
            conn.setRequestMethod("POST");

            // 4. Set the headers
            conn.setRequestProperty("Content-Type", "application/json");

            conn.setDoOutput(true);

            // 5. Add JSON data into POST request body


            // 5.2 Get connection output stream
            DataOutputStream wr = new DataOutputStream(conn.getOutputStream());

            // 5.3 Copy Content "JSON" into
            mapper.writeValue(wr, toUpdate);

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


       updated = mapper.readValue(response.toString(), MediaCollection.class);

        } catch (Exception e) {
            e.printStackTrace();
            Log.e(TAG, "Error reading in created mediacollection object", e);
        }

    }
}



