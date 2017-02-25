package com.nostalgia.menu.friends.recycler;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.nostalgia.Nostalgia;
import com.nostalgia.menu.friends.model.ContactCard;
import com.nostalgia.menu.friends.model.PersonWrapper;
import com.nostalgia.menu.friends.viewholder.NativeViewHolder;
import com.nostalgia.menu.friends.viewholder.EmptySetViewHolder;
import com.nostalgia.menu.friends.viewholder.HeaderViewHolder;
import com.nostalgia.menu.friends.viewholder.BaseHolder;
import com.nostalgia.persistence.model.User;
import com.nostalgia.persistence.repo.VideoRepository;
import com.tonicartos.superslim.LayoutManager;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by alex on 11/19/15.
 */

public class ContactAdapter extends
        RecyclerView.Adapter<BaseHolder> {

    public static final String TAG = "ContactAdapter";
    private final ArrayList<PersonWrapper> mContactWrappers;

    /*
     * These are used to grab different layout files. "Contact" viewtype is currently used to display
     * all the different types of contact models. If we needed to add an extra line or, for instance,
     * a "twitter || fb || google" icon, we would add a VIEW_TYPE_SOCIAL_CONTACT that pulls
     * in a different layout file. See getItemViewType, below
     */
    private static final int VIEW_TYPE_HEADER = 0x01;
    private static final int VIEW_TYPE_EMPTYSET = 0x10;
    private static final int VIEW_TYPE_HEADER_PLUS = 0x11;
    private static final int VIEW_TYPE_CONTACT = 0x100;

    private Context mContext;
    private int mHeaderDisplay;

    private Nostalgia mApp;
    private VideoRepository mVidRepo;
    private final BaseRecyclerFragment mFragment;

    private User mUser;

    public ContactAdapter(Nostalgia nostalgia, BaseRecyclerFragment fragment){
        mApp = nostalgia;
        mFragment = fragment;
        mContactWrappers = new ArrayList<PersonWrapper>();

        mVidRepo = mApp.getVidRepo();
        mUser = mApp.getUserRepo().getLoggedInUser();
    }

    public ContactAdapter(Nostalgia nostalgia, BaseRecyclerFragment fragment, boolean isToggleable){
        mApp = nostalgia;
        mFragment = fragment;
        mContactWrappers = new ArrayList<PersonWrapper>();

        mVidRepo = mApp.getVidRepo();
        mUser = mApp.getUserRepo().getLoggedInUser();
        mIsToggleable = isToggleable;
    }

    public ContactAdapter(Nostalgia nostalgia, Context context, int headerMode, BaseRecyclerFragment fragment){
        mApp = nostalgia;
        mContext = context;
        mFragment = fragment;
        mHeaderDisplay = headerMode;
        mContactWrappers = new ArrayList<PersonWrapper>();

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
    public void addGroup(String groupName, String groupDesc, ArrayList<ContactCard> contacts, PersonWrapper.WrapperType wrapper, HeaderAction action){

        //Allocate memory before for loop.
        final int NUM_HEADERS = 1;
        ArrayList<PersonWrapper> group = new ArrayList<>(contacts.size() + NUM_HEADERS);

        PersonWrapper header = new PersonWrapper();
        int sectionFirstPosition = mContactWrappers.size();
        header.setSectionFirstPosition(sectionFirstPosition);

        if(action == HeaderAction.PLUS) {
            header.setWrapperType(PersonWrapper.WrapperType.HEADER_PLUS);
        } else if(action == HeaderAction.SEARCH){
            //Search isn't yet implemented yet but when it is -- change wrapper type here.
            header.setWrapperType(PersonWrapper.WrapperType.HEADER);
        } else {
            header.setWrapperType(PersonWrapper.WrapperType.HEADER);
        }


        header.setHeaderName(groupName);
        header.setHeaderDescription(groupDesc);

        mSectionHeaders.add(groupName);
        mSectionStarts.put(groupName, sectionFirstPosition);

        // Insert new header view and update section data.
        mContactWrappers.add(header);
        if(contacts.size() > 0) {
            for (int x = 0; x < contacts.size(); x++) {
                PersonWrapper target = new PersonWrapper();
                target.setWrapperType(wrapper);
                target.setContact(contacts.get(x));
                target.setSectionFirstPosition(sectionFirstPosition);
                target.setIsSelected(false);
                mContactWrappers.add(target);
            }
        } else {
            PersonWrapper emptySet = new PersonWrapper();
            emptySet.setWrapperType(PersonWrapper.WrapperType.EMPTY_SET);
            emptySet.setHeaderName("Empty Set");
            emptySet.setEmptySetDescription("Nothing to see here.");
            emptySet.setSectionFirstPosition(sectionFirstPosition);
            mContactWrappers.add(emptySet);

        }
        //We use mSectionStarts to replace emptySet with the new collection.
        mSectionEnds.put(groupName, mContactWrappers.size() - 1);

        mContactWrappers.addAll(group);
    }

    public void addEmptyGroup(String groupName, String groupDesc, String emptySetMessage, PersonWrapper.WrapperType wrapper, HeaderAction action){

        //Allocate memory before for loop.
        final int NUM_HEADERS = 1;

        //1 header + 1 empty set = 2;
        int numItems = 2;
        ArrayList<PersonWrapper> group = new ArrayList<>(numItems);

        PersonWrapper header = new PersonWrapper();
        int sectionFirstPosition = mContactWrappers.size();
        header.setSectionFirstPosition(sectionFirstPosition);

        if(action == HeaderAction.PLUS) {
            header.setWrapperType(PersonWrapper.WrapperType.HEADER_PLUS);
        } else if(action == HeaderAction.SEARCH){
            //Search isn't yet implemented yet but when it is -- change wrapper type here.
            header.setWrapperType(PersonWrapper.WrapperType.HEADER);
        } else {
            header.setWrapperType(PersonWrapper.WrapperType.HEADER);
        }

        header.setHeaderName(groupName);
        header.setHeaderDescription(groupDesc);


        // Insert new header view and update section data.
        mContactWrappers.add(header);

        PersonWrapper emptySet = new PersonWrapper();
        emptySet.setWrapperType(PersonWrapper.WrapperType.EMPTY_SET);
        emptySet.setEmptySetDescription(emptySetMessage);
        emptySet.setSectionFirstPosition(sectionFirstPosition);
        mContactWrappers.add(emptySet);

        mContactWrappers.addAll(group);
    }

    public ContactCard getItem(int position){
        PersonWrapper selected = mContactWrappers.get(position);
        return mContactWrappers.get(position).getContact();
    }

    private boolean mIsToggleable = false;
    public void setIsToggleable(boolean isToggleable){
        mIsToggleable = isToggleable;
    }

    public boolean getIsToggleable(){
        return mIsToggleable;
    }

    // Involves populating data into the item through holder
    @Override
    public void onBindViewHolder(BaseHolder holder, int position) {
        final PersonWrapper item = mContactWrappers.get(position);

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
    }

    /*
     * Extended classes should define mItemLayout, or make a call to setItemlayout();
     */
    public int mItemLayout = -1;

    @Override
    public int getItemViewType(int position) {
        int ret = -1;
        PersonWrapper.WrapperType t = mContactWrappers.get(position).getType();
        if(t == PersonWrapper.WrapperType.HEADER) {
            ret = VIEW_TYPE_HEADER;
        }else if(t == PersonWrapper.WrapperType.HEADER_PLUS){
            ret = VIEW_TYPE_HEADER_PLUS;
        } else if(t == PersonWrapper.WrapperType.NATIVE ||
                t == PersonWrapper.WrapperType.PHONE ||
                t == PersonWrapper.WrapperType.PENDING_INC ||
                t == PersonWrapper.WrapperType.PENDING_OUT ||
                t == PersonWrapper.WrapperType.SOCIAL){
            ret = VIEW_TYPE_CONTACT;
        } else if(t == PersonWrapper.WrapperType.EMPTY_SET) {
            ret = VIEW_TYPE_EMPTYSET;
        } else {
            Log.e(TAG, "Unknown ItemType");
        }

        return ret;
    }

    @Override
    public BaseHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);

        // Inflate the custom layout
        View contactView;

        // Return a new holder instance using different views for each
        final BaseHolder viewHolder;
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
            case VIEW_TYPE_CONTACT:
                contactView = inflater.inflate(NativeViewHolder.VIEW_LAYOUT, parent, false);
                viewHolder = new NativeViewHolder(contactView);
                viewHolder.setSelectionListener(mFragment.getSelectionListener());
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
        return mContactWrappers.size();
    }

    private void notifyHeaderChanges() {
        for (int i = 0; i < mContactWrappers.size(); i++) {
            PersonWrapper item = mContactWrappers.get(i);
            if (item.isHeader()) {
                notifyItemChanged(i);
            }
        }
    }

    public void addItemToSection(ContactCard toAdd, String section, PersonWrapper.WrapperType wrapper){
        //Decide where to put the new item:
        Integer firstPosition = mSectionStarts.get(section);
        Integer lastPosition = mSectionEnds.get(section);

        PersonWrapper target = new PersonWrapper();
        target.setWrapperType(wrapper);
        target.setContact(toAdd);
        target.setSectionFirstPosition(firstPosition);
        target.setIsSelected(false);

        boolean wasEmpty = PersonWrapper.WrapperType.EMPTY_SET == mContactWrappers.get(lastPosition).getType();

        if(wasEmpty) {
            mContactWrappers.remove(lastPosition);
            mContactWrappers.add(lastPosition, target);
        } else {
            mContactWrappers.add(lastPosition + 1, target);
            mSectionEnds.put(section, lastPosition + 1);

            for(String key : mSectionStarts.keySet()){
                Integer oldStart = mSectionStarts.get(key);
                if(lastPosition < oldStart){
                    mSectionStarts.put(key, oldStart + 1);

                    Integer oldEnd = mSectionEnds.get(key);
                    mSectionEnds.put(key, oldEnd + 1);

                    for(Integer shifted = oldStart + 1; shifted < oldEnd + 1; shifted++){
                        mContactWrappers.get(shifted).setSectionFirstPosition(oldStart + 1);
                    }

                }
            }
        }
        notifyDataSetChanged();
    }

    public ArrayList<PersonWrapper> getCollectionWrappers(){
        return mContactWrappers;

    }
}
