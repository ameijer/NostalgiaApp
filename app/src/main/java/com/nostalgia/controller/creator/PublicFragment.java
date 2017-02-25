package com.nostalgia.controller.creator;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.AppCompatEditText;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.vuescape.nostalgia.R;

public class PublicFragment extends Fragment {

    public PublicFragment() {
        super();
    }

    public static PublicFragment newInstance() {
        PublicFragment fragment = new PublicFragment();
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

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

    private AppCompatEditText mNameView;
    private AppCompatEditText mDescView;

    private String mDescription;
    private String mName;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View mView = inflater.inflate(R.layout.fragment_create_public, container, false);

        mNameView = (AppCompatEditText) mView.findViewById(R.id.name);
        mDescView = (AppCompatEditText) mView.findViewById(R.id.description);

        mNameView.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                getDetailsListener().onNameChange(s.toString());
                mName = s.toString();
            }
        });

        mDescView.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                getDetailsListener().onGeneralChange("description", s.toString());
                mDescription = s.toString();
            }
        });

        return mView;
    }

    public boolean checkCompleteness(){
        boolean isComplete = true;
        if(null == mName || mName.isEmpty()){
            isComplete = false;
        }

        if(null == mDescription || mDescription.isEmpty()){
            Toast.makeText(getContext(), "Public collections need a description.", Toast.LENGTH_LONG).show();
            isComplete = false;
        }

        return isComplete;
    }

    private DetailsListener mDetailsListener;
    public void setDetailsListener(DetailsListener listener){
        mDetailsListener = listener;
    }
    public DetailsListener getDetailsListener(){
        return mDetailsListener;
    }
}
