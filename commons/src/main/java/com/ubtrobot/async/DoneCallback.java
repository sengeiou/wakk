package com.ubtrobot.async;

/**
 * 完成回调接口
 *
 * @param <D> 完成结果
 */
public interface DoneCallback<D> {

    /**
     * 完成回调
     *
     * @param result 完成结果
     */
    void onDone(final D result);
}