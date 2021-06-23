package com.inject.injector;

import androidx.viewpager.widget.ViewPager;

/**
 * Created time : 2021/6/23 12:04.
 *
 * @author 10585
 */

public interface OnPageChangeListener extends ViewPager.OnPageChangeListener {
    @Override
   default void onPageSelected(int position){}

    @Override
    default void onPageScrollStateChanged(int state){}

    @Override
    default  void onPageScrolled(int position, float positionOffset, int positionOffsetPixels){}
}
