package com.inject.inject.sample;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;

import com.inject.annotation.BindView;
import com.inject.annotation.OnCheckedChanged;
import com.inject.inject.sample.pk.Test2Activity;
import com.inject.injector.Inject;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

/**
 * Created time : 2021/6/21 8:43.
 *
 * @author 10585
 */
public class TestActivity extends AppCompatActivity {
    @BindView("R.id.tv1")
    TextView mTv;
    @BindView("R.id.btn1")
    Button mButton;
    @BindView("android.R.id.checkbox")
    CheckBox mCheckbox;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test);
        Inject.inject(this);

        mTv.setText("测试Inject2");
        mButton.setOnClickListener(v -> {
            startActivity(new Intent(this, Test2Activity.class));
        });
    }


    @OnCheckedChanged({"android.R.id.checkbox"})
    void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

    }
}