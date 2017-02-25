package com.nostalgia.controller;

import android.animation.Animator;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.IntentFilter;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Typeface;
import android.location.LocationManager;
import android.provider.Settings;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.app.FragmentManager;
import android.content.Intent;
import android.graphics.Point;
import android.graphics.PointF;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.davemorrissey.labs.subscaleview.ImageSource;
import com.davemorrissey.labs.subscaleview.SubsamplingScaleImageView;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.nostalgia.controller.introduction.IntroductionActivity;
import com.nostalgia.persistence.caching.FontCache;
import com.nostalgia.controller.login.LoginFragment;
import com.nostalgia.persistence.model.User;
import com.nostalgia.runnable.LoadingTask;
import com.nostalgia.controller.capturemoment.MainCaptureActivity;
import com.nostalgia.Nostalgia;
import com.nostalgia.view.AutoResizeTextView;
import com.vuescape.nostalgia.R;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.util.Iterator;

/**
 * Created by alex meijer on 10/30/15.
 */

public class SplashActivity extends AppCompatActivity implements LoadingTask.LoadingTaskFinishedListener, SubsamplingScaleImageView.OnImageEventListener, LoginFragment.OnLoginConfirmListener {
    public static final String TAG = "splash screen";
    private static final long WEEK_IN_MILLIS = 1000 * 60 * 60 * 24 * 7;

    private Nostalgia app;
    private final SplashActivity me = this;
    private LinearLayout titleHolder;
    private AutoResizeTextView titleView;
    private boolean imageRendered = false;
    private static int SPLASH_TIME_OUT = 3000;
    LoadingTask mTask;


    SubsamplingScaleImageView myImage;
    ImageView backgroundImage;

    private FragmentManager mFragmentManager;

