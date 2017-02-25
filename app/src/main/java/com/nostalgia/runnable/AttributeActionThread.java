package com.nostalgia.runnable;

/**
 * Created by alex on 11/7/15.
 */
import com.nostalgia.Constants;
import com.nostalgia.Nostalgia;
import com.nostalgia.persistence.model.KnownLocation;
import com.nostalgia.persistence.model.LoginResponse;
import com.nostalgia.persistence.model.MediaCollection;
import com.nostalgia.persistence.model.User;
import com.nostalgia.persistence.model.Video;


import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

public class AttributeActionThread extends Thread {

    private final FieldType enumCallType;
    private final Object targetObj;
    private final objtype objType;
    private final String targetObjId;
    private final User performingAction;

    private static final String FAV_PATH = "/api/v0/atomic/favorite";
    private static final String FLAG_PATH = "/api/v0/atomic/flag";
    private static final String VIEWED_PATH = "/api/v0/atomic/report/viewed";
    private static final String SKIPPED_PATH = "/api/v0/atomic/report/skipped";
    private static final String UPVOTE_PATH = "/api/v0/atomic/upvote";
    private static final String DOWNVOTE_PATH = "/api/v0/atomic/downvote";
    private String path;
    private boolean success;
    private String contents;

    public AttributeActionThread(Object toPerformActionOn, User performingAction, FieldType fieldToChange) {
        this.targetObj = toPerformActionOn;

        this.enumCallType = fieldToChange;
        this.performingAction = performingAction;

        if(toPerformActionOn instanceof Video){
            objType = objtype.VIDEO;
            targetObjId = ((Video)toPerformActionOn).get_id();
        } else if(toPerformActionOn instanceof KnownLocation){
            objType = objtype.LOCATION;
            targetObjId = ((KnownLocation)toPerformActionOn).get_id();
        } else if(toPerformActionOn instanceof MediaCollection){
            objType = objtype.COLLECTION;
            targetObjId = ((MediaCollection)toPerformActionOn).get_id();
        } else if(toPerformActionOn instanceof User){
            objType = objtype.USER;
            targetObjId = ((User)toPerformActionOn).get_id();
        } else throw new IllegalArgumentException("invalid object passed in to getattrbutes");

        switch (fieldToChange){
            case UPVOTES:
                path = UPVOTE_PATH + "?userid=" + performingAction.get_id() + "&type=" + objType;
                break;
            case DOWNVOTES:
                path = DOWNVOTE_PATH + "?userid=" + performingAction.get_id() + "&type=" + objType;
                break;
            case FLAGS:
                path = FLAG_PATH+ "?userid=" + performingAction.get_id();
                break;
            case FAVORITES:
                path = FAV_PATH + "?userid=" + performingAction.get_id();
                break;
            case VIEWS:
                path = VIEWED_PATH + "?userid=" + performingAction.get_id();
                break;
            case SKIPS:
                path = SKIPPED_PATH + "?userid=" + performingAction.get_id();
                break;
        }
    }

    private enum objtype {
        VIDEO, USER, COLLECTION, LOCATION
    }

    @Override
    public void run() {
        URL postUrl = null;
        int responseCode = -1;
        try {
            postUrl = new URL(Constants.API_URL + ":" + Constants.API_PORT + path);



        HttpURLConnection conn = (HttpURLConnection) postUrl.openConnection();
            conn.setDoOutput(true);
        // 3. Specify POST method
        conn.setRequestMethod("POST");

        // 4. Set the headers
            conn.setRequestProperty("Content-Type", "text/plain");

        conn.getOutputStream().write(targetObjId.getBytes());

            conn.getOutputStream().flush();

            // 5.5 close
            conn.getOutputStream().close();

            // 6. Get the response
            responseCode = conn.getResponseCode();
            System.out.println("\nSending 'POST' request to URL : " + postUrl);
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





        }    catch (Exception e) {
        e.printStackTrace();
    }

        success = responseCode == 200;
    }

    public boolean  isSuccessful() {
        return success;
    }
}



