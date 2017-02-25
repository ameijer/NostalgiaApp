package com.nostalgia.menu.friends.viewholder;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.nostalgia.controller.peek.CreatorLauncher;
import com.nostalgia.menu.friends.model.PersonWrapper;

/**
 * Created by Aidan on 12/11/15.
 */
public class HeaderViewHolder extends BaseHolder {
    private CreatorLauncher mCreatorLauncher;
    private TextView mName;
    private TextView description;
    private ImageView mSecondaryButton;

    public static int VIEW_LAYOUT = R.layout.slim_header_item;
    public static int VIEW_LAYOUT_PLUS  = R.layout.slim_header_plus;

    public HeaderViewHolder(View itemView) {
        super(itemView);
        mName = (TextView) itemView.findViewById(R.id.name);
        description = (TextView) itemView.findViewById(R.id.description);

        mSecondaryButton = (ImageView) itemView.findViewById(R.id.create_button);
    }

    @Override
    public void bindItem(PersonWrapper toDisplay) {
        if(toDisplay.getType() == PersonWrapper.WrapperType.HEADER) {
            bindHeader(toDisplay);
        } else if (toDisplay.getType() == PersonWrapper.WrapperType.HEADER_PLUS){
            bindHeader(toDisplay);
        }
    }

    private void bindHeader(PersonWrapper wrapper){
        final PersonWrapper finalWrapper = wrapper;
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