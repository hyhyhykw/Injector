package com.hyhyhykw.inject.pk;

import android.os.Bundle;
import android.widget.TextView;

import com.hyhyhykw.annotation.BindView;
import com.hyhyhykw.inject.Inject;
import com.hyhyhykw.inject.R;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

/**
 * Created time : 2021/6/21 9:41.
 *
 * @author 10585
 */
public class Test2Activity extends AppCompatActivity {

    @BindView("tv2")
    TextView mTv;
    @BindView("btn2")
    TextView mButton;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test2);
        Inject.get().inject(this);
    }
}