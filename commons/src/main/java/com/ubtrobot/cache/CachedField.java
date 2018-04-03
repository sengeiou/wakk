package com.ubtrobot.cache;

import android.support.annotation.Nullable;

/**
 * 可缓存的字段
 *
 * @param <T> 字段类型
 */
public class CachedField<T> {

    private final FieldGetter<T> mGetter;
    private final boolean mAllowNull;

    private volatile T mValue;
    private volatile boolean mGot;

    public CachedField(FieldGetter<T> getter) {
        this(getter, false);
    }

    public CachedField(FieldGetter<T> getter, boolean allowNull) {
        mGetter = getter;
        mAllowNull = allowNull;
    }

    /**
     * 获取字段值。已缓存情况，直接返回字段值，否则通过 getter 获取值并缓存后返回
     *
     * @return 字段值
     */
    @Nullable
    public T get() {
        if (mGot) {
            return mValue;
        }

        synchronized (this) {
            if (mGot) {
                return mValue;
            }

            mValue = mGetter.get();
            mGot = mAllowNull || mValue != null;

            return mValue;
        }
    }

    public boolean isGot() {
        return mGot;
    }

    /**
     * 清除缓存
     */
    public void clear() {
        if (mGot) {
            synchronized (this) {
                mGot = false;
                mValue = null;
            }
        }
    }

    public interface FieldGetter<T> {

        T get();
    }
}