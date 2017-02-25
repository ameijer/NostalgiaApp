package com.nostalgia.controller.peek.playback;

import android.animation.Animator;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.app.NavUtils;
import android.support.v4.app.TaskStackBuilder;
import android.support.v7.app.AppCompatActivity;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.GestureDetector;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.exoplayer.AspectRatioFrameLayout;
import com.nostalgia.Nostalgia;
import com.nostalgia.SmartCookieManager;

import com.nostalgia.controller.exoplayer.VideoPlayerFragment;
import com.nostalgia.persistence.model.Video;
import com.nostalgia.persistence.repo.VideoRepository;
import com.nostalgia.runnable.AttributeActionThread;
import com.nostalgia.runnable.AttributeGetterTask;
import com.nostalgia.runnable.FieldType;
import com.vuescape.nostalgia.R;

import java.net.CookieHandler;
import java.net.HttpCookie;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;


/**
 * Created by alex on 11/3/15.
 */
public class ViewerActivity extends AppCompatActivity implements VideoPlayerFragment.PlaylistController, AttributeGetterTask.GetterTaskFinishedListener{
    private static final int PROFILE_SETTING = 1;

    public static final String TAG = "ViewerActivity";

    //For using ViewerActivity to get an IntentResult:
    public static final int VIDEO_FOCUS = 1000;

    // For use within demo app code.
    public static final String CONTENT_ID_EXTRA = "content_id";
    public static final String CONTENT_TYPE_EXTRA = "content_type";
    public static final int TYPE_DASH = 0;
    public static final int TYPE_SS = 1;
    public static final int TYPE_HLS = 2;
    public static final int TYPE_OTHER = 3;
    public static final String URI_LIST_EXTRA = "uri_list_extra";

    // For use when launching the demo app using adb.
    private static final String CONTENT_EXT_EXTRA = "type";
    private static final String EXT_DASH = ".mpd";
    private static final String EXT_SS = ".ism";
    private static final String EXT_HLS = ".m3u8";

    private static final int MENU_GROUP_TRACKS = 1;
    private static final int ID_OFFSET = 2;

    protected FragmentManager mainFragmentManager;

    private boolean mOptionsFragActive = false;

    private VideoPlayerFragment videoPlayerFragment;

    private Video playing = new Video();
    private ArrayList<Video> playlist = new ArrayList<Video>();

    private VideoRepository vidRepo;
    private FloatingActionButton floatingActionButton;

    private AspectRatioFrameLayout videoFrame;

    private Integer currentMediaIndex = -1;

    private LinearLayout.LayoutParams linearLayoutParams;
    private TextView quickThoughtText;
    private TextView dateTimeMessage;
    private TextView placeName;
    private TextView mVoteCount;

    private Boolean mHasVoted = false;

    private View mDownvoteSection;
    private View mUpvoteSection;

    private ImageView mUpvoteButton;
    private ImageView mDownvoteButton;
    private ImageView mUpvoteBigButton;
    private ImageView mDownvoteBigButton;
    private ImageView mUpvoteSmallButton;
    private ImageView mDownvoteSmallButton;

    //Default location Name
    String locationName = "Nostalgia";


    /*
     * Side drawer setup
     */
    private Nostalgia app ;
    private FrameLayout mRoot;
    private final ViewerActivity self = this;

    private GestureDetector mGestureDetector;
    private ImageView mReport;
    private ImageView mReportActive;

    @Override
    public void onCreate(Bundle savedInstanceState){
        //getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_peek_player);

        Intent intent = getIntent();
        this.playlist = (ArrayList<Video>) intent.getSerializableExtra("playlist");
        this.locationName = intent.getStringExtra("location_name");
        this.app = ((Nostalgia) getApplication());
        this.vidRepo = app.getVidRepo();
        mainFragmentManager = getSupportFragmentManager();

        linearLayoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);
        quickThoughtText = (TextView) findViewById(R.id.quick_thought_text);
        dateTimeMessage = (TextView) findViewById(R.id.date_time_message);
        placeName = (TextView) findViewById(R.id.place_name);
        mVoteCount = (TextView) findViewById(R.id.vote_count);

