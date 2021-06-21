package com.hyhyhykw.inject;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import com.hyhyhykw.annotation.BindView;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    @BindView("tv")
    TextView mTv;
    @BindView("btn")
    Button mButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Inject.get().inject(this);
        mTv.setText("测试Inject");

        mButton.setOnClickListener(v -> {
            startActivity(new Intent(
                    this, TestActivity.class
            ));
        });
    }
}