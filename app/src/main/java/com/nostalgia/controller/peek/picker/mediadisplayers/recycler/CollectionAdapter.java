package com.nostalgia.controller.peek.picker.mediadisplayers.recycler;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.nostalgia.Nostalgia;
import com.nostalgia.controller.peek.picker.mediadisplayers.model.MediaCollectionWrapper;
import com.nostalgia.controller.peek.picker.mediadisplayers.viewholder.CollectionViewHolder;
import com.nostalgia.controller.peek.picker.mediadisplayers.viewholder.EmptySetViewHolder;
import com.nostalgia.controller.peek.picker.mediadisplayers.viewholder.HeaderViewHolder;
import com.nostalgia.controller.peek.picker.mediadisplayers.viewholder.LocationViewHolder;
import com.nostalgia.controller.peek.picker.mediadisplayers.viewholder.MediaViewHolder;
import com.nostalgia.persistence.model.MediaCollection;
import com.nostalgia.persistence.model.User;
import com.nostalgia.persistence.model.Video;
import com.nostalgia.persistence.repo.VideoRepository;
import com.nostalgia.runnable.ProtectedResourceGetterTask;
import com.tonicartos.superslim.LayoutManager;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ExecutionException;

/**
 * Created by alex on 11/19/15.
 */

public class CollectionAdapter extends
        RecyclerView.Adapter<MediaViewHolder> {

    public static final String TAG = "ContactAdapter";
    private final ArrayList<MediaCollectionWrapper> mCollectionWrappers;

    private static final int VIEW_TYPE_NEARBY = 0x00;
    private static final int VIEW_TYPE_HEADER = 0x01;
    private static final int VIEW_TYPE_LOCATION = 0x10;
    private static final int VIEW_TYPE_SUBSCRIBED = 0x11;
    private static final int VIEW_TYPE_EMPTYSET = 0x101;
    private static final int VIEW_TYPE_HEADER_PLUS = 0x110;
    private static final int VIEW_TYPE_COLLECTION = 0x111;

    private Context mContext;
    private int mHeaderDisplay;

    private Nostalgia mApp;
    private VideoRepository mVidRepo;
    private final BaseRecyclerFragment mFragment;

    private User mUser;

    public CollectionAdapter(Nostalgia nostalgia, BaseRecyclerFragment fragment){
        mApp = nostalgia;
        mFragment = fragment;
        mCollectionWrappers = new ArrayList<MediaCollectionWrapper>();

        mVidRepo = mApp.getVidRepo();
        mUser = mApp.getUserRepo().getLoggedInUser();
    }

    public CollectionAdapter(Nostalgia nostalgia, BaseRecyclerFragment fragment, boolean isToggleable){
        mApp = nostalgia;
        mFragment = fragment;
        mCollectionWrappers = new ArrayList<MediaCollectionWrapper>();

        mVidRepo = mApp.getVidRepo();
        mUser = mApp.getUserRepo().getLoggedInUser();
        mIsToggleable = isToggleable;
    }

    public CollectionAdapter(Nostalgia nostalgia, Context context, int headerMode, BaseRecyclerFragment fragment){
        mApp = nostalgia;
        mContext = context;
        mFragment = fragment;
        mHeaderDisplay = headerMode;
        mCollectionWrappers = new ArrayList<MediaCollectionWrapper>();

        mVidRepo = mApp.getVidRepo();
        mUser = mApp.getUserRepo().getLoggedInUser();
    }

    //Starts is the position of the header for each group/section.
    private HashMap<String, Integer> mSectionStarts = new HashMap<String,Integer>();
    //mSectionEnds is the position of the last item for each group section.
    private HashMap<String, Integer> mSectionEnds = new HashMap<String,Integer>();

    public enum HeaderAction{
        PLUS,
        SEARCH,
        NONE
    }

    private ArrayList<String> mSectionHeaders = new ArrayList<>();
    public void addGroup(String groupName, String groupDesc, ArrayList<MediaCollection> collections, MediaCollectionWrapper.WrapperType wrapper, HeaderAction action){

        //Allocate memory before for loop.
        final int NUM_HEADERS = 1;
        ArrayList<MediaCollectionWrapper> group = new ArrayList<>(collections.size() + NUM_HEADERS);

        MediaCollectionWrapper header = new MediaCollectionWrapper();
        int sectionFirstPosition = mCollectionWrappers.size();
        header.setSectionFirstPosition(sectionFirstPosition);

        if(action == HeaderAction.PLUS) {
            header.setWrapperType(MediaCollectionWrapper.WrapperType.HEADER_PLUS);
        } else if(action == HeaderAction.SEARCH){
            //Search isn't yet implemented yet but when it is -- change wrapper type here.
            header.setWrapperType(MediaCollectionWrapper.WrapperType.HEADER);
        } else {
            header.setWrapperType(MediaCollectionWrapper.WrapperType.HEADER);
        }


        header.setHeaderName(groupName);
        header.setHeaderDescription(groupDesc);

        mSectionHeaders.add(groupName);
        mSectionStarts.put(groupName, sectionFirstPosition);

        // Insert new header view and update section data.
        mCollectionWrappers.add(header);
        if(collections.size() > 0) {
            for (int x = 0; x < collections.size(); x++) {

                MediaCollectionWrapper target = new MediaCollectionWrapper();
                target.setWrapperType(wrapper);
                target.setMediaCollection(collections.get(x));
                target.setSectionFirstPosition(sectionFirstPosition);
                target.setIsSelected(false);
                mCollectionWrappers.add(target);
            }
        } else {
            MediaCollectionWrapper emptySet = new MediaCollectionWrapper();
            emptySet.setWrapperType(MediaCollectionWrapper.WrapperType.EMPTY_SET);
            emptySet.setHeaderName("Empty Set");
            emptySet.setEmptySetDescription("Nothing to see here.");
            emptySet.setSectionFirstPosition(sectionFirstPosition);
            mCollectionWrappers.add(emptySet);

        }
        //We use mSectionStarts to replace emptySet with the new collection.
        mSectionEnds.put(groupName, mCollectionWrappers.size() - 1);

        mCollectionWrappers.addAll(group);
    }

    public void addEmptyGroup(String groupName, String groupDesc, String emptySetMessage, MediaCollectionWrapper.WrapperType wrapper, HeaderAction action){

        //Allocate memory before for loop.
        final int NUM_HEADERS = 1;

        //1 header + 1 empty set = 2;
        int numItems = 2;
        ArrayList<MediaCollectionWrapper> group = new ArrayList<>(numItems);

        MediaCollectionWrapper header = new MediaCollectionWrapper();
        int sectionFirstPosition = mCollectionWrappers.size();
        header.setSectionFirstPosition(sectionFirstPosition);

        if(action == HeaderAction.PLUS) {
            header.setWrapperType(MediaCollectionWrapper.WrapperType.HEADER_PLUS);
        } else if(action == HeaderAction.SEARCH){
            //Search isn't yet implemented yet but when it is -- change wrapper type here.
            header.setWrapperType(MediaCollectionWrapper.WrapperType.HEADER);
        } else {
            header.setWrapperType(MediaCollectionWrapper.WrapperType.HEADER);
        }

        header.setHeaderName(groupName);
        header.setHeaderDescription(groupDesc);


        // Insert new header view and update section data.
        mCollectionWrappers.add(header);

        MediaCollectionWrapper emptySet = new MediaCollectionWrapper();
        emptySet.setWrapperType(MediaCollectionWrapper.WrapperType.EMPTY_SET);
        emptySet.setEmptySetDescription(emptySetMessage);
        emptySet.setSectionFirstPosition(sectionFirstPosition);
        mCollectionWrappers.add(emptySet);

        mCollectionWrappers.addAll(group);
    }

    public MediaCollection getItem(int position){
        MediaCollectionWrapper selected = mCollectionWrappers.get(position);
        return mCollectionWrappers.get(position).getMediaCollection();
    }

    private boolean mIsToggleable = false;
    public void setIsToggleable(boolean isToggleable){
        mIsToggleable = isToggleable;
    }

    public boolean getIsToggleable(){
        return mIsToggleable;
    }

    public void grabThumbnails(Video vid, LocationViewHolder vh) {
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

    public void grabThumbnails(Video vid, CollectionViewHolder vh){
        final Video toDisplay = vid;
        final CollectionViewHolder vhFinal = vh;
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
    public void onBindViewHolder(MediaViewHolder holder, int position) {
        final MediaCollectionWrapper item = mCollectionWrappers.get(position);

        // A content binding implementation.
        holder.bindParentActivity(mFragment.getActivity());
        holder.bindCurrentUser(mUser);
        holder.bindItem(item);

        View itemView = holder.itemView;

        /** Embed section configuration. **/
        final LayoutManager.LayoutParams params;
        params = (LayoutManager.LayoutParams) itemView.getLayoutParams();

        // Added this line to xml instead.

        // Position of the first item in the section. This doesn't have to
        // be a header. However, if an item is a header, it must then be the
        // first item in a section.
        params.setFirstPosition(item.getSectionFirstPosition());
        itemView.setLayoutParams(params);

        if(!item.getType().equals(MediaCollectionWrapper.WrapperType.HEADER)
                && !item.getType().equals(MediaCollectionWrapper.WrapperType.EMPTY_SET)
                && !item.getType().equals(MediaCollectionWrapper.WrapperType.HEADER_PLUS)){
            MediaCollection collection = item.getMediaCollection();

            Collection<String> vids = collection.getMatchingVideos().keySet();

            boolean picFlag = false;
            int ctr = 0;
            for(String id : vids){
                Video randomVid = mVidRepo.findOneById(id, true, false);
                if(null != randomVid) {
                    List<String> possible = randomVid.getThumbNails();
                    if (possible != null && 0 < possible.size()) {

                        Video hasThumbnail = mVidRepo.findOneById(id, true, false);
                        try {
                            grabThumbnails(hasThumbnail, (CollectionViewHolder) holder);
                        } catch (ClassCastException e){
                            grabThumbnails(hasThumbnail, (LocationViewHolder) holder);
                        }

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
        MediaCollectionWrapper.WrapperType t = mCollectionWrappers.get(position).getType();
        if(t == MediaCollectionWrapper.WrapperType.HEADER) {
            ret = VIEW_TYPE_HEADER;
        }else if(t == MediaCollectionWrapper.WrapperType.HEADER_PLUS){
            ret = VIEW_TYPE_HEADER_PLUS;
        } else if(t == MediaCollectionWrapper.WrapperType.PERSONAL ||
                t == MediaCollectionWrapper.WrapperType.SHARED ||
                t == MediaCollectionWrapper.WrapperType.PUBLIC){
            ret = VIEW_TYPE_COLLECTION;
        } else if(t == MediaCollectionWrapper.WrapperType.NEARBY ||
                t == MediaCollectionWrapper.WrapperType.SUBSCRIBED){
            ret = VIEW_TYPE_LOCATION;
        } else if(t == MediaCollectionWrapper.WrapperType.EMPTY_SET) {
            ret = VIEW_TYPE_EMPTYSET;
        } else {
            Log.e(TAG, "Unknown ItemType");
        }

        return ret;
    }

    @Override
    public MediaViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);

        // Inflate the custom layout
        View contactView;

        // Return a new holder instance using different views for each
        final MediaViewHolder viewHolder;
        switch (viewType) {
            case VIEW_TYPE_HEADER:
                contactView = inflater.inflate(HeaderViewHolder.VIEW_LAYOUT, parent, false);
                viewHolder = new HeaderViewHolder(contactView);
                break;
            case VIEW_TYPE_HEADER_PLUS:
                contactView = inflater.inflate(HeaderViewHolder.VIEW_LAYOUT_PLUS, parent, false);
                viewHolder = new HeaderViewHolder(contactView);
                ((HeaderViewHolder) viewHolder).setCreatorLauncher(mFragment.getCreatorLauncher());
                break;
            case VIEW_TYPE_LOCATION:
                contactView = inflater.inflate(CollectionViewHolder.VIEW_LAYOUT, parent, false);
                viewHolder = new LocationViewHolder(contactView);
                viewHolder.setSelectionListener(mFragment.getCallback());
                viewHolder.setIsToggleable(mIsToggleable);
                break;
            case VIEW_TYPE_COLLECTION:
                contactView = inflater.inflate(CollectionViewHolder.VIEW_LAYOUT, parent, false);
                viewHolder = new CollectionViewHolder(contactView);
                viewHolder.setSelectionListener(mFragment.getCallback());
                viewHolder.setIsToggleable(mIsToggleable);
                break;
            case VIEW_TYPE_EMPTYSET:
                contactView = inflater.inflate(EmptySetViewHolder.VIEW_LAYOUT, parent, false);
                viewHolder = new EmptySetViewHolder(contactView);
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
        return mCollectionWrappers.size();
    }

    private void notifyHeaderChanges() {
        for (int i = 0; i < mCollectionWrappers.size(); i++) {
            MediaCollectionWrapper item = mCollectionWrappers.get(i);
            if (item.isHeader()) {
                notifyItemChanged(i);
            }
        }
    }

    public void addItemToSection(MediaCollection toAdd, String section, MediaCollectionWrapper.WrapperType wrapper){
        //Decide where to put the new item:
        Integer firstPosition = mSectionStarts.get(section);
        Integer lastPosition = mSectionEnds.get(section);

        MediaCollectionWrapper target = new MediaCollectionWrapper();
        target.setWrapperType(wrapper);
        target.setMediaCollection(toAdd);
        target.setSectionFirstPosition(firstPosition);
        target.setIsSelected(false);

        boolean wasEmpty = MediaCollectionWrapper.WrapperType.EMPTY_SET == mCollectionWrappers.get(lastPosition).getType();

        if(wasEmpty) {
            mCollectionWrappers.remove(lastPosition);
            notifyItemRemoved(lastPosition);
            mCollectionWrappers.add(lastPosition, target);
            notifyItemInserted(lastPosition);
        } else {
            mCollectionWrappers.add(lastPosition + 1, target);
            mSectionEnds.put(section, lastPosition + 1);

            for(String key : mSectionStarts.keySet()){
                Integer oldStart = mSectionStarts.get(key);
                if(lastPosition < oldStart){
                    mSectionStarts.put(key, oldStart + 1);

                    Integer oldEnd = mSectionEnds.get(key);
                    mSectionEnds.put(key, oldEnd + 1);

                    for(Integer shifted = oldStart + 1; shifted < oldEnd + 1; shifted++){
                        mCollectionWrappers.get(shifted).setSectionFirstPosition(oldStart + 1);
                    }

                }
            }
            notifyItemInserted(lastPosition+1);
        }
    }

    public ArrayList<MediaCollectionWrapper> getCollectionWrappers(){
        return mCollectionWrappers;

    }
}
