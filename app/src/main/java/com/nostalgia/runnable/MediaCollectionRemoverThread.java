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
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;

public class MediaCollectionRemoverThread extends Thread {
    private static final String TAG = "MediaCollRemover";
    private final String idToRemove;
    private final User removing;
    private MediaCollection removed;

    public MediaCollectionRemoverThread(String idOfCollToRemove, User removing) {
        this.idToRemove = idOfCollToRemove;
        this.removing = removing;
    }

    public MediaCollection getDeleted(){
        return removed;
    }

    public void run() {
        try {
            ObjectMapper mapper = new ObjectMapper();

            System.out.println("removing collection with id: " + idToRemove);

            URL httpDelete = null;

            httpDelete = new URL(Constants.API_URL + ":" + Constants.API_PORT + "/api/v0/admin/mediacollection/delete?removerToken=" + removing.get_id() + "&idToDelete=" + idToRemove);

            HttpURLConnection conn = (HttpURLConnection) httpDelete.openConnection();

            // 3. Specify POST method
            conn.setRequestMethod("DELETE");

            // 4. Set the headers
            conn.setRequestProperty("Content-Type", "application/json");


            // 6. Get the response
            int responseCode = conn.getResponseCode();
            System.out.println("\nSending 'DELETE' request to URL : " + httpDelete);
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




            removed = mapper.readValue(response.toString(), MediaCollection.class);

        } catch (Exception e) {
            e.printStackTrace();
            Log.e(TAG, "Error reading in created mediacollection object", e);
        }

    }
}



