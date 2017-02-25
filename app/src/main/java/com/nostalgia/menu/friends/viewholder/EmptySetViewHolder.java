package com.nostalgia.menu.friends.viewholder;

import android.view.View;
import android.widget.TextView;

import com.nostalgia.menu.friends.model.PersonWrapper;

/**
 * Created by Aidan on 12/11/15.
 */
public class EmptySetViewHolder extends NativeViewHolder {
    private TextView description;

    public static int VIEW_LAYOUT = R.layout.slim_emptyset_item;

    public EmptySetViewHolder(View itemView) {
        super(itemView);
        description = (TextView) itemView.findViewById(R.id.description);

        mType = ViewHolderType.HEADER;
    }

    @Override
    public void bindItem(PersonWrapper toDisplay) {
        if(toDisplay.getType() == PersonWrapper.WrapperType.EMPTY_SET){
            bindEmptySet(toDisplay);
        }
    }

    private void bindEmptySet(PersonWrapper wrapper){
        description.setText(wrapper.getEmptySetDescription());
    }
}