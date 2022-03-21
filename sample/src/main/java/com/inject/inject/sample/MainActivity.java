package com.inject.inject.sample;

import android.os.Bundle;
import android.text.Editable;
import android.view.MotionEvent;
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
import com.inject.annotation.Dp;
import com.inject.annotation.OnCheckedChanged;
import com.inject.annotation.OnClick;
import com.inject.annotation.OnLongClick;
import com.inject.annotation.OnPageChange;
import com.inject.annotation.OnTextChanged;
import com.inject.annotation.OnTouch;
import com.inject.annotation.Sp;
import com.inject.index.CheckChangeType;
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


    @Dp(12)
    int dp12;

    @Dp(13)
    float dp13;

    @Sp(12)
    Long sp12;

    @Sp(13)
    Double sp13;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Inject.inject(this);
        mTv.setText("测试Inject");
    }


    @OnClick({"R.id.tv", "R.id.btn"})
    void onViewClicked(View view) {
        int viewId = view.getId();
        if (viewId == R.id.tv) {
        } else if (viewId == R.id.btn) {
        }
    }

    @OnTouch({"R.id.tv", "R.id.btn"})
    boolean onViewTouch(View v, MotionEvent event) {
        int action = event.getAction();
        switch (action) {
            case MotionEvent.ACTION_DOWN:
            case MotionEvent.ACTION_CANCEL:
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_MOVE:
                break;
        }
        return false;
    }

    @OnLongClick({"R.id.checkbox", "R.id.tv"})
    boolean onViewLongClicked(View view) {
        int viewId = view.getId();
        if (viewId == R.id.checkbox) {
        } else if (viewId == R.id.tv) {
        }
        return false;
    }

    @OnCheckedChanged({"R.id.checkbox"})
    void onCheckChanged(CompoundButton buttonView, boolean isChecked) {
        int viewId = buttonView.getId();
        if (viewId == R.id.checkbox) {
        }
    }

    @OnCheckedChanged(value = {"R.id.radio"}, type = CheckChangeType.RadioGroup)
    void onCheckChanged(RadioGroup group, int checkedId) {
        int viewId = group.getId();
        if (viewId == R.id.radio) {
        }
    }

    @OnTextChanged(value = {"R.id.tv", "R.id.btn"}, listen = OnTextChanged.Listen.ON_TEXT_CHANGE)
    void onTextChange(CharSequence s, int start, int before, int count) {
    }

    @OnTextChanged(value = {"R.id.tv"}, listen = OnTextChanged.Listen.AFTER_TEXT_CHANGE)
    void afterTextChanged(Editable s) {
    }

    @OnTextChanged(value = {"R.id.btn"}, listen = OnTextChanged.Listen.BEFORE_TEXT_CHANGE)
    void beforeTextChanged(CharSequence s, int start, int count, int after) {
    }

    @OnPageChange(value = "R.id.vpg2", listen = OnPageChange.Listen.ON_PAGE_SCROLL_STATE_CHANGED)
    void onVpg2PageScrollStateChanged(int state) {
    }

    @OnPageChange(value = "R.id.vpg1", listen = OnPageChange.Listen.ON_PAGE_SELECTED)
    void onVpg1PageSelected(int position) {
    }

    @OnPageChange(value = "R.id.vpg2", listen = OnPageChange.Listen.ON_PAGE_SELECTED)
    void onVpg2PageSelected(int position) {
    }

    @OnPageChange(value = "R.id.vpg1", listen = OnPageChange.Listen.ON_PAGE_SCROLLED)
    void onVpg1PageScrolled(int position, float positionOffset, int positionOffsetPixels) {
    }
}