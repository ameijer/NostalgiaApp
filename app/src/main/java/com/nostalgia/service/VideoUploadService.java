package com.nostalgia.service;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Binder;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.widget.Toast;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nostalgia.Nostalgia;
import com.nostalgia.persistence.model.User;
import com.nostalgia.persistence.model.Video;
import com.nostalgia.persistence.repo.VideoRepository;
import com.nostalgia.runnable.VideoUploadTask;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Aidan on 11/19/15.
 */
public class VideoUploadService extends Service implements ServiceConnection, VideoRepository.VideoStatusChangeListener{


    private static final String TAG = "VideoUploadSvc";
    private Context ctx;
    private NetworkConnectivityService netConnSvc;
    private boolean bounded = false;
    private boolean uploading = false;
    private static final ObjectMapper mapper = new ObjectMapper();
    private VideoRepository vidRepo;

    public VideoUploadService(Context context){
        ctx = context;
    }

    public VideoUploadService(){
        super();
    }

    @Override
    public void onCreate(){
        super.onCreate();
        IntentFilter filter = new IntentFilter();
        filter.addAction("com.nostalgia.wifi.on");
        filter.addAction("com.nostalgia.wifi.off");
        registerReceiver(receiver, filter);
        ctx = getApplicationContext();
        bindToConnectivityService();
        vidRepo = ((Nostalgia)getApplication()).getVidRepo();
        vidRepo.setStatusCallback(this);
        uploadLooper.start();

    }

    boolean checking = false;
    private class UploadCheckerThread extends Thread {
        @Override
        public void run() {

            Log.i(TAG, "Scanning for videos to upload");

            if (netConnSvc != null){
                //check if wifi is on, and upload wifi videos if so
                if (netConnSvc.isWifiConnected()) {
                    try {
                        handleWifiUploads();
                    } catch (Exception e) {
                        Log.e(TAG, "error in uploadchecker handlewifi uploads", e);
                    }
                }
            }


            //check timed videos
            try {
                handleTimedUploads();
            } catch (Exception e) {
                Log.e(TAG, "error in uploadchecker handlewifi uploads", e);
            }

        }

    }

    Thread uploadLooper = new Thread(){
        @Override
        public void run() {
            while (true) {
                if(!checking) {
                    checking = true;
                    UploadCheckerThread uploadChecker = new UploadCheckerThread();
                    uploadChecker.start();
                    try {
                        uploadChecker.join();
                    } catch (Exception e) {
                        Log.e(TAG, "interrupted!", e);
                    }
                    checking = false;
                }
                try {
                    Thread.sleep(180 * 1000);
                } catch (InterruptedException e) {
                    Log.e(TAG, "interrupted", e);
                    return;
                }
            }
        }

    };

    public void bindToConnectivityService() {
        Intent connectSvcIntent = new Intent(this, NetworkConnectivityService.class);
        ctx.bindService(connectSvcIntent, this, Context.BIND_AUTO_CREATE);
        ctx.startService(connectSvcIntent);
    }

    public void stop() {
        stopService(new Intent(this, NetworkConnectivityService.class));
        unbindService(this);
    }
    // Binder given to clients
    private final IBinder mBinder = new UploadBinder();

    @Override
    public void onVideoStatusListChanged() {
        Log.i(TAG, "video status changed, checking for uploads");
        if(checking){
            Log.w(TAG, "already checking, skipping reactive check");
            return;
        }
        checking = true;
        UploadCheckerThread uploadChecker = new UploadCheckerThread();
        uploadChecker.start();
        try {
            uploadChecker.join();
        } catch (Exception e) {
            Log.e(TAG, "interrupted!", e);
        }
        checking = false;
    }

