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
import android.util.Log;

import com.couchbase.lite.Manager;
import com.couchbase.lite.android.AndroidContext;
import com.nostalgia.Constants;
import com.nostalgia.Nostalgia;
import com.nostalgia.SmartCookieManager;
import com.nostalgia.persistence.model.User;
import com.nostalgia.persistence.repo.UserRepository;

import java.net.CookieHandler;
import java.net.CookieStore;
import java.net.HttpCookie;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Created by alex on 1/14/16.
 */
//should receive onlogin, onupdate, onResetToken intents
//as well as performs periodic checks on both the exisitence of a logged in user, as well as confirming that the proper cookies are set
public class StreamingAuthenticationService extends Service{

    private static final String TAG = "StreamingAuthSvc";

    private static final Object MUTEX = new Object();

    private UserRepository userRepo;
    private Nostalgia app;
    private  SmartCookieManager manager = null;

    // Binder given to clients
    private final IBinder mBinder = new StreamingAuthenticationServiceBinder();

    /**
     * Class used for the client Binder.  Because we know this service always
     * runs in the same process as its clients, we don't need to deal with IPC.
     */
    public class StreamingAuthenticationServiceBinder extends Binder {
        StreamingAuthenticationService getService() {
            // Return this instance of NetworkConnectivityService so clients can call public methods
            return StreamingAuthenticationService.this;
        }
    }

    public StreamingAuthenticationService(){
        super();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId){

        User loggedIn = userRepo.getLoggedInUser();
        if(loggedIn != null) {
            setStreamingCookies(loggedIn);
        } else {
            Log.w(TAG, "No user detected at service mStart, cookies not loaded");
        }
        startTokenChecker();
        return START_STICKY;
    }

    //should receive onupdate, onResetToken intents
    @Override
    public void onCreate() {
        super.onCreate();

        IntentFilter filter = new IntentFilter();
        filter.addAction("com.nostalgia.LOGOUT");
        filter.addAction("com.nostalgia.update");
        filter.addAction("com.nostalgia.tokens.reset");

        registerReceiver(receiver, filter);
        app = (Nostalgia) getApplication();
        userRepo = app.getUserRepo();

        try {
            manager = (SmartCookieManager) CookieHandler.getDefault();

            if(manager == null) throw new NullPointerException();
        } catch (Exception e){
            manager = new SmartCookieManager(null);

            CookieHandler.setDefault(manager);

        }
    }

    private Thread checker;

    private void startTokenChecker() {


        checker = new Thread(){
            @Override
            public void run(){
                User loggedIn = userRepo.getLoggedInUser();
                if(loggedIn != null && !hasTokensLoaded(loggedIn)) {
                    setStreamingCookies(loggedIn);
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

    private void stopTokenChecker(){
        if(checker != null){
            checker.interrupt();
        }
        checker = null;
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
        stopTokenChecker();
    }


    private final BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            synchronized (MUTEX) {
                Log.i(TAG, "received intent: " + intent.getAction());
                User loggedIn = null;
                switch (intent.getAction()) {
                    case ("com.nostalgia.LOGOUT"):
                        wipeAllStreamingTokens();
                        stopSelf();
                        break;
                    case ("com.nostalgia.update"):
                        loggedIn = userRepo.getLoggedInUser();
                        if (loggedIn != null && !hasTokensLoaded(loggedIn))
                            setStreamingCookies(loggedIn);
                        break;
                    case ("com.nostalgia.tokens.reset"):
                        //forcibly reset tokens
                        wipeAllStreamingTokens();

                        loggedIn = userRepo.getLoggedInUser();
                        if(loggedIn != null) {
                            setStreamingCookies(loggedIn);
                        } else {
                            Log.e(TAG, "reset tokens, but unable to re-load new ones due to null user!");
                        }
                        break;
                    default:
                        break;
                }
            }
        }
    };

    private boolean wipeAllStreamingTokens() {
        CookieStore cookieJar =  manager.getCookieStore();
        return cookieJar.removeAll();
    }


    private boolean hasTokensLoaded(User checking){


        if(manager == null) return false;

        CookieStore cookieJar =  manager.getCookieStore();
        List<HttpCookie> allCookies = cookieJar.getCookies();

        HashSet<String> tokensToMatch = new HashSet<String>();

        for(String val : checking.getStreamTokens().values()){
            tokensToMatch.add(val);
        }

        for(HttpCookie cook : allCookies){
            String cookieValue = cook.getValue();

            if(tokensToMatch.remove(cookieValue)){
                Log.i(TAG, "System already has user value for cookie: " + cook.getName() + ", skipping...");
            }
        }

        if(tokensToMatch.size() > 0){
            for(String missing : tokensToMatch) {
                Log.i(TAG, "System did not have token with value: " + missing);
            }
            return false;
        } else {
            return true;
        }

    }


    public synchronized boolean setStreamingCookies(User hasCookieInfo){
        if(hasCookieInfo == null) return false;
        if(hasCookieInfo.getStreamTokens() == null) return false;


        CookieStore cookieJar =  manager.getCookieStore();

        ArrayList<HttpCookie> cookies = new ArrayList<HttpCookie>();
        for(String name : hasCookieInfo.getStreamTokens().keySet()){
            String value = hasCookieInfo.getStreamTokens().get(name);
            if(value == null) continue;
            HttpCookie cur = new HttpCookie(name, value);
            //  cur.setPath("/");
            cookies.add(cur);
            URL url = null;
            try {
                url = new URL(Constants.STREAMING_URL);
                URI forManager = url.toURI();
                cookieJar.add(forManager, cur);

            } catch (Exception e) {
                Log.e(TAG, "error adding cookie: " + name, e);
            }
            Log.i(TAG, "added cookie: " + name + " with value: " + value + " for address: " + url.toString());
        }

        return true;
    }


}
