package com.hyhyhykw.inject;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import com.hyhyhykw.annotation.BindView;
import com.hyhyhykw.inject.pk.Test2Activity;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

/**
 * Created time : 2021/6/21 8:43.
 *
 * @author 10585
 */
public class TestActivity extends AppCompatActivity {
    @BindView("tv1")
    TextView mTv;
    @BindView("btn1")
    Button mButton;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test);
        Inject.get().inject(this);

        mTv.setText("测试Inject2");
        mButton.setOnClickListener(v -> {
            startActivity(new Intent(this, Test2Activity.class));
        });
    }

}