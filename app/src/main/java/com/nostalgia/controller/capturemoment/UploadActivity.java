package com.nostalgia.controller.capturemoment;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.NavUtils;
import android.support.v4.app.TaskStackBuilder;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;

import com.nostalgia.Nostalgia;
import com.nostalgia.persistence.model.User;
import com.nostalgia.persistence.model.Video;
import com.nostalgia.persistence.repo.UserRepository;
import com.nostalgia.persistence.repo.VideoRepository;
import com.vuescape.nostalgia.R;


/**
 * Created by alex on 11/3/15.
 */
public class UploadActivity extends AppCompatActivity {
    private static final int PROFILE_SETTING = 1;

    public static final String TAG = "UploaderActivity";

    protected FragmentManager mainFragmentManager;

    private VideoRepository vidRepo;
    private FloatingActionButton floatingActionButton;

    private Nostalgia app ;
    private UserRepository userRepo;

    private Uri focusedFilePath;
    private Video mVideo;

    @Override
    public void onCreate(Bundle savedInstanceState){
        //getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.review_player_activity);

        Intent intent = getIntent();
        Video mVideo = (Video) intent.getSerializableExtra("videoToReview");
        focusedFilePath = intent.getData();

        this.app = ((Nostalgia) getApplication());
        this.vidRepo = app.getVidRepo();
        this.userRepo = app.getUserRepo();
        mainFragmentManager = getSupportFragmentManager();

        final LinearLayout.LayoutParams linearLayoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);

        Toolbar myToolbar = (Toolbar) findViewById(R.id.my_toolbar);
        setSupportActionBar(myToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        User owner = userRepo.getLoggedInUser();
        mVideo.setOwnerId(owner.get_id());
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.review_player, menu);

        return true;
    }

    public void backClicked(View view) {
        Intent returnIntent = new Intent();
        setResult(RESULT_CANCELED, returnIntent);
        finish();
    }

    public void cancel() {
        Intent returnIntent = new Intent();
        setResult(RESULT_CANCELED, returnIntent);
        finish();
    }

    public void onOptionsConfirmed() {
        //vidRepo.save(mVideo);
        if(mVideo.getProperties() == null){
            mVideo.setProperties(new HashMap<String, String>());
        }
        User owner = userRepo.getLoggedInUser();

        //TODO set location in video
        Point duke = new Point(36.0263022, -79.1096901);

        mVideo.setLocation(duke);

        Location current = app.getLocation();

        if(current == null){
            Log.e(TAG, "error - no location specified");
            Toast.makeText(this, "error finding location, video not uploaded", Toast.LENGTH_SHORT).show();
            return;
        }


        Point point  = new Point(current.getLongitude(), current.getLatitude());
        mVideo.setLocation(point);
        VideoUploadTask uploader = new VideoUploadTask(focusedFilePath.getPath(), mVideo, app, true);

        uploader.execute();

        Intent returnIntent = new Intent();
        setResult(RESULT_OK, returnIntent);
        finish();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        super.onOptionsItemSelected(item);
        switch (item.getItemId()) {
            case android.R.id.home:
                Intent upIntent = NavUtils.getParentActivityIntent(this);
                if (NavUtils.shouldUpRecreateTask(this, upIntent)) {
                    // This activity is NOT part of this app's task, so create a new task
                    // when navigating up, with a synthesized back stack.
                    TaskStackBuilder.create(this)
                            // Add all of this activity's parents to the back stack
                            .addNextIntentWithParentStack(upIntent)
                                    // Navigate up to the closest parent
                            .startActivities();
                } else {
                    // This activity is part of this app's task, so simply
                    // navigate up to the logical parent activity.
                    NavUtils.navigateUpTo(this, upIntent);
                }
                return true;
            case R.id.accept_and_upload_video:
                onOptionsConfirmed();

                return true;
            default:
                // If we got here, the user's action was not recognized.
                // Invoke the superclass to handle it.
                return super.onOptionsItemSelected(item);

        }
    }

    @Override
    public void onStop(){
        super.onStop();
    }

    public void onOptionsCancelled() {
       cancel();
    }
}
