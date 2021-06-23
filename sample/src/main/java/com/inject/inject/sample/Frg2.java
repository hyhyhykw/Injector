package com.inject.inject.sample;

import com.inject.annotation.BindView;
import com.inject.annotation.OnPageChange;
import com.inject.annotation.OnTextChanged;

import androidx.viewpager.widget.ViewPager;

/**
 * Created time : 2021/6/23 10:35.
 *
 * @author 10585
 */
public class Frg2 extends MyFrg {

    @BindView("vpg1")
    ViewPager mVpg;

    @OnPageChange(value = "vpg1", listen = OnPageChange.Listen.ON_PAGE_SELECTED)
    void onPageSelected(int position) {

    }

    @OnTextChanged(value = {"tv", "tv1"}, listen = OnTextChanged.Listen.BEFORE_TEXT_CHANGE)
    void beforeTextChanged(CharSequence s, int start, int count, int after) {

    }
}