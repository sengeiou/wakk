package com.ubtrobot.async;

/**
 * 进度回调接口
 *
 * @param <P> 进度对象
 */
public interface ProgressCallback<P> {

    /**
     * 进度回调
     *
     * @param progress 描述进度的对象
     */
    void onProgress(final P progress);
}