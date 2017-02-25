package com.nostalgia.controller.introduction;

import android.content.Context;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.nostalgia.persistence.caching.FontCache;
import com.nostalgia.view.AutoResizeTextView;
import com.vuescape.nostalgia.R;

public class TextEmailAuthFragment extends IntroFragment {

    private TextView mGotIt;
    public TextEmailAuthFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment HelloFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static TextEmailAuthFragment newInstance() {
        TextEmailAuthFragment fragment = new TextEmailAuthFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
        }

    }
    private AutoResizeTextView mTitleView;
    private LinearLayout mTitleHolder;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View mView = inflater.inflate(R.layout.fragment_intro_hello, container, false);
        mGotIt = (TextView) mView.findViewById(R.id.got_it_button);
        mGotIt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getAcceptedListener().onAccepted("HELLO");
            }
        });

        Typeface font = FontCache.get("teamspirit.regular.ttf", getActivity());
        mTitleView = (AutoResizeTextView) mView.findViewById(R.id.title_view);
        mTitleView.setTypeface(font);

        mTitleHolder = (LinearLayout) mView.findViewById(R.id.title_holder);
        mTitleHolder.setVisibility(LinearLayout.VISIBLE);
        mTitleView.setVisibility(TextView.VISIBLE);
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
