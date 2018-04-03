package com.ubtrobot.async;

/**
 * 失败回调接口
 *
 * @param <F> 失败出错结果
 */
public interface FailCallback<F> {

    /**
     * 失败回调
     *
     * @param result 失败出错结果
     */
    void onFail(final F result);
}