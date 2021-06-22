package com.inject.annotation;

/**
 * Created time : 2021/6/21 8:17.
 * 绑定方法的实现类
 *
 * @author 10585
 * @see BindView
 * @see BindViews
 * @see OnClick
 */
public interface Injector {
    void inject(Object object);
}
