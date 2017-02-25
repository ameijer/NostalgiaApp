package com.nostalgia.runnable;

/**
 * Created by alex on 10/30/15.
 */
import android.os.AsyncTask;


import com.nostalgia.Nostalgia;

public class LoadingTask extends AsyncTask<String, Integer, Integer> {

    public interface LoadingTaskFinishedListener {
        void onTaskFinished(); // If you want to pass something back to the listener add a param to this method
    }

    private final int TOTAL_DELAY = 4000;
    public static final String TAG = "LoadingTask";
    // This is the progress bar you want to update while the task is in progress
    private final LoadingTaskFinishedListener finishedListener;



    private final Nostalgia app;

    /**
     * A Loading task that will load some resources that are necessary for the app to mStart
     * @param finishedListener - the listener that will be told when this task is finished
     */
    public LoadingTask(LoadingTaskFinishedListener finishedListener, Nostalgia app) {
        super();

        this.finishedListener = finishedListener;
        this.app = app;
    }

    @Override
    protected Integer doInBackground(String... params) {
//        Log.i("Tutorial", "Starting task with url: "+params[0]);

            try {
                Thread.sleep(TOTAL_DELAY);
            } catch (Exception e) {
                e.printStackTrace();
            }

        // Perhaps you want to return something to your post execute
        return 1234;
    }

    @Override
    protected void onProgressUpdate(Integer... values) {
        super.onProgressUpdate(values);

    }

    @Override
    protected void onPostExecute(Integer result) {
        super.onPostExecute(result);
        finishedListener.onTaskFinished(); // Tell whoever was listening we have finished
    }
}
