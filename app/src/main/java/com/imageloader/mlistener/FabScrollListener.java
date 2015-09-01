package com.imageloader.mlistener;

import android.support.v7.widget.RecyclerView;

/**
 * Created by wangzhiguo on 15/8/31.
 */
public abstract class FabScrollListener extends RecyclerView.OnScrollListener {

    private boolean isVisable = true;//是否可见
    private int mScrolledDistance = 0;
    private static final int THRESHOLD = 20;//滑动距离超过20才产生动画效果，用户体验会好一点
    @Override
    public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
        super.onScrolled(recyclerView, dx, dy);//dy==deltY增量，往上滑dy>0
        //1.往上滑动
        if(isVisable && mScrolledDistance > THRESHOLD) {
            //fab消失
            onHide();
            isVisable = false;
            mScrolledDistance = 0;
        } else if(!isVisable && mScrolledDistance < -THRESHOLD) {
            onShow();
            isVisable = true;
            mScrolledDistance = 0;
        }
        //计算滚动距离，叠加（两种情况我才叠加：1.下滑（同时fab是显示的）2.上滑（同时fab是不显示的））
        if((isVisable && dy > 0) || (!isVisable && dy < 0)) {
            mScrolledDistance += dy;
        }

        //2.往下滑动
        //fab出现
    }

    public abstract void onHide();
    public abstract void onShow();
}