    /**
     * Class used for the client Binder.  Because we know this service always
     * runs in the same process as its clients, we don't need to deal with IPC.
     */
    public class UploadBinder extends Binder {
        VideoUploadService getService() {
            // Return this instance of NetworkConnectivityService so clients can call public methods
            return VideoUploadService.this;
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    @Override
    public void onDestroy(){
        super.onDestroy();
        unregisterReceiver(receiver);
        unbindService(this);
    }

    private final BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if(intent.getAction().equalsIgnoreCase("com.nostalgia.wifi.on")){
                try {
                    Log.d(TAG, "received wifi connected signal, checking wifi uplaods");
                    handleWifiUploads();
                } catch (Exception e) {
                    Log.e(TAG, "error in bcastreceiver", e);
                }


            } else if(intent.getAction().equalsIgnoreCase("com.nostalgia.wifi.off")){
                cancelWifiUploads();
            }

        }
    };

    public void cancelWifiUploads() {
        uploading = false;
    }


    private void handleWifiUploads() throws Exception {
        try {
            if (!uploading) {
                uploading = true;

                ArrayList<Video> pendingUpload = vidRepo.getPendingWifiUploads();

                for(Video toUpload : pendingUpload){
                    Log.d(TAG, "Pending upload: " + toUpload);
                }

                while (uploading && pendingUpload.size() > 0) {
                    Video toUpload = pendingUpload.remove(0);

                    upload(toUpload);

                }

            } else {
                Log.w(TAG, "skipping wifi upload due to active upload jobs");
            }
        } finally{
            uploading = false;
        }
        return;
    }

    private void handleTimedUploads() throws Exception {
        try {
            if (!uploading) {
                uploading = true;

                ArrayList<Video> pendingUpload = vidRepo.getPendingTimedUploads();

                while (uploading && pendingUpload.size() > 0) {
                    Video toUpload = pendingUpload.remove(0);

                    upload(toUpload);

                }
            } else {
                Log.w(TAG, "skipping timed upload due to active upload jobs");
            }

        } finally{
            uploading = false;
        }
        return;
    }



    private int upload(Video toUpload) throws Exception {
        File fileToUpload = new File(this.getFilesDir() + java.io.File.separator + toUpload.get_id() + ".dat");

        if(!fileToUpload.exists()){
            Log.e(TAG, "error uploading video, file not found for video: " + toUpload.get_id());
            toUpload.setStatus("ERROR_UPLOAD");
            vidRepo.save(toUpload);
            return -1;
        } else {
            Log.e(TAG, "file for video found @ " + fileToUpload.getAbsolutePath());
        }

        String tagString = toUpload.getProperties().get("initialTags");

        if(tagString == null){
            Log.w(TAG, "WARNING - NO INITIAL TAGS PROVIDED", new NullPointerException());
        }

        VideoUploadTask uploader = new VideoUploadTask(fileToUpload.getPath(), toUpload,  (Nostalgia) getApplication(), false);
        int result = uploader.execute().get(); //BLOCKING

        Log.d(TAG, "returned from uploader.get() with status: " + result);
        if(result == 200){
            Log.i(TAG, "Video upload of video: " + toUpload.get_id() + " successful, deleting original");
            FileUtils.deleteQuietly(fileToUpload);
            vidRepo.delete(toUpload);

        } else {
            Log.e(TAG, "ERROR UPLOADING FILE. result code: " + result);
            toUpload.setStatus("ERROR_UPLOAD");
            vidRepo.save(toUpload);
        }
        return result;
    }



    @Override
    public void onServiceConnected(ComponentName name, IBinder service) {

        Log.i(TAG, "componenet: " + name + " connected");
        // We've bound to LocalService, cast the IBinder and get LocalService instance
        NetworkConnectivityService.NetConnBinder binder = (NetworkConnectivityService.NetConnBinder) service;
        netConnSvc = binder.getService();

        bounded = true;

        boolean wifiConnected = netConnSvc.isWifiConnected();

        Log.i(TAG, "Bound network service reports wifi enabled status: " + wifiConnected);

    }

    @Override
    public void onServiceDisconnected(ComponentName name) {
        bounded = false;
        Log.i(TAG, "componenet: " + name + " DISconnected");
    }
}
