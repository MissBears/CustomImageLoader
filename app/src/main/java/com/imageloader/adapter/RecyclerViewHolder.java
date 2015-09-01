package com.imageloader.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.imageloader.R;
import com.imageloader.utils.ImageLoader;


/**
 * Created by wangzhiguo on 15/8/31.
 */
public class RecyclerViewHolder extends RecyclerView.ViewHolder {
    private ImageView mImageView;
    private TextView mTextView;
    private Context mContext;
    public RecyclerViewHolder(View itemView, int type, Context context) {
        super(itemView);
        mContext = context;
        if(type == ItemType.GENERAL) {
            mImageView = (ImageView) itemView.findViewById(R.id.iv_img);
            mTextView = (TextView) itemView.findViewById(R.id.itemTextView);
        }
    }

    public void setImage(String url) {
        if(mImageView != null) {
            ImageLoader.getInstancee(mContext).loadImage(url, mImageView);
        }
    }

    public void setText(String text) {
        if(mTextView != null) {
            mTextView.setText(text);
        }
    }
}
