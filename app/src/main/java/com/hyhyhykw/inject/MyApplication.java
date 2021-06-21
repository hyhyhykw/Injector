package com.hyhyhykw.inject;

import android.app.Application;

/**
 * Created time : 2021/6/21 9:12.
 *
 * @author 10585
 */
public class MyApplication extends Application {


    @Override
    public void onCreate() {
        super.onCreate();
        Inject.get().setInjectorIndex(new MyInjectorIndex());
    }
}