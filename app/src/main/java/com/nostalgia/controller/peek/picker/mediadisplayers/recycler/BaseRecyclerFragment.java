package com.nostalgia.controller.peek.picker.mediadisplayers.recycler;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.nostalgia.Nostalgia;
import com.nostalgia.controller.peek.CreatorLauncher;
import com.nostalgia.controller.peek.picker.mediadisplayers.model.MediaCollectionWrapper;
import com.nostalgia.persistence.model.KnownLocation;
import com.nostalgia.persistence.model.MediaCollection;
import com.nostalgia.persistence.model.User;
import com.nostalgia.persistence.repo.LocationRepository;
import com.nostalgia.persistence.repo.MediaCollectionRepository;
import com.nostalgia.persistence.repo.UserRepository;
import com.nostalgia.view.DividerItemDecoration;
import com.tonicartos.superslim.LayoutManager;
import com.vuescape.nostalgia.R;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * Created by alex on 11/19/15.
 */
public abstract class BaseRecyclerFragment extends Fragment {
    public static String TAG = "BaseCollectionSelection";

    private AppCompatActivity parentActivity;

    private CreatorLauncher mCreatorLauncher;

    private UserRepository mUserRepo;
    private MediaCollectionRepository collRepo;
    private LocationRepository locRepo;
    private User mUser;

    private SelectionListener callback;
    private RecyclerView mRecyclerView;

    protected CollectionAdapter mCollectionAdapter;

    private final int mLayoutId = R.layout.slim_recycler_view_holder;


    public abstract void loadCollectionGroups();
    public abstract void addNewItem(MediaCollection toAdd, String section, MediaCollectionWrapper.WrapperType type);

    public interface SelectionListener{
        void onCancelled();
        void onSelection(Object selection, boolean isSelected);
    }

    public void setSelectionListener(SelectionListener listener){
        this.callback = listener;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        // This makes sure that the container activity has implemented
        // the callback interface. If not, it throws an exception
        try {
            parentActivity = (AppCompatActivity) getActivity();

            try {
                mCreatorLauncher = (CreatorLauncher) parentActivity;
            } catch (ClassCastException e){
                e.printStackTrace();
            }

            mUserRepo = ((Nostalgia) parentActivity.getApplication()).getUserRepo();
            collRepo = ((Nostalgia) parentActivity.getApplication()).getCollRepo();
            locRepo = ((Nostalgia) parentActivity.getApplication()).getLocationRepository();

            mUser = mUserRepo.getLoggedInUser();
        } catch (ClassCastException e) {
            throw new ClassCastException(getActivity().toString()
                    + " incorrect cast");
        }
    }

    private boolean mIsMultiSelectable = false;

    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        if (null != getArguments() && getArguments().getBoolean("isMultiSelectable")) {
            mIsMultiSelectable = true;
        }

        mCollectionAdapter = new CollectionAdapter((Nostalgia) getActivity().getApplication(), this, mIsMultiSelectable);
        mCollectionAdapter.setIsToggleable(mIsMultiSelectable);

        loadCollectionGroups();
    }
   private View mView;

    public View getRootView(){
        return mView;
    }

    public CreatorLauncher getCreatorLauncher(){
        return mCreatorLauncher;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = super.onCreateView(inflater, container, savedInstanceState);
        mView = inflater.inflate(mLayoutId, container, false);

        return mView;
    }

    @Override
    public void onResume(){
        super.onResume();
        try {
            setSelectionListener((SelectionListener) getActivity());
        } catch(ClassCastException e){
            Log.e(TAG, "Attached to activity that does not implement SelectionListener.");
        }
    }

    @Override
    public void onStop(){
        super.onStop();
        setSelectionListener(null);
    }


    public String getCollectionName(String locId){
        LocationRepository locRepo;
        locRepo = ((Nostalgia) parentActivity.getApplication()).getLocationRepository();

        List idWrap = new ArrayList<String>();
        idWrap.add(locId);

        String ret = "";
        Map<String, KnownLocation> stupid;
        try {
            stupid = locRepo.getLocationsById(idWrap, true, true);
            Collection<KnownLocation> stupidList = stupid.values();
            if(stupidList.toArray().length > 0) {
                KnownLocation loc = (KnownLocation) stupidList.toArray()[0];
                ret = loc.getName();
            }
        }catch(Exception e){
            e.printStackTrace();
        }

        return ret;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initVidAdapter(view);
    }

    @Override
    public void onDestroyView(){
        super.onDestroyView();
        clearRecyclerView();
    }

    @Override
    public void onDestroy(){
        super.onDestroy();
        mCollectionAdapter = null;
    }

    public UserRepository getUserRepo() {return mUserRepo;}
    public MediaCollectionRepository getCollRepo(){
        return collRepo;
    }
    public User getUser(){return mUser;}

    public void initVidAdapter(View myView) {
        mRecyclerView = (RecyclerView) myView.findViewById(R.id.recycler_view);

        mRecyclerView.addItemDecoration(
                new DividerItemDecoration(getActivity()));

        mRecyclerView.setLayoutManager(new LayoutManager(getActivity()));
        mRecyclerView.setAdapter(mCollectionAdapter);
    }

    public void clearRecyclerView(){
        mRecyclerView.destroyDrawingCache();
        mRecyclerView = null;
    }

    public AppCompatActivity getParentActivity(){
        return parentActivity;
    }

    public CollectionAdapter getCollectionAdapter(){
        return mCollectionAdapter;
    }
    public int getLayoutId(){
        return mLayoutId;
    }

    public SelectionListener getCallback(){
        return callback;
    }


    public void notifyDataSetChanged(){
        parentActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mCollectionAdapter.notifyDataSetChanged();
            }
        });
    }

}
