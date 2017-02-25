package com.nostalgia.controller.peek.picker.locationdisplayers.recycler.viewholder;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.nostalgia.controller.peek.picker.locationdisplayers.recycler.model.KnownLocationWrapper;
import com.vuescape.nostalgia.R;

/**
 * Created by Aidan on 12/11/15.
 */
public class HeaderViewHolder extends PeekViewHolder{
    private ImageView thumbnail;
    private TextView name;
    private TextView description;

    public static int VIEW_LAYOUT = R.layout.slim_header_item;

    public HeaderViewHolder(View itemView) {
        super(itemView);
        thumbnail = (ImageView) itemView.findViewById(R.id.thumbnail);
        name = (TextView) itemView.findViewById(R.id.name);
        description = (TextView) itemView.findViewById(R.id.description);

        mType = ViewHolderType.HEADER;
    }

    @Override
    public void bindItem(KnownLocationWrapper toDisplay) {
            /*
             * TODO: If toDisplay.getType = subscribe or known,
             * then change background color or adapter or something.
             */
        if(toDisplay.getType() == KnownLocationWrapper.WrapperType.HEADER) {
            bindHeader(toDisplay);
        }
    }

    @Override
    public void onBind(){
        thumbnail.setImageDrawable(null);
    }

    private void bindHeader(KnownLocationWrapper wrapper){
        name.setText(wrapper.getHeaderName());
        description.setText(wrapper.getHeaderDescription());
    }
}