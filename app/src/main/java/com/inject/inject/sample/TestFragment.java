package com.inject.inject.sample;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.inject.annotation.OnClick;
import com.inject.injector.Inject;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

/**
 * Created time : 2021/6/21 10:52.
 *
 * @author 10585
 */
public class TestFragment extends MyFrg {

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

    @OnClick({"tv1", "btn1"})
    void onViewClick(View view) {

    }
}