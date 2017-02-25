package com.nostalgia.controller.peek.picker.locationdisplayers.recycler;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v4.app.Fragment;
import android.support.v7.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.nostalgia.Nostalgia;
import com.nostalgia.controller.peek.picker.locationdisplayers.recycler.model.KnownLocationWrapper;
import com.nostalgia.controller.peek.picker.locationdisplayers.recycler.viewholder.HeaderViewHolder;
import com.nostalgia.controller.peek.picker.locationdisplayers.recycler.viewholder.LocationViewHolder;
import com.nostalgia.controller.peek.picker.locationdisplayers.recycler.viewholder.PeekViewHolder;
import com.nostalgia.persistence.model.KnownLocation;

import com.nostalgia.persistence.model.MediaCollection;
import com.nostalgia.persistence.model.User;
import com.nostalgia.persistence.model.Video;
import com.nostalgia.persistence.repo.MediaCollectionRepository;
import com.nostalgia.persistence.repo.VideoRepository;
import com.nostalgia.runnable.ProtectedResourceGetterTask;
import com.tonicartos.superslim.LinearSLM;


import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

/**
 * Created by alex on 11/19/15.
 */

public class LocationRecyclerAdapter extends
        RecyclerView.Adapter<PeekViewHolder> {

    public static final String TAG = "ContactAdapter";
    private final ArrayList<KnownLocationWrapper> mLocationWrappers;

    private static final int VIEW_TYPE_NEARBY = 0x00;
    private static final int VIEW_TYPE_HEADER = 0x01;
    private static final int VIEW_TYPE_LOCATION = 0x10;
    private static final int VIEW_TYPE_SUBSCRIBED = 0x11;

    private Context mContext;
    private int mHeaderDisplay;

    private Nostalgia mApp;
    private VideoRepository mVidRepo;
    private MediaCollectionRepository collRepo;
    private final Fragment mFragment;

    private User mUser;

    public LocationRecyclerAdapter(Nostalgia nostalgia, Fragment fragment){
        mApp = nostalgia;
        mFragment = fragment;
        mLocationWrappers = new ArrayList<KnownLocationWrapper>();
        collRepo = mApp.getCollRepo();
        mVidRepo = mApp.getVidRepo();
        mUser = mApp.getUserRepo().getLoggedInUser();
    }

    public LocationRecyclerAdapter(Nostalgia nostalgia, Context context, int headerMode, Fragment fragment){
        mApp = nostalgia;
        mContext = context;
        mFragment = fragment;
        mHeaderDisplay = headerMode;
        mLocationWrappers = new ArrayList<KnownLocationWrapper>();

        mVidRepo = mApp.getVidRepo();
        collRepo = mApp.getCollRepo();
        mUser = mApp.getUserRepo().getLoggedInUser();
    }

    public void addGroup(String groupName, String groupDesc,List<KnownLocation> locations, KnownLocationWrapper.WrapperType wrapper){

        //Allocate memory before for loop.
        final int NUM_HEADERS = 1;
        ArrayList<KnownLocationWrapper> group = new ArrayList<>(locations.size() + NUM_HEADERS);

        KnownLocationWrapper header = new KnownLocationWrapper();
        int sectionFirstPosition = mLocationWrappers.size();
        header.setSectionFirstPosition(sectionFirstPosition);
        header.setWrapperType(KnownLocationWrapper.WrapperType.HEADER);
        header.setHeaderName(groupName);
        header.setHeaderDescription(groupDesc);

        /*
         * TODO:
         * Temporary: add thumbnails to KnownLocations
         */

        /*
         * Location thumbnail
         */

        // Insert new header view and update section data.
        mLocationWrappers.add(header);

        for (int x = 0; x < locations.size(); x++) {
            KnownLocationWrapper target = new KnownLocationWrapper();
            target.setWrapperType(wrapper);
            target.setKnownLocation(locations.get(x));
            target.setSectionFirstPosition(sectionFirstPosition);
            mLocationWrappers.add(target);
        }

        mLocationWrappers.addAll(group);
    }


    public void removeAll(){
        mLocationWrappers.clear();
    }


    public KnownLocation getItem(int position){
        KnownLocationWrapper selected = mLocationWrappers.get(position);
        return mLocationWrappers.get(position).getKnownLocation();
    }

    public void grabThumbnails(Video vid, LocationViewHolder vh){
        final Video toDisplay = vid;
        final LocationViewHolder vhFinal = vh;
        if (toDisplay.getThumbNails() != null && toDisplay.getThumbNails().size() > 0) {
            final String url = toDisplay.getThumbNails().get(0);
            new Thread() {
                @Override
                public void run() {
                    File sdCard = mFragment.getActivity().getFilesDir();
                    File dir = new File(sdCard, "cache/images");
                    dir.mkdirs();
                    final File img = new File(dir, "screenshot-" +toDisplay.get_id() + ".jpg");
                    if(!img.exists()) {

                        ProtectedResourceGetterTask getter = new ProtectedResourceGetterTask(mUser.getStreamTokens(), img, url);

                        getter.execute();
                        try {
                            getter.get();
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        } catch (ExecutionException e) {
                            e.printStackTrace();
                        }
                    }
                    if(img.exists()) {

                        Log.i(TAG, "File: " + img.getName() + "  last accessed on: " + new Date(img.lastModified()));
                        img.setLastModified(System.currentTimeMillis());
                        final Bitmap myBitmap = BitmapFactory.decodeFile(img.getAbsolutePath());

                        Runnable run = new Runnable() {

                            public void run() {
                                vhFinal.setThumbnail(myBitmap);
                            }
                        };
                        mFragment.getActivity().runOnUiThread(run);
                    }

                }

            }.start();
        }
    }


    // Involves populating data into the item through holder
    @Override
    public void onBindViewHolder(PeekViewHolder holder, int position) {
        final KnownLocationWrapper item = mLocationWrappers.get(position);

        // A content binding implementation.
        holder.bindParentActivity(mFragment.getActivity());
        holder.bindCurrentUser(mUser);
        holder.bindItem(item);

        View itemView = holder.itemView;

        /** Embed section configuration. **/
        try {
            final com.tonicartos.superslim.LayoutManager.LayoutParams params = (com.tonicartos.superslim.LayoutManager.LayoutParams) itemView.getLayoutParams();

            // Added this line to xml instead.

            // Position of the first item in the section. This doesn't have to
            // be a header. However, if an item is a header, it must then be the
            // first item in a section.


            params.setFirstPosition(item.getSectionFirstPosition());

            itemView.setLayoutParams(params);
        } catch(ClassCastException e){
            final RecyclerView.LayoutParams params = (RecyclerView.LayoutParams) itemView.getLayoutParams();

            itemView.setLayoutParams(params);
        }

        if(holder.getType() == PeekViewHolder.ViewHolderType.LOCATION){
            KnownLocation location = item.getKnownLocation();

            String collectionId = location.getLocationCollections().get("primary");

            MediaCollection matchingColl = collRepo.findOneById(collectionId);

            Map<String,String> vids = matchingColl.getMatchingVideos();
            Object [] prior = vids.keySet().toArray();

            boolean picFlag = false;
            int ctr = 0;
            while(ctr < prior.length && !picFlag) {
                String id = vids.get(prior[ctr]);
                Video randomVid = mVidRepo.findOneById(id, true, false);
                if(null != randomVid) {
                    List<String> possible = randomVid.getThumbNails();
                    if (possible != null && 0 < possible.size()) {

                        Video hasThumbnail = mVidRepo.findOneById(id, true, false);
                        grabThumbnails(hasThumbnail, (LocationViewHolder) holder);

                        break;
                    }
                }
                ctr++;
            }
        }

    }

    /*
     * Extended classes should define mItemLayout, or make a call to setItemlayout();
     */
    public int mItemLayout = -1;

    @Override
    public int getItemViewType(int position) {
        int ret = -1;
        KnownLocationWrapper.WrapperType t = mLocationWrappers.get(position).getType();
        if(t == KnownLocationWrapper.WrapperType.HEADER){
            ret = VIEW_TYPE_HEADER;
        } else if(t == KnownLocationWrapper.WrapperType.NEARBY ||
                t == KnownLocationWrapper.WrapperType.SUBSCRIBED){
            ret = VIEW_TYPE_LOCATION;
        } else {
            Log.e(TAG, "Unknown ItemType");
        }

        return ret;
    }

    @Override
    public PeekViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);

        // Inflate the custom layout
        View contactView;

        // Return a new holder instance using different views for each
        final PeekViewHolder viewHolder;
        switch (viewType) {
            case VIEW_TYPE_HEADER:
                contactView = inflater.inflate(HeaderViewHolder.VIEW_LAYOUT, parent, false);
                viewHolder = new HeaderViewHolder(contactView);
                break;
            case VIEW_TYPE_LOCATION:
                contactView = inflater.inflate(LocationViewHolder.VIEW_LAYOUT, parent, false);
                viewHolder = new LocationViewHolder(contactView);
                if(mFragment instanceof BaseRecyclerFragment) {
                    viewHolder.setSelectionListener(((BaseRecyclerFragment) mFragment).getCallback());
                }
                break;
            default:
                Log.e(TAG, "Unsupported viewType type");
                viewHolder = null;
        }
        return viewHolder;
    }


    // Return the total count of items
    @Override
    public int getItemCount() {
        return mLocationWrappers.size();
    }

    private void notifyHeaderChanges() {
        for (int i = 0; i < mLocationWrappers.size(); i++) {
            KnownLocationWrapper item = mLocationWrappers.get(i);
            if (item.isHeader()) {
                notifyItemChanged(i);
            }
        }
    }

}
