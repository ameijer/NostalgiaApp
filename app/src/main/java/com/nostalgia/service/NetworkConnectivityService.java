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

import java.lang.reflect.Method;

/**
 * Created by Aidan on 11/19/15.
 */
public class NetworkConnectivityService extends Service {
    private Context ctx;
    public NetworkConnectivityService(Context context){
        ctx = context;
    }
    private boolean wifiConnected = false;

    private String mConnectionType = "";
    public final static String NOT_CONNECTED = "NOT_CONNECTED";
    public final static String WIFI = "WIFI";
    public final static String GPRS = "GPRS";
    public final static String LTE = "LTE";


    // Binder given to clients
    private final IBinder mBinder = new NetConnBinder();

    /**
     * Class used for the client Binder.  Because we know this service always
     * runs in the same process as its clients, we don't need to deal with IPC.
     */
    public class NetConnBinder extends Binder {
        NetworkConnectivityService getService() {
            // Return this instance of NetworkConnectivityService so clients can call public methods
            return NetworkConnectivityService.this;
        }
    }

    public NetworkConnectivityService(){
        super();
    }

    public boolean isWifiConnected(){
        return wifiConnected;
    }

    @Override
    public void onCreate(){
        super.onCreate();
        IntentFilter filter = new IntentFilter();
        filter.addAction("android.net.conn.CONNECTIVITY_CHANGE");

        registerReceiver(receiver, filter);
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
    }

    public String getConnectionType(){
        checkConnectivity();

        return mConnectionType;
    }
    private final BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            ConnectivityManager conMan = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo netInfo = conMan.getActiveNetworkInfo();
            if (netInfo != null && netInfo.getType() == ConnectivityManager.TYPE_WIFI) {
                Log.d("WifiReceiver", "Have Wifi Connection");
                Intent i = new Intent("com.nostalgia.wifi.on");
                sendBroadcast(i);
                wifiConnected = true;
            }else {
                Log.d("WifiReceiver", "Don't have Wifi Connection");
                Intent i = new Intent("com.nostalgia.wifi.off");
                sendBroadcast(i);
                wifiConnected = false;
            }
        }
    };



    public boolean checkConnectivity() {
        boolean isConnected = false;

        ConnectivityManager connManager = (ConnectivityManager) ctx.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo mWifi = connManager.getActiveNetworkInfo();
        //android.net.NetworkInfo mobile = connManager.getActiveNetworkInfo();

        if( mWifi != null && mWifi.isConnected()){
            mConnectionType = mWifi.getTypeName();
            //Toast.makeText(ctx, mWifi.getTypeName(), Toast.LENGTH_SHORT).show();
            return true;
        } else {
            mConnectionType = NOT_CONNECTED;
        }

        try {
            TelephonyManager mgr = (TelephonyManager) ctx.getSystemService(Context.TELEPHONY_SERVICE);
            int networkType = mgr.getNetworkType();
            if (networkType == TelephonyManager.NETWORK_TYPE_GPRS){
                Class cmClass = Class.forName(connManager.getClass().getName());
                Method method = cmClass.getDeclaredMethod("getMobileDataEnabled");
                method.setAccessible(true);
                isConnected = (Boolean)method.invoke(connManager);
                if (isConnected){
                    mConnectionType = GPRS;
                    Toast.makeText(ctx, "GPRS", Toast.LENGTH_SHORT).show();
                    return isConnected;
                }
            }
            mConnectionType = NOT_CONNECTED;
            Toast.makeText(ctx, "No connection", Toast.LENGTH_SHORT).show();
            return isConnected;

        } catch (Exception e) {
            return false;
        }
    }
}
