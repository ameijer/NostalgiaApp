package com.nostalgia.service;

import android.content.Context;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;
import android.app.Dialog;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by aidan on 11/18/15.
 *
 * Will run quietly in the background. Only consumes power when there is a subscriber,
 * and has a grace period where it will remain on for a certain amount of time even when there are no subscribers.
 */
public class BackgroundLocationDetector implements
        LocationListener,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener {

    private Context mParentContext;
    private static final String TAG = "LocationUpdaterService";

    //INTERVAL if our app is the only one making GooglePlayServices api calls,
    // then this is the rate that they'll go out!
    private static final long INTERVAL = 1000 * 150;

    //FASTEST_INTERVAL our app can handle. GooglePlayServices might have other apps polling
    // GPS. We set a FASTEST_INTERVAL rate to throttle those updates.
    private static final long FASTEST_INTERVAL = 1000 * 90;

    private LocationRequest mLocationRequest;
    private GoogleApiClient mGoogleApiClient;
    private Location mCurrentLocation;
    private String mLastUpdateTime;

    private boolean locationServicesInitialized = false;

    private Integer mListeningActivityCount = 0;

    private ArrayList<BackgroundLocationListener> mLocationListeners = new ArrayList<BackgroundLocationListener>();

    public interface BackgroundLocationListener{
        void onLocationChange(Location location, String updatedTime);
    }

    /*
     * @param context parentContext responsible for googlePlayServices
     */
    public BackgroundLocationDetector(Context context){
        mParentContext = context;
    }

    /*
     * IntentService lifecycle callbacks;
     */
    public void initializeLocationServices(){
        createLocationRequest();

        //show error dialog if GoolglePlayServices not available
        if (!isGooglePlayServicesAvailable()) {
            return;
        }

        mGoogleApiClient = new GoogleApiClient.Builder(mParentContext)
                .addApi(LocationServices.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();

        locationServicesInitialized = true;
    }

    /*
     * Google API client callbacks and utils
     */
    @Override
    public void onConnected(Bundle bundle) {
        Log.d(TAG, "onConnected - isConnected ...............: " + mGoogleApiClient.isConnected());
        mCurrentLocation = LocationServices.FusedLocationApi.getLastLocation(
                mGoogleApiClient);

        if(null != mCurrentLocation){
            onLocationChanged(mCurrentLocation);
        }

        startLocationUpdates();
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        Log.d(TAG, "Connection failed: " + connectionResult.toString());
    }


    @Override
    public void onLocationChanged(Location location) {
        Log.d(TAG, "Firing onLocationChanged..............................................");
        mCurrentLocation = location;
        mLastUpdateTime = DateFormat.getTimeInstance().format(new Date());

        Iterator i = mLocationListeners.iterator();
        while(i.hasNext()){
            Log.d(TAG, "Broadcasting onLocationChange.");
            ((BackgroundLocationListener) i.next()).onLocationChange(mCurrentLocation, mLastUpdateTime);
        }
    }

    private void stopGoogleApiClient(){
        mGoogleApiClient.disconnect();
    }

    //End Google API client Callbacks and utils

    protected void createLocationRequest() {
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(INTERVAL);
        mLocationRequest.setFastestInterval(FASTEST_INTERVAL);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
    }

    protected void startLocationUpdates() {
        PendingResult<Status> pendingResult = LocationServices.FusedLocationApi.requestLocationUpdates(
                mGoogleApiClient, mLocationRequest, this);

        Log.d(TAG, "Location update started ..............: ");
    }

    protected void stopLocationUpdates() {
        LocationServices.FusedLocationApi.removeLocationUpdates(
                mGoogleApiClient, this);
        Log.d(TAG, "Location update stopped .......................");
    }



    /*
     * Interface for each listening activity
     */
    public boolean subscribe(BackgroundLocationListener l){
        boolean isListening;

        synchronized(timerIsRunning) {
            if (mLocationListeners.isEmpty() && !timerIsRunning) {
                //First listener, mStart services!
                startLocationServices();
            } else if (timerIsRunning) {
                stopTimerTask();
            }
        }
        if(mLocationListeners.contains(l)){
            isListening = true;
            Log.d(TAG, "BackgroundLocationDetector.subscribe() called multiple times by same object, without unsubscribe() in between calls.");
        } else {
            mListeningActivityCount = mListeningActivityCount+1;
            mLocationListeners.add(l);
            isListening = true;
        }

        return isListening;
    }

    public void unsubscribe(BackgroundLocationListener l) {
        if(mLocationListeners.contains(l)) {
            mListeningActivityCount = mListeningActivityCount - 1;
            mLocationListeners.remove(l);
        } else {
            Log.d(TAG, "BackgroundLocationDetector.stopListening() called by an object that wasn't listening.");
            mListeningActivityCount = 0;
        }

        if(mListeningActivityCount == 0){
            startPowerDownTimer();
        }
    }

    public boolean isListening(BackgroundLocationListener l){
        return mLocationListeners.contains(l);
    }

    /*
     * Singleton-like callbacks for active listening activities
     */
    public void onResume() {
        if (mGoogleApiClient.isConnected()) {
            startLocationUpdates();
            Log.d(TAG, "Location update resumed .....................");
        }
    }

    private void startLocationServices(){
        if(!locationServicesInitialized){
            initializeLocationServices();
        }

        if(mGoogleApiClient != null) {
            mGoogleApiClient.connect();
        }
    }

    public void onStop() {
        Log.d(TAG, "onStop fired ..............");
        mGoogleApiClient.disconnect();
        Log.d(TAG, "isConnected ...............: " + mGoogleApiClient.isConnected());
    }

    /*
     * Once all actvities have been paused or stopped, enter a low power state that isn't
     * hitting the LocationServices constantly.
     */
    private Timer timer;
    private TimerTask timerTask;
    private Boolean timerIsRunning = false;

    private void startPowerDownTimer(){
        synchronized(timerIsRunning) {
            //set a new Timer
            timer = new Timer();
            //initialize the TimerTask's job
            initializeTimerTask();
            //schedule the timer, after the first 5000ms the TimerTask will run every 10000ms
            timerIsRunning = true;
            timer.schedule(timerTask, 20000);
        }
    }

    private void stopTimerTask() {
        //stop the timer, if it's not already null
        if (timer != null) {
            timer.cancel();
            timer = null;
        }
    }

    private void initializeTimerTask() {
        timerTask = new TimerTask() {
            public void run() {
                stopLocationUpdates();
                stopGoogleApiClient();
                timerIsRunning = false;
            }
        };
    }

}
