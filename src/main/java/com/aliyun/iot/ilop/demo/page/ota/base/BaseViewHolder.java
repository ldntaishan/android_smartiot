package com.aliyun.iot.ilop.demo.page.ota.base;

import android.support.v7.widget.RecyclerView.ViewHolder;
import android.view.View;

/**
 * Created by david on 2018/4/9.
 *
 * @author david
 * @date 2018/04/09
 */
public abstract class BaseViewHolder<T> extends ViewHolder{
    public BaseViewHolder(View itemView) {
        super(itemView);
    }

    /**
     * 绑定数据
     * @param data 数据
     * @param maybeLatest 是否最后一条
     */
    public abstract void bindData(T data, boolean maybeLatest);
}
