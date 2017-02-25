package com.nostalgia.controller.introduction;

import android.content.Context;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.TranslateAnimation;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.nostalgia.controller.peek.picker.DefaultLocationFragment;
import com.nostalgia.persistence.caching.FontCache;
import com.nostalgia.persistence.model.MediaCollection;
import com.nostalgia.view.AutoResizeTextView;
import com.vuescape.nostalgia.R;

public class LocationSeedFragment extends IntroFragment {

    //TODO: Upon further review.. this really doesn't need to be a wrapper fragment
    // for DefaultLocationFragment. Might be better off just adding these functions
    // to defaultLocationFragment.

    private TextView mGotIt;
    private IntroductionActivity mParentActivity;

    private DefaultLocationFragment mDefaultLocationFragment;
    public LocationSeedFragment() {
        // Required empty public constructor
        super();
    }

    public interface OnLocationPickedListener{
        void onSelection(MediaCollection collection, boolean isSelected);
        void onFinish();
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment HelloFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static LocationSeedFragment newInstance() {
        LocationSeedFragment fragment = new LocationSeedFragment();
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
    private LinearLayout mTextOverlay;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View mView = inflater.inflate(R.layout.fragment_intro_loc, container, false);

        mTextOverlay = (LinearLayout) mView.findViewById(R.id.text_overlay);

        mGotIt = (TextView) mView.findViewById(R.id.got_it_button);
        mGotIt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mTextOverlay.animate().translationY(-mTextOverlay.getHeight()).setInterpolator(new AccelerateInterpolator(3));
            }
        });

        mDefaultLocationFragment = (DefaultLocationFragment) getChildFragmentManager().findFragmentByTag("defaultlocations");

        if(null == mDefaultLocationFragment){
            mDefaultLocationFragment = DefaultLocationFragment.newInstance();
        }

        mDefaultLocationFragment.setSelectionListener(mParentActivity);

        FragmentTransaction fragTransaction = getChildFragmentManager().beginTransaction();
        fragTransaction.replace(R.id.loc_frag_holder, mDefaultLocationFragment, "defaultlocations");

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

}
