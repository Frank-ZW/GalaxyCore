package org.craftgalaxy.galaxycore.client.chat.wrapper;

public interface IChatWrapper<T> {

    void cancel();
    void accept(T var1);
    T get();
    String format();
    IChatWrapper<T> release();
}
