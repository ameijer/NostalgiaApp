package com.nostalgia.runnable;

/**
 * Created by alex on 10/30/15.
 */

import android.os.AsyncTask;
import android.text.TextUtils;
import android.util.Log;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nostalgia.Nostalgia;
import com.nostalgia.persistence.model.Video;

import org.apache.commons.codec.binary.Hex;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Map;


public class ProtectedResourceGetterTask extends AsyncTask<String, Integer, File> {

    public static final String TAG = "ResourceGetterTask";

    private final Map<String, String> cookieMap;
    private final File target;
    private final String urlString;

    public void setOnDownloadListener(DownloadEventListener finishedListener) {
        this.finishedListener = finishedListener;
    }

    private void setCookies(HttpURLConnection needsCookies) {
        ArrayList<String> cookies = new ArrayList<String>();

        for(String key: cookieMap.keySet()){
            cookies.add(key + "=" + cookieMap.get(key));
        }


        needsCookies.setRequestProperty("Cookie", TextUtils.join(";", cookies));
    }

    public interface DownloadEventListener {
        void onResourceFinished(File downloaded);
    }


    // This is the progress bar you want to update while the task is in progress
    private DownloadEventListener finishedListener;


    public ProtectedResourceGetterTask(Map<String, String> userCookies, File target, String sourceUrl) {
        this.cookieMap = userCookies;
        this.target = target;
        this.urlString = sourceUrl;

        if(target.exists()){
            Log.w(TAG, "target file already exists, deleting existing data");
            FileUtils.deleteQuietly(target);
        }
    }


    @Override
    protected File doInBackground(String... urls) {
        URL url = null;
        try {
            url = new URL(urlString);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        HttpURLConnection urlConnection = null;

        try {
            urlConnection = (HttpURLConnection) url.openConnection();
        } catch (IOException e) {
            e.printStackTrace();
        }

        setCookies(urlConnection);
        if(!target.exists()){
            try {
                target.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        FileOutputStream out = null;
        try {
             out = new FileOutputStream(target);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        try {
            InputStream in = new BufferedInputStream(urlConnection.getInputStream());
            IOUtils.copy(in, out);
            in.close();
        } catch (IOException e) {
            e.printStackTrace();
        }finally {
            urlConnection.disconnect();
            try {
                out.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return target;
    }


    @Override
    protected void onProgressUpdate(Integer... values) {
        super.onProgressUpdate(values);

    }

    @Override
    protected void onPostExecute(File result) {
        super.onPostExecute(result);
        if(finishedListener != null) {
            finishedListener.onResourceFinished(target); // Tell whoever was listening we have finished
        }
    }
}
