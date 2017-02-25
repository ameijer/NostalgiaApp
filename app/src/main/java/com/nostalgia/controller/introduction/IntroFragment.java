package com.nostalgia.controller.introduction;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;

public class IntroFragment extends Fragment {
    public interface OnAcceptedListener{
        void onAccepted(String name);
    }

    private OnAcceptedListener mListener;
    public IntroFragment() {
        super();
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

    public void setAcceptedListener(OnAcceptedListener listener){
        mListener = listener;
    }
    public OnAcceptedListener getAcceptedListener(){
        return mListener;
    }
}
