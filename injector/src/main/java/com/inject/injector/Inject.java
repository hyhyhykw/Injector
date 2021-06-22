package com.inject.injector;

import android.view.View;

import com.inject.annotation.Injector;
import com.inject.annotation.InjectorIndex;

import androidx.fragment.app.Fragment;

/**
 * Created time : 2021/6/20 12:27.
 *
 * @author 10585
 * @see InjectorIndex
 */
public class Inject {

    private static InjectorIndex mInjectorIndex;

    public static void setInjectorIndex(InjectorIndex injectorIndex) {
        mInjectorIndex = injectorIndex;
    }

    /**
     * 针对具有findViewById方法的类的绑定
     *
     * @param object 包含需要绑定控件的主类
     * @see android.app.Activity#findViewById(int)
     * @see android.app.Dialog#findViewById(int)
     * @see View#findViewById(int)
     * @see Fragment#requireView()
     */
    public static void inject(Object object) {
        Class<? extends Injector> aClass = mInjectorIndex.getIndex()
                .get(object.getClass().getName());

        try {
            Injector injector = aClass.newInstance();

            injector.inject(object);
        } catch (InstantiationException | IllegalAccessException e) {
            e.printStackTrace();
        }
    }

}