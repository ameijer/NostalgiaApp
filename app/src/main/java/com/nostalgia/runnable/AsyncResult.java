package com.nostalgia.runnable;

/**
 * Created by alex on 11/7/15.
 */
public class AsyncResult<T>{

    T mResponse;
    boolean mOk;
    String mErrorMessage;

    T getResponse(){
        return mResponse;
    }

    boolean isOk(){
        return mOk;
    }

    String getErrorMessage(){
        return mErrorMessage;
    }

    public AsyncResult (T response){
        mErrorMessage = "";
        mOk = true;
        mResponse = response;
    }

    public AsyncResult (String errorMessage){
        mErrorMessage = errorMessage;
        mOk = false;
    }

}
