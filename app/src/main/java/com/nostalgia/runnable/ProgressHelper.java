package com.nostalgia.runnable;

import android.app.Activity;
import android.os.Handler;
import android.util.Log;
import android.widget.TextView;

import com.nostalgia.view.CaptureButton;

import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * Created by alex on 10/31/15.
 */
public class ProgressHelper {

    public static float TIME_LIMIT = 21f;

    private float currentProgress = 0;
    private float previousProgress = 0;
    private Handler handle=new Handler();
    private final CaptureButton button;
    private final Activity activity;
    public static final String TAG = "Progress Helper";
    public long mStart;

    private TextView countdown;
    private float currentCountdown = TIME_LIMIT;
    private TimedButtonEventListener callback;

    public void setButtonRingColor(int buttonRingColor) {
        this.getProgressColorRunnable(activity, activity.getResources().getColor(buttonRingColor)).run();
    }

    public void setButtonCenter(int buttonCenter) {
        this.getColorRunnable(activity, activity.getResources().getColor(buttonCenter)).run();
    }

    public interface TimedButtonEventListener{
        void onTimerStart();
        void onTimerStop(long duration);
        //public void onTimerCancel();
    }

    public void setTimerListener(TimedButtonEventListener listener){
        this.callback = listener;
    }

    public ProgressHelper(TextView countdown, CaptureButton button, Activity activity) {
        this.button = button;
        this.activity = activity;
        this.countdown = countdown;
        updateCountdownText(currentCountdown);
    }

    private void updateCountdownText(float d){
        String pretty = "";

        if(d == (long) d) {
            pretty =  String.format("%d",(long)d);
        } else {
            pretty = String.format("%s",d);
        }

        countdown.setText(pretty);
    }

    private int secondsLeft(float progress){
        return (int) Math.ceil((100f - progress)/100f * TIME_LIMIT);
    }

    private Runnable mUiCountdownThread;

    TimerTask d;
    private int countdownSec;

    private Timer mTimer;
    private TimerTask mTimerTask;
    private void startCountdown(){
        mTimer = new Timer();
        initializeTimerTask();
        mTimer.schedule(mTimerTask, 50, 50);
    }

    private void stopCountdown(){
        if(null != mTimer) {
            mTimer.purge();
            mTimer.cancel();
        }

        /*
         * Let any timertasks run out.
         */
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        reset();
    }

    private final Handler handler = new Handler();
    public void initializeTimerTask() {
        mTimerTask = new TimerTask() {
            @Override
            public void run() {
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        currentProgress += 0.25;
                        //get the current timeStamp
                        //show the toast
                        countdownSec = secondsLeft(currentProgress);

                        if (currentCountdown != countdownSec) {
                            currentCountdown = countdownSec;
                            updateCountdownText(currentCountdown);
                        }

                        button.setPercentageDecimal(currentProgress / 100f);

                        if (currentProgress >= 100) {
                            //handle.postDelayed(getRunnable(activity),50);
                            completeTask();
                        }
                    }
                });
            }
        };
    }


    private Runnable getColorRunnable(final Activity activity, final int color){
        return new Runnable() {
            @Override
            public void run() {
                activity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                    }
                });
            }
        };
    }

    private Runnable getProgressColorRunnable(final Activity activity, final int color){
        return new Runnable() {
            @Override
            public void run() {
                activity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                    }
                });
            }
        };
    }

    public void clear(){
        if(null == mTimer){
            reset();
        } else {
            stopCountdown();
        }
    }

    public void completeTask(){
        //currentProgress = 100;
        long duration = System.currentTimeMillis() - mStart;

        if(null != callback) {
            callback.onTimerStop(duration);
        }

        getColorRunnable(activity, activity.getResources().getColor(android.R.color.holo_green_light)).run();
        getProgressColorRunnable(activity, activity.getResources().getColor(android.R.color.holo_green_light)).run();

        if(null != mTimer) {
            mTimer.purge();
            mTimer.cancel();
        }

        button.setPercentageDecimal(0 / 100f);
    }

    public void cancelCountdown(){
        stopCountdown();
    }

    private void reset(){
        mTimer = null;
        mTimerTask = null;

        currentProgress = 0;
        currentCountdown = TIME_LIMIT;
        getColorRunnable(activity, activity.getResources().getColor(android.R.color.darker_gray)).run();
        getProgressColorRunnable(activity, activity.getResources().getColor(android.R.color.holo_red_dark)).run();
        updateCountdownText(currentCountdown);
        button.setPercentageDecimal(0 / 100f);
    }

    private Runnable mCountdownThread;
    public void startDeterminate() {

        try {
        } catch (NullPointerException e){
            Log.i(TAG, "NPE on center icon", e);
        }
        currentProgress = 0;
        getColorRunnable(activity, activity.getResources().getColor(android.R.color.darker_gray)).run();
        getProgressColorRunnable(activity, activity.getResources().getColor(android.R.color.holo_red_dark)).run();

        //mCountdownThread = getRunnable(activity);
        //mCountdownThread.run();

        startCountdown();
        mStart = System.currentTimeMillis();

        if(callback != null){
            callback.onTimerStart();
        }

    }


}
