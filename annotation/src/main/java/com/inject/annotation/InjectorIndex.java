package com.inject.annotation;

import java.util.Map;

/**
 * Created time : 2021/6/20 12:47.
 * 因为ButterKnife生成的代码不能混淆，所以用这种方法生成索引
 *
 * @author 10585
 */
public interface InjectorIndex {

    Map<String, Class<? extends Injector>> getIndex();
}
