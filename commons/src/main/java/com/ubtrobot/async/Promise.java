package com.ubtrobot.async;

import java.util.concurrent.TimeUnit;

/**
 * 异步回调结果的"承诺"
 *
 * @param <D> 完成结果类型
 * @param <F> 出错结果类型
 * @param <P> 进度对象类型
 */
public interface Promise<D, F, P> extends Cancelable {

    int STATE_PENDING = 0;
    int STATE_REJECTED = 1;
    int STATE_RESOLVED = 2;
    int STATE_CANCELED = 3;

    /**
     * 获取状态
     *
     * @return 状态
     */
    int state();

    boolean isPending();

    boolean isResolved();

    boolean isRejected();

    boolean isCanceled();

    Promise<D, F, P> done(DoneCallback<? super D> callback);

    Promise<D, F, P> fail(FailCallback<? super F> callback);

    Promise<D, F, P> progress(ProgressCallback<? super P> callback);

    D getDone() throws InterruptedException;

    D getDone(long timeout, TimeUnit unit) throws InterruptedException;

    F getFail() throws InterruptedException;

    F getFail(long timeout, TimeUnit unit) throws InterruptedException;
}