package com.imageloader.activity;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;

import com.imageloader.Images;
import com.imageloader.R;
import com.imageloader.adapter.RecyclerAdapter;
import com.imageloader.mlistener.FabScrollListener;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private Toolbar mToolbar;
    private RecyclerView mRecyclerView;

    private RecyclerAdapter mAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initToolbar();
        findView();
        initView();
    }

    private void initToolbar() {
        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);
        setTitle("自定义图片加载框架");
    }

    private void findView() {
        mRecyclerView = (RecyclerView) findViewById(R.id.recyclerView);
    }

    private void initView() {
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(layoutManager);
        mAdapter = new RecyclerAdapter(Images.images,getList());
        mRecyclerView.setAdapter(mAdapter);
        //监听滑动(上滑：出去，下滑：进来);
        mRecyclerView.addOnScrollListener(new FabScrollListener() {
            @Override
            public void onHide() {
                //出去
                hide();
            }

            @Override
            public void onShow() {
                //显示
                show();
            }
        });
    }

    private void hide() {
        mToolbar.animate().translationY(-mToolbar.getHeight());//隐藏mToolbar的坐标为负
        mToolbar.animate().alpha(0);
    }

    private void show() {
        mToolbar.animate().translationY(0);//恢复到原来的位置0
        mToolbar.animate().alpha(1);
    }

    private List<String> getList() {
        List<String> list = new ArrayList<String>();
        for(int i = 0;i < 30;i++) {
            list.add("自定义ImageLoader" + i);
        }
        return list;

    }


}
