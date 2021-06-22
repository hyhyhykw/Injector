package com.inject.injector;

import android.view.View;

import androidx.annotation.Nullable;

/**
 * Created time : 2021/6/22 7:51.
 *
 * @author 10585
 * @see com.inject.annotation.OnClick
 */
public final class ViewClick {
    private static long lastClickTime;

    public static boolean isFastDoubleClick() {
        long time = System.currentTimeMillis();
        long timeD = time - lastClickTime;

        if (0 < timeD && timeD < 500) {
            return true;
        }
        lastClickTime = time;

        return false;
    }

    /**
     * 视图点击事件的封装，防止点击过快 null处理，防止崩溃
     *
     * @param view     视图
     * @param listener 点击事件
     */
    public static void setViewClick(@Nullable View view,
                                    @Nullable final View.OnClickListener listener) {
        setViewClick(view, true, listener);
    }

    /**
     * 视图点击事件的封装，防止点击过快
     *
     * @param view     视图
     * @param fast     是否需要防止点击过快
     * @param listener 点击事件
     */
    public static void setViewClick(@Nullable View view,
                                    boolean fast,
                                    @Nullable final View.OnClickListener listener) {
        if (null == view) return;
        if (null == listener) {
            view.setOnClickListener(null);
            return;
        }
        view.setOnClickListener(v -> {
            if (fast && isFastDoubleClick()) return;
            listener.onClick(v);
        });
    }
}