        mDownvoteSection = findViewById(R.id.downvote_clickable_section);
        mDownvoteSection.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onDownvoteClick();
            }
        });

        mUpvoteSection = findViewById(R.id.upvote_clickable_section);
        mUpvoteSection.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                onUpvoteClick();
            }
        });

        mUpvoteButton = (ImageView) findViewById(R.id.upvote);
        mDownvoteButton = (ImageView) findViewById(R.id.downvote);

        mUpvoteSmallButton = (ImageView) findViewById(R.id.upvote_small);
        mDownvoteSmallButton = (ImageView) findViewById(R.id.downvote_small);

        mUpvoteBigButton = (ImageView) findViewById(R.id.upvote_big);
        mDownvoteBigButton = (ImageView) findViewById(R.id.downvote_big);

        mReport = (ImageView) findViewById(R.id.report_flag);
        mReport.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                doReport();
            }
        });

        mReportActive = (ImageView) findViewById(R.id.report_flag_active);
        mReportActive.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                doReport();
            }
        });

        placeName.setText(locationName);

        mHasVoted = determineVotingStatus();

        videoPlayerFragment = new VideoPlayerFragment();
        videoPlayerFragment.setPlaylistController(this);
        prepareNextMediaItem();

        mRoot = (FrameLayout) findViewById(R.id.peek_player_root);

        View touchSurface = findViewById(R.id.touch_surface);
        touchSurface.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                rootClick();
            }
        });


        //initially, mStart with the choice fragment
        FragmentTransaction fragTransaction = mainFragmentManager.beginTransaction();
        fragTransaction.replace(R.id.review_player_fragment, videoPlayerFragment);
        fragTransaction.commit();
    }

    private void rootClick(){
        //If the user hasn't voted or reported the video, enforce.
        if(VotingState.NO_VOTE == mVotingState && !mHasReported){
            Toast.makeText(this, "Gotta vote first.", Toast.LENGTH_SHORT).show();
        } else {
            clearVotingState();
            switchToNewMediaFragment();
        }
    }

    private boolean determineVotingStatus(){
        setVotingState(VotingState.NO_VOTE);

        return false;
    }

    private void setVotingState(VotingState newState){

        //Displaying newState depends on the past, so it must be altered first!

        if(null == newState){
            newState = mVotingState;
        }

        if(newState != VotingState.UNDETERMINED) {
            updateVotingDisplay(newState);

            switch (newState) {
                case DOWNVOTED:
                    mVotingState = VotingState.DOWNVOTED;
                    mHasVoted = true;
                    break;
                case UPVOTED:
                    mVotingState = VotingState.UPVOTED;
                    mHasVoted = true;
                    break;
                case NO_VOTE:
                    mVotingState = VotingState.NO_VOTE;
                    mHasVoted = false;
                    break;
            }
        } else {
            //if votingState == UNDETERMINED;
            clearVotingState();
        }
    }

    private void clearVotingState(){
        if(0 == mNormalVotingHeight){
            //Haven't measured yet, try again:
        }

        mVotingState = VotingState.UNDETERMINED;
        mHasVoted = false;
    }

    private void doDownvote(){
        setVotingState(VotingState.DOWNVOTED);

        mDownvoteCount = mDownvoteCount + 1;

        final Video target = playing;
        AttributeActionThread adder = new AttributeActionThread(target, app.getUserRepo().getLoggedInUser(), FieldType.DOWNVOTES);
        adder.start();

        try {
            adder.join();
        } catch (InterruptedException e){
            e.printStackTrace();
        }
    }

    private void onDownvoteClick(){
        if(null != mHasVoted && mHasVoted){
            //User is either canceling an upvote or switching from downvote to upvote.
            switch(mVotingState){
                case DOWNVOTED:
                    undoVote(VotingState.DOWNVOTED);
                    setVotingState(VotingState.NO_VOTE);
                    break;
                case UPVOTED:
                    undoVote(VotingState.UPVOTED);
                    doDownvote();
                    break;
                default:
                    Log.e(TAG, "BAD VOTING STATE");
            }
        } else {
            //User hasn't voted yet, so this is a clean upvote.
            doDownvote();
        }

        runVoteCount();
    }

    private void onUpvoteClick(){
        if(mHasVoted){
            //User is either canceling an upvote or switching from downvote to upvote.
            switch(mVotingState) {
                case UPVOTED:
                    undoVote(VotingState.UPVOTED);
                    setVotingState(VotingState.NO_VOTE);
                    break;
                case DOWNVOTED:
                    undoVote(VotingState.DOWNVOTED);
                    doUpvote();
                    break;
                default:
                    Log.e(TAG, "BAD VOTING STATE");
            }
        } else {
            //User hasn't voted yet, so this is a clean upvote.
                doUpvote();
        }

        runVoteCount();
    }

    private void undoUpvote(){
        Toast.makeText(this, "TODO: Undo upvote", Toast.LENGTH_LONG).show();
        mUpvoteCount = mUpvoteCount - 1;
    }
    private void undoDownvote(){
        Toast.makeText(this, "TODO: Undo downvote", Toast.LENGTH_LONG).show();
        mDownvoteCount = mDownvoteCount -1;
    }

    private void undoVote(VotingState toUndo){
        switch(toUndo) {
            case DOWNVOTED:
                undoDownvote();
                break;
            case UPVOTED:
                undoUpvote();
                break;
        }
    }

    private void doUpvote(){
        mUpvoteCount = mUpvoteCount + 1;
        setVotingState(VotingState.UPVOTED);

        final Video target = playing;
        AttributeActionThread adder = new AttributeActionThread(target, app.getUserRepo().getLoggedInUser(), FieldType.UPVOTES);
        adder.start();

        try {
            adder.join();
        } catch (InterruptedException e){
            e.printStackTrace();
        }
    }

    private boolean mVotingShowing = false;
    private void toggleVoting(){
        if(mVotingShowing){
            hideVoting();
        } else {
            showVoting();
        }
    }

    private void hideVoting() {
        LinearLayout mUpvoteView = (LinearLayout) findViewById(R.id.upvote_visible_section);
        mUpvoteView.animate()
                .translationX(mUpvoteView.getWidth() + mUpvoteView.getPaddingRight())
                .setInterpolator(new AccelerateInterpolator(2))
                .setListener(new Animator.AnimatorListener() {
                                 @Override
                                 public void onAnimationStart(Animator animation) {

                                 }

                                 @Override
                                 public void onAnimationEnd(Animator animation) {
                                 }

                                 @Override
                                 public void onAnimationCancel(Animator animation) {

                                 }

                                 @Override
                                 public void onAnimationRepeat(Animator animation) {

                                 }
                             }
                );

        LinearLayout mDownvoteView = (LinearLayout) findViewById(R.id.downvote_visible_section);
        mDownvoteView.animate()
                .translationX(-mUpvoteView.getWidth() - mUpvoteView.getPaddingLeft())
                .setInterpolator(new AccelerateInterpolator(2))
                .setListener(new Animator.AnimatorListener() {
                                 @Override
                                 public void onAnimationStart(Animator animation) {

                                 }

                                 @Override
                                 public void onAnimationEnd(Animator animation) {
                                 }

                                 @Override
                                 public void onAnimationCancel(Animator animation) {

                                 }

                                 @Override
                                 public void onAnimationRepeat(Animator animation) {

                                 }
                             }
                );

        mVotingShowing = false;
    }

    private void showVoting() {
        LinearLayout mUpvoteView = (LinearLayout) findViewById(R.id.upvote_visible_section);
        mUpvoteView.animate()
                .translationX(0)
                .setInterpolator(new DecelerateInterpolator(2))
                .setListener(new Animator.AnimatorListener() {
                                 @Override
                                 public void onAnimationStart(Animator animation) {
                                 }

                                 @Override
                                 public void onAnimationEnd(Animator animation) {
                                     //mCoverFrame.setVisibility(View.VISIBLE);
                                 }

                                 @Override
                                 public void onAnimationCancel(Animator animation) {

                                 }

                                 @Override
                                 public void onAnimationRepeat(Animator animation) {

                                 }
                             }
                );

        LinearLayout mDownvoteView = (LinearLayout) findViewById(R.id.downvote_visible_section);
        mDownvoteView.animate()
                .translationX(0)
                .setInterpolator(new DecelerateInterpolator(2))
                .setListener(new Animator.AnimatorListener() {
                                 @Override
                                 public void onAnimationStart(Animator animation) {
                                 }

                                 @Override
                                 public void onAnimationEnd(Animator animation) {
                                     //mCoverFrame.setVisibility(View.VISIBLE);
                                 }

                                 @Override
                                 public void onAnimationCancel(Animator animation) {

                                 }

                                 @Override
                                 public void onAnimationRepeat(Animator animation) {

                                 }
                             }
                );

        mVotingShowing = true;
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus)
    {
        super.onWindowFocusChanged(hasFocus);
        if(hasFocus)
        {
        }
    }

    private boolean mHasReported = false;
    private void doReport(){
        Toast.makeText(this, "Violation reported, thanks.", Toast.LENGTH_SHORT).show();

        if(!mHasReported) {
            mHasReported = true;
            mReportActive.setVisibility(ImageView.VISIBLE);
            mReport.setVisibility(ImageView.GONE);

            final Video target = playing;
            AttributeActionThread adder = new AttributeActionThread(target, app.getUserRepo().getLoggedInUser(), FieldType.FLAGS);
            adder.start();

            try {
                adder.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        } else {
            //TODO: Undo reporting.
            mHasReported = false;
            mReportActive.setVisibility(ImageView.GONE);
            mReport.setVisibility(ImageView.VISIBLE);
        }
    }

    private void setupGestureDetection(){
        GestureDetector.OnGestureListener listener = new GestureDetector.OnGestureListener(){
            @Override
            public boolean onDown(MotionEvent e) {
                Toast.makeText(self, "Down", Toast.LENGTH_SHORT);
                return true;
            }

            @Override
            public void onShowPress(MotionEvent e) {
                Toast.makeText(self, "Show Press", Toast.LENGTH_SHORT);
            }

            @Override
            public boolean onSingleTapUp(MotionEvent e) {
                Toast.makeText(self, "Single Tap Up", Toast.LENGTH_SHORT);
                return true;
            }

            @Override
            public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
                return true;
            }

            @Override
            public void onLongPress(MotionEvent e) {
                Toast.makeText(self, "Long Press", Toast.LENGTH_SHORT);
            }

            @Override
            public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
                if (e1.getX() < e2.getX()) {
                    Log.d(TAG, "Left to Right swipe performed");
                }

                if (e1.getX() > e2.getX()) {
                    Log.d(TAG, "Right to Left swipe performed");
                }

                if (e1.getY() < e2.getY()) {
                    Log.d(TAG, "Up to Down swipe performed");
                }

                if (e1.getY() > e2.getY()) {
                    Log.d(TAG, "Down to Up swipe performed");
                }

                return true;
            }
        };

        mGestureDetector = new GestureDetector(self, listener);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        mGestureDetector.onTouchEvent(event);

        return super.onTouchEvent(event);
    }

    private void showMenuSheet() {
        LinearLayout v = (LinearLayout) findViewById(R.id.view_noncritical_information);
        if(View.VISIBLE == v.getVisibility()) {
            v.setVisibility(View.GONE);
            videoPlayerFragment.pause();
        } else {
            v.setVisibility(View.VISIBLE);
            videoPlayerFragment.play();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.peek_player, menu);

        return true;
    }

    private void switchToNewMediaFragment(){
        FragmentTransaction fragTransaction = mainFragmentManager.beginTransaction();
        fragTransaction.remove(videoPlayerFragment);
        fragTransaction.commit();
        videoPlayerFragment = new VideoPlayerFragment();
        videoPlayerFragment.setPlaylistController(this);
        fragTransaction = mainFragmentManager.beginTransaction();
        fragTransaction.add(R.id.review_player_fragment, videoPlayerFragment);
        fragTransaction.commit();

        prepareNextMediaItem();
    }

    @Override
    public void onMediaEnd(){
        clearVotingState();
        switchToNewMediaFragment();
    }

    private void prepareNextMediaItem(){
        currentMediaIndex = currentMediaIndex + 1;

        if(currentMediaIndex >= this.playlist.size()){
            finish();
            return;
        }

        if(0 > currentMediaIndex) {
            currentMediaIndex = 0;
        }

        playing = this.playlist.get(currentMediaIndex);

        if((null == playing) || (null == playing.getUrl())){
            prepareNextMediaItem();
            return;
        }

        updateMediaInfo();
        videoPlayerFragment.setNextMedia(playing);
    }

    @Override
    public void onPlaybackError(int errorcode) {
        switch(errorcode){
            case(404):
                Log.e(TAG, "404 error found, skipping video");
                Toast.makeText(getApplicationContext(),
                        "Error - video not found, skipping to next video", Toast.LENGTH_SHORT).show();
                switchToNewMediaFragment();
                break;

            case(401):
                Log.e(TAG, "401 received, printing cookies: ");
                CookieHandler currentHandler = CookieHandler.getDefault();

                if(currentHandler instanceof SmartCookieManager){
                    SmartCookieManager man = (SmartCookieManager) currentHandler;
                    List<HttpCookie> cooks = man.getCookieStore().getCookies();
                    Log.e(TAG, "numcookies: " + cooks.size());
                    for(HttpCookie cook : cooks){
                        Log.e(TAG, "Cookie found: " + cook.toString());
                    }

                } else {
                    Log.e(TAG, "unknown default cookie handler");
                }
                break;
            default:
                Log.e(TAG, "error code: " + errorcode + "unrecognized");
                Toast.makeText(getApplicationContext(),
                        "error code: " + errorcode + "unrecognized", Toast.LENGTH_LONG).show();
                break;
        }
    }

    private AttributeGetterTask mDownGetter;
    private AttributeGetterTask mUpGetter;

    private void updateMediaInfo(){

        mDownGetter = new AttributeGetterTask(playing, FieldType.DOWNVOTES, app.getUserRepo().getLoggedInUser());
        mDownGetter.setFinishedListener(this);
        mUpGetter = new AttributeGetterTask(playing, FieldType.UPVOTES,app.getUserRepo().getLoggedInUser() );
        mUpGetter.setFinishedListener(this);

        mUpGetter.execute();
        mDownGetter.execute();

        clearVotingState();
        mHasVoted = determineVotingStatus();

        long epochDate = playing.getDateCreated();
        if(0 < epochDate) {
            Date d = new Date(epochDate);
            String date = d.toString();
            SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, yyyy");
            date = sdf.format(d);


            DateUtils de = new DateUtils();
            String ago = DateUtils.getRelativeDateTimeString(this, epochDate, DateUtils.MINUTE_IN_MILLIS, 30 * DateUtils.DAY_IN_MILLIS, DateUtils.FORMAT_ABBREV_RELATIVE).toString();
            dateTimeMessage.setText(ago);
        }

        String quickThought = playing.getProperties().get("quick_thought");
        if(quickThought != null && !quickThought.isEmpty()){
            quickThoughtText.setText(quickThought);
        } else {
            quickThoughtText.setText("");
        }

    }

    private boolean votingStateKnown = false;

    private void updateVotingDisplay(VotingState newState){
        if(VotingState.UPVOTED == newState || VotingState.DOWNVOTED == newState){
            displayVoting(newState);
        } else {
            displayNoVote();
        }
    }

    private enum VotingState {
       UPVOTED, DOWNVOTED, NO_VOTE, UNDETERMINED
    }

    private VotingState mVotingState = VotingState.UNDETERMINED;
    private void displayVoting(VotingState newState){
        if (newState == VotingState.DOWNVOTED) {
            displayDownvote();
        } else if (newState == VotingState.UPVOTED) {
            displayUpvote();
        } else {
            Log.e(TAG, "Undetermined voting state.");
        }
    }

    private int mNormalVotingHeight;
    private int mSmallVotingHeight;
    private int mBigVotingHeight;

    private int mNormalVotingWidth;
    private int mSmallVotingWidth;
    private int mBigVotingWidth;

    private void displayDownvote(){
        mDownvoteButton.setVisibility(ImageView.GONE);
        mUpvoteButton.setVisibility(ImageView.GONE);

        mDownvoteSmallButton.setVisibility(ImageView.GONE);
        mUpvoteBigButton.setVisibility(ImageView.GONE);

        mDownvoteBigButton.setVisibility(ImageView.VISIBLE);
        mUpvoteSmallButton.setVisibility(ImageView.VISIBLE);
    }

    private void displayUpvote(){
        mDownvoteButton.setVisibility(ImageView.GONE);
        mUpvoteButton.setVisibility(ImageView.GONE);

        mDownvoteSmallButton.setVisibility(ImageView.VISIBLE);
        mUpvoteBigButton.setVisibility(ImageView.VISIBLE);

        mDownvoteBigButton.setVisibility(ImageView.GONE);
        mUpvoteSmallButton.setVisibility(ImageView.GONE);
    }

    private void displayNoVote(){
        mDownvoteButton.setVisibility(ImageView.VISIBLE);
        mUpvoteButton.setVisibility(ImageView.VISIBLE);

        mDownvoteSmallButton.setVisibility(ImageView.GONE);
        mDownvoteBigButton.setVisibility(ImageView.GONE);

        mUpvoteBigButton.setVisibility(ImageView.GONE);
        mUpvoteSmallButton.setVisibility(ImageView.GONE);
    }

    private void runVoteCount(){
            long totalCount = mUpvoteCount - mDownvoteCount;
            mVoteCount.setText(Long.toString(totalCount));
        return;
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

    public Video getCurrentVideo(){
        return playing;
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
            default:
                // If we got here, the user's action was not recognized.
                // Invoke the superclass to handle it.
                return super.onOptionsItemSelected(item);

        }
    }

    private long mUpvoteCount = -1;
    private long mDownvoteCount = -1;

    @Override
    public void onAttributeGotten(String ownerObjectId, long simpleCount) {

        try{
            mUpvoteCount = mUpGetter.getNumericalValue();
        } catch (Exception e){
            Toast.makeText(this,"Upvote error.", Toast.LENGTH_LONG).show();
            e.printStackTrace();
        }

        try{
            mDownvoteCount = mDownGetter.getNumericalValue();
        } catch (Exception e){
            Toast.makeText(this,"Downvote error.", Toast.LENGTH_LONG).show();
            e.printStackTrace();
        }

        if(-1 != mDownvoteCount && -1 != mUpvoteCount) {
            runVoteCount();
        }
    }

    @Override
    public void onError(String ownerObjectId) {
        Toast.makeText(this, "Attribute Error", Toast.LENGTH_LONG).show();
    }
}
