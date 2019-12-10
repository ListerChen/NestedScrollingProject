
package com.lister.nestedscrolltest.common;

import android.content.Context;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.lang.reflect.Constructor;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class BaseAdapter extends RecyclerView.Adapter<BaseViewHolder> {

    private Context mContext;
    private List<AdapterItem> mListItems;
    private SparseArray<Class<? extends BaseViewHolder>> mViewTypes;

    public BaseAdapter(Context context, List<AdapterItem> listItems,
                       SparseArray<Class<? extends BaseViewHolder>> viewTypes) {
        mContext = context;
        mListItems = listItems;
        mViewTypes = viewTypes;
    }

    @NonNull
    @Override
    public BaseViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        try {
            Class clazz = mViewTypes.get(viewType);
            ViewHolderAnnotation annotation = (ViewHolderAnnotation) clazz
                    .getAnnotation(ViewHolderAnnotation.class);
            if (annotation == null) {
                throw new IllegalArgumentException("ViewHolderAnnotation should not be null");
            }
            View view = LayoutInflater.from(mContext).inflate(annotation.layoutId(),
                    parent, false);
            Constructor constructor = clazz.getConstructor(View.class);
            BaseViewHolder baseViewHolder = (BaseViewHolder) constructor.newInstance(view);
            baseViewHolder.initView();
            return baseViewHolder;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public void onBindViewHolder(@NonNull BaseViewHolder holder, int position) {
        holder.bindData(mListItems.get(position).getData());
    }

    @Override
    public int getItemCount() {
        return mListItems == null ? 0 : mListItems.size();
    }

    @Override
    public int getItemViewType(int position) {
        return mListItems.get(position).getViewType();
    }
}
