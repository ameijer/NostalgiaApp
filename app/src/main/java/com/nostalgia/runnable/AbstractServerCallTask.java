package com.nostalgia.runnable;

import android.os.AsyncTask;
import android.util.Log;

import org.json.JSONException;

/**
 * Created by alex on 11/7/15.
 */
abstract class AbstractServerCallTask<Result extends AsyncResult> extends AsyncTask<Void, Void, AsyncResult<Result>> {


    private static final String TAG = "AbstractServerCallTask";

    @Override
    protected AsyncResult<Result> doInBackground(Void... arg0) {
        if (!AppUtils.isNetworkAvailable())
            return new AsyncResult("No network available. Please, check your connection");
        try {
            return new AsyncResult<Result>(callServerMethod());
        } catch (Exception e) {
            Log.e(TAG, "error calling server method", e);
            return null;
        }
    }

    abstract Result callServerMethod() throws  JSONException ;

    @Override
    protected void onPostExecute(AsyncResult<Result> asyncResult){
        if (asyncResult.isOk()){
            Result r = asyncResult.getResponse();
            if (r.isOk())
                onSuccess(r);
            else
                onRequestFailed(r); // we got to server, but somehing is wrong
        }
        else
            onError(asyncResult.getErrorMessage());
    }
    protected abstract void onError(String errorMessage);
    protected abstract void onRequestFailed(Result r) ;
    protected abstract void onSuccess(Result r);
}
