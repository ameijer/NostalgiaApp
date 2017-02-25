package com.nostalgia.controller.creator;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.widget.AppCompatEditText;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.nostalgia.menu.friends.SimpleContactSelectorFragment;
import com.nostalgia.menu.friends.recycler.BaseRecyclerFragment;
import com.nostalgia.persistence.model.User;
import com.vuescape.nostalgia.R;

public class SharedFragment extends Fragment implements BaseRecyclerFragment.SelectionListener{

    public SharedFragment() {
        super();
    }

    public static SharedFragment newInstance() {
        SharedFragment fragment = new SharedFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    public interface SelectionListener{
        void onCancelled();
        void onSelection(Object selection, boolean isSelected);
    }

    @Override
    public void onCancelled(){

    }

    @Override
    public void onSelection(Object selection, boolean isSelected){
        if(isSelected){
            mSharingListener.onPersonAdded((User) selection);
        } else {
            mSharingListener.onPersonRemoved((User) selection);
        }
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

    private LinearLayout mContactsRoot;
    private SimpleContactSelectorFragment mContactSelectorFragment;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View mView = inflater.inflate(R.layout.fragment_create_shared, container, false);

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

        mContactsRoot = (LinearLayout) mView.findViewById(R.id.contacts_root);


        FragmentManager mainFragmentManager = getChildFragmentManager();

        mContactSelectorFragment = SimpleContactSelectorFragment.newInstance(true);
        mContactSelectorFragment.setSelectionListener(this);

        //initially, mStart with the choice fragment
        FragmentTransaction fragTransaction = mainFragmentManager.beginTransaction();
        fragTransaction.replace(R.id.contacts_holder, mContactSelectorFragment);
        fragTransaction.commit();

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

    private SharingListener mSharingListener;
    public void setSharingListener(SharingListener listener){
        mSharingListener = listener;
    }
    public SharingListener getSharingListener(){
        return mSharingListener;
    }
}
