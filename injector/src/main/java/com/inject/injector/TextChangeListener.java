package com.inject.injector;

import android.text.Editable;
import android.text.TextWatcher;

/**
 * Created time : 2021/6/23 12:03.
 *
 * @author 10585
 */

public interface TextChangeListener extends TextWatcher {

    @Override
    default void beforeTextChanged(CharSequence s, int start, int count, int after) {
    }

    @Override
    default void onTextChanged(CharSequence s, int start, int before, int count) {
    }

    @Override
    default void afterTextChanged(Editable s) {

    }
}
