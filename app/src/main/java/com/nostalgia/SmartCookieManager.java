package com.nostalgia;

import android.util.Log;

import java.io.IOException;
import java.net.CookieHandler;
import java.net.CookieManager;
import java.net.CookiePolicy;
import java.net.CookieStore;
import java.net.HttpCookie;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Created by alex on 11/24/15.
 */
public class SmartCookieManager extends CookieHandler{

        private CookieManager dumbManager;

        /**
         * Constructs a new cookie manager using a specified cookie store and a
         * cookie policy.
         *
         * @param cookiePolicy
         *            a CookiePolicy to be used by cookie manager
         *            ACCEPT_ORIGINAL_SERVER will be used if the arg is null.
         */
        public SmartCookieManager(CookiePolicy cookiePolicy) {
            CookieStore myStore = new NostalgiaCookieStoreImpl();
            dumbManager = new CookieManager(myStore, cookiePolicy);
        }

        /**
         * Searches and gets all cookies in the cache by the specified uri in the
         * request header.
         *
         * @param uri
         *            the specified uri to search for
         * @param requestHeaders
         *            a list of request headers
         * @return a map that record all such cookies, the map is unchangeable
         * @throws IOException
         *             if some error of I/O operation occurs
         */
        @Override
        public Map<String, List<String>> get(URI uri,
                                             Map<String, List<String>> requestHeaders) throws IOException {
            URI generalized = null;
            try {
               generalized =  new URI(uri.getScheme() + "://" + uri.getHost());
            } catch (URISyntaxException e) {
                e.printStackTrace();
            }

            if(generalized == null) return null;

           final  Map<String, List<String>> dumbOutput = dumbManager.get(generalized, requestHeaders);
           Map<String, List<String>> preserved = null;
            if(dumbOutput == null){
                Log.e("SmartCookieManager", "dumboutput: " + dumbOutput);
            } else {
                Log.v("SmartCookieManager", "raw cookie contents: " + dumbOutput);
                 preserved = dumbOutput;
                return preserved;
            }
            return preserved;
        }


        /**
         * Sets cookies according to uri and responseHeaders
         *
         * @param uri
         *            the specified uri
         * @param responseHeaders
         *            a list of request headers
         * @throws IOException
         *             if some error of I/O operation occurs
         */
        @Override
        public void put(URI uri, Map<String, List<String>> responseHeaders) throws IOException {
           dumbManager.put(uri, responseHeaders);
        }

    public CookieManager getDumbManager() {
        return dumbManager;
    }

        /**
         * Gets current cookie store.
         *
         * @return the cookie store currently used by cookie manager.
         */
        public CookieStore getCookieStore() {
            return dumbManager.getCookieStore();
        }

}
