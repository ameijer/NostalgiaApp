package com.nostalgia.runnable;

/**
 * Created by alex on 10/30/15.
 */

import android.os.AsyncTask;
import android.util.Log;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nostalgia.Constants;
import com.nostalgia.Nostalgia;
import com.nostalgia.persistence.model.KnownLocation;
import com.nostalgia.persistence.model.MediaCollection;
import com.nostalgia.persistence.model.User;
import com.nostalgia.persistence.model.Video;


import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;


public class AttributeGetterTask extends AsyncTask<String, Integer, Integer> {

    private static final String GET_VAL_URL = Constants.API_URL + ":" + Constants.API_PORT + "/api/v0/atomic/getval/";
    private static final ObjectMapper mapper = new ObjectMapper();
    private final String callType;
    private final Object targetObj;
    private final FieldType enumCallType;

    private final objtype objType;
    private final User requesting;

    private JSONObject resultObj;
    private String rawResult;
    private final String targetObjId;

    private int makeTehCall() throws Exception{
        String charset = "UTF-8";
        URL httpGet= new URL(GET_VAL_URL + objType + "?type=" + callType + "&id=" + targetObjId + "&userId=" + requesting.get_id());

// 2. Open connection
        HttpURLConnection conn = (HttpURLConnection) httpGet.openConnection();

        // 3. Specify POST method
        conn.setRequestMethod("GET");

        // 4. Set the headers
        conn.setRequestProperty("Content-Type", "application/json");


        // 6. Get the response
        int responseCode = conn.getResponseCode();
        System.out.println("\nSending 'get' request to URL : " + httpGet);
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


        rawResult = response.toString();

        return responseCode;
    }



    public void setFinishedListener(GetterTaskFinishedListener finishedListener) {
        this.finishedListener = finishedListener;
    }

    public interface GetterTaskFinishedListener {
        void onAttributeGotten(String ownerObjectId, long simpleCount);
        void onError(String ownerObjectId);
    }

    public static final String TAG = "AttrTask";
    // This is the progress bar you want to update while the task is in progress
    private GetterTaskFinishedListener finishedListener;


    public long getNumericalValue(){
        if(resultObj != null) {
            try {
                return resultObj.getLong("numerical");
            } catch (JSONException e) {
                Log.e(TAG, "error getting numerical", e);
                return 0;
            }
        } else return -1;
    }

    public boolean hasUserParticipated(User toCheck){
        if(rawResult == null) return false;

        return rawResult.contains("true");
    }


    public String getRawResult(){
        return rawResult;
    }

    public AttributeGetterTask(Object hasPointers, FieldType field, User requesting) {
        //Welcome to javascript, my friend.
        this.targetObj = hasPointers;
        this.callType = field.toString();
        this.enumCallType = field;
        this.requesting = requesting;

        if(hasPointers instanceof Video){
            objType = objtype.VIDEO;
            targetObjId = ((Video)hasPointers).get_id();
        } else if(hasPointers instanceof KnownLocation){
            objType = objtype.LOCATION;
            targetObjId = ((KnownLocation)hasPointers).get_id();
        } else if(hasPointers instanceof MediaCollection){
            objType = objtype.COLLECTION;
            targetObjId = ((MediaCollection)hasPointers).get_id();
        } else if(hasPointers instanceof User){
            objType = objtype.USER;
            targetObjId = ((User)hasPointers).get_id();
        } else throw new IllegalArgumentException("invalid object passed in to getattrbutes");

    }

    private enum objtype {
        VIDEO, USER, COLLECTION, LOCATION
    }

    @Override
    protected Integer doInBackground(String... params) {
        int status = -1;

        try {
            status = this.makeTehCall();
        } catch (Exception e) {
            Log.e(TAG, "ERROR ATTRIBUTE GETTER ASYNC", e);
            if(finishedListener != null){
                finishedListener.onError(targetObjId);
            }
        }

        if(status != 200 ){
            if(finishedListener != null){
                finishedListener.onError(targetObjId);
            }
            Log.e(TAG, "ERROR - BAD STATUS CODE");
        }
        return status;
    }



    // private Object mutex = new Object();
    @Override
    protected void onProgressUpdate(Integer... values) {
        super.onProgressUpdate(values);

    }

    @Override
    protected void onPostExecute(Integer result) {
        super.onPostExecute(result);

        try {
            resultObj = new JSONObject(rawResult);
        } catch (JSONException e) {
            Log.e(TAG, "error parsing json object out", e);
        }
        if(finishedListener != null) {
            finishedListener.onAttributeGotten(targetObjId, getNumericalValue()); // Tell whoever was listening we have finished
        }
    }




}
