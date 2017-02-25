package com.nostalgia.runnable;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;

import java.io.IOException;
import java.net.InetAddress;

/**
 * Created by alex on 11/7/15.
 */
public class AppUtils {


    private static final String TAG = "AppUtils";
    private static boolean networkAvailable;

    public static boolean isNetworkAvailable() {
        try {
            return InetAddress.getByName("www.google.com").isReachable(30);
        } catch (IOException e) {
            Log.e(TAG, "Ioexception networking", e);
            return false;
        }
    }
}
