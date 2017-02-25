/*
 * Copyright (C) 2014 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.nostalgia.controller.exoplayer;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.graphics.SurfaceTexture;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnKeyListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.accessibility.CaptioningManager;
import android.widget.MediaController;
import android.widget.PopupMenu;
import android.widget.PopupMenu.OnMenuItemClickListener;
import android.widget.Toast;

import com.google.android.exoplayer.AspectRatioFrameLayout;
import com.google.android.exoplayer.ExoPlayer;
import com.google.android.exoplayer.MediaFormat;
import com.google.android.exoplayer.audio.AudioCapabilities;
import com.google.android.exoplayer.audio.AudioCapabilitiesReceiver;
import com.google.android.exoplayer.drm.UnsupportedDrmException;
import com.google.android.exoplayer.metadata.GeobMetadata;
import com.google.android.exoplayer.metadata.PrivMetadata;
import com.google.android.exoplayer.metadata.TxxxMetadata;
import com.google.android.exoplayer.text.CaptionStyleCompat;
import com.google.android.exoplayer.text.Cue;
import com.google.android.exoplayer.text.SubtitleLayout;
import com.google.android.exoplayer.util.MimeTypes;
import com.google.android.exoplayer.util.Util;
import com.nostalgia.controller.exoplayer.player.DashRendererBuilder;
import com.nostalgia.controller.exoplayer.player.DemoPlayer;
import com.nostalgia.controller.exoplayer.player.DemoPlayer.RendererBuilder;
import com.nostalgia.controller.exoplayer.player.ExtractorRendererBuilder;
import com.nostalgia.controller.exoplayer.player.HlsRendererBuilder;
import com.nostalgia.controller.exoplayer.player.SmoothStreamingRendererBuilder;
import com.nostalgia.persistence.model.Video;
import com.nostalgia.view.VideoTextureView;
import com.vuescape.nostalgia.R;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;


/**
 * An activity that plays media using {@link DemoPlayer}.
 */
public class VideoPlayerFragment extends Fragment implements TextureView.SurfaceTextureListener, OnClickListener,
    DemoPlayer.Listener, DemoPlayer.CaptionListener, DemoPlayer.Id3MetadataListener,
    AudioCapabilitiesReceiver.Listener {

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

    private static final String TAG = "VideoPlayerFragment";
    private static final int MENU_GROUP_TRACKS = 1;
    private static final int ID_OFFSET = 2;


    private EventLogger eventLogger;
    private MediaController mediaController;
    private View shutterView;
    private AspectRatioFrameLayout videoFrame;
    private VideoTextureView mTextureView;
    private SubtitleLayout subtitleLayout;

    //public EpochSeeker mEpochSeeker;

    private DemoPlayer player;
    private boolean playerNeedsPrepare;
    private Video playing;

    private long playerPosition;
    private boolean enableBackgroundAudio = false;

    private Uri contentUri;
    private int contentType;
    private String contentId;
    private ArrayList<String> contentStringList;
    private ArrayList<Uri> contentUriList = new ArrayList<Uri>();
    private AudioCapabilitiesReceiver audioCapabilitiesReceiver;

    private PlaylistController mPlaylistController;

    public interface PlaylistController{
        void onMediaEnd();
        void onPlaybackError(int errorcode);
    }
    
    private FloatingActionButton floatingActionButton;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = super.onCreateView(inflater, container, savedInstanceState);

        View myView = inflater.inflate(R.layout.fragment_large_player, container, false);

        View root = myView.findViewById(R.id.large_player_fragment_root);
        root.setOnTouchListener(new OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                if (motionEvent.getAction() == MotionEvent.ACTION_DOWN) {
                } else if (motionEvent.getAction() == MotionEvent.ACTION_UP) {
                    view.performClick();
                    //startNextVideo();
                }
                return true;
            }
        });

        root.setOnKeyListener(new OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (keyCode == KeyEvent.KEYCODE_BACK || keyCode == KeyEvent.KEYCODE_ESCAPE
                        || keyCode == KeyEvent.KEYCODE_MENU) {
                    return false;
                }
                return mediaController.dispatchKeyEvent(event);
            }
        });

        shutterView = myView.findViewById(R.id.shutter);

        mTextureView = (VideoTextureView) myView.findViewById(R.id.texture_view);
        mTextureView.setSurfaceTextureListener(this);

        /*
        mTextureView.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mPlaylistController != null) {
                    mPlaylistController.onMediaEnd();
                }
            }
        });


        mTextureView.setOnTouchListener(new OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                if (motionEvent.getAction() == MotionEvent.ACTION_DOWN) {
                    //toggleControlsVisibility();
                } else if (motionEvent.getAction() == MotionEvent.ACTION_UP) {
                    view.performClick();

                }
                return true;
            }
        });
        */


        //subtitleLayout = (SubtitleLayout) myView.findViewById(R.id.subtitles);

        mediaController = new MediaController(getActivity());
        mediaController.setAnchorView(root);

        audioCapabilitiesReceiver = new AudioCapabilitiesReceiver(getActivity(), this);
        audioCapabilitiesReceiver.register();

        return myView;
    }

