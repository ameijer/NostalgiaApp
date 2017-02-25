package com.nostalgia.controller.peek.picker.mediadisplayers.viewholder;

import android.graphics.Bitmap;
import android.support.v7.widget.PopupMenu;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.nostalgia.controller.peek.picker.mediadisplayers.model.MediaCollectionWrapper;
import com.nostalgia.persistence.model.MediaCollection;
import com.nostalgia.runnable.DownloadImageTask;
import com.vuescape.nostalgia.R;

import java.util.List;
import java.util.Map;


/**
 * Created by Aidan on 12/11/15.
 */
public class LocationViewHolder extends MediaViewHolder {
    public static String TAG = "NativeViewHolder";

    private int mMenuType;

    private ImageView thumbnail;
    private TextView name;
    private TextView description;
    private TextView owner;
    private TextView numvids;
    private TextView numFans;

    private ImageView subscribeButton;
    private FrameLayout backFrame;

    private MediaCollectionWrapper mCollectionWrapper;

    public static int VIEW_LAYOUT = R.layout.slim_location_list_item;

    public LocationViewHolder(View itemView) {
        super(itemView);
        thumbnail = (ImageView) itemView.findViewById(R.id.location_thumbnail);
        name = (TextView) itemView.findViewById(R.id.location_name);
        description = (TextView) itemView.findViewById(R.id.location_description);
        owner = (TextView) itemView.findViewById(R.id.location_owner);
        backFrame = (FrameLayout) itemView.findViewById(R.id.item_background);

        subscribeButton = (ImageView) itemView.findViewById(R.id.location_subscriber);

        mType = ViewHolderType.COLLECTION;
    }

    @Override
    public void bindItem(MediaCollectionWrapper toDisplay) {
            /*
             * TODO: If toDisplay.getType = subscribe or known,
             * then change background color or adapter or something.
             */
        mCollectionWrapper = toDisplay;

        thumbnail.setImageBitmap(null);

        if(MediaCollectionWrapper.WrapperType.NEARBY == toDisplay.getType()) {
            bindNearbyLocation(toDisplay);
        } else if(MediaCollectionWrapper.WrapperType.SUBSCRIBED == toDisplay.getType()){
            bindSubscribedLocation(toDisplay);
        }
    }

    @Override
    public void onBind(){
    }

    private boolean mIsSubscribed = true;

    public ImageView getThumbnail(){
        return thumbnail;
    }

