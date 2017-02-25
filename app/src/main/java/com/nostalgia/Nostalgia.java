package com.nostalgia;

import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.support.multidex.MultiDex;
import android.util.Log;
import android.widget.Toast;

import com.couchbase.lite.CouchbaseLiteException;
import com.couchbase.lite.Database;
import com.couchbase.lite.Document;
import com.couchbase.lite.Emitter;
import com.couchbase.lite.Manager;
import com.couchbase.lite.Mapper;
import com.couchbase.lite.Query;
import com.couchbase.lite.QueryEnumerator;
import com.couchbase.lite.QueryRow;
import com.couchbase.lite.ReplicationFilter;
import com.couchbase.lite.SavedRevision;
import com.couchbase.lite.View;
import com.couchbase.lite.android.AndroidContext;
import com.facebook.CallbackManager;
import com.couchbase.lite.replicator.Replication;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.nostalgia.persistence.model.User;
import com.nostalgia.persistence.repo.LocationRepository;
import com.nostalgia.persistence.repo.MediaCollectionRepository;
import com.nostalgia.persistence.repo.Synchronize;
import com.nostalgia.persistence.repo.UserRepository;
import com.nostalgia.persistence.repo.VideoRepository;

import com.nostalgia.service.BackgroundLocationDetector;
import com.nostalgia.service.LocationUpdaterService;
import com.nostalgia.service.NetworkConnectivityService;
import com.nostalgia.service.StreamingAuthenticationService;
import com.nostalgia.service.SyncService;
import com.nostalgia.service.VideoUploadService;
import com.vuescape.nostalgia.R;

import org.acra.ACRA;
import org.acra.ReportingInteractionMode;
import org.acra.annotation.ReportsCrashes;
import org.acra.sender.HttpSender;
import org.apache.commons.io.FileUtils;
import java.io.File;
import java.net.CookieHandler;
import java.net.CookieStore;
import java.net.HttpCookie;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Created by alex on 10/30/15.
 */
