
package com.lister.nestedscrolltest.bounce;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.util.SparseArray;

import com.lister.nestedscrolltest.R;
import com.lister.nestedscrolltest.common.AdapterItem;
import com.lister.nestedscrolltest.common.BaseAdapter;
import com.lister.nestedscrolltest.common.BaseViewHolder;
import com.lister.nestedscrolltest.common.CommonViewType;
import com.lister.nestedscrolltest.common.TextItem;
import com.lister.nestedscrolltest.common.TextViewHolder;

import java.util.ArrayList;
import java.util.List;

public class BounceActivity extends AppCompatActivity {

    private RecyclerView mBounceRvList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bounce);

        mBounceRvList = findViewById(R.id.bounce_rv_list);
        initRvList();
    }

    private void initRvList() {
        List<AdapterItem> itemList = new ArrayList<>();
        for (int i = 0; i < 30; i++) {
            itemList.add(new TextItem("data: " + (i + 1)));
        }
        SparseArray<Class<? extends BaseViewHolder>> viewHolders = new SparseArray<>();
        viewHolders.put(CommonViewType.VIEW_TYPE_TEXT, TextViewHolder.class);
        BaseAdapter adapter = new BaseAdapter(this, itemList, viewHolders);
        mBounceRvList.setAdapter(adapter);
        mBounceRvList.setLayoutManager(new LinearLayoutManager(this));
    }
}
