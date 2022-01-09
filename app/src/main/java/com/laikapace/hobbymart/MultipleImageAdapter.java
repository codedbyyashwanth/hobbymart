package com.laikapace.hobbymart;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.smarteist.autoimageslider.SliderViewAdapter;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

public class MultipleImageAdapter  extends SliderViewAdapter<MultipleImageAdapter.MultipleImageViewHolder> {

    private final List<MultipleImageData> multipleImageData;


    public MultipleImageAdapter(Context context, ArrayList<MultipleImageData> MultipleImageData) {
        this.multipleImageData = MultipleImageData;
    }


    @Override
    public MultipleImageViewHolder onCreateViewHolder(ViewGroup parent) {
        View inflate = LayoutInflater.from(parent.getContext()).inflate(R.layout.single_image, null);
        return new MultipleImageViewHolder(inflate);
    }


    @Override
    public void onBindViewHolder(MultipleImageViewHolder viewHolder, final int position) {
        final MultipleImageData item = multipleImageData.get(position);
        Picasso.get().load(item.getImgUrl()).into(viewHolder.imageViewBackground);
    }


    @Override
    public int getCount() {
        return multipleImageData.size();
    }

    static class MultipleImageViewHolder extends SliderViewAdapter.ViewHolder {
        View itemView;
        ImageView imageViewBackground;

        public MultipleImageViewHolder(View itemView) {
            super(itemView);
            imageViewBackground = itemView.findViewById(R.id.single_image);
            this.itemView = itemView;
        }
    }
}
