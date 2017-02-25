package com.nostalgia.runnable;

/**
 * Created by alex on 11/7/15.
 */

import android.util.Log;

import com.nostalgia.Constants;
import com.nostalgia.persistence.model.KnownLocation;
import com.nostalgia.persistence.model.MediaCollection;
import com.nostalgia.persistence.model.User;
import com.nostalgia.persistence.model.Video;

import org.apache.commons.codec.binary.Hex;
import org.apache.commons.codec.digest.DigestUtils;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class PasswordActionThread extends Thread {



    private static final String FORGOT_PATH = "/api/v0/user/password/forgot";
    private static final String CHANGE_PATH = "/api/v0/user/password/change";
    private static final String TAG = PasswordActionThread.class.getName();
    private  String email;

    private String orig;
    private boolean success;
    private String id;
    private String newPass;

    //if you forgot the password, load in the users email ther eand send them a reset
    public PasswordActionThread(String email){
        this.email = email;
    }

    //if they want to change the password, load the relevent field in here
    public PasswordActionThread(String origPassword, String newPassword, String userId) {
        this.orig = new String(Hex.encodeHex(DigestUtils.sha512(origPassword)));
        this.newPass = new String(Hex.encodeHex(DigestUtils.sha512(newPassword)));
        this.id = userId;


    }



    @Override
    public void run() {
        URL postUrl = null;
        HttpURLConnection conn = null;
        int responseCode = -1;
        try {

            if (email != null){
                postUrl = new URL(Constants.API_URL + ":" + Constants.API_PORT + FORGOT_PATH + "?email=" +email);


              conn=  (HttpURLConnection) postUrl.openConnection();

                // 3. Specify POST method
                conn.setRequestMethod("GET");
                conn.setDoOutput(false);

            } else if(newPass != null && id != null && orig != null){
                postUrl = new URL(Constants.API_URL + ":" + Constants.API_PORT + CHANGE_PATH + "?orig=" + orig + "&userId=" + id);


               conn = (HttpURLConnection) postUrl.openConnection();

                // 3. Specify POST method
                conn.setRequestMethod("POST");

                // 4. Set the headers
                conn.setRequestProperty("Content-Type", "application/json");

                DataOutputStream wr = new DataOutputStream(conn.getOutputStream());

                conn.setDoOutput(true);
                wr.writeUTF(newPass);

// 5.4 Send the request
                wr.flush();

                // 5.5 close
                wr.close();

            } else {
                Log.e(TAG, "ERROR - could not infer the action you want to perform", new IllegalArgumentException());
                return;
            }




            // 6. Get the response
            responseCode = conn.getResponseCode();
            System.out.println("\nSending request to URL : " + postUrl);
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



