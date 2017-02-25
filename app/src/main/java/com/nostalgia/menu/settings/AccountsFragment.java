package com.nostalgia.menu.settings;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import com.nostalgia.Nostalgia;
import com.nostalgia.persistence.model.User;
import com.nostalgia.persistence.repo.UserRepository;

import java.util.ArrayList;

/**
 * Created by alex on 12/25/15.
 */
public class AccountsFragment extends Fragment implements View.OnClickListener {

    private static final String TAG = "AccountsFragment";
    private static final int ACCOUNT_ADDER_CODE = 2001;

    private ListView accountsList;
    private Button addAccountButton;

    private UserRepository userRepo;
    private Context context;

    private AccountItemAdapter adapt;

    private ArrayList<User.Account> currentAccounts = new ArrayList<User.Account>();

    @Override
    public void onAttach(Activity activity){
        super.onAttach(activity);

        userRepo = ((Nostalgia)activity.getApplication()).getUserRepo();
        context = getContext();

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = super.onCreateView(inflater, container, savedInstanceState);

        View myView = inflater.inflate(R.layout.accounts_settings_frag, container, false);


        accountsList = (ListView) myView.findViewById(R.id.settings_account_list);
        addAccountButton = (Button) myView.findViewById(R.id.settings_add_new_account);
        addAccountButton.setOnClickListener(this);


        populateAccountList();
        return myView;
    }

    private void populateAccountList() {

        User current = userRepo.getLoggedInUser();
        currentAccounts.clear();
        currentAccounts.addAll(current.getAccountsList());

        if (adapt == null){
            adapt = new AccountItemAdapter(getContext(), currentAccounts);
            accountsList.setAdapter(adapt);
        }

        if(currentAccounts.size() < 1){
            //fill in empty holder
            User.Account empty = new User.Account();
            empty.name = "<no accounts linked>";
            currentAccounts.add(empty);
        }

        adapt.notifyDataSetChanged();
    }

    ///return true if new account successfully added
    private void startLinkNewAccountActivity(){
        Intent intent = new Intent(getActivity(), LinkNewAccountActivity.class);
        startActivityForResult(intent, ACCOUNT_ADDER_CODE);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == ACCOUNT_ADDER_CODE && resultCode == getActivity().RESULT_OK) {
            String added = data.getStringExtra(LinkNewAccountActivity.ACCOUNT_TYPE_ADDED);
            onAccountAdded(added);
        }
    }

    private void onAccountAdded(String added) {
        Toast.makeText(getActivity(), "account for: " + added + " added",
                Toast.LENGTH_LONG).show();
        populateAccountList();
    }

    @Override
    public void onClick(View v) {
        User current = userRepo.getLoggedInUser();
        switch(v.getId()) {
            case R.id.settings_add_new_account:
                startLinkNewAccountActivity();
                break;

            default:
                Log.e(TAG, "Unhandled view onclick: " + v.getTag());
                break;
        }
    }
}
