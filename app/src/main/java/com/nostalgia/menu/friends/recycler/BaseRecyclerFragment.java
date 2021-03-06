package com.nostalgia.menu.friends.recycler;

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
import com.nostalgia.persistence.model.User;
import com.nostalgia.persistence.repo.LocationRepository;
import com.nostalgia.persistence.repo.MediaCollectionRepository;
import com.nostalgia.persistence.repo.UserRepository;
import com.nostalgia.view.DividerItemDecoration;
import com.tonicartos.superslim.LayoutManager;
import com.vuescape.nostalgia.R;

/**
 * Created by aidan on 3/22/16.
 */
public abstract class BaseRecyclerFragment extends Fragment {
    public static String TAG = "BaseRecyclerFragment";

    private AppCompatActivity parentActivity;

    private CreatorLauncher mCreatorLauncher;

    private UserRepository mUserRepo;
    private MediaCollectionRepository collRepo;
    private LocationRepository locRepo;
    private User mUser;

    private SelectionListener mSelectionListener;
    private RecyclerView mRecyclerView;

    protected ContactAdapter mContactAdapter;

    private final int mLayoutId = R.layout.slim_recycler_view_holder;

    public abstract void loadCollectionGroups();

    public interface SelectionListener{
        void onCancelled();
        void onSelection(Object selection, boolean isSelected);
    }

    public void setSelectionListener(SelectionListener listener){
        mSelectionListener = listener;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        // This makes sure that the container activity has implemented
        // the CreatorLauncher interface. If not, it throws an exception
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

    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        boolean isMultiSelectable = false;
        if (null != getArguments() && getArguments().getBoolean("isMultiSelectable")) {
            isMultiSelectable = true;
        }

        mContactAdapter = new ContactAdapter((Nostalgia) getActivity().getApplication(), this, isMultiSelectable);
        mContactAdapter.setIsToggleable(isMultiSelectable);
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

        initVidAdapter(mView);

        return mView;
    }

    @Override
    public void onResume(){
        super.onResume();

        if(null == mSelectionListener) {
            try {
                setSelectionListener((SelectionListener) getActivity());
            } catch (ClassCastException e) {
                Log.e(TAG, "No SelectionListener set and attached to activity that does not implement SelectionListener.");
            }
        }
    }

    @Override
    public void onStop(){
        super.onStop();
        setSelectionListener(null);
        clearVidAdapter();
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

    }

    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public void onDestroyView(){
        super.onDestroyView();
    }

    @Override
    public void onDestroy(){
        super.onDestroy();
        mContactAdapter = null;
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
        mRecyclerView.setAdapter(mContactAdapter);
    }

    public void clearVidAdapter() {
        if (mRecyclerView != null){
            mRecyclerView.destroyDrawingCache();
            mRecyclerView = null;
        }
    }

    public AppCompatActivity getParentActivity(){
        return parentActivity;
    }

    public ContactAdapter getAdapter(){
        return mContactAdapter;
    }
    public int getLayoutId(){
        return mLayoutId;
    }

    public SelectionListener getSelectionListener(){
        return mSelectionListener;
    }

    public void notifyDataSetChanged(){
        parentActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mContactAdapter.notifyDataSetChanged();
            }
        });
    }
}
