package com.nostalgia.persistence.repo;

import com.couchbase.lite.Database;
import com.couchbase.lite.auth.Authenticator;
import com.couchbase.lite.auth.AuthenticatorFactory;
import com.couchbase.lite.replicator.Replication;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Calendar;
import java.util.Date;

/**
 * Created by alex on 11/10/15.
 */
public class Synchronize {

    public Replication pullReplication;
    public Replication pushReplication;

    private boolean facebookAuth;
    private boolean basicAuth;
    private boolean cookieAuth;

    public void restart() {
        if(pullReplication != null) {
            pullReplication.restart();
        }

        if(pushReplication != null){
            pushReplication.start();
        }


    }

    public static class Builder {
        public Replication pullReplication;
        public Replication pushReplication;

        private boolean facebookAuth;
        private boolean basicAuth;
        private boolean cookieAuth;

        public Builder(Database database, String url, Boolean continuousPull, String push_filter_name, boolean push) {

            if (pullReplication == null && pushReplication == null) {

                URL syncUrl;
                try {
                    syncUrl = new URL(url);
                } catch (MalformedURLException e) {
                    throw new RuntimeException(e);
                }

                pullReplication = database.createPullReplication(syncUrl);
                if (continuousPull == true) {
                    pullReplication.setContinuous(true);
                }

                if(push) {
                    pushReplication = database.createPushReplication(syncUrl);
                    pushReplication.setContinuous(true);


                    pushReplication.setFilter(push_filter_name);
                }
                //            Map<String, Object> params = new HashMap<String, Object>();
//            params.put("name", "Waldo");
//            push.setFilterParams(params);


            }
        }


        public Builder facebookAuth(String token) {

            Authenticator facebookAuthenticator = AuthenticatorFactory.createFacebookAuthenticator(token);

            pullReplication.setAuthenticator(facebookAuthenticator);

            if(pushReplication != null) {
                pushReplication.setAuthenticator(facebookAuthenticator);
            }
            return this;
        }

        public Builder basicAuth(String username, String password) {

            Authenticator basicAuthenticator = AuthenticatorFactory.createBasicAuthenticator(username, password);

            pullReplication.setAuthenticator(basicAuthenticator);

            if(pushReplication != null) {
                pushReplication.setAuthenticator(basicAuthenticator);
            }
            return this;
        }

        public Builder cookieAuth(String cookieValue) {

            String cookieName = "SyncGatewaySession";
            boolean isSecure = false;
            boolean httpOnly = false;

            // expiration date - 1 day from now
            Calendar cal = Calendar.getInstance();
            cal.setTime(new Date());
            int numDaysToAdd = 1;
            cal.add(Calendar.DATE, numDaysToAdd);
            Date expirationDate = cal.getTime();

            pullReplication.setCookie(cookieName, cookieValue, "/", expirationDate, isSecure, httpOnly);

            if(pushReplication != null)
            pushReplication.setCookie(cookieName, cookieValue, "/", expirationDate, isSecure, httpOnly);

            return this;
        }

        public Builder addChangeListener(Replication.ChangeListener changeListener) {
            pullReplication.addChangeListener(changeListener);

            if(pushReplication != null) {
                pushReplication.addChangeListener(changeListener);
            }
            return this;
        }

        public Synchronize build() {
            return new Synchronize(this);
        }

    }

    private Synchronize(Builder builder) {
        pullReplication = builder.pullReplication;
        pushReplication = builder.pushReplication;

        facebookAuth = builder.facebookAuth;
        basicAuth = builder.basicAuth;
        cookieAuth = builder.cookieAuth;
    }

    public void start() {
        pullReplication.start();
        if(pushReplication != null) {
            pushReplication.start();
        }
    }

    public void destroyReplications() {
        pullReplication.stop();

        if(pushReplication != null) {
            pushReplication.stop();
            pushReplication.deleteCookie("SyncGatewaySession");
            pushReplication = null;
        }
        pullReplication.deleteCookie("SyncGatewaySession");


        pullReplication = null;

    }

    public void stopReplications() {
        pullReplication.stop();

        if(pushReplication != null) {
            pushReplication.stop();
        }

    }

}
