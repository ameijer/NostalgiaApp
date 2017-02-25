package com.nostalgia.runnable;

/**
 * Created by alex on 10/30/15.
 */

import android.os.AsyncTask;
import android.util.Log;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nostalgia.Constants;
import com.nostalgia.Nostalgia;
import com.nostalgia.persistence.model.Video;

import org.apache.commons.codec.binary.Hex;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.io.IOUtils;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

public class VideoUploadTask extends AsyncTask<String, Integer, Integer> {

    private static final String UPLOAD_URL_METADATA= Constants.API_URL + ":" + Constants.API_PORT + "/api/v0/video/new";
    private static final String UPLOAD_URL_VIDDATA= Constants.API_URL + ":" + Constants.API_PORT + "/api/v0/video/upload/data";
    private final boolean autoAdd;

    private static final ObjectMapper mapper = new ObjectMapper();

    private String uploadFileForVid(String vidId, String MD5, File toUpload) throws Exception{
        String charset = "UTF-8";
        String url = UPLOAD_URL_VIDDATA + "?vidId=" + vidId + "&checksum=" + MD5;
        Log.d(TAG, "posting video file to: " + url);
        URL postUrl= new URL(url);

        // 2. Open connection
        HttpURLConnection conn = (HttpURLConnection) postUrl.openConnection();

        // 3. Specify POST method
        conn.setRequestMethod("POST");

        // 4. Set the headers
        conn.setRequestProperty("Content-Type", "binary/octet-stream");

        conn.setDoOutput(true);

        boolean exists = toUpload.exists();
        Log.d(TAG, "does upload exist? " + exists);

       InputStream in = new FileInputStream(toUpload);
        // 5.2 Get connection output stream
        DataOutputStream wr = new DataOutputStream(conn.getOutputStream());

        IOUtils.copy(in,wr);
        // 5.4 Send the request
        wr.flush();
        in.close();
        wr.close();

        responseCode = conn.getResponseCode();
        Log.d("vid ul task", "\nSending 'POST' request to URL : " + postUrl);
        Log.d("vid task", "Response Code : " + responseCode);

        BufferedReader inStream = new BufferedReader(
                new InputStreamReader(conn.getInputStream()));
        String inputLine;
        StringBuffer response = new StringBuffer();
        try {
            while ((inputLine = inStream.readLine()) != null) {
                response.append(inputLine);
            }
            in.close();
        } catch (Exception e){
            Log.e(TAG, "error in upload");
        }

        String res = response.toString();
        Log.d(TAG, "video upload response: " + res);
        return res;
    }

    int responseCode = -1;

    private String uploadVidMetadata(Video metadata) throws Exception{
        String charset = "UTF-8";

    String url = UPLOAD_URL_METADATA + "?auto=" + Boolean.toString(autoAdd);
        Log.d(TAG, "about to post to: " + url);
        URL postUrl= new URL(url);

// 2. Open connection
        HttpURLConnection conn = (HttpURLConnection) postUrl.openConnection();

        // 3. Specify POST method
        conn.setRequestMethod("POST");


        // 4. Set the headers
        conn.setRequestProperty("Content-Type", "application/json");
        conn.setRequestProperty("Accept-Encoding", "identity");
        ObjectMapper om = new ObjectMapper();

        String videoAsJSON = om.writeValueAsString(metadata);
        DataOutputStream wr = new DataOutputStream(conn.getOutputStream());
        // 5.3 Copy Content "JSON" into
        om.writeValue(wr, metadata);
// 5.4 Send the request
        wr.flush();

        // 5.5 close
        wr.close();

        // 6. Get the response
       int responseCode2 = conn.getResponseCode();
        Log.d("vid ul task", "\nSending 'POST' request to URL : " + postUrl);
       Log.d("vid task", "Response Code : " + responseCode2);

        BufferedReader in = new BufferedReader(
                new InputStreamReader(conn.getInputStream()));
        String inputLine;
        StringBuffer response = new StringBuffer();
try {
        while ((inputLine = in.readLine()) != null) {
            response.append(inputLine);
        }
        in.close();

        // 7. Print result
        System.out.println("uploaded metadat awith id: " + response.toString());

    } catch (MalformedURLException e) {
        e.printStackTrace();
    } catch (IOException e) {
        e.printStackTrace();
    }

        return response.toString();
    }


    public void setFinishedListener(UploadTaskFinishedListener finishedListener) {
        this.finishedListener = finishedListener;
    }

    public interface UploadTaskFinishedListener {
        void onTaskFinished(); // If you want to pass something back to the listener add a param to this method
    }

    public static final String TAG = "LoadingTask";
    // This is the progress bar you want to update while the task is in progress
    private UploadTaskFinishedListener finishedListener;



    private final Nostalgia app;
    private final Video toUpload;
    private final String targetPath;

    public VideoUploadTask(String focusedFilePath, Video thisVideo, Nostalgia app, boolean autoAddLocations) {
        this.app = app;
        this.toUpload = thisVideo;
        this.targetPath = focusedFilePath;
        this.autoAdd = autoAddLocations;
    }

    @Override
    protected Integer doInBackground(String... params) {
        String result = "";
        int resultCode = -1;
        try {
////upload metadata
            String savedId = null;
            try {
                savedId = uploadVidMetadata(toUpload);
            } catch (Exception e) {
                Log.e(TAG, "error uploading video meta data");
            }

            File in = new File(targetPath);

            FileInputStream fis = null;
            String md5 = null;

            //
            fis = new FileInputStream(in);

            long size = fis.getChannel().size();

            Log.d(TAG, "attempting to upload file: " + savedId + " of size: " + size);
            byte[] md5Bytes = DigestUtils.md5(fis);
    Log.d(TAG, "md5 computed as: " + Hex.encodeHex(md5Bytes));
            md5 = new String(Hex.encodeHex(md5Bytes));
            fis.close();
            //

            //using returned data, upload video
            try {

                if (!in.exists()) {
                    throw new Exception("file not found");
                }
                result = uploadFileForVid(savedId, md5, in);

                if(result.equalsIgnoreCase(md5)){
                    return 200;
                }

                // sendFileToServer(savedId, md5, in);
            } catch (Exception e) {
                Log.e(TAG, "error uploading video", e);
            }
        } catch(Exception e){
            e.printStackTrace();
        } finally {
            return responseCode;
        }
    }



    // private Object mutex = new Object();
    @Override
    protected void onProgressUpdate(Integer... values) {
        super.onProgressUpdate(values);

    }

    @Override
    protected void onPostExecute(Integer result) {
        super.onPostExecute(result);
        if(finishedListener != null) {
            finishedListener.onTaskFinished(); // Tell whoever was listening we have finished
        }
    }
}
