package com.aliyun.iot.ilop.demo.view;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.widget.FrameLayout;

import com.aliyun.iot.demo.R;

public class EQSettingItemView extends FrameLayout {
    public EQSettingItemView(@NonNull Context context) {
        super(context);
        initView();
    }

    public EQSettingItemView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        initView();
    }

    public EQSettingItemView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView();
    }

    private void initView() {
        inflate(getContext(), R.layout.equipment_setting_item_view, this);
    }
}
