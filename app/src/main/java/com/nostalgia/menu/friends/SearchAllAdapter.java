package com.nostalgia.menu.friends;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.nostalgia.menu.friends.unused.OnFriendActionListener;
import com.nostalgia.persistence.model.User;
import com.vuescape.nostalgia.R;

import java.util.ArrayList;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * Created by alex on 1/23/16.
 */
public class SearchAllAdapter extends ArrayAdapter<User> {
    private final Context context;
    private final ArrayList<User> values;
    private final User current;
    private OnFriendActionListener callback;

    public SearchAllAdapter(Context context, ArrayList<User> values, OnFriendActionListener listener, User current) {
        super(context, R.layout.add_friend_list_item, values);
        this.context = context;
        this.values = values;
        this.current = current;
        this.callback = listener;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        View rowView = inflater.inflate(R.layout.add_friend_list_item, parent, false);

        TextView placesDiscovered = (TextView) rowView.findViewById(R.id.add_friend_list_item_places);
        CircleImageView imageView = (CircleImageView) rowView.findViewById(R.id.add_friend_list_item_image);
        TextView name = (TextView) rowView.findViewById(R.id.add_friend_list_item_name);
        ImageView add_rem_friend = (ImageView) rowView.findViewById(R.id.add_friend_button);


        User friend = values.get(position);
        final String thisId = friend.get_id();

        //set up button
        if(current.getFriends() != null && current.getFriends().keySet().contains(friend.get_id())){
            //we are already friends, display remove button
            add_rem_friend.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(callback != null){
                        callback.onFriendRemove(thisId);
                    }
                }
            });
        } else {
            //we are not friends, display add button
            add_rem_friend.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(callback != null){
                        callback.onFriendAdd(thisId);
                    }
                }
            });
        }

        //set up image
        byte[] pngRaw = Base64.decode(friend.getIcon(), Base64.DEFAULT);

        Bitmap bmp = BitmapFactory.decodeByteArray(pngRaw, 0, pngRaw.length);
        imageView.setImageBitmap(bmp);

        //set up texts
        name.setText(friend.getUsername());
        placesDiscovered.setText("Arbitrary Score:" + friend.getCreatedLocations().size());

        return rowView;
    }


}
