package com.hyhyhykw.inject;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.hyhyhykw.annotation.BindView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

/**
 * Created time : 2021/6/21 10:52.
 *
 * @author 10585
 */
public class TestFragment extends MyFrg {
    @BindView("tv1")
    TextView mTv;
    @BindView("btn1")
    Button mButton;


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.activity_test, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view,
                              @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Inject.get().inject(this, view);

        mTv.setText("Fragment Inject");
    }
}