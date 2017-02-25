package com.nostalgia.runnable;

/**
 * Created by alex on 11/7/15.
 */
abstract class DefaultServerCallTask<Result extends AsyncResult> extends AbstractServerCallTask<Result>{


    @Override
    protected void onError(String errorMessage) {
        //AppUtils.showToast(errorMessage);

    }

    @Override
    protected void onRequestFailed(Result r) {
        //AppUtils.showToast(r.getErrorMessage()); // show server-side generated message
    }
}