    private void bindMediaCollection(MediaCollection toDisplay, int menuType){

        /*
         * Location settings menu options
         */
        mMenuType = menuType;

        /*
         * Location title
         */
        try {
            String nameText = (toDisplay.getName() != null) ? toDisplay.getName() : "Somewhere";
            name.setText(nameText);
        } catch (Exception e) {
            e.printStackTrace();
        }

        /*
         * Collection creator
         */
        Map<String, String> props = toDisplay.getProperties();
        try {
            String creatorText;
            if(props != null && props.get("DESCRIPTION") != null) {
                creatorText = props.get("CREATOR_NAME");
            } else {
                creatorText = "";
            }

            owner.setText(creatorText);
        } catch (Exception e) {
            e.printStackTrace();
        }

        try {
            String descText;
            if(props != null && props.get("DESCRIPTION") != null) {
                descText = props.get("DESCRIPTION");
            } else {
                descText = "";
            }
            description.setText(descText);
        } catch (Exception e) {
            e.printStackTrace();
        }

        List<String> thumbnails = toDisplay.getThumbnails();
        if(thumbnails != null && thumbnails.size() > 0){
            new DownloadImageTask(thumbnail)
                    .execute(thumbnails.get(0));
        }

        if(getIsToggleable()){
            if(mCollectionWrapper.getIsSelected()){
                subscribeButton.setImageDrawable(getParentActivity().getResources().getDrawable(R.drawable.ic_check_black_36dp));
                subscribeButton.setImageAlpha(255);
            } else {
                subscribeButton.setImageDrawable(getParentActivity().getResources().getDrawable(R.drawable.ic_add_black_36dp));
                subscribeButton.setImageAlpha(120);
            }
        } else {
            subscribeButton.setImageDrawable(getParentActivity().getResources().getDrawable(R.drawable.ic_more_vert_gray_48dp));
        }

        subscribeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!getIsToggleable()) {
                    showFilterPopup(v, backFrame);
                } else {
                    mainClickAction();
                }
            }
        });

        backFrame.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mainClickAction();
                return;
            }
        });
    }

    private void mainClickAction(){
        if (getParentActivity() != null) {
            if (getIsToggleable()) {
                toggleSelection();
            }
            if (null != getSelectionListener()) {
                getSelectionListener().onSelection(mCollectionWrapper.getMediaCollection(), mCollectionWrapper.getIsSelected());
            }
        } else {
            Log.e(TAG, "Set item parent activity");
        }
    }


    private int mToggleOnIcon = R.drawable.ic_check_black_36dp;
    private int mToggleOffIcon = R.drawable.ic_add_black_36dp;
    public void setToggleOnIcon(int icon){
        mToggleOnIcon = icon;
    }

    //public void setToggleOnIcon

    private void toggleSelection(){
        boolean toggleSelected = !mCollectionWrapper.getIsSelected();
        mCollectionWrapper.setIsSelected(toggleSelected);
        if(toggleSelected) {
            subscribeButton.setImageDrawable(getParentActivity().getResources().getDrawable(R.drawable.ic_check_black_36dp));
            subscribeButton.setImageAlpha(255);
        } else {
            subscribeButton.setImageDrawable(getParentActivity().getResources().getDrawable(R.drawable.ic_add_black_36dp));
            subscribeButton.setImageAlpha(120);
        }
    }

    private void unhighlightSelection(View selection){
        final View shade = selection.findViewById(R.id.item_shade);

        AlphaAnimation fadeOutAnimation = new AlphaAnimation(1, 0); // mStart alpha, end alpha
        fadeOutAnimation.setDuration(300); // time for animation in milliseconds
        fadeOutAnimation.setFillAfter(true); // make the transformation persist
        fadeOutAnimation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationEnd(Animation animation) {
                shade.setVisibility(View.GONE);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {
            }

            @Override
            public void onAnimationStart(Animation animation) {
            }
        });

        shade.setAnimation(fadeOutAnimation);
    }

    private void highlightSelection(View selection){
        final View shade = selection.findViewById(R.id.item_shade);
        AlphaAnimation fadeInAnimation = new AlphaAnimation(0,1); // mStart alpha, end alpha
        fadeInAnimation.setDuration(300); // time for animation in milliseconds
        fadeInAnimation.setFillAfter(true); // make the transformation persist
        fadeInAnimation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationEnd(Animation animation) {
            }

            @Override
            public void onAnimationRepeat(Animation animation) {
            }

            @Override
            public void onAnimationStart(Animation animation) {
                shade.setAlpha(0);
                shade.setVisibility(View.VISIBLE);
            }
        });

        shade.setAnimation(fadeInAnimation);
    }

    private void showFilterPopup(View v, View selection) {
        final View currentSelection = selection;
        highlightSelection(currentSelection);
        PopupMenu popup = new PopupMenu(getParentActivity(), v);
        popup.setOnDismissListener(new PopupMenu.OnDismissListener() {
                                       @Override
                                       public void onDismiss(PopupMenu menu) {
                                           unhighlightSelection(currentSelection);
                                       }
                                   }
        );
        // Inflate the menu from xml
        popup.getMenuInflater().inflate(mMenuType, popup.getMenu());
        // Setup menu item selection
        popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            public boolean onMenuItemClick(MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.menu_unsubscribe:
                        //TODO: Implement play, manage, etc.
                        return true;
                    case R.id.menu_subscribe:
                        //TODO: Implement play, manage, etc.
                        return true;
                    default:
                        return false;
                }
            }
        });
        // Handle dismissal with: popup.setOnDismissListener(...);
        // Show the menu
        popup.show();
    }

    private void bindNearbyLocation(MediaCollectionWrapper wrapper){
        //Nothing specific to Nearby implemented yet.
        mCollectionWrapper = wrapper;
        bindMediaCollection(wrapper.getMediaCollection(), R.menu.location_nearby_options);
    }

    private void bindSubscribedLocation(MediaCollectionWrapper wrapper){
        //Nothing specific to Subscribed implemented yet.
        mCollectionWrapper = wrapper;
        bindMediaCollection(wrapper.getMediaCollection(), R.menu.location_trail_options);
    }

    public void setThumbnail(Bitmap bitmap){
        getThumbnail().setAlpha(1f);
        getThumbnail().setImageBitmap(bitmap);
    }
}