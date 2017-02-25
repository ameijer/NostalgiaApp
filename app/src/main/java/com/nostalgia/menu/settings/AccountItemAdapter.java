package com.nostalgia.menu.settings;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.nostalgia.persistence.model.User;

import java.util.ArrayList;

/**
 * Created by alex on 1/9/16.
 */
public class AccountItemAdapter extends ArrayAdapter<User.Account> {

    private final Context context;

    private final ArrayList<User.Account> accounts;
    public AccountItemAdapter(Context context, ArrayList<User.Account> currentAccounts) {
        super(context, 0, currentAccounts);
        this.context = context;
        this.accounts = currentAccounts;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // Get the data item for this position
        User.Account acct = accounts.get(position);
        // Check if an existing view is being reused, otherwise inflate the view
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.account_list_item, parent, false);
        }
        // Lookup view for data population
        TextView tvName = (TextView) convertView.findViewById(R.id.account_item_provider_name);
        TextView tvEmail = (TextView) convertView.findViewById(R.id.account_item_user_email);
        // Populate the data into the template view using the data object
        tvName.setText(acct.name);
        if(acct.email != null) {
            tvName.setText(acct.email);
        }
        // Return the completed view to render on screen
        return convertView;
    }

}
