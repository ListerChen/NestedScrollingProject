
package com.lister.nestedscrolltest;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.os.Bundle;
import android.util.SparseArray;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.lister.nestedscrolltest.common.AdapterItem;
import com.lister.nestedscrolltest.common.BaseAdapter;
import com.lister.nestedscrolltest.common.BaseViewHolder;
import com.lister.nestedscrolltest.common.CommonViewType;
import com.lister.nestedscrolltest.common.TextItem;
import com.lister.nestedscrolltest.common.TextViewHolder;
import com.youth.banner.Banner;
import com.youth.banner.BannerConfig;
import com.youth.banner.loader.ImageLoader;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private NestedLinearLayout mNestedLinearLayout;
    private Banner mBanner;
    private RecyclerView mCommentRecyclerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initViews();
        initBanner();
        initComments();
    }

    private void initViews() {
        mNestedLinearLayout = findViewById(R.id.main_nested_linear_layout);
        mBanner = findViewById(R.id.main_banner);
        mCommentRecyclerView = findViewById(R.id.main_rv_comments);
        mCommentRecyclerView.setNestedScrollingEnabled(true);
    }

    private void initBanner() {
        Integer[] images = {R.drawable.taylor1, R.drawable.taylor2, R.drawable.taylor3};
        mBanner.setBannerStyle(BannerConfig.CIRCLE_INDICATOR);
        mBanner.setImageLoader(new GlideImageLoader());
        mBanner.setImages(Arrays.asList(images));
        mBanner.setDelayTime(2000);
        mBanner.setIndicatorGravity(BannerConfig.CENTER);
        mBanner.start();
    }

    private void initComments() {
        List<AdapterItem> itemList = new ArrayList<>();
        for (int i = 0; i < 50; i++) {
            itemList.add(new TextItem("评论: " + (i + 1)));
        }
        SparseArray<Class<? extends BaseViewHolder>> viewHolders = new SparseArray<>();
        viewHolders.put(CommonViewType.VIEW_TYPE_TEXT, TextViewHolder.class);
        BaseAdapter adapter = new BaseAdapter(this, itemList, viewHolders);
        mCommentRecyclerView.setAdapter(adapter);
        mCommentRecyclerView.setLayoutManager(new LinearLayoutManager(this));
    }

    public class GlideImageLoader extends ImageLoader {

        @Override
        public void displayImage(Context context, Object path, ImageView imageView) {
            RequestOptions requestOptions = new RequestOptions().centerCrop();
            Glide.with(context).load(path).apply(requestOptions).into(imageView);
        }
    }
}