    public void animateBackground() {

        final PointF center = myImage.getCenter();
        center.x += mDisplayWidth;

        /*
         * ajh ImageView animation -
         */
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                myImage.animateCenter(center)
                        .withDuration(25000)
                        .withInterruptible(false)
                        .start();
            }
        });



    }

    private boolean mIsLoggedIn = false;

    private User mLoggedIn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        //boring init stuff
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_splash_screen);

        app = ((Nostalgia) getApplication());
        mLoggedIn = app.getUserRepo().getLoggedInUser();
        if(mLoggedIn == null){
            startSilentLoginProcess();
        }


        /*
         * ajh ImageView animation -
         */
        myImage  = (SubsamplingScaleImageView) findViewById(R.id.background_image);
        myImage.setImage(ImageSource.resource(R.drawable.seascape));

        myImage.setMinimumScaleType(SubsamplingScaleImageView.SCALE_TYPE_CENTER_CROP);
        myImage.setDebug(true);
        myImage.setOnImageEventListener(this);



        /*
         * ajh ImageView animation +
         */

        DisplayMetrics displayMetrics = getResources().getDisplayMetrics();


        DisplayMetrics metrics = getResources().getDisplayMetrics();

        Display display = getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        mDisplayWidth = size.x;
        mDisplayHeight = size.y;

        checkGPS(this);
        scrubCache();
    }//oncreate

    private int mDisplayWidth;
    private int mDisplayHeight;
    int imageWidth;

    private void completeSplash(){
        loginOrStartApp();
    }

    private void startSilentLoginProcess() {
        User currentUser = app.getUserRepo().getLoggedInUser();

        if (null == currentUser) {
            mIsLoggedIn = false;
            //startFacebookLoginProcess();
        } else {
            mLoggedIn = currentUser;
            mIsLoggedIn = true;
        }
    }


    private void loginOrStartApp(){
        Nostalgia.UserStatus status = app.getCurrentUserStatus();

        int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);
        if (ConnectionResult.SUCCESS != resultCode) {

            Dialog dialog = GooglePlayServicesUtil.getErrorDialog(resultCode, this, 0);
            if (dialog != null) {
                dialog.show();
                return;
            }
        }

        if(status == Nostalgia.UserStatus.LOGGED_IN) {
            startApp();
        } else if(status == Nostalgia.UserStatus.NOT_LOGGED_IN ) {
            mFragmentManager = getSupportFragmentManager();
            FragmentTransaction t = mFragmentManager.beginTransaction();
            LoginFragment loginFragment = new LoginFragment();

            loginFragment.setOnConfirmListener(this);

            t.add(R.id.login_fragment, loginFragment);
            t.commit();

            LinearLayout loginContainer = (LinearLayout) findViewById(R.id.login_fragment);
            loginContainer.setVisibility(View.VISIBLE);
        } else if(status == Nostalgia.UserStatus.WAITING_FOR_SERVER) {
            Toast.makeText(this, "Waiting for server...", Toast.LENGTH_LONG).show();
        } else {
            Toast.makeText(this, "Couldn't determine status.", Toast.LENGTH_LONG).show();
        }
    }

    private void startApp() {
        if(null == mLoggedIn){
            startProgressView();
        } else {
            Intent i = new Intent(this, MainCaptureActivity.class);
            startActivity(i);
            finish();
        }
    }

    private void startProgressView(){
        mProgressViewActive = true;
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                final LinearLayout loginContainer = (LinearLayout) findViewById(R.id.login_fragment);
                loginContainer.animate().translationY(loginContainer.getMeasuredHeight()).setDuration(200).setListener(new Animator.AnimatorListener() {
                    @Override
                    public void onAnimationStart(Animator animation) {

                    }

                    @Override
                    public void onAnimationEnd(Animator animation) {
                        loginContainer.setVisibility(View.GONE);
                        LinearLayout progressView = (LinearLayout) findViewById(R.id.progress_view);
                        progressView.setVisibility(LinearLayout.VISIBLE);
                        animateProgress(R.id.progress_icon);
                    }

                    @Override
                    public void onAnimationCancel(Animator animation) {

                    }

                    @Override
                    public void onAnimationRepeat(Animator animation) {

                    }
                });
            }
        });

    }

    private void stopProgressView(){
        mProgressViewActive = false;
    }

    private void animateProgress(int progressViewId){
        final int finalId = progressViewId;
        if(mProgressViewActive) {
            findViewById(finalId).animate().rotationBy(10 * 360).setDuration(10 * 1500).setInterpolator(new AccelerateDecelerateInterpolator()).withEndAction(
                    new Runnable() {
                        @Override
                        public void run() {
                            animateProgress(finalId);
                        }
                    }
            ).start();
        }
    }

    @Override
    public void onStart(){
        super.onStart();
        IntentFilter filter = new IntentFilter();
        filter.addAction("com.nostalgia.update");

        registerReceiver(receiver, filter);
    }

    @Override
    public void onStop(){
        super.onStop();
        unregisterReceiver(receiver);
    }

    @Override
    public void onLoginSuccess(String sessionToken, String region) {
        if(region == null || region.length() < 2){
            region = "us_east";
        }

        //announce to system
        Intent i = new Intent("com.nostalgia.update");
        i.putExtra("sessionToken", sessionToken);
        i.putExtra("region", region);
        sendBroadcast(i);
        startApp();
    }

    private boolean mProgressViewActive = false;
    private BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.i(TAG, "received intent: " + intent.getAction());

            switch (intent.getAction()) {
                case ("com.nostalgia.update"):
                    mLoggedIn = app.getUserRepo().getLoggedInUser();
                    if(mProgressViewActive) {
                        stopProgressView();
                        startApp();
                    }
                    break;
                default:
                    break;
            }

        }
    };

    @Override
    public void onLogout(){

    }

    private boolean scrubCache(){
        File sdCard = getFilesDir();
        File dir = new File(sdCard, "cache");

        if(!dir.exists()){
            Log.w(TAG, "no cache dir found, skipping purge");
            return false;
        }


        Iterator<File> iter = FileUtils.iterateFiles(dir, null, true);


        long dateToBeat = System.currentTimeMillis() - WEEK_IN_MILLIS;
        while(iter.hasNext()){
            File toProcess= iter.next();

            if(toProcess.lastModified() < dateToBeat){
                //this file is old and need to be deleted
                Log.i(TAG, "purging cache file: " + toProcess.getName());
                FileUtils.deleteQuietly(toProcess);
            }
        }
        return true;
    }
    boolean location_enabled = false;
    private boolean checkGPS(final Context context){
        LocationManager lm = (LocationManager)context.getSystemService(Context.LOCATION_SERVICE);


        location_enabled = lm.isProviderEnabled(LocationManager.GPS_PROVIDER) || lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER);


        if(!location_enabled) {
            // notify user
            AlertDialog.Builder dialog = new AlertDialog.Builder(context);
            dialog.setMessage("gps_network_not_enabled");
            dialog.setPositiveButton("open_location_settings", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface paramDialogInterface, int paramInt) {

                    Intent myIntent = new Intent( Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                    context.startActivity(myIntent);
                    //get gps
                }
            });
            dialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {

                @Override
                public void onClick(DialogInterface paramDialogInterface, int paramInt) {

                }
            });
            dialog.show();
        }

        return location_enabled;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();


    }
    @Override
    public void onPause(){
        super.onPause();

    }

    @Override
    public void onTaskFinished() {
        completeSplash();
    }

    private int mImageHeight;
    private int mImageWidth;
    private int mMeasuredWidth;
    private int mMeasuredHeight;

    @Override
    public void onReady() {
        initialCenter = myImage.getCenter();
        initialScale = myImage.getScale();

        mImageHeight = myImage.getSHeight();
        mImageWidth = myImage.getSWidth();
        mMeasuredWidth = myImage.getMeasuredWidth();
        mMeasuredHeight = myImage.getMeasuredHeight();

        PointF nc = new PointF(0, mMeasuredHeight/2f);

        Typeface font = FontCache.get("teamspirit.regular.ttf", me);
        titleView = (AutoResizeTextView) findViewById(R.id.title_view);
        titleView.setTypeface(font);

        titleHolder = (LinearLayout) findViewById(R.id.title_holder);
        titleHolder.setVisibility(LinearLayout.VISIBLE);
        titleView.setVisibility(TextView.VISIBLE);

        if(null != titleView) {
            titleHolder.setVisibility(LinearLayout.VISIBLE);
            titleView.setVisibility(TextView.VISIBLE);
            ImageView titleIcon = (ImageView) findViewById(R.id.title_icon);
            titleIcon.setVisibility(ImageView.VISIBLE);
        }
        imageRendered = true;

        animateBackground();
        new LoadingTask(me, app).execute();
    }

    private PointF initialCenter;
    private float initialScale;

    @Override
    public void onImageLoaded() {
        imageRendered = true;
    }

    @Override
    public void onPreviewLoadError(Exception e) {
        e.printStackTrace();

    }

    @Override
    public void onImageLoadError(Exception e) {
        e.printStackTrace();
    }

    @Override
    public void onTileLoadError(Exception e) {
        e.printStackTrace();
    }

        /*
     * Processing the large background image resource.
     */

    private void processBitmapDimensions(){
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeResource(getResources(), R.drawable.seascape, options);
        int imageHeight = options.outHeight;
        int imageWidth = options.outWidth;
        String imageType = options.outMimeType;
    }

    public static int calculateInSampleSize(
            BitmapFactory.Options options, int reqWidth, int reqHeight) {
        // Raw height and width of image
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {

            final int halfHeight = height / 2;
            final int halfWidth = width / 2;

            // Calculate the largest inSampleSize value that is a power of 2 and keeps both
            // height and width larger than the requested height and width.
            while ((halfHeight / inSampleSize) > reqHeight
                    && (halfWidth / inSampleSize) > reqWidth) {
                inSampleSize *= 2;
            }
        }

        return inSampleSize;
    }

    public static Bitmap decodeSampledBitmapFromResource(Resources res, int resId,
                                                         int reqWidth, int reqHeight) {

        // First decode with inJustDecodeBounds=true to check dimensions
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeResource(res, resId, options);

        // Calculate inSampleSize
        options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);

        // Decode bitmap with inSampleSize set
        options.inJustDecodeBounds = false;
        return BitmapFactory.decodeResource(res, resId, options);
    }

    private void getScreenDimensions(){
        DisplayMetrics displayMetrics = getResources().getDisplayMetrics();
        int width = displayMetrics.widthPixels;
        int height = displayMetrics.heightPixels;
    }

    //End processing large background image

}
