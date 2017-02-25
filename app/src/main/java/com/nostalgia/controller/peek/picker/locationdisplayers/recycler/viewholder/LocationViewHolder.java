package com.nostalgia.controller.peek.picker.locationdisplayers.recycler.viewholder;

import android.graphics.Bitmap;
import android.net.Uri;
import android.support.v4.widget.PopupMenuCompat;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.nostalgia.Nostalgia;
import com.nostalgia.controller.peek.picker.locationdisplayers.recycler.model.KnownLocationWrapper;
import com.nostalgia.persistence.model.KnownLocation;
import com.nostalgia.persistence.model.User;
import com.nostalgia.persistence.model.Video;
import com.nostalgia.runnable.DownloadImageTask;
import com.nostalgia.runnable.SubscriberThread;
import com.nostalgia.runnable.KnownLocationUNSubscribeThread;
import com.vuescape.nostalgia.R;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;


/**
 * Created by Aidan on 12/11/15.
 */
public class LocationViewHolder extends PeekViewHolder {
    public static String TAG = "OldLocationViewHolder";

    private int mMenuType;

    private ImageView thumbnail;
    private TextView name;
    private TextView description;
    private TextView owner;
    private TextView numvids;
    private TextView numFans;
    private View mShade;

    private ImageView subscribeButton;
    private FrameLayout backFrame;
    View mSpaceView;
    int mPosition;

    private KnownLocationWrapper mLocationWrapper;

    private Nostalgia mApp;

    public static int VIEW_LAYOUT = R.layout.slim_location_list_item;


    public LocationViewHolder(View itemView) {
        super(itemView);
        thumbnail = (ImageView) itemView.findViewById(R.id.location_thumbnail);
        name = (TextView) itemView.findViewById(R.id.location_name);
        //numvids = (TextView) itemView.findViewById(R.id.location_numvids);
        description = (TextView) itemView.findViewById(R.id.location_description);
        owner = (TextView) itemView.findViewById(R.id.location_owner);
        backFrame = (FrameLayout) itemView.findViewById(R.id.item_background);

        mShade = itemView.findViewById(R.id.item_shade);

        subscribeButton = (ImageView) itemView.findViewById(R.id.location_subscriber);

        mType = ViewHolderType.LOCATION;
        mSpaceView = itemView.findViewById(R.id.space);
    }

    @Override
    public void bindItem(KnownLocationWrapper toDisplay) {
            /*
             * TODO: If toDisplay.getType = subscribe or known,
             * then change background color or adapter or something.
             */
        mLocationWrapper = toDisplay;

        if(KnownLocationWrapper.WrapperType.NEARBY == toDisplay.getType()) {
            bindNearbyLocation(toDisplay);
        } else if(KnownLocationWrapper.WrapperType.SUBSCRIBED == toDisplay.getType()){
            bindSubscribedLocation(toDisplay);
        }
    }

    @Override
    public void onBind(){
        thumbnail.setImageDrawable(null);
    }

    private boolean mIsSubscribed = true;

    public ImageView getThumbnail(){
        return thumbnail;
    }

    private void bindKnownLocation(KnownLocation toDisplay, int menuType){

        /*
         * Location settings menu options
         */
        mMenuType = menuType;

        /*
         * Location title
         */
        try {
            if (toDisplay.getName() != null) {
                name.setText(toDisplay.getName());
            } else {
                name.setText("Somewhere");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        /*
         * Location creator
         */
        try {
            if (toDisplay.getCreatorId() != null) {
                owner.setText("Nostalgia Official");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        List<String> thumbnails = toDisplay.getThumbnails();
        if(thumbnails != null && thumbnails.size() > 0){
            //backdrop.setImageURI(Uri.parse(thumbnails.get(0)));
            new DownloadImageTask(thumbnail)
                    .execute(thumbnails.get(0));
        }


        subscribeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showFilterPopup(v, backFrame);
            }
        });

        backFrame.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (getParentActivity() != null) {
                    getSelectionListener().onLocationPicked(mLocationWrapper.getKnownLocation());
                } else {
                    Log.e(TAG, "Set item parent activity");
                }
                return;
            }
        });
    }

    private void unhighlightSelection(View selection){
        mShade.setVisibility(View.GONE);
    }

    private void highlightSelection(View selection){
        mShade.setVisibility(View.VISIBLE);
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
                        unsubscribeToLocation();
                        return true;
                    case R.id.menu_subscribe:
                        subscribeToLocation();
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

    private boolean unsubscribeToLocation(){
        KnownLocationUNSubscribeThread unSub = new KnownLocationUNSubscribeThread( mLocationWrapper.getKnownLocation().get_id(), getCurrentUser().get_id());
        unSub.start();
        try {
            unSub.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        boolean success = unSub.isUnsubscribed();
        if(success){
            Toast.makeText(getParentActivity(), mLocationWrapper.getKnownLocation().getName() + " forgotten.", Toast.LENGTH_LONG).show();
        }
        return success;
    }

    private boolean subscribeToLocation(){
        String locationId = mLocationWrapper.getKnownLocation().get_id();
        User subscriber = getCurrentUser();

        if (subscriber == null) {
            String msg = "error - must have logged in user to subscribe to location";
            Log.e(TAG, msg);
            return false;
        }

        SubscriberThread subscriberThread = new SubscriberThread(locationId, subscriber.get_id());
        subscriberThread.start();

        try {
            subscriberThread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }


        String subscribedTo = subscriberThread.getSubscribed();

        boolean success;
        success = subscribedTo != null;
        mLocationWrapper.setIsSubscribed(success);
        return success;
    }

    private void bindNearbyLocation(KnownLocationWrapper wrapper){
        //Nothing specific to Nearby implemented yet.
        mLocationWrapper = wrapper;
        bindKnownLocation(wrapper.getKnownLocation(), R.menu.location_nearby_options);
    }

    private void bindSubscribedLocation(KnownLocationWrapper wrapper){
        //Nothing specific to Subscribed implemented yet.
        mLocationWrapper = wrapper;
        bindKnownLocation(wrapper.getKnownLocation(), R.menu.location_trail_options);
    }

    private void bindHeader(KnownLocationWrapper wrapper){
        name.setText("Header");
    }

    public void setThumbnail(Bitmap bitmap){
        getThumbnail().setAlpha(1f);
        getThumbnail().setImageBitmap(bitmap);
    }

}