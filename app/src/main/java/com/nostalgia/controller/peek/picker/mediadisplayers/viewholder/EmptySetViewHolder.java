package com.nostalgia.controller.peek.picker.mediadisplayers.viewholder;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.nostalgia.controller.peek.picker.mediadisplayers.model.MediaCollectionWrapper;
import com.vuescape.nostalgia.R;

/**
 * Created by Aidan on 12/11/15.
 */
public class EmptySetViewHolder extends CollectionViewHolder {
    private TextView description;

    public static int VIEW_LAYOUT = R.layout.slim_emptyset_item;

    public EmptySetViewHolder(View itemView) {
        super(itemView);
        description = (TextView) itemView.findViewById(R.id.description);

        mType = ViewHolderType.HEADER;
    }

    @Override
    public void bindItem(MediaCollectionWrapper toDisplay) {
            /*
             * TODO: If toDisplay.getType = subscribe or known,
             * then change background color or adapter or something.
             */
        if(toDisplay.getType() == MediaCollectionWrapper.WrapperType.EMPTY_SET){
            bindEmptySet(toDisplay);
        }
    }

    @Override
    public void onBind(){
    }

    private void bindEmptySet(MediaCollectionWrapper wrapper){
        description.setText(wrapper.getEmptySetDescription());
    }
}