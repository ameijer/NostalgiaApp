package com.nostalgia.runnable;

/**
 * Created by alex on 11/7/15.
 */

import android.util.Log;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nostalgia.Constants;
import com.nostalgia.persistence.model.User;

import org.apache.commons.io.IOUtils;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

public class CollectionVideoAdderThread extends Thread {
    private static final String TAG = "CollVidAdderThread";
    private final List<String> collectionsToAddTo;
    private final String vidIdToAdd;
    private static final ObjectMapper mapper = new ObjectMapper();
    private List<String> added;

    public CollectionVideoAdderThread(List<String> collectionsToAddTo, String vidIdToAdd) {
        super();
        this.collectionsToAddTo = collectionsToAddTo;
        this.vidIdToAdd = vidIdToAdd;

    }

    public List<String> getAddedColls(){
        return added;
    }

    public void run() {

        URL httpPost = null;
        try {
        httpPost = new URL(Constants.API_URL + ":" + Constants.API_PORT + "/api/v0/video/addtocoll?vidId=" + vidIdToAdd);


            String toWrite = mapper.writeValueAsString(collectionsToAddTo);


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

wr.writeUTF(toWrite);


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

            added = mapper.readValue(response.toString(), new TypeReference<List<String>>(){});

        } catch (Exception e) {
            e.printStackTrace();
            Log.e(TAG, "Error reading in created location object", e);
        }

    }
}



