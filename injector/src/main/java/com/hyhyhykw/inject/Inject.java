package com.hyhyhykw.inject;

import android.view.View;

import com.hyhyhykw.annotation.Injector;
import com.hyhyhykw.annotation.InjectorIndex;

/**
 * Created time : 2021/6/20 12:27.
 *
 * @author 10585
 */
public class Inject {

    private InjectorIndex mInjectorIndex;

    public void setInjectorIndex(InjectorIndex injectorIndex) {
        mInjectorIndex = injectorIndex;
    }

    private static final class Holder {
        static final Inject INJECT = new Inject();
    }

    public static Inject get() {
        return Holder.INJECT;
    }

    public void inject(Object object) {
        inject(object,null);
    }

    public void inject(Object object, View view) {
        Class<? extends Injector> aClass = mInjectorIndex.getIndex()
                .get(object.getClass().getName());

        try {
            Injector injector = aClass.newInstance();

            injector.inject(object,view);
        } catch (InstantiationException | IllegalAccessException e) {
            e.printStackTrace();
        }
    }
}