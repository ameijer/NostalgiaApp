package com.nostalgia.menu.friends;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;


import com.nostalgia.Nostalgia;
import com.nostalgia.menu.friends.unused.FriendFocusActivity;
import com.nostalgia.menu.friends.unused.OnFriendActionListener;
import com.nostalgia.persistence.model.User;
import com.nostalgia.persistence.repo.UserRepository;
import com.nostalgia.runnable.FriendActionThread;

import com.nostalgia.runnable.UserSearchThread;
import com.vuescape.nostalgia.R;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;


/**
 * Created by alex on 1/18/16.
 */
public class SearchAllFragment extends Fragment implements OnFriendActionListener, AdapterView.OnItemSelectedListener {


    public static final String TAG = "SearchAllFragment";

    private SearchAllAdapter mAdapter;

    private ListView matchingList;
    UserRepository userRepo;
    private final ArrayList<User> friendsList = new ArrayList<User>();

    private Nostalgia mApp;
    private EditText searchParam;

    public static SearchAllFragment newInstance(){
        Bundle args = new Bundle();
        SearchAllFragment fragment = new SearchAllFragment();
        return fragment;
    }

    public void updateFriendsList(Collection<User> newFriends){
        friendsList.clear();
        friendsList.addAll(newFriends);
        if(mAdapter != null) {
            mAdapter.notifyDataSetChanged();
        }
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View myView =  inflater.inflate(R.layout.add_friends_list_frag, container, false);

        matchingList = (ListView) myView.findViewById(R.id.add_friends_list);

        mApp = (Nostalgia) getActivity().getApplication();

        return myView;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        userRepo  = ((Nostalgia) getActivity().getApplication()).getUserRepo();

        User current = userRepo.getLoggedInUser();
        mAdapter = new SearchAllAdapter(getActivity(), friendsList, this, current);
        matchingList.setAdapter(mAdapter);
        matchingList.setOnItemSelectedListener(this);

        searchParam = (EditText) view.findViewById(R.id.friend_search_box);
        searchParam.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                searchForFriends();
            }
        });
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }


    @Override
    public void onFriendAdd(String idOfUserToAdd) {
        User me = userRepo.getLoggedInUser();
        //fire off request to backend to add
        FriendActionThread adder = new FriendActionThread(FriendActionThread.ActionType.REQUEST, idOfUserToAdd, me);
        adder.start();
        try {
            adder.join();
        } catch (InterruptedException e) {
            Log.e(TAG, "INTERRUPTED", e);
        }


    }

    @Override
    public void onFriendRemove(String idOfFriendToRemove) {
        //fire off friend removal request
        User me = userRepo.getLoggedInUser();
        //fire off request to backend to add
        FriendActionThread remover = new FriendActionThread(FriendActionThread.ActionType.REMOVE, idOfFriendToRemove, me);
        remover.start();
        try {
            remover.join();
        } catch (InterruptedException e) {
            Log.e(TAG, "INTERRUPTED", e);
        }
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        User focused = friendsList.get(position);
        startFriendFocusActivity(focused);
    }

    private void startFriendFocusActivity(User focused) {
        Intent friendFocus = new Intent(getActivity(), FriendFocusActivity.class);
        friendFocus.putExtra("focused", focused);
        startActivity(friendFocus);
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }

    private void searchForFriends() {

        String param = searchParam.getText().toString();
        List<User> users;
        if(!param.isEmpty()) {
            //fill list with friends, add button to request them like instagram
            UserSearchThread searcher = new UserSearchThread(param);
            searcher.start();

            try {
                searcher.join();
            } catch (InterruptedException e) {
                Log.e(TAG, "Interrupted!", e);
            }

            users = searcher.getMatching();
            if (users == null) {
                users = new ArrayList<User>();
            }
        } else {
            users = new ArrayList<User>();
        }

        updateFriendsList(users);
    }

    protected void requestFriend(Object tag){
        FriendActionThread thread = new FriendActionThread(FriendActionThread.ActionType.REQUEST, tag.toString(), mApp.getUserRepo().getLoggedInUser());
        thread.start();
        try {
            thread.join();
        } catch (InterruptedException e) {
            Log.e("accept friend", "error", e);
        }


        User hasPendingFriend = thread.getMyUpdatedState();

        if(hasPendingFriend == null || hasPendingFriend.get_id().equalsIgnoreCase(mApp.getUserRepo().getLoggedInUser().get_id())){
            Toast.makeText(getContext(), "friend added successfully", Toast.LENGTH_LONG).show();
            return;
        } else {
            Toast.makeText(getContext(), "error adding friend", Toast.LENGTH_LONG).show();
        }
    }

    private void acceptFriend(Object tag) {
        FriendActionThread thread = new FriendActionThread(FriendActionThread.ActionType.ACCEPT, tag.toString(), mApp.getUserRepo().getLoggedInUser());
        thread.start();
        try {
            thread.join();
        } catch (InterruptedException e) {
            Log.e("accept friend", "error", e);
        }

        User hasFriend = thread.getMyUpdatedState();

        if(hasFriend == null || hasFriend.get_id().equalsIgnoreCase(mApp.getUserRepo().getLoggedInUser().get_id())){
            Toast.makeText(getContext(), "friend added successfully", Toast.LENGTH_LONG).show();
            return;
        } else {

            Toast.makeText(getContext(), "error adding friend", Toast.LENGTH_LONG).show();
        }

    }

}
