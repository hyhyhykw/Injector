package com.inject.index;

import java.util.Map;

/**
 * Created time : 2021/6/20 12:47.
 * 因为ButterKnife生成的代码不能混淆，所以用这种方法生成索引
 *
 * @author 10585
 * @see Injector
 */
public interface InjectorIndex {

    /**
     * ad
     *
     * @return
     */
    Map<String, Class<? extends Injector>> getIndex();
}