@ReportsCrashes(
        mode = ReportingInteractionMode.TOAST,
        httpMethod = HttpSender.Method.PUT,
        reportType = HttpSender.Type.JSON,
        formUri = "https://www.bugreport.vuescape.io/acra-myapp/_design/acra-storage/_update/report",
        formUriBasicAuthLogin = "REDACTED",
        formUriBasicAuthPassword = "REDACTED",
        resToastText = R.string.crash_dialog
)
public class Nostalgia extends Application implements
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener, UserRepository.UserEventListener,
        BackgroundLocationDetector.BackgroundLocationListener{

    private Manager manager = null;
    private Database database = null;
    private CallbackManager fbCallbackManager;
    private View allDocumentsView;

    public static final String BACKUP_STREAMING_URL = "http://exoatmospherics.com";
    private UserRepository userRepo;
    private VideoRepository vidRepo;
    private MediaCollectionRepository collRepo;
    private LocationRepository locationRepository;


    //deletes all files in cache
    public boolean flushCache(){
        File sdCard = getFilesDir();
        File dir = new File(sdCard, "cache");

        if(!dir.exists()){
            Log.w(TAG, "no cache dir found, skipping flush");
            return false;
        }


        Iterator<File> iter = FileUtils.iterateFiles(dir, null, true);

        while(iter.hasNext()){
            File toProcess= iter.next();

                Log.i(TAG, "purging cache file: " + toProcess.getName());
                FileUtils.deleteQuietly(toProcess);

        }
        return true;
    }

    public UserRepository getUserRepo() {
        return userRepo;
    }

    public String getDeviceId() {
        return deviceId;
    }

    String deviceId ;

    private static final String TAG = "Nostalgia App";
    private static final String DB_NAME = "nostalgiadb";

    private static final String KEY_IN_RESOLUTION = "is_in_resolution";

    /**
     * Request code for auto Google Play Services error resolution.
     */
    protected static final int REQUEST_CODE_RESOLUTION = 1;

    public GoogleApiClient getmGoogleApiClient() {
        return mGoogleApiClient;
    }

    /**
     * Google API client.
     */
    private GoogleApiClient mGoogleApiClient;

    /**
     * Determines if the client is in a resolution state, and
     * waiting for resolution intent to return.
     */
    private boolean mIsInResolution;

    /**
     *
     *
     */

    private BackgroundLocationDetector mBackgroundLocationDetector;

    private Location mLastKnownLocation = null;
    private String mLastUpdatedTime = null;

    private boolean locationUpdaterStarted = false;
    @Override
    public void onLocationChange(Location location, String updatedTime) {

        if(!locationUpdaterStarted){
            startService(new Intent(this, LocationUpdaterService.class));
            locationUpdaterStarted = true;
        }

        mLastKnownLocation = location;
        mLastUpdatedTime = updatedTime;
    }

    public LocationRepository getLocationRepository() {
        return locationRepository;
    }

    public MediaCollectionRepository getCollRepo() {
        return collRepo;
    }

    public Database getDatabase() {
        return database;
    }

    public void purgeDatabase() throws CouchbaseLiteException {
        Query query = database.createAllDocumentsQuery();
        query.setAllDocsMode(Query.AllDocsMode.ALL_DOCS);
        QueryEnumerator result = query.run();
        for (Iterator<QueryRow> it = result; it.hasNext(); ) {
            QueryRow row = it.next();

            Document toWipe = row.getDocument();
            toWipe.purge();
        }
        updateCurrentUserStatus();
    }

    public void restartReplication() {
        Intent intent = new Intent();
        intent.addFlags(Intent.FLAG_INCLUDE_STOPPED_PACKAGES);
        intent.setAction("com.nostalgia.sync.restartReplication");

        sendBroadcast(intent);
    }


    public enum UserStatus{NOT_LOGGED_IN, LOGGED_IN, WAITING_FOR_SERVER}
    //= User.NOT_LOGGED_IN;
    private UserStatus currentUserStatus = UserStatus.WAITING_FOR_SERVER;

    @Override
    public void onCreate() {
        super.onCreate();



        deviceId = Installation.id(this.getApplicationContext());
        Log.d("NOSTALGIA APP", "Starting application services");

        mBackgroundLocationDetector = new BackgroundLocationDetector(this);
        mBackgroundLocationDetector.subscribe(this);

        //init database
        try {
            manager = new Manager(new AndroidContext(this), Manager.DEFAULT_OPTIONS);
            database = manager.getDatabase(DB_NAME);

        } catch (Exception e) {
            Log.e(TAG, "Error getting database", e);
            return;
        }

        init_repos();

        restartServices();


    }

    private void restartServices() {
        //mStart sync service
        Intent syncIntent = new Intent(this, SyncService.class);
        stopService(syncIntent);
        syncIntent.putExtra("dbname", DB_NAME);
        startService(syncIntent);

        //mStart streaming token set service
        Intent streamingTokenIntent = new Intent(this, StreamingAuthenticationService.class);
        stopService(streamingTokenIntent);
        startService(streamingTokenIntent);
    }

    private void init_repos() {
        allDocumentsView = database.getView("all_docs_by_channel");
        allDocumentsView.setMap(new Mapper() {
            @Override
            public void map(Map<String, Object> document, Emitter emitter) {

                Object type = document.get("type");
                if (type == null) return;
                String fullId = document.get("_id").toString();

                String channel = fullId.substring(0, fullId.indexOf('-'));
                emitter.emit(channel, document.get("type"));

                // }
            }
        }, "1.0");


        userRepo = new UserRepository(database, this);
        userRepo.setCallback(this);

        collRepo = new MediaCollectionRepository(database, this);

        vidRepo = new VideoRepository(database, this);
        try {
            removeObsoleteDocs();
        } catch (Exception e) {
            Log.e(TAG, "error dumping old docs", e);
        }
        updateCurrentUserStatus();

        startService(new Intent(this, VideoUploadService.class));
//        startService(new Intent(this, NetworkConnectivityService.class)); //started by videoupload
        locationRepository = new LocationRepository(database, this);
        locationRepository.updateViews();


// The following line triggers the initialization of ACRA
        ACRA.init(this);
    }

    private void updateCurrentUserStatus() {
        User existing = userRepo.getLoggedInUser();
        if(existing != null){
            //USER IS LOGGED IN.
            currentUserStatus = UserStatus.LOGGED_IN;
            if(!locationUpdaterStarted){
                startService(new Intent(this, LocationUpdaterService.class));
                locationUpdaterStarted = true;
            }
        } else {
            currentUserStatus = UserStatus.NOT_LOGGED_IN;
        }
    }

    public UserStatus getCurrentUserStatus(){
        return currentUserStatus;
    }

    public CallbackManager getFbCallbackManager(){
        return fbCallbackManager;
    }

    private void retryConnecting() {
        mIsInResolution = false;
        if (!mGoogleApiClient.isConnecting()) {
            mGoogleApiClient.connect();
        }
    }

    /**
     * Called when {@code mGoogleApiClient} is connected.
     */
    @Override
    public void onConnected(Bundle connectionHint) {
        Log.i(TAG, "GoogleApiClient connected");
    }


    public Location getLocation(){
        return mLastKnownLocation;

    }

    /**
     * Called when {@code mGoogleApiClient} connection is suspended.
     */
    @Override
    public void onConnectionSuspended(int cause) {
        Log.i(TAG, "GoogleApiClient connection suspended");
        retryConnecting();
    }

    /**
     * Called when {@code mGoogleApiClient} is trying to connect but failed.
     * Handle {@code result.getResolution()} if there is a resolution
     * available.
     */
    @Override
    public void onConnectionFailed(ConnectionResult result) {
        Log.i(TAG, "GoogleApiClient connection failed: " + result.toString());

        if (mIsInResolution) {
            return;
        }
        mIsInResolution = true;
        retryConnecting();
    }

    @Override
    protected void attachBaseContext(Context base){
        super.attachBaseContext(base);
        MultiDex.install(this);

    }

    @Override
    public void onUpdate(String loggingInId) {
        User loggingIn = userRepo.findOneById(loggingInId);
        if(loggingIn == null){
            Log.e(TAG, "error retrieving found user");
        }
      
        Intent intent = new Intent();
        intent.addFlags(Intent.FLAG_INCLUDE_STOPPED_PACKAGES);
        intent.setAction("com.nostalgia.update");
        intent.putExtra("id", loggingInId);
        sendBroadcast(intent);




    }

    private void removeObsoleteDocs() {
        Log.i(TAG, "checking for obsolete docs");
        User hasDocs = userRepo.getLoggedInUser();
        if(hasDocs == null) return;

        List<String> targetChannels = hasDocs.getAdmin_channels();

        List<String> currentChannels =  this.getAllSubscribedChannels();

        for(String channel : currentChannels){
            if(!targetChannels.contains(channel)){
                //delete docs that we no longer are tracking
                Document toDelete = findDocumentForChannel(channel);
                if(toDelete == null) continue;

                //only delete documents that we don't own
                Object raw = toDelete.getProperty("creatorId");

                if(raw == null){
                    raw = toDelete.getProperty("ownerId");
                }

                if(raw != null){
                    String ownerId = raw.toString();

                    if(hasDocs.get_id().equals(ownerId)){
                        //skip the deletion
                        continue;
                    }
                }

                try {
                    Log.w(TAG, "DELETEING DOC: " + toDelete.getId());
                    toDelete.purge();
                } catch (Exception e) {
                    Log.e(TAG, "error deleting documents", e);
                }

            }
        }

    }

    private Document findDocumentForChannel(String channel) {
        Query query = database.getView("all_docs_by_channel").createQuery();

        List<Object> keys = new ArrayList<Object>();
        keys.add(channel);
        query.setKeys(keys);

        QueryEnumerator result = null;
        try {
            result = query.run();
        } catch (CouchbaseLiteException e) {
            Log.e(TAG, "error querying for logged in user");
            return null;
        }


        for (Iterator<QueryRow> it = result; it.hasNext(); ) {
            return it.next().getDocument();

        }
        return null;
    }

    @Override
    public void onLogout() {
        Intent intent = new Intent();
        intent.addFlags(Intent.FLAG_INCLUDE_STOPPED_PACKAGES);
        intent.setAction("com.nostalgia.LOGOUT");
        sendBroadcast(intent);
    }

    @Override
    public void onChannelUpdate(List<String> channels) {
        if(locationRepository != null) {
            locationRepository.updateViews();
        }
    }

    public VideoRepository getVidRepo() {
        return vidRepo;
    
    }


    public List<String> getAllSubscribedChannels() {
        Query query = database.getView("all_docs_by_channel").createQuery();

        QueryEnumerator result = null;
        try {
            result = query.run();
        } catch (CouchbaseLiteException e) {
            Log.e(TAG, "error querying for logged in user");
            return null;
        }


        ArrayList<String> all = new ArrayList<String>();
        for (Iterator<QueryRow> it = result; it.hasNext(); ) {
            all.add(it.next().getKey().toString());

        }
        return all;


    }
}
