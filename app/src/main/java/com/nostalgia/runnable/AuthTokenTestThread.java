package com.nostalgia.runnable;

/**
 * Created by alex on 11/7/15.
 */

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nostalgia.Constants;
import com.nostalgia.controller.login.HttpUtils;
import com.nostalgia.persistence.model.User;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Date;

public class AuthTokenTestThread extends Thread {

    private static final String TAG = "authtoktest";
    private final User user;
    private final TestType type;
    private final Context context;
    private final boolean omit;
    private int resultCode;

    public int getResultCode() {
        return resultCode;
    }

    public enum TestType {
        POST_PASS, GET_FAIL, GET_QUERY_PASS, GET_PASS
    }

    public String getMatching() {
        return matching;
    }

    private String matching = null;

    public AuthTokenTestThread(User user, TestType type, Context toShowToastIn, boolean omitCredientials) {
        this.user = user;
        this.type = type;
        this.context = toShowToastIn;
        this.omit = omitCredientials;

    }

    public void run() {


        try {
       URL httpGet = null;

            switch (type){

                case GET_FAIL:
                    httpGet = new URL(Constants.API_URL + ":" + Constants.API_PORT + "/test/auth/admin");
                    break;
                case GET_QUERY_PASS:
                    httpGet = new URL(Constants.API_URL + ":" + Constants.API_PORT + "/test/auth/params/profile?testval=testval_gen_" + new Date().toString());
                    break;
                case POST_PASS:
                case GET_PASS:
                    httpGet = new URL(Constants.API_URL + ":" + Constants.API_PORT + "/test/auth/profile");
                    break;
            }



// 2. Open connection
            HttpURLConnection conn = (HttpURLConnection) httpGet.openConnection();

            // 3. Specify POST method
            if(type == TestType.POST_PASS){
                conn.setRequestMethod("POST");
                System.out.println("\nSending 'POST' request to URL : " + httpGet);
                conn.setDoOutput(true);

                // 5. Add JSON data into POST request body

String payload = "payload generated @" + new Date().toString();
              conn.getOutputStream().write(payload.getBytes());

                // 5.4 Send the request
                conn.getOutputStream().flush();

                // 5.5 close
                conn.getOutputStream().close();
            } else {
                conn.setRequestMethod("GET");
                System.out.println("\nSending 'GET' request to URL : " + httpGet);
            }
            // 4. Set the headers
            conn.setRequestProperty("Content-Type", "application/json");

            if(user.getToken() == null){
                Log.e(TAG, "error - user does not have an oauth token to use! ", new NullPointerException());
                Toast.makeText(context, "error - user does not have an oauth token to use! ", Toast.LENGTH_LONG).show();
                return;
            }
            if(!omit) {
                conn.setRequestProperty("Authorization", "Bearer " + user.getToken());
            }
            // 6. Get the response
            int responseCode = conn.getResponseCode();

            resultCode = responseCode;
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


              matching = response.toString();



        } catch (Exception e) {
            e.printStackTrace();
        }


    }


}



