package com.nostalgia.service;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Binder;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.widget.Toast;

import com.couchbase.lite.CouchbaseLiteException;
import com.couchbase.lite.Database;
import com.couchbase.lite.Document;
import com.couchbase.lite.Manager;
import com.couchbase.lite.ReplicationFilter;
import com.couchbase.lite.SavedRevision;
import com.couchbase.lite.android.AndroidContext;
import com.couchbase.lite.replicator.Replication;
import com.nostalgia.Constants;
import com.nostalgia.Nostalgia;
import com.nostalgia.persistence.model.User;
import com.nostalgia.persistence.repo.Synchronize;
import com.nostalgia.persistence.repo.UserRepository;
import com.nostalgia.runnable.UserAttributeUpdaterThread;


import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by alex on 1/10/16.
 *
 * Handles Database synchronization with backend resources
 */
public class SyncService extends Service {
    private static final String TAG = SyncService.class.getName();
    private Context ctx;

    private static final Object MUTEX = new Object();
    private Synchronize sync;
    private UserRepository userRepository;
    private Database database;
    private Nostalgia app;
    private int numTries = 10;


    public SyncService(Context context){
        ctx = context;
    }
    private boolean wifiConnected = false;

    private String mConnectionType = "";
    public final static String NOT_CONNECTED = "NOT_CONNECTED";
    public final static String WIFI = "WIFI";
    public final static String GPRS = "GPRS";
    public final static String LTE = "LTE";


    // Binder given to clients
    private final IBinder mBinder = new SyncServiceBinder();

    /**
     * Class used for the client Binder.  Because we know this service always
     * runs in the same process as its clients, we don't need to deal with IPC.
     */
    public class SyncServiceBinder extends Binder {
        SyncService getService() {
            // Return this instance of NetworkConnectivityService so clients can call public methods
            return SyncService.this;
        }
    }

    public SyncService(){
        super();


    }

    public static final String PUSH_FILTER_NAME = "push_filter";
    ReplicationFilter filter = new ReplicationFilter() {
        @Override
        public boolean filter(SavedRevision revision, Map<String, Object> params) {
            Document pushing = revision.getDocument();
            String status = null;
            String type = null;
            try {
                type = pushing.getProperty("type").toString();
                status = pushing.getProperty("status").toString();

            } catch (Exception e){
                Log.i(TAG, "Error retrieving values");
            }
            try {
                if (type.equalsIgnoreCase("Video")) {
                    if (status.contains("PENDING") || status.contains("ERROR")) {
                        Log.i(TAG, "Filtering out push rep for video: " + pushing.getId() + " with status: " + status);
                        return false;
                    } else return true;

                }
            } catch (NullPointerException e) {
                Log.e(TAG, "ERROR IN PUSH FILTER FOR DOC: " + pushing.getId(), e);
                Log.e(TAG, "not pushing updates", e);
                return false;
            }


            return true;
        }
    };

    private void startReplicationSyncWithCustomCookie(String cookieValue, String preferredLocation) {

        if(preferredLocation == null){
            preferredLocation = "us_east";
        }

        if(cookieValue == null) return;

        String loc = Constants.SYNC_SERVER_URL;

        try {
            sync = new Synchronize.Builder(database, loc, true, PUSH_FILTER_NAME, false)
                    .cookieAuth(cookieValue)
                    .addChangeListener(getReplicationChangeListener())
                    .build();
            sync.start();
        }catch (Exception e){
            Log.e(TAG, "swallowed sync error: ", e);
        }



    }

