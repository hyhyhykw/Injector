package com.inject.inject.sample;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.inject.annotation.BindView;
import com.inject.annotation.OnClick;
import com.inject.injector.Inject;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    @BindView("tv")
    TextView mTv;
    @BindView("tv1")
    TextView mTv1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Inject.inject(this);
        mTv.setText("测试Inject");
    }

    @OnClick(value = {"tv", "btn", "tv1"}, fast = false)
    void onViewClick(View view) {
        if (view.getId() == R.id.btn) {
            startActivity(new Intent(
                    this, TestActivity.class
            ));
        }
    }

    @OnClick({"tv1", "btn1"})
    void onViewClick2(View view) {

    }
}