//    public static ViewerPlayerFragment newInstance(Video thisVideo) {
//        ViewerPlayerFragment myFragment = new ViewerPlayerFragment();
//
//        Bundle args = new Bundle();
//        args.putSerializable("video", thisVideo);
//        myFragment.setArguments(args);
//
//        return myFragment;
//    }



    public void setPlaylistController(PlaylistController controller ){
        mPlaylistController = controller;
    }

    public void setNextMedia(Video nextMedia){
        playing = nextMedia;
    }

    public void setIsLoopingVideo(boolean loopingVideo){
        mIsLoopingVideo = loopingVideo;
    }

    @Override
    public void onDestroyView(){
        super.onDestroyView();
        audioCapabilitiesReceiver.unregister();
    }

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    }

    public void pause(){
        player.pause();
    }
    public void play(){
        player.setPlayWhenReady(true);
    }
    public void backgrounded(boolean bg){
        player.setBackgrounded(bg);
    }

    @Override
    public void onResume() {
        super.onResume();
        Intent intent = getActivity().getIntent();

        contentUri = intent.getData();
        contentType = intent.getIntExtra(CONTENT_TYPE_EXTRA, TYPE_HLS);
        contentId = intent.getStringExtra(CONTENT_ID_EXTRA);
        contentStringList = intent.getStringArrayListExtra(URI_LIST_EXTRA);
        if(playing == null){
            //TODO: Call storyline over
        }

        if (player == null) {
            try {
                preparePlayer(contentUri);
            } catch (NullPointerException e){
                Log.e(TAG, "Error prepping player", e);
            }
        } else {
            player.setBackgrounded(false);
        }
    }
    //TODO: instead of releasePlayer() on activity pause, it should simply pause the video
    // and the widget should let you play it in the background.
    @Override
    public void onPause() {
        super.onPause();
        if (!enableBackgroundAudio) {
            releasePlayer();
        } else {
            player.setBackgrounded(true);
        }
        shutterView.setVisibility(View.VISIBLE);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        releasePlayer();
    }

    // OnClickListener methods

    @Override
    public void onClick(View view) {

        //if (videoFrame == view) {
        //  startNextVideo();
        //}

    }


    // AudioCapabilitiesReceiver.Listener methods

    @Override
    public void onAudioCapabilitiesChanged(AudioCapabilities audioCapabilities) {
        if (player == null) {
            return;
        }
        boolean backgrounded = player.getBackgrounded();
        boolean playWhenReady = player.getPlayWhenReady();
        releasePlayer();
        preparePlayer(contentUri);
        player.setBackgrounded(backgrounded);
    }

  // Internal methods

    private RendererBuilder getRendererBuilder() {
        String userAgent = Util.getUserAgent(getActivity(), "ExoPlayerDemo");
        switch (contentType) {
            case TYPE_SS:
                return new SmoothStreamingRendererBuilder(getActivity(), userAgent, contentUri.toString(),
                        new SmoothStreamingTestMediaDrmCallback());
            case TYPE_DASH:
                return new DashRendererBuilder(getActivity(), userAgent, contentUri.toString(),
                        new WidevineTestMediaDrmCallback(contentId));
            case TYPE_HLS:
                //return new HlsRendererBuilder(getActivity(), userAgent, contentUri.toString());
                return new HlsRendererBuilder(getActivity(), userAgent, playing.getUrl());
            case TYPE_OTHER:
                return new ExtractorRendererBuilder(getActivity(), userAgent, contentUri);
            default:
                Toast.makeText(getActivity(), "Renderer builder unsupported type: " + contentType, Toast.LENGTH_LONG).show();
                throw new IllegalStateException("Unsupported type: " + contentType);
        }
    }

    private void preparePlayer(Uri playWhenReady) {
        //if (player == null) {
        player = new DemoPlayer(getRendererBuilder());
        player.addListener(this);
        player.setCaptionListener(this);
        player.setMetadataListener(this);
        player.seekTo(playerPosition);
        playerNeedsPrepare = true;
        mediaController.setMediaPlayer(player.getPlayerControl());
        mediaController.setEnabled(true);
        eventLogger = new EventLogger();
        eventLogger.startSession();
        player.addListener(eventLogger);
        player.setInfoListener(eventLogger);
        player.setInternalErrorListener(eventLogger);
        //debugViewHelper = new DebugTextViewHelper(player, debugTextView);
        //debugViewHelper.mStart();
        //}
        if (playerNeedsPrepare) {
            player.prepare(playWhenReady);
            playerNeedsPrepare = false;
        }


        //player.setSurface(surfaceView.getHolder().getSurface());
        player.setPlayWhenReady(true);
    }


  private void releasePlayer() {
    if (player != null) {
      playerPosition = player.getCurrentPosition();
      player.release();
      player = null;
      eventLogger.endSession();
      eventLogger = null;
    }
  }


    private boolean mIsLoopingVideo = false;
    // DemoPlayer.Listener implementation
    @Override
    public void onStateChanged(boolean playWhenReady, int playbackState) {

        switch(playbackState) {
            case ExoPlayer.STATE_BUFFERING:
                break;
            case ExoPlayer.STATE_ENDED:
                if(!mIsLoopingVideo) {
                    if (mPlaylistController != null) {
                        mPlaylistController.onMediaEnd();
                    }
                } else {
                    player.seekTo(0);
                    player.setPlayWhenReady(true);
                }

                break;
            case ExoPlayer.STATE_IDLE:
                break;
            case ExoPlayer.STATE_PREPARING:
                break;
            case ExoPlayer.STATE_READY:
                break;
            default:
                break;
        }
    }

    @Override
    public void onError(Exception e) {
        if (e instanceof UnsupportedDrmException) {
            // Special case DRM failures.
            UnsupportedDrmException unsupportedDrmException = (UnsupportedDrmException) e;
            int stringId = Util.SDK_INT < 18 ? R.string.drm_error_not_supported
                    : unsupportedDrmException.reason == UnsupportedDrmException.REASON_UNSUPPORTED_SCHEME
                    ? R.string.drm_error_unsupported_scheme : R.string.drm_error_unknown;
            Toast.makeText(getActivity(), stringId, Toast.LENGTH_LONG).show();
        }
        playerNeedsPrepare = true;
    }

    @Override
  public void onVideoSizeChanged(int width, int height, int unappliedRotationDegrees,
      float pixelWidthAspectRatio) {

        //TODO: Does this belong? does onVideoSizeChange get called elsewhere? Should it be?
        mTextureView.onVideoSizeChanged(width, height, unappliedRotationDegrees, pixelWidthAspectRatio);
        shutterView.setVisibility(View.GONE);

        //ajh exoplayer fitting properly
        //videoFrame.setAspectRatio(
        //    height == 0 ? 1 : (width * pixelWidthAspectRatio) / height);
  }

  // User controls

  private boolean haveTracks(int type) {
    return player != null && player.getTrackCount(type) > 0;
  }

  private void configurePopupWithTracks(PopupMenu popup,
      final OnMenuItemClickListener customActionClickListener,
      final int trackType) {
    if (player == null) {
      return;
    }
    int trackCount = player.getTrackCount(trackType);
    if (trackCount == 0) {
      return;
    }
    popup.setOnMenuItemClickListener(new OnMenuItemClickListener() {
      @Override
      public boolean onMenuItemClick(MenuItem item) {
        return (customActionClickListener != null
            && customActionClickListener.onMenuItemClick(item))
            || onTrackItemClick(item, trackType);
      }
    });
    Menu menu = popup.getMenu();
    // ID_OFFSET ensures we avoid clashing with Menu.NONE (which equals 0)
    menu.add(MENU_GROUP_TRACKS, DemoPlayer.TRACK_DISABLED + ID_OFFSET, Menu.NONE, R.string.off);
    for (int i = 0; i < trackCount; i++) {
      menu.add(MENU_GROUP_TRACKS, i + ID_OFFSET, Menu.NONE,
          buildTrackName(player.getTrackFormat(trackType, i)));
    }
    menu.setGroupCheckable(MENU_GROUP_TRACKS, true, true);
    menu.findItem(player.getSelectedTrack(trackType) + ID_OFFSET).setChecked(true);
  }

  private static String buildTrackName(MediaFormat format) {
    if (format.adaptive) {
      return "auto";
    }
    String trackName;
    if (MimeTypes.isVideo(format.mimeType)) {
      trackName = joinWithSeparator(joinWithSeparator(buildResolutionString(format),
          buildBitrateString(format)), buildTrackIdString(format));
    } else if (MimeTypes.isAudio(format.mimeType)) {
      trackName = joinWithSeparator(joinWithSeparator(joinWithSeparator(buildLanguageString(format),
          buildAudioPropertyString(format)), buildBitrateString(format)),
          buildTrackIdString(format));
    } else {
      trackName = joinWithSeparator(joinWithSeparator(buildLanguageString(format),
          buildBitrateString(format)), buildTrackIdString(format));
    }
    return trackName.length() == 0 ? "unknown" : trackName;
  }

  private static String buildResolutionString(MediaFormat format) {
    return format.width == MediaFormat.NO_VALUE || format.height == MediaFormat.NO_VALUE
        ? "" : format.width + "x" + format.height;
  }

  private static String buildAudioPropertyString(MediaFormat format) {
    return format.channelCount == MediaFormat.NO_VALUE || format.sampleRate == MediaFormat.NO_VALUE
        ? "" : format.channelCount + "ch, " + format.sampleRate + "Hz";
  }

  private static String buildLanguageString(MediaFormat format) {
    return TextUtils.isEmpty(format.language) || "und".equals(format.language) ? ""
        : format.language;
  }

  private static String buildBitrateString(MediaFormat format) {
    return format.bitrate == MediaFormat.NO_VALUE ? ""
        : String.format(Locale.US, "%.2fMbit", format.bitrate / 1000000f);
  }

  private static String joinWithSeparator(String first, String second) {
    return first.length() == 0 ? second : (second.length() == 0 ? first : first + ", " + second);
  }

  private static String buildTrackIdString(MediaFormat format) {
    return format.trackId == null ? ""
        : String.format(Locale.US, " (%d)", format.trackId);
  }

  private boolean onTrackItemClick(MenuItem item, int type) {
    if (player == null || item.getGroupId() != MENU_GROUP_TRACKS) {
      return false;
    }
    player.setSelectedTrack(type, item.getItemId() - ID_OFFSET);
    return true;
  }

  private void toggleControlsVisibility()  {
      mediaController.hide();
  }

  // DemoPlayer.CaptionListener implementation

  @Override
  public void onCues(List<Cue> cues) {
    subtitleLayout.setCues(cues);
  }

  // DemoPlayer.MetadataListener implementation

  @Override
  public void onId3Metadata(Map<String, Object> metadata) {
    for (Map.Entry<String, Object> entry : metadata.entrySet()) {
      if (TxxxMetadata.TYPE.equals(entry.getKey())) {
        TxxxMetadata txxxMetadata = (TxxxMetadata) entry.getValue();
        Log.i(TAG, String.format("ID3 TimedMetadata %s: description=%s, value=%s",
            TxxxMetadata.TYPE, txxxMetadata.description, txxxMetadata.value));
      } else if (PrivMetadata.TYPE.equals(entry.getKey())) {
        PrivMetadata privMetadata = (PrivMetadata) entry.getValue();
        Log.i(TAG, String.format("ID3 TimedMetadata %s: owner=%s",
            PrivMetadata.TYPE, privMetadata.owner));
      } else if (GeobMetadata.TYPE.equals(entry.getKey())) {
        GeobMetadata geobMetadata = (GeobMetadata) entry.getValue();
        Log.i(TAG, String.format("ID3 TimedMetadata %s: mimeType=%s, filename=%s, description=%s",
            GeobMetadata.TYPE, geobMetadata.mimeType, geobMetadata.filename,
            geobMetadata.description));
      } else {
        Log.i(TAG, String.format("ID3 TimedMetadata %s", entry.getKey()));
      }
    }
  }

    /*
     * SurfaceTextureListeners:
     */

    @Override
    public void onSurfaceTextureAvailable(SurfaceTexture surface, int width,
    int height) {
        if (player != null) {
            player.setSurface(new Surface(surface));
        }
    }

    @Override
    public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width,
    int height) {
        //no op
    }

    @Override
    public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
        if (player != null) {
            player.blockingClearSurface();
        }
        playerNeedsPrepare = true;
        return true;
    }

    @Override
    public void onSurfaceTextureUpdated(SurfaceTexture surface) {
        //no op
    }

  @TargetApi(19)
  private float getUserCaptionFontScaleV19() {
    CaptioningManager captioningManager =
        (CaptioningManager) getActivity().getSystemService(Context.CAPTIONING_SERVICE);
    return captioningManager.getFontScale();
  }

  @TargetApi(19)
  private CaptionStyleCompat getUserCaptionStyleV19() {
    CaptioningManager captioningManager =
        (CaptioningManager) getActivity().getSystemService(Context.CAPTIONING_SERVICE);
    return CaptionStyleCompat.createFromCaptionStyle(captioningManager.getUserStyle());
  }

  /**
   * Makes a best guess to infer the type from a media {@link Uri} and an optional overriding file
   * extension.
   *
   * @param uri The {@link Uri} of the media.
   * @param fileExtension An overriding file extension.
   * @return The inferred type.
   */
  private static int inferContentType(Uri uri, String fileExtension) {
    String lastPathSegment = !TextUtils.isEmpty(fileExtension) ? "." + fileExtension
        : uri.getLastPathSegment();
    if (lastPathSegment == null) {
      return TYPE_OTHER;
    } else if (lastPathSegment.endsWith(EXT_DASH)) {
      return TYPE_DASH;
    } else if (lastPathSegment.endsWith(EXT_SS)) {
      return TYPE_SS;
    } else if (lastPathSegment.endsWith(EXT_HLS)) {
      return TYPE_HLS;
    } else {
      return TYPE_OTHER;
    }
  }

    private int contentUriIndex = 1;
    public void showNextMedia(){

        Uri nextUri = Uri.parse(playing.getUrl());
        contentUri = nextUri;
        //TODO: Infer renderer -- shouldn't be switching that up between videos though.
        player.release();
        //player.prepare(contentUri);
        preparePlayer(contentUri);
        // player.setPlayWhenReady(true);
        // playerNeedsPrepare = false;
        contentUriIndex = contentUriIndex + 1;
    }

    boolean muted = false;
    public void unmute() {
        if(player != null) {
            player.setMute(false);
        }
        muted = false;
    }

    public void mute() {
        if(player != null) {
            player.setMute(true);
        }
        muted = true;
    }
}
