package com.nostalgia.runnable;

/**
 * Created by alex on 11/7/15.
 */

import android.util.Log;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nostalgia.Constants;
import com.nostalgia.persistence.model.User;

import org.apache.commons.io.IOUtils;


import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;

public class FriendActionThread extends Thread {
    private final ActionType actionType;

    public enum ActionType{
        ACCEPT, REMOVE, REQUEST, DENY
    }
    private final String friendId, myId;
    public User getMyUpdatedState() {
        return result;
    }

    private User result;
    private static final ObjectMapper mapper = new ObjectMapper();

    public FriendActionThread(ActionType action, String friendId, User me) {
       this.friendId = friendId;
        this.myId = me.get_id();
        this.actionType  = action;
    }

    public void run() {

try {
        URL httpPost = null;

        httpPost = new URL(Constants.API_URL + ":" + Constants.API_PORT + "/api/v0/friends/" + actionType.name() + "?friendId=" + friendId);


// 2. Open connection
    HttpURLConnection conn = (HttpURLConnection) httpPost.openConnection();

    // 3. Specify POST method
    conn.setRequestMethod("POST");

    // 4. Set the headers
    conn.setRequestProperty("Content-Type", "text/plain");

    conn.setDoOutput(true);

    // 5. Add JSON data into POST request body

    //`5.1 Use Jackson object mapper to convert Contnet object into JSON
    ObjectMapper mapper = new ObjectMapper();

    // 5.2 Get connection output stream
    OutputStream out = conn.getOutputStream();
    out.write(myId.getBytes());

    // 5.4 Send the request
    out.flush();
    out.close();

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



                result = mapper.readValue(response.toString(), User.class);



        } catch (Exception e) {
            e.printStackTrace();
        }

        if(result != null){
            Log.d("prefs", "user  returned: " + result);
        } else {
            Log.d("prefs", "no results found");
        }

    }
}



