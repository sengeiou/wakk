package com.ubtrobot.async;

public interface Consumer<T> {

    void accept(T value);
}