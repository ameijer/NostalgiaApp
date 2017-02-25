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
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;

public class CollectionACLThread extends Thread {
    private static final String TAG = CollectionACLThread.class.getName().substring(0,20);
    private final ActionType actionType;
    private final List<String> targets;

    public enum ActionType{
        ADD_AS_READER, ADD_AS_WRITER, REMOVE_AS_READER, REMOVE_ALL_ACCESS, REMOVE_AS_WRITER
    }
    private final String collectionId, myId;
    public MediaCollection getMyUpdatedState() {
        return result;
    }

    private MediaCollection result;
    private static final ObjectMapper mapper = new ObjectMapper();

    public CollectionACLThread(ActionType action, List<String> friendIds, User me, String collectionIdToChange) {
        this.collectionId = collectionIdToChange;
        this.myId = me.get_id();
        this.actionType  = action;
        targets = friendIds;
    }

    public void run() {

        try {
            URL httpPost = null;

            httpPost = new URL(Constants.API_URL + ":" + Constants.API_PORT + "/api/v0/mediacollection/shared/userops?collId=" + collectionId + "&adderId=" + myId + "&privelige=" + actionType.name());


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

            String jsondList = mapper.writeValueAsString(targets);
            Log.i(TAG, "performing operation on follwing json list of friends: " + jsondList);
            wr.writeUTF(jsondList);

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
            Log.d(TAG, "response: " + response.toString());



            result = mapper.readValue(response.toString(), MediaCollection.class);



        } catch (Exception e) {
            e.printStackTrace();
        }

        if(result != null){
            Log.d(TAG, "collection  returned: " + result);
        } else {
            Log.d(TAG, "no collection found");
        }

    }
}



