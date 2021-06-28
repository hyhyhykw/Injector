package com.inject.inject.sample;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;

import com.inject.annotation.BindAnim;
import com.inject.annotation.BindArray;
import com.inject.annotation.OnClick;
import com.inject.injector.Inject;

import java.util.LinkedList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

/**
 * Created time : 2021/6/21 10:52.
 *
 * @author 10585
 */
public class TestFragment extends MyFrg {
    @BindAnim("R.anim.slide_in_from_left")
    Animation slideInFromLeft;

    @BindArray("array1")
    LinkedList<String> array1;
    @BindArray("array2")
    LinkedList<CharSequence> array2;
    @BindArray("array3")
    List<CharSequence> array3;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.activity_test, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view,
                              @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Inject.inject(this);
    }

    @OnClick({"R.id.tv1", "R.id.btn1"} )
    void onViewClicked(View view) {
        int viewId = view.getId();
        if (viewId == R.id.tv1) {
        } else if (viewId == R.id.btn1) {
        }
    }
}