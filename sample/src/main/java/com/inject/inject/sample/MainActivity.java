package com.inject.inject.sample;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.view.View;
import android.view.animation.Animation;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.inject.annotation.BindAnim;
import com.inject.annotation.BindArray;
import com.inject.annotation.BindView;
import com.inject.annotation.BindViews;
import com.inject.index.CheckChangeType;
import com.inject.annotation.OnCheckedChanged;
import com.inject.annotation.OnClick;
import com.inject.annotation.OnLongClick;
import com.inject.annotation.OnPageChange;
import com.inject.annotation.OnTextChanged;
import com.inject.injector.Inject;

import java.util.List;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    @BindView("tv")
    TextView mTv;
    @BindView("tv1")
    TextView mTv1;

    @BindView("vpg2")
    View mVpg2;

    @BindViews({
            "tv",
            "tv1",
            "btn"
    })
    List<TextView> tvs;

    @BindViews({
            "tv",
            "tv1"
    })
    View[] tvs1;

    @BindViews({
            "btn",
            "btn1"
    })
    Button[] btns;

    @BindAnim("R.anim.slide_in_from_left")
    Animation slideInFromLeft;

    @BindArray("array1")
    String[] array1;
    @BindArray("array2")
    CharSequence[] array2;
    @BindArray("array3")
    List<String> array3;

    @BindArray("array3")
    List<CharSequence> array4;

    @BindView("radio")
    View rgp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Inject.inject(this);
        mTv.setText("测试Inject");
    }


    @OnCheckedChanged({"checkbox"})
    void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

    }

    @OnCheckedChanged(value = "radio", type = CheckChangeType.RadioGroup)
    void onCheckedChanged(RadioGroup group, int checkedId) {

    }

    @OnTextChanged(value = {"tv", "tv1"}, listen = OnTextChanged.Listen.BEFORE_TEXT_CHANGE)
    void beforeTextChanged(CharSequence s, int start, int count, int after) {

    }

    @OnTextChanged(value = {"tv"}, listen = OnTextChanged.Listen.ON_TEXT_CHANGE)
    void onTextChanged(CharSequence s, int start, int before, int count) {

    }

    @OnTextChanged(value = {"tv1"}, listen = OnTextChanged.Listen.AFTER_TEXT_CHANGE)
    void afterTextChanged(Editable s) {

    }

    @OnPageChange(value = "vpg1", listen = OnPageChange.Listen.ON_PAGE_SELECTED)
    void onPageSelected(int position) {

    }

    @OnPageChange(value = "vpg1", listen = OnPageChange.Listen.ON_PAGE_SCROLL_STATE_CHANGED)
    void onPageScrollStateChanged(int state) {

    }

    @OnPageChange(value = "vpg1", listen = OnPageChange.Listen.ON_PAGE_SCROLLED)
    void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

    }

    @OnPageChange(value = "vpg2", listen = OnPageChange.Listen.ON_PAGE_SELECTED)
    void onPageSelected2(int position) {

    }

    @OnPageChange(value = "vpg2", listen = OnPageChange.Listen.ON_PAGE_SCROLLED)
    void onPageScrolled2(int position, float positionOffset, int positionOffsetPixels) {

    }

    @OnClick(value = {"tv", "btn", "tv1"}, fast = false)
    void onViewClick(View view) {
        if (view.getId() == R.id.btn) {
            startActivity(new Intent(
                    this, TestActivity.class
            ));
        }
    }

    @OnLongClick({"tv", "btn", "tv1"})
    boolean onViewLongClick(View view) {

        return true;
    }

    @OnClick({"tv1", "btn1"})
    void onViewClick2(View view) {

    }
}