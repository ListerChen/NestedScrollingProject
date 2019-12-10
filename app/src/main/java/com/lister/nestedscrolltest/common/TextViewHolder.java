
package com.lister.nestedscrolltest.common;

import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.lister.nestedscrolltest.R;

@ViewHolderAnnotation(layoutId = R.layout.item_common_text)
public class TextViewHolder extends BaseViewHolder<String> {

    private TextView mTextView;

    public TextViewHolder(@NonNull View itemView) {
        super(itemView);
    }

    @Override
    public void initView() {
        mTextView = itemView.findViewById(R.id.common_text_comment);
    }

    @Override
    public void bindData(String data) {
        mTextView.setText(data);
    }
}
