package com.nostalgia.controller.capturemoment.review;

import android.content.Intent;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;

import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.NavUtils;
import android.support.v4.app.TaskStackBuilder;

import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;

import android.util.Log;

import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nostalgia.Nostalgia;
import com.nostalgia.controller.capturemoment.review.places.LocationCreationActivity;
import com.nostalgia.controller.creator.CollectionCreatorActivity;
import com.nostalgia.controller.peek.CreatorLauncher;
import com.nostalgia.controller.peek.picker.mediadisplayers.NearbySelectorFragment;
import com.nostalgia.controller.peek.picker.mediadisplayers.model.MediaCollectionWrapper;
import com.nostalgia.controller.peek.picker.mediadisplayers.recycler.BaseRecyclerFragment;
import com.nostalgia.controller.peek.picker.mediadisplayers.recycler.CollectionSelectorFragment;
import com.nostalgia.persistence.model.KnownLocation;
import com.nostalgia.persistence.model.MediaCollection;
import com.nostalgia.persistence.model.User;
import com.nostalgia.persistence.model.Video;
import com.nostalgia.persistence.repo.UserRepository;
import com.nostalgia.persistence.repo.VideoRepository;
import com.vuescape.nostalgia.R;

import org.geojson.Point;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.HashMap;

public class MediaReviewerPagerActivity extends AppCompatActivity implements BaseRecyclerFragment.SelectionListener, PlayerFragment.MuteListener, CreatorLauncher {

    public static final int MEDIA_PAGE = 0;
    public static final int LOCATION_PAGE = 1;
    public static final int SETTINGS_PAGE = 2;
    private static final String TAG = "MediaReviewActivity";

    private Uri mFocusedFilePath;
    private Nostalgia app;
    private VideoRepository vidRepo;
    private UserRepository userRepo;
    private FragmentManager mainFragmentManager;

    private String mVideoId;
    private User mUser;
    private ViewPager mPager;

    private String tabTitles[] = new String[] { "Video", "Places", "Collections","Privacy"};

