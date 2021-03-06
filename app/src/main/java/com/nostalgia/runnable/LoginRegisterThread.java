package com.nostalgia.runnable;

/**
 * Created by alex on 11/7/15.
 */
import android.util.Log;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nostalgia.Constants;
import com.nostalgia.Nostalgia;
import com.nostalgia.persistence.model.LoginResponse;
import com.nostalgia.persistence.model.User;

import org.apache.commons.codec.binary.Hex;
import org.apache.commons.codec.digest.DigestUtils;

import org.geojson.Point;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

public class LoginRegisterThread extends Thread {
    private String mEmail;
    private String mUsername;
    private String mPassword;
    private String oAuth;
    private final Nostalgia nostalgia;
    private final boolean isRegister;

    private final Point location;
    private User mNoob;
    private LoginResponse loginResponse;
    private final String type;

    public enum LoginTypes {
        facebook, nostalgia, google
    }

    public LoginRegisterThread(Nostalgia app, String email, String uname, String pass, String type, boolean isRegister, Point location) {
        mEmail = email;
        mUsername = uname;
        mPassword = pass;
        this.type = type;
        this.isRegister = isRegister;
        this.nostalgia = app;
        this.location = location;

    }

    public LoginRegisterThread(Nostalgia app, User toAdd, String password, boolean isRegister, String type) {
        mNoob = toAdd;
        this.isRegister = isRegister;
        location = null;
        this.type = type;
        this.mPassword = password;
        this.nostalgia = app;
    }

    public LoginRegisterThread(Nostalgia app, String uname, String pass, String type, boolean isRegister, Point location) {
        mUsername = uname;
        mPassword = pass;
        this.type = type;
        this.isRegister = isRegister;
        this.nostalgia = app;
        this.location = location;
    }

    public LoginRegisterThread(Nostalgia app, String oAuth, String type, boolean isRegister, Point location) {
        this.oAuth = oAuth;
        this.type = type.toString();
        this.isRegister = isRegister;
        this.nostalgia = app;
        this.location = location;
    }

    public void run() {
        if (mNoob == null){
            mNoob = new User();
        mNoob.setEmail(mEmail);
        mNoob.setUsername(mUsername);
        mNoob.setToken(oAuth);
        mNoob.setLastKnownLoc(location);
    }
    ArrayList<String> devices = new ArrayList<String>();
    devices.add(nostalgia.getDeviceId());
    mNoob.setAuthorizedDevices(devices);
    if(mPassword != null) {
        mPassword = new String(Hex.encodeHex(DigestUtils.sha512(mPassword)));
    }

    Log.d("prefs", "registering new user: " + mNoob.getUsername());

    ObjectMapper mapper = new ObjectMapper();
    try {
        System.out.println(mapper.writeValueAsString(mNoob));

        URL httpPost = null;
        if (isRegister) {
            httpPost = new URL(Constants.API_URL + ":" + Constants.API_PORT + "/api/v0/user/register?type=" + type + "&password=" + mPassword);
        } else {
            httpPost = new URL(Constants.API_URL + ":" + Constants.API_PORT + "/api/v0/user/login?type=" + type + "&password=" + mPassword);

        }

        HttpURLConnection conn = (HttpURLConnection) httpPost.openConnection();

        // 3. Specify POST method
        conn.setRequestMethod("POST");

        // 4. Set the headers
        conn.setRequestProperty("Content-Type", "application/json");

        conn.setDoOutput(true);

        // 5.2 Get connection output stream
        DataOutputStream wr = new DataOutputStream(conn.getOutputStream());

        // 5.3 Copy Content "JSON" into
        mapper.writeValue(wr, mNoob);

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


            loginResponse = mapper.readValue(response.toString(), LoginResponse.class);


    }catch (Exception e) {
        e.printStackTrace();
    }

    if(loginResponse != null){
        Log.d("prefs", "User registered!");
    } else {
        Log.d("prefs", "User NOT registered!");
    }
}

    public LoginResponse getLoginResponse() {
        return loginResponse;
    }
}



