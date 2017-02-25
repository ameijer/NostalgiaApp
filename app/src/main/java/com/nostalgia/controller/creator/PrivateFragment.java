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

import com.vuescape.nostalgia.R;

public class PrivateFragment extends Fragment {

    public PrivateFragment() {
        super();
    }

    public static PrivateFragment newInstance() {
        PrivateFragment fragment = new PrivateFragment();
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

    private String mName = "";
    private String mDescription = "";


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View mView = inflater.inflate(R.layout.fragment_create_personal, container, false);

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
            }
        });

        return mView;
    }

    public boolean checkCompleteness(){
        boolean isComplete = true;
        if(null == mName || mName.isEmpty()){
            isComplete = false;
        }

        //Shared collections don't need a description, but we should avoid an NPE
        if(null == mDescription || mDescription.isEmpty()){
            getDetailsListener().onGeneralChange("description", "");
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