    public void destroySync() {

        if(sync != null ) {
            int maxWait = 30;
            try {
                Thread.sleep(400);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            sync.stopReplications();

            while (maxWait > 0 && replicationStatus != Replication.ReplicationStatus.REPLICATION_STOPPED) {
                Log.i(TAG, "waiting on changes to push before shutting down, status: " + this.replicationStatus);
                maxWait--;
                try {
                    Thread.sleep(250);
                } catch (InterruptedException e) {
                    Log.e(TAG, "error sleeping thread", e);
                }
            }
            sync.destroyReplications();
            sync = null;
        }

    }


    public void stopSync() {

        if(sync != null && replicationStatus != Replication.ReplicationStatus.REPLICATION_STOPPED) {
            int maxWait = 30;
            try {
                Thread.sleep(400);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            sync.stopReplications();

            while (maxWait > 0 && replicationStatus != Replication.ReplicationStatus.REPLICATION_STOPPED) {
                Log.i(TAG, "waiting on changes to push before shutting down, status: " + this.replicationStatus);
                maxWait--;
                try {
                    Thread.sleep(250);
                } catch (InterruptedException e) {
                    Log.e(TAG, "error sleeping thread", e);
                }
            }

        }
    }
    private Replication.ReplicationStatus replicationStatus;
    public Replication.ReplicationStatus getReplicationStatus() {
        return replicationStatus;
    }
    private Replication.ChangeListener getReplicationChangeListener() {
        return new Replication.ChangeListener() {

            @Override
            public void changed(Replication.ChangeEvent event) {
                Replication replication = event.getSource();
                if (event.getError() != null) {
                    Throwable lastError = event.getError();
                    try {
                        if (lastError.getMessage().contains("existing change tracker")) {
                            Log.e("Replication Event", String.format("Sync error: %s:", lastError.getMessage()), lastError);
                        }
                    }catch (NullPointerException e) {
                       Log.w(TAG, "swallowed NPE in change tracker");
                    }
                    Log.w(TAG, "error synchronizing db", event.getError());
                }
                Log.d(TAG, "STATUS: " + replication.getStatus() + ": " + event.toString());
                replicationStatus =  replication.getStatus();
            }
        };
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId){

        //init database
        try {
            database = app.getDatabase();
        database.setFilter(PUSH_FILTER_NAME, filter);
            startSyncChecker();
        } catch (Exception e) {
            Log.e(TAG, "Error getting database", e);
            return START_FLAG_RETRY;
        }
        return START_STICKY;
    }

    @Override
    public void onCreate(){
        super.onCreate();

        IntentFilter filter = new IntentFilter();
        filter.addAction("android.net.conn.CONNECTIVITY_CHANGE");
        filter.addAction("com.nostalgia.LOGOUT");
        filter.addAction("com.nostalgia.update");
        filter.addAction("com.nostalgia.sync.restart");
        filter.addAction("com.nostalgia.sync.update");
        filter.addAction("com.nostalgia.sync.restartReplication");

        registerReceiver(receiver, filter);
        app = (Nostalgia) getApplication();
        userRepository = app.getUserRepo();

        ConnectivityManager conMan = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = conMan.getActiveNetworkInfo();
        wifiConnected = netInfo != null && netInfo.getType() == ConnectivityManager.TYPE_WIFI;


    }
    private Thread checker;
    private void startSyncChecker() {
        //update synchronization status based on user settings


        checker = new Thread(){
            @Override
            public void run(){
                User loggedIn = userRepository.getLoggedInUser();
                if(loggedIn != null) {
                    updateSync(loggedIn);
                }

                try {
                    Thread.sleep(20000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

            }
        };
        checker.start();

    }

    private void stopSyncChecker(){
        if(checker != null){
            checker.interrupt();
        }
        checker = null;
    }

    //changes replication status based on current conditions
    private synchronized void updateSync(User loggedIn) {

        String syncPref = loggedIn.getSettings().get("sync");
        if(syncPref == null){
            syncPref = "always";
            loggedIn.getSettings().put("sync", syncPref);
            try {
                Map<String, String> changed = new HashMap<>();
                changed.put("sync", syncPref);
                UserAttributeUpdaterThread attr = new UserAttributeUpdaterThread(loggedIn.get_id(), UserAttributeUpdaterThread.Attribute.SETTING, changed);
                attr.start();
                userRepository.save(loggedIn);
            } catch (Exception e) {
                Log.e(TAG, "error saving user", e);
            }

        }

        switch(syncPref){
            case("off"):
                Log.i(TAG, "user has sync set to off");
                stopSync();
                break;
            case("wifi"):
                if(wifiConnected){
                    if(replicationStatus == Replication.ReplicationStatus.REPLICATION_STOPPED || replicationStatus == Replication.ReplicationStatus.REPLICATION_OFFLINE || replicationStatus == null) {
                        startReplicationSyncWithCustomCookie(loggedIn.getSyncToken(), loggedIn.getHomeRegion());
                    }
                } else {
                    stopSync();
                }
                break;
            case("always"):
            default:
                //always on
                if(replicationStatus == Replication.ReplicationStatus.REPLICATION_STOPPED || replicationStatus == Replication.ReplicationStatus.REPLICATION_OFFLINE || replicationStatus == null) {
                    startReplicationSyncWithCustomCookie(loggedIn.getSyncToken(), loggedIn.getHomeRegion());
                }
                break;
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
        stopSyncChecker();
    }


    private final BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            synchronized (MUTEX) {
                Log.i(TAG, "received intent: " + intent.getAction());
                User loggedIn = null;
                switch (intent.getAction()) {
                    case ("android.net.conn.CONNECTIVITY_CHANGE"):
                        ConnectivityManager conMan = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
                        NetworkInfo netInfo = conMan.getActiveNetworkInfo();
                        wifiConnected = netInfo != null && netInfo.getType() == ConnectivityManager.TYPE_WIFI;
                        loggedIn = userRepository.getLoggedInUser();
                        if (loggedIn != null)
                            updateSync(loggedIn);
                        break;
                    case ("com.nostalgia.LOGOUT"):
                        destroySync();
                        purgeDB();
                        break;
                    case("com.nostalgia.sync.update"):
                    case ("com.nostalgia.update"):
                        loggedIn = userRepository.getLoggedInUser();
                        if (loggedIn != null) {
                            updateSync(loggedIn);
                        } else {
                            //mStart replication manually
                            String sessionToken = intent.getStringExtra("sessionToken");
                            String region = intent.getStringExtra("region");
                            if(region == null || sessionToken == null) {
                                Log.e(TAG, "unable to mStart replication - missing information!");
                                return;
                            }
                            startReplicationSyncWithCustomCookie(sessionToken, region);
                        }
                        break;
                    case ("com.nostalgia.sync.restart"):
                        restartSync();
                        break;
                    case ("com.nostalgia.sync.restartReplication"):
                        restartReplication();
                        break;
                    default:
                        break;
                }
            }

        }
    };

    private void restartReplication() {
        sync.restart();
    }

    private void purgeDB() {
        try {

            app.purgeDatabase();

        } catch (CouchbaseLiteException e) {
            Log.e(TAG, "error deleting db", e);
        }
    }

    public boolean restartSync(){
        if(sync != null){
            Log.i(TAG, "Restart: destroying existing replications...");
            destroySync();
            Log.i(TAG, "Replications destroyed");
        }

        Log.i(TAG, "Feteching logged in user... ");
        User loggedIn = userRepository.getLoggedInUser();
        if(loggedIn == null){
            Log.e(TAG, "ERROR - UNABLE TO RESTART SYNC - NO VALID USER FOUND");
            return false;
        } else {
            Log.i(TAG, "starting sync for user: " + loggedIn.getUsername());
        }

        updateSync(loggedIn);
        return true;
    }

}
