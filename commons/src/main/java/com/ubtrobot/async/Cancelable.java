package com.ubtrobot.async;

/**
 * 可取消的
 */
public interface Cancelable {

    /**
     * 取消
     *
     * @return 是否取消成功
     */
    boolean cancel();
}