package com.nostalgia.controller.introduction;

import android.content.Context;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.TranslateAnimation;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.nostalgia.controller.peek.picker.DefaultCollectionFragment;
import com.nostalgia.controller.peek.picker.mediadisplayers.recycler.BaseRecyclerFragment;
import com.nostalgia.persistence.caching.FontCache;
import com.nostalgia.persistence.model.MediaCollection;
import com.nostalgia.view.AutoResizeTextView;
import com.vuescape.nostalgia.R;

public class CollectionSeedFragment extends IntroFragment {

    private IntroductionActivity mParentActivity;
    private TextView mGotIt;
    public CollectionSeedFragment() {
        // Required empty public constructor
    }

    private BaseRecyclerFragment.SelectionListener mSelectionListener;

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment HelloFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static CollectionSeedFragment newInstance() {
        CollectionSeedFragment fragment = new CollectionSeedFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mParentActivity = (IntroductionActivity) getActivity();
    }

    private AutoResizeTextView mTitleView;
    private LinearLayout mTitleHolder;
    private DefaultCollectionFragment mDefaultCollectionFragment;
    private LinearLayout mTextOverlay;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View mView = inflater.inflate(R.layout.fragment_intro_col, container, false);

        mTextOverlay = (LinearLayout) mView.findViewById(R.id.text_overlay);

        mGotIt = (TextView) mView.findViewById(R.id.got_it_button);
        mGotIt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //getAcceptedListener().onAccepted("LOCATION_SEED");
                mTextOverlay.animate().translationY(-mTextOverlay.getHeight()).setInterpolator(new AccelerateInterpolator(3));
            }
        });

        mDefaultCollectionFragment = (DefaultCollectionFragment) getChildFragmentManager().findFragmentByTag("defaultcollections");

        if(null == mDefaultCollectionFragment){
            mDefaultCollectionFragment = DefaultCollectionFragment.newInstance();
        }

        mDefaultCollectionFragment.setSelectionListener(mParentActivity);
        FragmentTransaction fragTransaction = getChildFragmentManager().beginTransaction();
        fragTransaction.replace(R.id.col_frag_holder, mDefaultCollectionFragment, "defaultcollections");

        fragTransaction.commit();
        getChildFragmentManager().executePendingTransactions();
        return mView;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

    public void setSelectionListener(BaseRecyclerFragment.SelectionListener listener){
        mSelectionListener = listener;
    }
    public BaseRecyclerFragment.SelectionListener getSelectionListener(){
        return mSelectionListener;
    }
}
