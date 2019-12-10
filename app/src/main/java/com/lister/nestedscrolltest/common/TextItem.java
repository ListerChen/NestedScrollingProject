
package com.lister.nestedscrolltest.common;

public class TextItem implements AdapterItem<String> {

    private String mText;

    public TextItem(String text) {
        mText = text;
    }

    @Override
    public int getViewType() {
        return CommonViewType.VIEW_TYPE_TEXT;
    }

    @Override
    public String getData() {
        return mText;
    }
}
