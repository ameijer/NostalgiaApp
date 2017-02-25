package com.nostalgia.service;

import android.app.IntentService;
import android.content.Intent;
import android.location.Location;
import android.util.Log;

import com.nostalgia.Nostalgia;
import com.nostalgia.persistence.model.User;
import com.nostalgia.persistence.repo.UserRepository;
import com.nostalgia.runnable.UserLocationUpdaterThread;

import org.geojson.Point;

/**
 * Created by alex on 11/17/15.
 */
public class LocationUpdaterService extends IntentService {

    Nostalgia app;
    private static final String TAG = "LocationUpd8terService";
    UserRepository userRepo;

    /**
     * Creates an IntentService.  Invoked by your subclass's constructor.
     *
     * @param name Used to name the worker thread, important only for debugging.
     */
    public LocationUpdaterService(String name) {
        super(name);

    }

    public LocationUpdaterService() {
        super(TAG);

    }

    public void triggerUpdate(){
        User current = userRepo.getLoggedInUser();
        Location currentLocation = null;

        if(current != null) {
            currentLocation = app.getLocation();
            //TODO get location from gps
            Point point = null;

            if(currentLocation != null) {
                point = new Point(currentLocation.getLongitude(), currentLocation.getLatitude());
            }

            //trigger update
            try {
                UserLocationUpdaterThread updater = new UserLocationUpdaterThread(current.get_id(), point);
                updater.start();

                updater.join();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
    @Override
    public void onCreate(){
        super.onCreate();
        app = (Nostalgia) this.getApplication();
        userRepo = app.getUserRepo();
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        while(true){
            User current = userRepo.getLoggedInUser();
            if(current != null) {
                if ((current.getLastLocationUpdate() < (System.currentTimeMillis() - 70000))) {
                    this.triggerUpdate();
                }
            }
            try {
                Thread.sleep(60000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