    private FloatingActionButton fabReviewer;
    private View topPadding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_media_reviewer_pager);

        Intent intent = getIntent();
        mFocusedFilePath = intent.getData();
        app = ((Nostalgia) getApplication());
        vidRepo = app.getVidRepo();
        userRepo = app.getUserRepo();
        mUser = userRepo.getLoggedInUser();

        mainFragmentManager = getSupportFragmentManager();

        initializeVideoWithSettings();

        topPadding = findViewById(R.id.top_padding_view);

        mPager = (ViewPager) findViewById(R.id.viewPager);
        mPager.setAdapter(new MyPagerAdapter(mainFragmentManager));
        mPager.setOffscreenPageLimit(2);

        mPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                if (mPlayerFragment != null) {
                    try {
                        mPlayerFragment.onPageChange(position);
                    } catch (NullPointerException e) {
                        e.printStackTrace();
                    }
                }

                if (0 == position) {
                    topPadding.setVisibility(View.GONE);
                } else {
                    topPadding.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });

        ImageView cancelMedia = (ImageView) findViewById(R.id.cancel_media);
        cancelMedia.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                popupCancelDialog();
            }
        });
        setupCancelDialog();

        ImageView acceptAndUpload = (ImageView) findViewById(R.id.accept_and_upload_video);
        acceptAndUpload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                acceptAndUploadVideo();
            }
        });

        fabReviewer = (FloatingActionButton) findViewById(R.id.fab_reviewer);
        fabReviewer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onFabClick();
            }
        });

        TabLayout t = (TabLayout) findViewById(R.id.tabLayout);
        t.setupWithViewPager(mPager);
    }

    @Override
    public void onMuteToggle(boolean isMuted){
        mIsMuted = isMuted;
    }

    private void initializeVideoWithSettings(){
        Video mVideo = new Video();

        User loggedIn = app.getUserRepo().getLoggedInUser();
        if(loggedIn == null){
            Log.e(TAG, "error getting log in video to pre-populate video - null user");
            Toast.makeText(this, "Must be logged in to post videos!",Toast.LENGTH_LONG).show();
            return;

        }
        mVideo.setOwnerId(loggedIn.get_id());
        if(mVideo.getProperties() == null){
            mVideo.setProperties(new HashMap<String, String>());
        }

        mVideo.getProperties().put("sharing_who", loggedIn.getSettings().get("sharing_who"));
        mVideo.getProperties().put("sharing_where", loggedIn.getSettings().get("sharing_where"));
        mVideo.getProperties().put("sharing_when", loggedIn.getSettings().get("sharing_when"));

        mVideo.setStatus("PENDING");
        try {
            vidRepo.save(mVideo);
        } catch(Exception e){
            e.printStackTrace();
        }
        this.mVideoId = mVideo.get_id();
    }

    private final MediaReviewerPagerActivity self = this;
    private PlayerFragment mPlayerFragment;
    public NearbySelectorFragment mNearbySelectorFragment;
    private CollectionSelectorFragment mCollectionSelectorFragment;

    @Override
    public void launchCreator(String visibility) {
        switch(mPager.getCurrentItem()){
            case 0:
                break;
            case 1:
                goToCreatePlace();
                break;
            case 2:
                goToCreateCollection(visibility);
                break;
        }
    }

    private class MyPagerAdapter extends FragmentPagerAdapter {

        public MyPagerAdapter(FragmentManager fm) {
            super(fm);
        }
        @Override
        public Fragment getItem(int pos){
                try{
                switch (pos) {
                    default:
                    case 0:
                        mPlayerFragment = (null == mPlayerFragment) ? new PlayerFragment() : mPlayerFragment;
                        mPlayerFragment.setMuteListener(self);
                        return mPlayerFragment;
                    case 1:
                        mNearbySelectorFragment = (null == mNearbySelectorFragment) ? NearbySelectorFragment.newInstance(true) : mNearbySelectorFragment;
                        return mNearbySelectorFragment;
                    case 2:
                        mCollectionSelectorFragment = (null == mCollectionSelectorFragment) ? CollectionSelectorFragment.newInstance(true) : mCollectionSelectorFragment;
                        return mCollectionSelectorFragment;
                }
            } catch(Exception e){
                e.printStackTrace();
                return null;
            }
        }

        @Override
        public int getCount() {
            return 3;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            // Generate title based on item position
            return tabTitles[position];
        }
    }

    private void acceptAndUploadVideo(){
        if(checkIfReadyForSubmittal()) {
            try {
                userSubmittal();
            } catch (Exception e) {
                Log.e(TAG, "Error submitting user", e);
            }
        }
    }

    private void onFabClick(){

    }

    private void goToCreatePlace(){
        Intent mpdIntent = new Intent(this, LocationCreationActivity.class);
        startActivityForResult(mpdIntent, LocationCreationActivity.MULTI_SELECT);
    }

    private void goToCreateCollection(String visibility){
        Intent creator = new Intent(this, CollectionCreatorActivity.class);
        creator.putExtra("visbility", visibility);
        startActivityForResult(creator, 1);
    }

    private LinearLayout mCancelDialog;
    private void popupCancelDialog(){
        mCancelDialog.setVisibility(LinearLayout.VISIBLE);
    }

    private void setupCancelDialog(){
        mCancelDialog = (LinearLayout) findViewById(R.id.delete_dialog);
        TextView doCancel = (TextView) mCancelDialog.findViewById(R.id.do_delete);
        doCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cancelMedia();
            }
        });

        TextView dontCancel = (TextView) mCancelDialog.findViewById(R.id.dont_delete);
        dontCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mCancelDialog.setVisibility(View.GONE);
            }
        });
    }

    private void cancelMedia(){

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
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        super.onOptionsItemSelected(item);
        switch (item.getItemId()) {
            case android.R.id.home:
                return true;
            case R.id.accept_and_upload_video:
                acceptAndUploadVideo();
                return true;
            default:
                // If we got here, the user's action was not recognized.
                // Invoke the superclass to handle it.
                return super.onOptionsItemSelected(item);

        }
    }

    private boolean checkIfReadyForSubmittal(){
        boolean ret = true;

        if(mSelections.size() > 0) {
            setVideoTaggedLocations(null);
            setVideoTaggedCollections(mSelections);
        } else {
            mPager.setCurrentItem(LOCATION_PAGE, true);
            Toast.makeText(this, "Where u at tho?",Toast.LENGTH_LONG).show();
            ret = false;
        }
        return ret;
    }

    private boolean mIsMuted = false;
    private void userSubmittal() throws Exception {

        User owner = userRepo.getLoggedInUser();
        Video mVideo = vidRepo.findOneById(mVideoId, false, false);
        boolean muted = mPlayerFragment.isMuted();

        String originalSetting = owner.getSettings().get("video_sound");

        if (mIsMuted){
             mVideo.getProperties().put("video_sound", Video.SOUND_MUTE);
        } else {
            mVideo.getProperties().put("video_sound", Video.SOUND_ENABLED);
        }

        mVideo.getProperties().put("vidsource","NATIVE");

        if(mPlayerFragment != null){
            mPlayerFragment.updateVideo(mVideo);
        }

        Location current = app.getLocation();

        if(current == null){
            Log.e("ReviewActivity", "error - no location specified");
            Toast.makeText(this, "Error finding location, video not uploaded.", Toast.LENGTH_SHORT).show();
            return;
        } else {
            Point point = new Point(current.getLongitude(), current.getLatitude());
            mVideo.setLocation(point);
        }

        //adjust status based on what kind of upload it is

        String uploadTime = Video.WHEN_NOW;
        if(uploadTime == null){
            Log.e(TAG, "error - no value for sharing_when, defaulting to now");
            uploadTime = Video.WHEN_NOW;
        }

        switch(uploadTime){
            case(Video.WHEN_DAY):
            case(Video.WHEN_HOUR):
            case(Video.WHEN_NOW):
                mVideo.setStatus("PENDING_TIME");
                mVideo.setDateCreated(System.currentTimeMillis());
                break;
            default:
                Log.e(TAG, "unrecognized uploadtime value in usersubmittal: " + uploadTime + ". falling back to wifi");
            case(Video.WHEN_WIFI):
                mVideo.setStatus("PENDING_WIFI");
                mVideo.setDateCreated(System.currentTimeMillis());
                break;

        }

        try {
            vidRepo.save(mVideo);
        } catch(Exception e){
            e.printStackTrace();
        }
        saveVideoFile(mVideoId);

        Intent returnIntent = new Intent();
        setResult(RESULT_OK, returnIntent);
        finish();
    }
    private String newFilePath;

    private void saveVideoFile(String vidId){

        File file = null;
        try {
            File afd = new File(mFocusedFilePath.getPath());

            // Create new file to copy into.
            newFilePath = this.getFilesDir() + java.io.File.separator + vidId + ".dat";
            file =  new File(newFilePath);
            file.createNewFile();

            copyFdToFile(afd, file);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
    public static void copyFdToFile(File src, File dst) throws IOException {
        FileChannel inChannel = new FileInputStream(src).getChannel();
        FileChannel outChannel = new FileOutputStream(dst).getChannel();
        long size = inChannel.size();
        try {
            inChannel.transferTo(0, inChannel.size(), outChannel);
        } catch (Exception e){
            e.printStackTrace();
        } finally {
            if (inChannel != null)
                inChannel.close();
            if (outChannel != null)
                outChannel.close();
        }
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

    private static final ObjectMapper mapper = new ObjectMapper();

    private void setVideoTaggedCollections(ArrayList<MediaCollection> selectedCollections){
        if(selectedCollections == null){
            //No collections to add.
            return;
        }

        Video mVideo = vidRepo.findOneById(mVideoId, false, false);

        ArrayList<String> ids = new ArrayList<String>();
        for(MediaCollection col : selectedCollections) {
            ids.add(col.get_id());
        }

        try {
            String oldList = mVideo.getProperties().get("initialTags");
            if(oldList != null && !oldList.isEmpty()){
                ArrayList<String> updatedList = new ArrayList<>();
                Class c = updatedList.getClass();
                updatedList = (ArrayList<String>) mapper.readValue(oldList, c);
                updatedList.addAll(ids);
                mVideo.getProperties().put("initialTags", mapper.writeValueAsString(updatedList));
            }  else {
                mVideo.getProperties().put("initialTags", mapper.writeValueAsString(ids));
            }
        } catch (JsonProcessingException e) {
            Log.e(TAG, "JSON Exception", e);
        } catch (IOException e) {
            e.printStackTrace();
        }

        try {
            vidRepo.save(mVideo);
        } catch(Exception e){
            e.printStackTrace();
        }
    }

    private void setVideoTaggedLocations(ArrayList<KnownLocation> selectedLocations){
        if(selectedLocations == null){
            //No locations to add.
            return;
        }

        Video mVideo = vidRepo.findOneById(mVideoId, false, false);

        ArrayList<String> ids = new ArrayList<String>();
        for(KnownLocation loc : selectedLocations) {
           ids.add(loc.get_id());
        }

        try {
            String oldList = mVideo.getProperties().get("initialTags");
            if(oldList != null && !oldList.isEmpty()){
                ArrayList<String> updatedList = new ArrayList<>();
                Class c = updatedList.getClass();
                updatedList = (ArrayList<String>) mapper.readValue(oldList, c);
                updatedList.addAll(ids);
                mVideo.getProperties().put("initialTags", mapper.writeValueAsString(updatedList));
            } else {
                mVideo.getProperties().put("initialTags", mapper.writeValueAsString(ids));
            }
        } catch (JsonProcessingException e) {
            Log.e(TAG, "JSON Exception", e);
        } catch (IOException e) {
            e.printStackTrace();
        }

        try {
            vidRepo.save(mVideo);
        } catch(Exception e){
            e.printStackTrace();
        }
    }

    @Override
    public void onCancelled() {
        //Do nothing
    }

    private ArrayList<MediaCollection> mSelections = new ArrayList<MediaCollection>();
    @Override
    public void onSelection(Object selection, boolean isSelected) {
        MediaCollection loc = (MediaCollection) selection;
        if(isSelected) {
            mSelections.add(loc);
        } else {
            try {
                mSelections.remove(loc);
            } catch (Exception e){
                //can't find loc in list. Not exactly sure how pointers will be evaluated.
                e.printStackTrace();
            }
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch(resultCode) {
            case CollectionCreatorActivity.RESULT_COLL_CREATED:
                //No use for colId here, but I'm leaving this for potential future use.
                try {
                    MediaCollection toAdd = (MediaCollection) data.getSerializableExtra("CREATED_LOCATION");
                    mCollectionSelectorFragment.addNewItem(toAdd, "Nearby",MediaCollectionWrapper.WrapperType.NEARBY);
                } catch (Exception e){
                    e.printStackTrace();
                }
                break;
            default:
                Log.e(TAG, "User Media unexpected resultCode: " + Integer.toString(resultCode));
                break;
        }
    }
}