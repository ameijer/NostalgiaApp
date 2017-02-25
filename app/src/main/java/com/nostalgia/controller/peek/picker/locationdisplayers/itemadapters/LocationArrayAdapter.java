package com.nostalgia.controller.peek.picker.locationdisplayers.itemadapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.nostalgia.persistence.model.KnownLocation;
import com.nostalgia.persistence.model.MediaCollection;
import com.nostalgia.persistence.repo.MediaCollectionRepository;
import com.vuescape.nostalgia.R;

import java.util.List;

/**
 * Created by alex on 11/19/15.
 */
public class LocationArrayAdapter extends ArrayAdapter<KnownLocation> {

    public static final String TAG = "LocationArrayListAdapter";
    private final int itemLayoutResource;
    private final MediaCollectionRepository collRepo;
    private List<KnownLocation> objects;
    private final Context context;

    /*
     * Extended classes should define mItemLayout, or make a call to setItemlayout();
     */
    public int mItemLayout = -1;

    private static class LocationViewHolder {
        ImageView backdrop;
        TextView name;
        TextView description;
        TextView owner;
        TextView numvids;
        TextView numFans;

        public LocationViewHolder(View itemView) {
            backdrop = (ImageView) itemView.findViewById(R.id.location_thumbnail);
            name = (TextView) itemView.findViewById(R.id.location_name);
            numvids = (TextView) itemView.findViewById(R.id.location_numvids);
            description = (TextView) itemView.findViewById(R.id.location_description);
            owner = (TextView) itemView.findViewById(R.id.location_owner);
        }
    }

    public LocationArrayAdapter(Context context, int resource, List<KnownLocation> objects, MediaCollectionRepository collRepo) {
        super(context, resource, objects);
        this.context = context;
        this.objects = objects;
        this.itemLayoutResource = resource;
        this.collRepo = collRepo;
    }

    @Override
    public View getView(int position, View itemView, ViewGroup parent) {
        LocationViewHolder vh = null;
        if (itemView == null) {
            LayoutInflater vi = (LayoutInflater)parent.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            itemView = vi.inflate(itemLayoutResource, null);
            vh = new LocationViewHolder(itemView);
            itemView.setTag(vh);
        } else {
            vh = (LocationViewHolder) itemView.getTag();
        }

        try {
            KnownLocation toDisplay = this.objects.get(position);

            if(toDisplay.getName()!= null) {
                vh.name.setText(toDisplay.getName());
            } else {
                vh.name.setText("Somewhere");
            }

            String collectionId = toDisplay.getLocationCollections().get("primary");

            MediaCollection matchingColl = collRepo.findOneById(collectionId);
            if(matchingColl.getMatchingVideos() != null) {
                vh.numvids.setText(matchingColl.getMatchingVideos().keySet().size() + " videos");
                //vh.numvids.setText(toDisplay.get
            } else {
                vh.numvids.setText("No videos here :(");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return itemView;
    }

}
