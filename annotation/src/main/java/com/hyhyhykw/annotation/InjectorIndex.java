package com.hyhyhykw.annotation;

import java.util.Map;

/**
 * Created time : 2021/6/20 12:47.
 *
 * @author 10585
 */
public interface InjectorIndex {

    Map<String , Class<? extends Injector>> getIndex();
}
