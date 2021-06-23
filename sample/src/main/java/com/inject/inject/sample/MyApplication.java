package com.inject.inject.sample;

import android.app.Application;

import com.inject.injector.Inject;

/**
 * Created time : 2021/6/21 9:12.
 *
 * @author 10585
 */
public class MyApplication extends Application {


    @Override
    public void onCreate() {
        super.onCreate();
        Inject.setInjectorIndex(new MyInjectorIndex());
    }
}