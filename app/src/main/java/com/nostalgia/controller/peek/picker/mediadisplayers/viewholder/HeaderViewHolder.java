package com.nostalgia.controller.peek.picker.mediadisplayers.viewholder;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.nostalgia.controller.peek.CreatorLauncher;
import com.nostalgia.controller.peek.picker.mediadisplayers.model.MediaCollectionWrapper;
import com.vuescape.nostalgia.R;

/**
 * Created by Aidan on 12/11/15.
 */
public class HeaderViewHolder extends CollectionViewHolder {
    private CreatorLauncher mCreatorLauncher;
    private ImageView thumbnail;
    private TextView mName;
    private TextView description;
    private ImageView mSecondaryButton;

    public static int VIEW_LAYOUT = R.layout.slim_header_item;
    public static int VIEW_LAYOUT_PLUS  = R.layout.slim_header_plus;

    public HeaderViewHolder(View itemView) {
        super(itemView);
        thumbnail = (ImageView) itemView.findViewById(R.id.thumbnail);
        mName = (TextView) itemView.findViewById(R.id.name);
        description = (TextView) itemView.findViewById(R.id.description);

        mSecondaryButton = (ImageView) itemView.findViewById(R.id.create_button);
    }

    @Override
    public void bindItem(MediaCollectionWrapper toDisplay) {
        if(toDisplay.getType() == MediaCollectionWrapper.WrapperType.HEADER) {
            bindHeader(toDisplay);
        } else if (toDisplay.getType() == MediaCollectionWrapper.WrapperType.HEADER_PLUS){
            bindHeader(toDisplay);
        }
    }

    @Override
    public void onBind(){
        thumbnail.setImageDrawable(null);
    }

    private void bindHeader(MediaCollectionWrapper wrapper){
        final MediaCollectionWrapper finalWrapper = wrapper;
        mName.setText(wrapper.getHeaderName());
        description.setText(wrapper.getHeaderDescription());

        if(null != mSecondaryButton){
            mSecondaryButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    createCollection(finalWrapper.getHeaderName());
                }
            });
        }
    }

    private void createCollection(String visibility){
        getCreatorLauncher().launchCreator(visibility);
    }

    public void setCreatorLauncher(CreatorLauncher creatorLauncher){
        mCreatorLauncher = creatorLauncher;
    }

    public CreatorLauncher getCreatorLauncher(){
        return mCreatorLauncher;
    }
}