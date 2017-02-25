package com.nostalgia.controller.capturemoment.review;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.text.Editable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AutoCompleteTextView;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.nostalgia.Nostalgia;
import com.nostalgia.controller.exoplayer.VideoPlayerFragment;
import com.nostalgia.persistence.model.Video;
import com.nostalgia.persistence.repo.UserRepository;
import com.nostalgia.persistence.repo.VideoRepository;
import com.vuescape.nostalgia.R;

import java.lang.reflect.Field;
import java.text.SimpleDateFormat;
import java.util.Date;


/**
 * Created by alex on 11/3/15.
 */
public class PlayerFragment extends Fragment implements VideoPlayerFragment.PlaylistController{
    private static final int PROFILE_SETTING = 1;

    public static final String TAG = "ViewerActivity";

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

    private VideoPlayerFragment videoPlayerFragment;

    private static final String SOUND_MUTE = "MUTE";
    private static final String SOUND_ENABLED = "ENABLED";
    Uri focusedFilePath = null;
    private VideoRepository vidRepo;
    private AutoCompleteTextView quickThoughtEdit;

    private ImageButton mute_unmute;
    private boolean mute = false;
    private TextView dateTimeMessage;


    private MuteListener mMuteListener;

    /*
     * Side drawer setup
     */
    private Nostalgia app ;
    private UserRepository userRepo;

    public interface MuteListener{
        void onMuteToggle(boolean isMuted);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (savedInstanceState != null) {
            videoPlayerFragment = (VideoPlayerFragment) getChildFragmentManager().findFragmentByTag("ReviewPlayerFragTag");
        } else {
            videoPlayerFragment = new VideoPlayerFragment();
            videoPlayerFragment.setIsLoopingVideo(true);
        }
    }

    private void prepareNextMediaItem(VideoPlayerFragment videoPlayerFragment){
        Intent intent = getActivity().getIntent();
        Uri contentUri = intent.getData();
        Integer contentType = intent.getIntExtra(CONTENT_TYPE_EXTRA, TYPE_OTHER);
    }

    /*
     * onPageExit is called by the mediaReviewerPagerActivity when a new page is focused on,
     * and that newly focused page is not reviewerFragmentContainer. Thus, this function
     * should except two cases:
     *  1. PlayerFragment had been the focus, but is not anymore.
     *  2. PlayerFragment had not been the focus, and still isn't.
     */
    public void onPageChange(int newPage){
        if(newPage == MediaReviewerPagerActivity.MEDIA_PAGE){
            onEnterPage();
        } else {
            onExitPage();
        }
    }

    public void onEnterPage(){
        videoPlayerFragment.backgrounded(false);
    }

    public void onExitPage(){
        videoPlayerFragment.backgrounded(true);
    }

    @Override
    public void onAttach(Activity activity){
        super.onAttach(activity);
        userRepo = ((Nostalgia)activity.getApplication()).getUserRepo();
        Log.d("ReviewerFragment", "Media Page Attach");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = super.onCreateView(inflater, container, savedInstanceState);

        View myView = inflater.inflate(R.layout.fragment_reviewer_player, container, false);

        getChildFragmentManager().beginTransaction().replace(R.id.review_player_fragment, videoPlayerFragment, "ReviewPlayerFragTag").commit();
        mainFragmentManager = getChildFragmentManager();

        mute_unmute = (ImageButton) myView.findViewById(R.id.mute_unmute);

        mute = false;

        if(mute){
            mute_unmute.setImageDrawable(getResources().getDrawable(R.drawable.mute));
            videoPlayerFragment.mute();
        } else {
            mute_unmute.setImageDrawable(getResources().getDrawable(R.drawable.volume));
            videoPlayerFragment.unmute();
        }

        setupMuteListener();

        quickThoughtEdit = (AutoCompleteTextView) myView.findViewById(R.id.edit_quick_thought);
        dateTimeMessage = (TextView) myView.findViewById(R.id.date_time_message);

        return myView;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        FrameLayout root = (FrameLayout) view.findViewById(R.id.review_player_root);

        int w = root.getMeasuredHeight();
        int h = root.getMeasuredWidth();
    }

    @Override
    public void onMediaEnd(){
    }

    @Override
    public void onPlaybackError(int errorcode) {
    }

    @Override
    public void onResume() {
        super.onResume();
        prepareNextMediaItem(videoPlayerFragment);
    }

    public void setMuteListener(MuteListener muteListener){
        mMuteListener = muteListener;
    }

    private void setupMuteListener(){
        mute_unmute.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (mute) {
                    //unmute
                    mute = false;
                    mute_unmute.setImageDrawable(getResources().getDrawable(R.drawable.volume));
                    videoPlayerFragment.unmute();
                } else {
                    mute = true;
                    mute_unmute.setImageDrawable(getResources().getDrawable(R.drawable.mute));
                    videoPlayerFragment.mute();
                }
                mMuteListener.onMuteToggle(mute);
            }
        });
    }

    private void displayDate(TextView dateTimeMessage){
        Date d = new Date();
        String date = d.toString();
        SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, yyyy");
        date = sdf.format(d);
        dateTimeMessage.setText(date);
    }

    @Override
    public void onDetach() {
        super.onDetach();

        try {
            Field childFragmentManager = Fragment.class.getDeclaredField("mChildFragmentManager");
            childFragmentManager.setAccessible(true);
            childFragmentManager.set(this, null);

        } catch (NoSuchFieldException e) {
            throw new RuntimeException(e);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    public void updateVideo(Video v){
        Editable quickThought = quickThoughtEdit.getEditableText();
        String thought = "";
        if(0 < quickThought.length()){
            thought = quickThought.toString();
        }

        v.getProperties().put("quick_thought", thought);
    }

    public boolean isMuted() {
        return mute;
    }
}
